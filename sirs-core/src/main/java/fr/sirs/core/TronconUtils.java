package fr.sirs.core;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.linearref.LengthIndexedLine;
import fr.sirs.core.component.BorneDigueRepository;
import fr.sirs.core.component.SessionGen;
import fr.sirs.core.component.SystemeReperageRepository;
import fr.sirs.core.component.TronconDigueRepository;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.Positionable;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.SystemeReperageBorne;
import fr.sirs.core.model.TronconDigue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import org.apache.sis.util.ArgumentChecks;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.referencing.CRS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

import static fr.sirs.core.LinearReferencingUtilities.*;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.AbstractPositionDocument;
import fr.sirs.core.model.AvecForeignParent;
import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.GardeTroncon;
import fr.sirs.core.model.ObjetPhotographiable;
import fr.sirs.core.model.Observation;
import fr.sirs.core.model.Photo;
import fr.sirs.core.model.PositionProfilTravers;
import fr.sirs.core.model.ProprieteTroncon;
import java.util.Iterator;
import java.util.logging.Level;
import org.geotoolkit.referencing.LinearReferencing;

/**
 * A set of utility methods for manipulation of geometries of {@link Positionable}
 * or {@link TronconDigue} objects.
 * 
 * @author Johann Sorel (Geomatys)
 * @author Alexis Manin (Geomatys)
 */
public class TronconUtils {
 
    public static final String SR_ELEMENTAIRE = "Elémentaire";
    
    private static final GeometryFactory GF = GO2Utilities.JTS_FACTORY;
        
    /**
     * 
     * @param troncon troncon a decouper
     * @param cutLinear partie du troncon a garder
     * @param newName
     * @param session
     * @return nouveau troncon découpé
     */
    public static TronconDigue cutTroncon(TronconDigue troncon, LineString cutLinear, String newName, SessionGen session) {
        ArgumentChecks.ensureNonNull("Troncon to cut", troncon);
        ArgumentChecks.ensureNonNull("Line string to extract", cutLinear);
        ArgumentChecks.ensureNonNull("Database session", session);

        /* First, we get index (as distance along the linear) of the bounds of new 
         * tronçon segments. It will allow us to retrieve objects which are 
         * projected in those bounds and must be effectively copied.
         */
        final LengthIndexedLine index = new LengthIndexedLine(troncon.getGeometry());
        final double startDistance = index.project(cutLinear.getStartPoint().getCoordinate());
        final double endDistance = index.project(cutLinear.getEndPoint().getCoordinate());

        final SystemeReperageRepository srRepo = (SystemeReperageRepository) session.getRepositoryForClass(SystemeReperage.class);
        final BorneDigueRepository bdRepo = (BorneDigueRepository) session.getRepositoryForClass(BorneDigue.class);
        final TronconDigueRepository tdRepo = (TronconDigueRepository) session.getRepositoryForClass(TronconDigue.class);

        final TronconDigue tronconCp = troncon.copy();
        tronconCp.setGeometry(cutLinear);
        tronconCp.setLibelle(newName);
        // On enlève toute réference vers un SR appartenant au tronçon copié
        tronconCp.setSystemeRepDefautId(null);

        //on évince toutes les bornes qui ne sont pas sur le nouveau tronçon. On 
        // garde un index des ids de borne conservés, cela accélerera le tri sur
        // les SR.
        final SegmentInfo[] sourceTronconSegments = buildSegments(asLineString(troncon.geometryProperty().get()));
        final ListIterator<String> borneIt = tronconCp.getBorneIds().listIterator();
        final HashSet<String> keptBornes = new HashSet<>();
        while (borneIt.hasNext()) {
            final BorneDigue borne = bdRepo.get(borneIt.next());
            final ProjectedPoint proj = projectReference(sourceTronconSegments, borne.getGeometry());
            if (proj.distanceAlongLinear < startDistance || proj.distanceAlongLinear > endDistance) {
                borneIt.remove();
            } else {
                keptBornes.add(borne.getId());
            }
        }

        /* On copie les SR du tronçon original. Pour chaque, on regarde si il contient
         * des bornes référencées sur le nouveau tronçon. Si c'est le cas, on le 
         * garde pour enregistrement à la fin de l'opération. On garde aussi une
         * réference vers le SR original, pour pouvoir mettre à jour la position
         * des structures.
         */
        final HashMap<String, SystemeReperage> newSRs = new HashMap<>();
        for (final SystemeReperage sr : srRepo.getByLinearId(troncon.getId())) {
            final SystemeReperage srCp = sr.copy();
            final ListIterator<SystemeReperageBorne> srBorneIt = srCp.getSystemeReperageBornes().listIterator();
            while (srBorneIt.hasNext()) {
                if (!keptBornes.contains(srBorneIt.next().getBorneId())) {
                    srBorneIt.remove();
                }
            }
            if (!srCp.systemeReperageBornes.isEmpty()) {
                newSRs.put(sr.getId(), srCp);
            }
        }
                
        /* On parcourt la liste des objets positionnés sur le tronçon originel.
         * Pour tout objet contenu dans le morceau découpé (tronçon de sortie)
         * on met simplement à jour ses positions linéaires pour rester cohérent
         * avec sa géométrie. Les objets qui intersectent le nouveau tronçon sont 
         * quand à eux découpés.
         * Note : On fait une copie des objets à affecter au nouveau tronçon
         */
        final HashMap<SystemeReperage, List<Positionable>> needSRIDUpdate = new HashMap<>();
        final ListIterator<Positionable> posIt = getPositionableList(troncon).listIterator();
        final List<Positionable> newPositions = new ArrayList<>();
        SegmentInfo[] cutTronconSegments = null;
        while (posIt.hasNext()) {
            final Positionable tmpPosition = posIt.next();

            //on vérifie que cet objet intersecte le segment
            Geometry rawObjGeom = tmpPosition.getGeometry();
            
            if (rawObjGeom == null) {
                //on la calcule
                rawObjGeom = buildGeometry(troncon.getGeometry(), tmpPosition, bdRepo);
                if (rawObjGeom == null) {
                    throw new IllegalStateException("Impossible de déterminer la géométrie de l'objet suivant :\n" + tmpPosition);
                }
                tmpPosition.setGeometry(rawObjGeom);
            }

            /*
            Les opérations de vividsolutions "intersects", "contains" et "intersection"
            ne donnent pas les mêmes résultats selon qu'un point est représenté par
            une géométrie de type "POINT(x y)" ou par une géométrie de type "LINESTRING(x y, x y)"
            c'est-à-dire une ligne formée de deux points de même coordonnées.
            
            Dans les lignes suivantes la géométrie interpObjGeom est destinée
            à représenter temporairement sous forme de géométrie "POINT" les 
            géométries "LINESTRING" qui, de fait représentent des points, de manière
            à obtenir les bons résultats d'opérations topologiques.
            */
            
            // Dinstinction des cas entre les points et les polylignes
            Geometry interpObjGeom = rawObjGeom;
            if(rawObjGeom instanceof LineString){
                final LineString line = (LineString) rawObjGeom;
                
                // Si l'objet est ponctuel il faut transformer sa géométrie en point pour détecter intersection
                if(line.getNumPoints()==2 && line.getPointN(0).equals(line.getPointN(1))){
                    interpObjGeom = line.getPointN(0);
                }
            }
            
            
            if (!cutLinear.intersects(interpObjGeom)) {
                posIt.remove();
                continue;
            } 
            
            final Positionable position = tmpPosition.copy();
            newPositions.add(position);
            
            // Mise à jour des infos géographiques
            if (!cutLinear.contains(interpObjGeom)) {
                rawObjGeom = cutLinear.intersection(interpObjGeom);
                position.setGeometry(rawObjGeom);
            }

            if (rawObjGeom instanceof Point) {
                position.setPositionDebut((Point) rawObjGeom);
                position.setPositionFin((Point) rawObjGeom);
            } else {
                final LineString structureLine = asLineString(rawObjGeom);
                position.setPositionDebut(structureLine.getStartPoint());
                position.setPositionFin(structureLine.getEndPoint());
                position.setGeometry(rawObjGeom);
            }

            // Mise à jour du réferencement linéaire
            final SystemeReperage sr = newSRs.get(position.getSystemeRepId());
            if (sr == null) {
                position.setSystemeRepId(null);
                position.setBorneDebutId(null);
                position.setBorneFinId(null);
                position.setBorne_debut_distance(Float.NaN);
                position.setBorne_fin_distance(Float.NaN);
            } else {
                final PosInfo info;
                if (cutTronconSegments == null) {
                    info = new PosInfo(position, tronconCp, session);
                    cutTronconSegments = info.getTronconSegments(true);
                } else {
                    info = new PosInfo(position, tronconCp, cutTronconSegments, session);
                }
                final PosSR posSr = info.getForSR(sr);

                // On garde la reference de l'objet, car on devra le lier au nouveau SR quand ce dernier aura été inséré.
                List<Positionable> boundObjets = needSRIDUpdate.get(sr);
                if (boundObjets == null) {
                    boundObjets = new ArrayList<>();
                    needSRIDUpdate.put(sr, boundObjets);
                }
                boundObjets.add(position);
                
                position.setBorneDebutId(posSr.borneStartId);
                position.setBorne_debut_distance((float) posSr.distanceStartBorne);
                position.setBorne_debut_aval(posSr.startAval);
                position.setBorneFinId(posSr.borneEndId);
                position.setBorne_fin_distance((float) posSr.distanceEndBorne);
                position.setBorne_fin_aval(posSr.endAval);
                position.setPositionDebut(null);
                position.setPositionFin(null);
            }
        }
        
        // On sauvegarde les modifications
        tdRepo.add(tronconCp);
        
        // On essaye de trouver un SR par défaut pour notre nouveau tronçon.   
        final SystemeReperage newDefaultSR = newSRs.remove(troncon.getSystemeRepDefautId());
        if (newDefaultSR != null) {
            newDefaultSR.setLinearId(tronconCp.getDocumentId());
            srRepo.add(newDefaultSR, tronconCp, true);
        }
        for (final SystemeReperage newSR : newSRs.values()) {
            newSR.setLinearId(tronconCp.getDocumentId());
            srRepo.add(newSR, tronconCp, false);
        }
        
        // Maintenant que notre tronçon et nos SR sont enregistrés, on peut relier 
        // les objets du tronçon à leur SR.
        if (!needSRIDUpdate.isEmpty()) {
            Iterator<Map.Entry<SystemeReperage, List<Positionable>>> it = needSRIDUpdate.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<SystemeReperage, List<Positionable>> next = it.next();
                final String srid = next.getKey().getId();
                for (final Positionable o : next.getValue()) {
                    o.setSystemeRepId(srid);
                }
            }
            
            // Et on termine par la sérialisation des objets positionés sur le nouveau tronçon.
            for (final Positionable pos : newPositions) {
                if (pos instanceof AvecForeignParent) {
                    ((AvecForeignParent)pos).setForeignParentId(tronconCp.getId());
                }
                try {
                    AbstractSIRSRepository repo = session.getRepositoryForClass(pos.getClass());
                    repo.add(pos);
                } catch (Exception e) {
                    SirsCore.LOGGER.log(Level.WARNING, "Position object cannot be copied to new troncon.", e);
                }
            }
        }
        return tronconCp;
    }
    
    /**
     * Retrieve the list of properties linked to a linear which id is given as a 
     * parameter.
     * @param linearId
     * @return 
     */
    public static List<ProprieteTroncon> getProprieteList(final String linearId){
        return InjectorCore.getBean(SessionCore.class).getProprietesByTronconId(linearId);
    }
    
    /**
     * Retrieve the list of properties linked to a linear given as a parameter.
     * @param linear
     * @return 
     */
    public static List<ProprieteTroncon> getProprieteList(final TronconDigue linear){
        return getProprieteList(linear.getId());
    }
    
    /**
     * Retrieve the list of gardes linked to a linear which id is given as a 
     * parameter.
     * @param linearId
     * @return 
     */
    public static List<GardeTroncon> getGardeList(final String linearId){
        return InjectorCore.getBean(SessionCore.class).getGardesByTronconId(linearId);
    }
    
    /**
     * Retrieve the list of gardes linked to a linear given as a parameter.
     * @param linear
     * @return 
     */
    public static List<GardeTroncon> getGardeList(final TronconDigue linear){
        return getGardeList(linear.getId());
    }
    
    /**
     * Retrieve the list of objects linked to a linear which id is given as a 
     * parameter.
     * @param linearId
     * @return 
     */
    public static List<Objet> getObjetList(final String linearId){
        return InjectorCore.getBean(SessionCore.class).getObjetsByTronconId(linearId);
    }
    
    /**
     * Retrieve the list of objects linked to a linear given as a parameter.
     * @param linear
     * @return 
     */
    public static List<Objet> getObjetList(final TronconDigue linear){
        return getObjetList(linear.getId());
    }
    
    /**
     * Retrieve the list of document positions linked to a linear which id is 
     * given as a parameter.
     * @param linearId
     * @return 
     */
    public static List<AbstractPositionDocument> getPositionDocumentList(final String linearId){
        return InjectorCore.getBean(SessionCore.class).getPositionDocumentsByTronconId(linearId);
    }
    
    /**
     * Retrieve the list of document positions linked to a linear given as a 
     * parameter.
     * @param linear
     * @return 
     */
    public static List<AbstractPositionDocument> getPositionDocumentList(final TronconDigue linear){
        return getPositionDocumentList(linear.getId());
    }
    
    /**
     * Retrieve the list of photographs contained in objects or profil en 
     * travers positions linked to a linear which id is given as a parameter.
     * @param linearId
     * @return 
     */
    public static List<Photo> getPhotoList(final String linearId){
        final List<Photo> photos = new ArrayList<>();
        final List<PositionProfilTravers> positions = InjectorCore.getBean(SessionCore.class).getPositionProfilTraversRepository().getByLinearId(linearId);
        final List<Objet> objets = getObjetList(linearId);
        for(final PositionProfilTravers position : positions){
            final List<Photo> p = position.getPhotos();
            if(p!=null && !p.isEmpty()) photos.addAll(p);
        }
        for(final Objet objet : objets){
            if (objet instanceof ObjetPhotographiable){
                final List<Photo> p = ((ObjetPhotographiable) objet).getPhotos();
                if(p!=null && !p.isEmpty()) photos.addAll(p);
            } else if (objet instanceof Desordre){
                for (final Observation observation : ((Desordre) objet).getObservations()){
                    final List<Photo> p = observation.getPhotos();
                    if(p!=null && !p.isEmpty()) photos.addAll(p);
                }
            }
        }
        return photos;
    }
    
    /**
     * Return the positionable included, linked or included into linked elements
     * for the linar given as a parameter.
     * @param linear
     * @return a list containing the objets, positions de documents, proprietes,
     * gardes and photos related to the linear.
     */
    public static List<Positionable> getPositionableList(final TronconDigue linear){
        final List<Positionable> positionables = new ArrayList<>();
        
        final List<Objet> objets = TronconUtils.getObjetList(linear);
        if(objets !=null && !objets.isEmpty()) positionables.addAll(objets);
        final List<AbstractPositionDocument> positions = TronconUtils.getPositionDocumentList(linear);
        if(positions !=null && !positions.isEmpty()) positionables.addAll(positions);
        final List<ProprieteTroncon> proprietes = TronconUtils.getProprieteList(linear);
        if(proprietes !=null && !proprietes.isEmpty()) positionables.addAll(proprietes);
        final List<GardeTroncon> gardes = TronconUtils.getGardeList(linear);
        if(gardes !=null && !gardes.isEmpty()) positionables.addAll(gardes);
        final List<Photo> photos = TronconUtils.getPhotoList(linear);
        if(photos !=null && !photos.isEmpty()) positionables.addAll(photos);
        
        return positionables;
    }
    
    /**
     * Retrieve the list of photographs contained in objects or profil en 
     * travers positions linked to a linear given as a parameter.
     * @param linear
     * @return 
     */
    public static List<Photo> getPhotoList(final TronconDigue linear){
        return getPhotoList(linear.getId());
    }
    
    /**
     * On ajoute / copie les propriétés du second tronçon (incluant les structures) dans le premier.
     * 
     * TODO : check SR par défaut dans le troncon final.
     * 
     * @param mergeResult Le tronçon qui va servir de base à la fusion, qui va 
     * être mis à jour.
     * @param mergeParam Le tronçon dont on va prendre les propriétés pour les copier dans le second.
     * @param session La session applicative permettant de mettre à jour les SRs.
     * @return le premier tronçon (mergeResult).
     */
    public static TronconDigue mergeTroncon(TronconDigue mergeResult, TronconDigue mergeParam, SessionGen session) {
        
        final SystemeReperageRepository srRepo = session.getSystemeReperageRepository();
        
        // on ajoute les bornes. Pas de copie / modification ici, car les bornes 
        // sont indépendantes des tronçons.
        final Set<String> borneIds = new HashSet<>();
        borneIds.addAll(mergeResult.getBorneIds());
        borneIds.addAll(mergeParam.getBorneIds());
        mergeResult.setBorneIds(new ArrayList<>(borneIds));
                
        /* On fusionne les SR. On cherche les systèmes portant le même nom dans 
         * les deux tronçons originaux, puis en fait un seul comportant les bornes
         * des deux. Pour le reste, on fait une simple copie des SR.
         */
        final HashMap<String, String> modifiedSRs = new HashMap<>(); 
        for(SystemeReperage sr2 : srRepo.getByLinearId(mergeParam.getId())){
            
            //on cherche le SR du meme nom
            SystemeReperage sibling = null;
            for(SystemeReperage sr1 : srRepo.getByLinearId(mergeResult.getId())){
                if(sr1.getLibelle().equals(sr2.getLibelle())){
                    sibling = sr1;
                    break;
                }
            }
                 
            if(sibling==null){
                //on copie le SR
                final SystemeReperage srCp = sr2.copy();
                srCp.setLinearId(mergeResult.getDocumentId());
                //sauvegarde du sr
                srRepo.add(srCp, mergeResult);
                modifiedSRs.put(sr2.getId(), srCp.getId());
            }else{
                //on merge les bornes
                final List<SystemeReperageBorne> srbs1 = sibling.getSystemeReperageBornes();
                final List<SystemeReperageBorne> srbs2 = sr2.getSystemeReperageBornes();
                    
                loop:
                for(SystemeReperageBorne srb2 : srbs2){
                    for(SystemeReperageBorne srb1 : srbs1){
                        if(srb1.getBorneId().equals(srb2.getBorneId())){
                            continue loop;
                        }
                    }                          
                    //cette borne n'existe pas dans l'autre SR, on la copie
                    srbs1.add(srb2.copy());
                }
                //maj du sr
                srRepo.update(sibling, mergeResult);
                modifiedSRs.put(sr2.getId(), sibling.getId());
            }
        }
        
        // On ajoute les structures du tronçon paramètre. 
        final HashSet<Positionable> toSave = new HashSet<>();
        for (final Positionable objet : getPositionableList(mergeParam)) {
            // si l'objet est une photo, alors il ne faut pas faire de copie car
            // elle est gérée de manière récursive dans l'élément conteneur.
            Positionable copy = objet;
            if (objet.getId().equals(objet.getDocumentId())) {
                toSave.add(objet.copy());
            }
        }
        
        // On les persiste en bdd pour retrouver facilement tous les élements à modifier
        for (final Positionable copy : toSave) {
            // On vérifie que la copie a un dépôt pour l'enregistrer.
            try {
                if (copy instanceof AvecForeignParent)
                    ((AvecForeignParent) copy).setForeignParentId(mergeResult.getId());
                ((AbstractSIRSRepository) InjectorCore.getBean(SessionCore.class).getRepositoryForClass(copy.getClass())).add(copy);
            } catch (Exception e) {
                SirsCore.LOGGER.log(Level.WARNING, "Cannot save a copy of " + copy.getDesignation()+ "[Class: " +copy.getClass()+"]");
            }
        }
              
        // On change le SR des objets copiés
        for (final Positionable copy : getPositionableList(mergeResult)) {
            final String srId = modifiedSRs.get(copy.getSystemeRepId());
            if (srId != null) {
                copy.setSystemeRepId(srId);
            }
        }
        
        // On sauvegarde les changements.        
        for (final Positionable copy : toSave) {
            // On vérifie que la copie a un dépôt pour l'enregistrer.
            try {
                if (copy instanceof AvecForeignParent)
                    ((AvecForeignParent) copy).setForeignParentId(mergeResult.getId());
                ((AbstractSIRSRepository) InjectorCore.getBean(SessionCore.class).getRepositoryForClass(copy.getClass())).add(copy);
            } catch (Exception e) {
                SirsCore.LOGGER.log(Level.WARNING, "Cannot save a copy of " + copy.getDesignation()+ "[Class: " +copy.getClass()+"]");
            }
        }
        
        //on combine les geometries
        final Geometry line1 = mergeResult.getGeometry();
        final Geometry line2 = mergeParam.getGeometry();
        
        final List<Coordinate> coords = new  ArrayList<>();
        coords.addAll(Arrays.asList(line1.getCoordinates()));
        coords.addAll(Arrays.asList(line2.getCoordinates()));
        
        final LineString serie = GF.createLineString(coords.toArray(new Coordinate[0]));
        serie.setSRID(line1.getSRID());
        serie.setUserData(line1.getUserData());
        mergeResult.setGeometry(serie);        
        
        return mergeResult;
    }
    
    
    
    /**
     * Creation ou mise a jour du systeme de reperage elementaire .
     * 
     * @param troncon 
     * @param session 
     */
    public static void updateSRElementaire(TronconDigue troncon, SessionCore session){
                
        final SystemeReperageRepository srRepo = session.getSystemeReperageRepository();
        final BorneDigueRepository bdRepo = session.getBorneDigueRepository();
        
        final List<SystemeReperage> srs = srRepo.getByLinearId(troncon.getId());
        
        SystemeReperage sr = null;
        for(SystemeReperage csr : srs){
            if(SR_ELEMENTAIRE.equalsIgnoreCase(csr.getLibelle())){
                sr = csr;
                break;
            }
        }
        
        //on le crée s'il n'existe pas
        if(sr==null){
            sr = srRepo.create();
            sr.setLibelle(SR_ELEMENTAIRE);
            sr.setLinearId(troncon.getDocumentId());
            srRepo.add(sr,troncon);
        }
        
        SystemeReperageBorne srbStart = null;
        SystemeReperageBorne srbEnd = null;
        
        if(sr.systemeReperageBornes.size()>0){
            srbStart = sr.systemeReperageBornes.get(0);
        }
        if(sr.systemeReperageBornes.size()>1){
            srbEnd = sr.systemeReperageBornes.get(sr.systemeReperageBornes.size()-1);
        }
        
        BorneDigue bdStart = null;
        BorneDigue bdEnd = null;
        if(srbStart==null){
            //creation de la borne de début
            bdStart = bdRepo.create();
            bdStart.setLibelle("Début du tronçon");
            bdStart.setGeometry(GO2Utilities.JTS_FACTORY.createPoint(new Coordinate()));
            bdRepo.add(bdStart);
            
            srbStart = session.getElementCreator().createElement(SystemeReperageBorne.class);
            srbStart.setBorneId(bdStart.getDocumentId());
            sr.systemeReperageBornes.add(srbStart);
        }else{
            bdStart = bdRepo.get(srbStart.getBorneId());
        }
        
        if(srbEnd==null){
            //creation de la borne de début
            bdEnd = session.getElementCreator().createElement(BorneDigue.class);
            bdEnd.setLibelle("Fin du tronçon");
            bdEnd.setGeometry(GO2Utilities.JTS_FACTORY.createPoint(new Coordinate()));
            bdRepo.add(bdEnd);
            
            srbEnd = session.getElementCreator().createElement(SystemeReperageBorne.class);
            srbEnd.setBorneId(bdEnd.getDocumentId());
            sr.systemeReperageBornes.add(srbEnd);
        }else{
            bdEnd = bdRepo.get(srbEnd.getBorneId());
        }
        

        //calcul des nouvelles valeurs pour les bornes
        final double length = troncon.getGeometry().getLength();
        final Coordinate[] coords = troncon.getGeometry().getCoordinates();
        
        bdStart.setGeometry(GO2Utilities.JTS_FACTORY.createPoint(coords[0]));
        bdEnd.setGeometry(GO2Utilities.JTS_FACTORY.createPoint(coords[coords.length-1]));

        bdRepo.update(bdStart);
        bdRepo.update(bdEnd);
        
        srbStart.setValeurPR(0);
        srbEnd.setValeurPR((float)length);
        
        srRepo.update(sr,troncon);
    }
    
    /**
     * Recalcule des geometries des differents positionnables apres que
     * la géometrie ou que les SR du troncon aient changés.
     * 
     * @param troncon 
     */
    public static void updatePositionableGeometry(TronconDigue troncon, SessionGen session){
        for(Objet obj : getObjetList(troncon)){
            final LineString structGeom = buildGeometry(
                    troncon.getGeometry(), obj, session.getBorneDigueRepository());
            obj.setGeometry(structGeom);
        }     
        session.getTronconDigueRepository().update(troncon);
    }
    
    
    /**
     * A function to switch SR of a given PR. Gives the new PR value in the target SR.
     * 
     * @param refLinear An array containing the segments of the reference linear
     * along wich one the distances are computed.
     * @param initialPR The initial PR (expressed in the initial SR).
     * @param initialSR The SR the initial PR is expressed in.
     * @param targetSR The SR the result PR is required expressed in.
     * @param borneRepo The borne repository.
     * @return 
     */
    public static float switchSRForPR(
            final SegmentInfo[] refLinear, 
            final double initialPR, 
            final SystemeReperage initialSR, 
            final SystemeReperage targetSR, 
            final BorneDigueRepository borneRepo){
        ArgumentChecks.ensureNonNull("Reference linear", refLinear);
        ArgumentChecks.ensureNonNull("Initial SR", initialSR);
        ArgumentChecks.ensureNonNull("Target SR", targetSR);
        ArgumentChecks.ensureNonNull("Database connection", borneRepo);
        
        
        // Map des bornes du SR de saisie des PR/Z : la clef contient le PR des bornes dans le SR de saisi. La valeur contient l'id de la borne.
        final Map.Entry<Float, String>[] orderedInitialSRBornes = 
                initialSR.systemeReperageBornes.stream().map((SystemeReperageBorne srBorne) ->{
           return new HashMap.SimpleEntry<>(srBorne.getValeurPR(), srBorne.getBorneId());
        }).sorted((Map.Entry<Float, String> first, Map.Entry<Float, String> second)-> {
                    return Float.compare(first.getKey(), second.getKey());// On trie suivant la valeurs des PR qui est en clef.
                }).toArray((int size) -> {return new Map.Entry[size];});
        
        Map.Entry<Float, String> nearestInitialSRBorne = orderedInitialSRBornes[0];
        Map.Entry<Float, String> followingInitialSRBorne = orderedInitialSRBornes[1];
        int borneCnt = 1;
        while(++borneCnt<orderedInitialSRBornes.length && initialPR>orderedInitialSRBornes[borneCnt].getKey()){
            nearestInitialSRBorne=orderedInitialSRBornes[borneCnt-1];
            followingInitialSRBorne=orderedInitialSRBornes[borneCnt];
        }
        
        final double initialRatio = (initialPR-nearestInitialSRBorne.getKey())/(followingInitialSRBorne.getKey()-nearestInitialSRBorne.getKey());
        
        final BorneDigue nearestInitialSRBorneDigue = borneRepo.get(nearestInitialSRBorne.getValue());
        final BorneDigue followingInitialSRBorneDigue = borneRepo.get(followingInitialSRBorne.getValue());
        
        //Distance du point dont le PR est initialPr sur le troncon ?
        
        //=> distance de la nearestborne sur le troncon : 
        final ProjectedPoint nearestInitialSRBorneProj = projectReference(refLinear, nearestInitialSRBorneDigue.getGeometry());
        //=> distance de la secondBorneDigue sur le troncon : 
        final ProjectedPoint followingInitialSRBorneProj = projectReference(refLinear, followingInitialSRBorneDigue.getGeometry());
        
        //=> distance sur le troncon du point dont le PR est initialPR :
        final double distanceOrigineTroncon = nearestInitialSRBorneProj.distanceAlongLinear + (followingInitialSRBorneProj.distanceAlongLinear - nearestInitialSRBorneProj.distanceAlongLinear)*initialRatio;
        
        // On parcourt les segments
        SegmentInfo bonSegment = null;
        double distanceSurLeBonSegment = distanceOrigineTroncon;
        for (final SegmentInfo segmentInfo : refLinear){
            if(segmentInfo.endDistance>distanceOrigineTroncon){
                bonSegment = segmentInfo; break;
            } else {
                distanceSurLeBonSegment-=segmentInfo.length;
            }
        }
        
        if(bonSegment!=null){
            final Point initialPointPR = GO2Utilities.JTS_FACTORY.createPoint(bonSegment.getPoint(distanceSurLeBonSegment, 0));
            return computePR(refLinear, targetSR, initialPointPR, borneRepo);
        }
        else{
            throw new SirsCoreRuntimeExecption("Unable to compute segment for the given PR and SRs.");
        }
    }
    
    
    
    
    
    
    /**
     * Compute PR value for the point referenced by input linear parameter.
     * 
     * @param refLinear Reference linear for bornes positions and relative distances.
     * @param targetSR The system to express output PR into.
     * @param toGetPRFor the point we want to compute a PR for.
     * @param borneRepo Database  connection to read {@link BorneDigue} objects referenced in target {@link SystemeReperage}.
     * @return Value of the computed PR, or {@link Float.NaN} if we cannot compute any.
     */
    public static float computePR(final SegmentInfo[] refLinear, final SystemeReperage targetSR, final Point toGetPRFor, final BorneDigueRepository borneRepo) {
        ArgumentChecks.ensureNonNull("Reference linear", refLinear);
        ArgumentChecks.ensureNonNull("Target SR", targetSR);
        ArgumentChecks.ensureNonNull("Point to compute PR for", toGetPRFor);
        ArgumentChecks.ensureNonNull("Database connection", borneRepo);
        
        /* Compute PR for start position. We project its current location on its 
         * parent linear, an try to find the nearest bornes which enclose it.
         * If the object is located at start or end of the SR, we'll try to compute 
         * it's PR from the nearest bornes, even if they're not bounding it.
         * 
         * To find nearest bornes, we project each borne of the SR on linear,
         * then sort them by distance from our input projected positionable object.
         * After that, we try to peek the nearest bornes, whose one is before and 
         * the other is after our input object. If it is not possible, we will 
         * get the closest, not matter which side they are.
         * 
         * To ease object manipulation, we'll store for each borne an array :
         * - First element is the borne PR.
         * - Second is the distance from the input point to the borne (negative if 
         * borne is uphill from our object, positive if it's downhill).
         */
        ProjectedPoint startPoint = projectReference(refLinear, toGetPRFor);
        double[][] prAndDistances = targetSR.systemeReperageBornes.stream()
                .map((SystemeReperageBorne srBorne) -> {
                    BorneDigue borne = borneRepo.get(srBorne.getBorneId());
                    ProjectedPoint projBorne = projectReference(refLinear, borne.getGeometry());
                    return new double[] {
                            srBorne.getValeurPR(),
                            startPoint.distanceAlongLinear - projBorne.distanceAlongLinear 
                    };
                })
                .sorted(((double[] first, double[] second)-> {
                    final double firstDistance = StrictMath.abs(first[1]);
                    final double secondDistance = StrictMath.abs(second[1]);                    
                    return Double.compare(firstDistance, secondDistance);
                }))
                .toArray((int size) -> {return new double[size][2];});
        
        final double[] nearestBorne = prAndDistances[0];
        double[] secondBorne = prAndDistances[1];
        
        int borneCounter = 1;
        final double nearestSignum = StrictMath.signum(nearestBorne[1]);
        double secondSignum = StrictMath.signum(secondBorne[1]);
        while (++borneCounter < prAndDistances.length && nearestSignum == secondSignum) {
            secondSignum = StrictMath.signum(prAndDistances[borneCounter][1]);
            if (secondSignum != nearestSignum) {
                secondBorne = prAndDistances[borneCounter];
            }
        }
        
        final double upHillBorneDistance, downHillBorneDistance;
        final double upHillBornePR, downHillBornePR;
        if (nearestBorne[1] > secondBorne[1]) {
            upHillBorneDistance = nearestBorne[1];
            upHillBornePR = nearestBorne[0];
            downHillBorneDistance = secondBorne[1];
            downHillBornePR = secondBorne[0];
        } else {
            upHillBorneDistance = secondBorne[1];
            upHillBornePR = secondBorne[0];
            downHillBorneDistance = nearestBorne[1];
            downHillBornePR = nearestBorne[0];
        }
        final double distanceBetweenBornes = StrictMath.abs(downHillBorneDistance - upHillBorneDistance);
        final double prRatio = (downHillBornePR - upHillBornePR) / distanceBetweenBornes;
        
        return (float)(upHillBornePR + prRatio * upHillBorneDistance);
    }
    
    /**
     * Compute PR values (start and end point) for input {@link Positionable}.
     * @param targetPos The Positionable object to compute PR for.
     * @param session Connection to database, to retrieve SR and bornes.
     */
    public static void computePRs(final Positionable targetPos, final SessionGen session) {
        ArgumentChecks.ensureNonNull("Input position to compute PR for.", targetPos);
        ArgumentChecks.ensureNonNull("Database connection.", session);
        
        /* To be able to compute a PR, we need at least a valid linear position,
         * which implies at least one valid borne and a distance to this borne.
         * The borne must be contained in the current positionable target. 
         * We also need the linear on which the object is projected.
         */
        final PosInfo objInfo = new PosInfo(targetPos, session);
        LinearReferencing.SegmentInfo[] linearSegments = objInfo.getTronconSegments(false);
        ArgumentChecks.ensureNonNull("Linear for position projection.", linearSegments);
        
        final String srid = objInfo.getTroncon().getSystemeRepDefautId();        
        ArgumentChecks.ensureNonEmpty("SRID ", srid);
        
        BorneDigueRepository borneRepo = session.getBorneDigueRepository();        
        SystemeReperageRepository srRepo = session.getSystemeReperageRepository();
        SystemeReperage currentSR = srRepo.get(srid);
        
        targetPos.setPR_debut(computePR(linearSegments, currentSR, objInfo.getGeoPointStart(), borneRepo));
        targetPos.setPR_fin(computePR(linearSegments, currentSR, objInfo.getGeoPointEnd(), borneRepo));
    }
    
    /**
     * Utility object for manipulation of spatial information of a {@link Positionable} object.
     */
    public static final class PosInfo {

        private final Positionable pos;
        private final SessionGen session;
        private TronconDigue troncon;
        private Geometry linear;
        private SegmentInfo[] linearSegments;
        
        public PosInfo(Positionable pos, SessionGen session) {
            this(pos,null,session);
        }
        
        public PosInfo(Positionable pos, TronconDigue troncon, SessionGen session) {
            this.pos = pos;
            this.troncon = troncon;
            this.session = session;
        }

        public PosInfo(Positionable pos, TronconDigue troncon, SegmentInfo[] linear, SessionGen session) {
            this.pos = pos;
            this.troncon = troncon;
            this.linearSegments = linear;
            this.session = session;
        }
        
        /**
         * Try to retrieve {@link TronconDigue} on which thee current Positionable is defined.
         * @return Troncon of the object, or null if we cannot retrieve it (no valid SR).
         */
        public TronconDigue getTroncon() {
            if (troncon == null && pos != null) {
                if (pos.getParent() != null) {
                    Element tmp = pos.getParent();
                    while (tmp != null && !(tmp instanceof TronconDigue)) {
                        tmp = tmp.getParent();
                    }
                    troncon = (TronconDigue) tmp;
                }
                // Maybe we have an incomplete version of the document, so we try by querying repository.
                if (troncon == null) {
                    try {
                        troncon = session.getTronconDigueRepository().get(pos.getDocumentId());
                    } catch (Exception e) {
                        troncon = null;
                    }
                }
                // Last chance, we must try to get it from SR
                if (troncon == null && pos.getSystemeRepId() != null) {
                    SystemeReperage sr = session.getSystemeReperageRepository().get(pos.getSystemeRepId());
                    if (sr.getLinearId() != null) {
                        troncon = session.getTronconDigueRepository().get(sr.getLinearId());
                    }
                }
            }

            return troncon;
        }
        
        /**
         * Return the geometry object associated to the {@link TronconDigue} bound to
         * the positionable.
         * @return 
         */
        public Geometry getTronconLinear(){
            if(linear==null) {
                if (getTroncon() != null) {
                    linear = getTroncon().getGeometry();
                }
            }
            return linear;
        }
        
        /**
         * Succession of segments which compose the geometry of the tronçon.
         * @param forceRefresh True if we want to reload tronçon geometry from 
         * database, false if we want to get it from cache.
         * @return An ordered list of the segments of current tronçon.
         */
        public SegmentInfo[] getTronconSegments(final boolean forceRefresh) {
            if (linearSegments == null || forceRefresh) {
                final LineString tmpLinear = asLineString(getTronconLinear());
                if (tmpLinear != null)
                        linearSegments = buildSegments(tmpLinear);
            }
            return linearSegments;
        }
        
        /**
         * Get input Positionable start point in native CRS. If it does not exist,
         * it's computed from linear position of the Positionable.
         * @return A point, never null.
         * @throws IllegalStateException If we cannot get nor compute any point.
         */
        public Point getGeoPointStart() {
            Point point = pos.getPositionDebut();
            //calcul de la position geographique
            if (point == null) {
                if (pos.getBorneDebutId() != null) {
                    //calcule a partir des bornes
                    final Point bornePoint = session.getBorneDigueRepository().get(pos.getBorneDebutId()).getGeometry();
                    double dist = pos.getBorne_debut_distance();
                    if (pos.getBorne_debut_aval()) {
                        dist *= -1;
                    }
                    point = computeCoordinate(getTronconSegments(false), bornePoint, dist, 0);

                } else if (pos.getPositionFin() != null) {
                    point = pos.getPositionFin();

                } else if (pos.getBorneFinId() != null) {
                    final Point bornePoint = session.getBorneDigueRepository().get(pos.getBorneFinId()).getGeometry();
                    double dist = pos.getBorne_fin_distance();
                    if (pos.getBorne_fin_aval()) {
                        dist *= -1;
                    }
                    point = computeCoordinate(getTronconSegments(false), bornePoint, dist, 0);

                } else {
                    throw new IllegalStateException("Pas de borne ou position de début/fin définie pour l'objet " + pos);
                }
            }
            return point;
        }
        
        /**
         * Get input Positionable start point, reprojected in given CRS. If it does not exist,
         * it's computed from linear position of the Positionable.
         * @param crs
         * @return A point, never null.
         * @throws org.opengis.util.FactoryException If we cannot access Referencing module.
         * @throws IllegalStateException If we cannot get nor compute any point.
         * @throws org.opengis.referencing.operation.TransformException if an error happens during reprojecction.
         */
        public Point getGeoPointStart(CoordinateReferenceSystem crs) throws 
                FactoryException, MismatchedDimensionException, TransformException{
            Point point = getGeoPointStart();
            final CoordinateReferenceSystem geomCrs = JTS.findCoordinateReferenceSystem(point);
            if(crs!=null && !CRS.equalsIgnoreMetadata(geomCrs,crs)){
                final MathTransform trs = CRS.findMathTransform(geomCrs, crs);
                point = (Point) JTS.transform(point, trs);
            }
            return point;
        }
        
        /**
         * Get input Positionable end point in native CRS. If it does not exist,
         * it's computed from linear position of the Positionable.
         * @return A point, never null.
         * @throws IllegalStateException If we cannot get nor compute any point.
         */
        public Point getGeoPointEnd() {
            Point point = pos.getPositionFin();
            //calcul de la position geographique
            if (point == null) {
                if (pos.getBorneFinId() != null) {
                    //calcule a partir des bornes
                    final Point bornePoint = session.getBorneDigueRepository().get(pos.getBorneFinId()).getGeometry();
                    double dist = pos.getBorne_fin_distance();
                    if (pos.getBorne_fin_aval()) {
                        dist *= -1;
                    }
                    point = computeCoordinate(getTronconSegments(false), bornePoint, dist, 0);

                } else if (pos.getPositionDebut() != null) {
                    point = pos.getPositionDebut();
                    
                } else if (pos.getBorneDebutId() != null) {
                    final Point bornePoint = session.getBorneDigueRepository().get(pos.getBorneDebutId()).getGeometry();
                    double dist = pos.getBorne_debut_distance();
                    if (pos.getBorne_debut_aval()) {
                        dist *= -1;
                    }
                    point = computeCoordinate(getTronconSegments(false), bornePoint, dist, 0);
                    
                } else {
                    throw new IllegalStateException("Pas de borne ou position de début/fin définie pour l'objet " + pos);
                }
            }
            return point;
        }
        
        /**
         * Get input Positionable end point, reprojected in given CRS. If it does not exist,
         * it's computed from linear position of the Positionable.
         * @param crs
         * @return A point, never null.
         * @throws org.opengis.util.FactoryException If we cannot access Referencing module.
         * @throws IllegalStateException If we cannot get nor compute any point.
         * @throws org.opengis.referencing.operation.TransformException if an error happens during reprojecction.
         */
        public Point getGeoPointEnd(CoordinateReferenceSystem crs) throws 
                FactoryException, TransformException {            
            Point point = getGeoPointEnd();
            final CoordinateReferenceSystem geomCrs = JTS.findCoordinateReferenceSystem(point);
            if(crs!=null && !CRS.equalsIgnoreMetadata(geomCrs,crs)){
                final MathTransform trs = CRS.findMathTransform(geomCrs, crs);
                point = (Point) JTS.transform(point, trs);
            }
            return point;
        }
        
        public PosSR getForSR() {
            String srid = pos.getSystemeRepId();
            if(srid==null){
                //On utilise le SR du troncon
                srid = getTroncon().getSystemeRepDefautId();
                if(srid==null) return new PosSR();
                final SystemeReperage sr = session.getSystemeReperageRepository().get(srid);
                if(sr==null) return new PosSR();
                return getForSR(sr);
            }else{
                //valeur deja présente
                final PosSR possr = new PosSR();
                possr.srid = srid;
                possr.borneStartId = pos.getBorneDebutId();
                possr.distanceStartBorne = pos.getBorne_debut_distance();
                possr.startAval = pos.getBorne_debut_aval();
                
                possr.borneEndId = pos.getBorneFinId();
                possr.distanceEndBorne = pos.getBorne_fin_distance();                
                possr.endAval = pos.getBorne_fin_aval();
                return possr;
            }
        }
        
        public PosSR getForSR(SystemeReperage sr) {
            final Point startPoint = getGeoPointStart();
            final Point endPoint = getGeoPointEnd();
            
            final List<BorneDigue> bornes = new ArrayList<>();
            final List<Point> references = new ArrayList<>();
            for(SystemeReperageBorne srb : sr.systemeReperageBornes){
                final String bid = srb.getBorneId();
                final BorneDigue bd = session.getBorneDigueRepository().get(bid);
                if(bd!=null){ 
                    bornes.add(bd);
                    references.add(bd.getGeometry());
                }
            }
            
            final PosSR possr = new PosSR();
            possr.srid = sr.getDocumentId();
                        
            final Map.Entry<Integer, Double> startRef = computeRelative(getTronconSegments(false), references.toArray(new Point[0]), startPoint);
            final BorneDigue startBorne = bornes.get(startRef.getKey());
            possr.borneStartId = startBorne.getDocumentId();
            possr.distanceStartBorne = startRef.getValue();
            possr.startAval = possr.distanceStartBorne < 0;
            possr.distanceStartBorne = Math.abs(possr.distanceStartBorne);
            
            final Map.Entry<Integer, Double> endRef = computeRelative(getTronconSegments(false), references.toArray(new Point[0]), endPoint);
            final BorneDigue endBorne = bornes.get(endRef.getKey());
            possr.borneEndId = endBorne.getDocumentId();
            possr.distanceEndBorne = endRef.getValue();
            possr.endAval = possr.distanceEndBorne < 0;
            possr.distanceEndBorne = Math.abs(possr.distanceEndBorne);
            
            return possr;
        }
    }
    
    public static final class PosSR{
        public String srid = "";
        public String borneStartId = "";
        public double distanceStartBorne;
        public boolean startAval;
        public String borneEndId = "";
        public double distanceEndBorne;
        public boolean endAval;
    }
}
