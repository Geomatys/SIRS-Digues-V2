package fr.sirs.core;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.linearref.LengthIndexedLine;
import fr.sirs.core.component.BorneDigueRepository;
import fr.sirs.core.component.SystemeReperageRepository;
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
import fr.sirs.core.component.TronconDigueRepository;
import fr.sirs.core.model.AbstractPositionDocument;
import fr.sirs.core.model.AvecForeignParent;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.GardeTroncon;
import fr.sirs.core.model.Photo;
import fr.sirs.core.model.ProprieteTroncon;
import fr.sirs.util.StreamingIterable;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.logging.Level;
import org.geotoolkit.referencing.LinearReferencing;
import org.geotoolkit.util.collection.CloseableIterator;

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
    public static TronconDigue cutTroncon(TronconDigue troncon, LineString cutLinear, String newName, SessionCore session) {
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
        final AbstractSIRSRepository<BorneDigue> bdRepo = session.getRepositoryForClass(BorneDigue.class);
        final AbstractSIRSRepository<TronconDigue> tdRepo = session.getRepositoryForClass(TronconDigue.class);

        //======================================================================
        // MODIFICATIONS DE BASE SUR LE TRONÇON
        //======================================================================
        final TronconDigue tronconCp = troncon.copy();
        tronconCp.setGeometry(cutLinear);
        tronconCp.setLibelle(newName);
        // On enlève toute réference vers un SR appartenant au tronçon copié
        tronconCp.setSystemeRepDefautId(null);

        // On sauvegarde les modifications
        tdRepo.add(tronconCp);

        //======================================================================
        // RÉFÉRENCES DE BORNES À CONSERVER
        //======================================================================
        /* On évince toutes les bornes qui ne sont pas sur le nouveau tronçon. On
         * garde un index des ids de borne conservés, cela accélerera le tri sur
         * les SR.
         */
        final SegmentInfo[] sourceTronconSegments = buildSegments(asLineString(troncon.geometryProperty().get()));
        final ListIterator<BorneDigue> borneIt = bdRepo.get(tronconCp.getBorneIds()).listIterator();
        final HashSet<String> keptBornes = new HashSet<>();
        while (borneIt.hasNext()) {
            final BorneDigue borne = borneIt.next();
            final ProjectedPoint proj = projectReference(sourceTronconSegments, borne.getGeometry());
            if (proj.distanceAlongLinear < startDistance || proj.distanceAlongLinear > endDistance) {
                borneIt.remove();
            } else {
                keptBornes.add(borne.getId());
            }
        }

        //======================================================================
        // COPIE DES SRs / RETRAIT DES BORNES HS / AFFECTATION D'UN SR PAR DEFAUT / MISE À JOUR DU SR ÉLÉMENTAIRE
        //======================================================================
        /* On copie les SR du tronçon original. Pour chaque, on regarde si il contient
         * des bornes référencées sur le nouveau tronçon. Si c'est le cas, on le
         * garde pour enregistrement à la fin de l'opération. On garde aussi une
         * réference vers le SR original, pour pouvoir mettre à jour la position
         * des structures.
         */
        final HashMap<String, SystemeReperage> newSRs = new HashMap<>();
        final StreamingIterable<SystemeReperage> srs = srRepo.getByLinearStreaming(troncon);
        try (final CloseableIterator<SystemeReperage> srIt = srs.iterator()) {
            while (srIt.hasNext()) {
                final SystemeReperage sr = srIt.next();
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
        }
        // On essaye de trouver un SR par défaut pour notre nouveau tronçon et on enregistre les SR.
        // On l'enleve de la map pour le remettre après insertion de tous les autres SRs, car il doit
        // être traité différemment.
        final SystemeReperage newDefaultSR = newSRs.remove(troncon.getSystemeRepDefautId());
        if (newDefaultSR != null) {
            newDefaultSR.setLinearId(tronconCp.getDocumentId());
            srRepo.add(newDefaultSR, tronconCp, true);
        }
        for (final SystemeReperage newSR : newSRs.values()) {
            newSR.setLinearId(tronconCp.getDocumentId());
            srRepo.add(newSR, tronconCp, false);
        }
        if (troncon.getSystemeRepDefautId() != null) {
            newSRs.put(troncon.getSystemeRepDefautId(), newDefaultSR);
        }

        // Mise à jour particulière pour le SR élémentaire qui doit avoir une borne de début et de fin.
        updateSRElementaire(tronconCp, session);

        //======================================================================
        // DÉCOUPAGE DES POSITIONABLES POSITIONÉS SUR LE MORCEAU DE TRONÇON
        //======================================================================
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
            final Positionable originalPositionable = posIt.next();

            //on vérifie que cet objet intersecte le segment
            Geometry rawObjGeom = originalPositionable.getGeometry();

            if (rawObjGeom == null) {
                //on la calcule
                rawObjGeom = buildGeometry(troncon.getGeometry(), originalPositionable, bdRepo);
                if (rawObjGeom == null) {
                    throw new IllegalStateException("Impossible de déterminer la géométrie de l'objet suivant :\n" + originalPositionable);
                }
                originalPositionable.setGeometry(rawObjGeom);
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

            final Positionable position = originalPositionable.copy();
            if (position instanceof AvecForeignParent) {
                ((AvecForeignParent)position).setForeignParentId(tronconCp.getId());
            }
            newPositions.add(position);

            // Mise à jour des infos géographiques
            if (!cutLinear.contains(interpObjGeom)) {
                interpObjGeom = cutLinear.intersection(interpObjGeom);
                position.setGeometry(interpObjGeom);
            }

            if (interpObjGeom instanceof Point) {
                position.setPositionDebut((Point) interpObjGeom);
                position.setPositionFin((Point) interpObjGeom);
            } else {
                final LineString structureLine = asLineString(interpObjGeom);
                position.setPositionDebut(structureLine.getStartPoint());
                position.setPositionFin(structureLine.getEndPoint());
                position.setGeometry(interpObjGeom);
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
//        tdRepo.update(tronconCp);

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
        }

        // Et on termine par la sérialisation des objets positionés sur le nouveau tronçon.
        session.executeBulk((Collection)newPositions);

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
    public static List<GardeTroncon> getGardeList(final TronconDigue linear) {
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
    public static List<Objet> getObjetList(final TronconDigue linear) {
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
        return InjectorCore.getBean(SessionCore.class).getPhotoList(linearId);
    }

    /**
     * Return the positionable included, linked or included into linked elements
     * for the linar given as a parameter.
     * @param linear
     * @return a list containing the objets, positions de documents, proprietes,
     * gardes and photos related to the linear.
     */
    public static List<Positionable> getPositionableList(final TronconDigue linear){
        return getPositionableList(linear.getId());
    }

    /**
     * Return the positionable included, linked or included into linked elements
     * for the linar given as a parameter.
     * @param linearId Id of the linear to filter on.
     * @return a list containing the objets, positions de documents, proprietes,
     * gardes and photos related to the linear.
     */
    public static List<Positionable> getPositionableList(final String linearId) {
        return InjectorCore.getBean(SessionCore.class).getPositionableByLinearId(linearId);
    }

    /**
     * Retrieve the list of photographs contained in objects or profil en
     * travers positions linked to a linear given as a parameter.
     * @param linear
     * @return
     */
    public static List<Photo> getPhotoList(final TronconDigue linear) {
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
    public static TronconDigue mergeTroncon(TronconDigue mergeResult, TronconDigue mergeParam, SessionCore session) {

        final SystemeReperageRepository srRepo = (SystemeReperageRepository) session.getRepositoryForClass(SystemeReperage.class);

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

        final StreamingIterable<SystemeReperage> srs = srRepo.getByLinearStreaming(mergeParam);
        try (final CloseableIterator<SystemeReperage> srIt = srs.iterator()) {
            while (srIt.hasNext()) {
                SystemeReperage sr2 = srIt.next();

                //on cherche le SR du meme nom
                SystemeReperage sibling = null;
                for (SystemeReperage sr1 : srRepo.getByLinearId(mergeResult.getId())) {
                    if (sr1.getLibelle().equals(sr2.getLibelle())) {
                        sibling = sr1;
                        break;
                    }
                }

                if (sibling == null) {
                    //on copie le SR
                    final SystemeReperage srCp = sr2.copy();
                    srCp.setLinearId(mergeResult.getDocumentId());
                    //sauvegarde du sr
                    srRepo.add(srCp, mergeResult);
                    modifiedSRs.put(sr2.getId(), srCp.getId());
                } else {
                    //on merge les bornes
                    final List<SystemeReperageBorne> srbs1 = sibling.getSystemeReperageBornes();
                    final List<SystemeReperageBorne> srbs2 = sr2.getSystemeReperageBornes();

                    loop:
                    for (SystemeReperageBorne srb2 : srbs2) {
                        for (SystemeReperageBorne srb1 : srbs1) {
                            if (srb1.getBorneId().equals(srb2.getBorneId())) {
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

        final SystemeReperageRepository srRepo = (SystemeReperageRepository) session.getRepositoryForClass(SystemeReperage.class);
        final AbstractSIRSRepository<BorneDigue> bdRepo = session.getRepositoryForClass(BorneDigue.class);

        SystemeReperage sr = null;
        final StreamingIterable<SystemeReperage> srs = srRepo.getByLinearStreaming(troncon);
        try (final CloseableIterator<SystemeReperage> srIt = srs.iterator()) {
            while (srIt.hasNext()) {
                final SystemeReperage csr = srIt.next();
                if (SR_ELEMENTAIRE.equalsIgnoreCase(csr.getLibelle())) {
                    sr = csr;
                    break;
                }
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
            bdRepo.add(bdStart);

            srbStart = session.getElementCreator().createElement(SystemeReperageBorne.class);
            srbStart.setBorneId(bdStart.getDocumentId());
            sr.systemeReperageBornes.add(srbStart);
        }else{
            bdStart = bdRepo.get(srbStart.getBorneId());
        }

        if(srbEnd==null){
            //creation de la borne de fin
            bdEnd = bdRepo.create();
            bdEnd.setLibelle("Fin du tronçon");
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

        bdRepo.executeBulk(bdStart, bdEnd);

        srbStart.setValeurPR(0);
        srbEnd.setValeurPR((float)length);

        srRepo.update(sr,troncon);
    }

    /**
     * Recalcule des geometries des differents positionnables apres que
     * la géometrie ou que les SR du troncon aient changés.
     *
     * @param troncon
     * @param session
     */
    public static void updatePositionableGeometry(TronconDigue troncon, SessionCore session){
        for(Objet obj : getObjetList(troncon)){
            final LineString structGeom = buildGeometry(
                    troncon.getGeometry(), obj, session.getRepositoryForClass(BorneDigue.class));
            obj.setGeometry(structGeom);
        }
        session.getRepositoryForClass(TronconDigue.class).update(troncon);
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
            final AbstractSIRSRepository<BorneDigue> borneRepo){
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
            throw new SirsCoreRuntimeException("Unable to compute segment for the given PR and SRs.");
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
    public static float computePR(final SegmentInfo[] refLinear, final SystemeReperage targetSR, final Point toGetPRFor, final AbstractSIRSRepository<BorneDigue> borneRepo) {
        ArgumentChecks.ensureNonNull("Reference linear", refLinear);
        ArgumentChecks.ensureNonNull("Target SR", targetSR);
        ArgumentChecks.ensureNonNull("Point to compute PR for", toGetPRFor);
        ArgumentChecks.ensureNonNull("Database connection", borneRepo);

        final ProjectedPoint prjPt = projectReference(refLinear, toGetPRFor);

        final TreeMap<Double,SystemeReperageBorne> bornes = new TreeMap<>();
        for(SystemeReperageBorne srb : targetSR.systemeReperageBornes){
            final BorneDigue borne = borneRepo.get(srb.getBorneId());
            final ProjectedPoint projBorne = projectReference(refLinear, borne.getGeometry());
            bornes.put(projBorne.distanceAlongLinear, srb);
        }

        Map.Entry<Double, SystemeReperageBorne> under = bornes.floorEntry(prjPt.distanceAlongLinear);
        Map.Entry<Double, SystemeReperageBorne> above = bornes.ceilingEntry(prjPt.distanceAlongLinear);
        if(under==null) under = above;
        if(above==null) above = under;
        if(under==null) return 0.0f;

        if(under.equals(above)){
            //exactement sur le point.
            return under.getValue().getValeurPR();
        }else{
            //on interpole entre les deux bornes.
            final double distance = prjPt.distanceAlongLinear;
            final SystemeReperageBorne underBorne = under.getValue();
            final SystemeReperageBorne aboveBorne = above.getValue();
            final double diffPr = aboveBorne.getValeurPR()-underBorne.getValeurPR();
            final double diffDist = above.getKey() - under.getKey();
            final double ratio = (distance - under.getKey()) / diffDist;
            final double pr = underBorne.getValeurPR() + ratio*diffPr;
            return (float) pr;
        }

    }

    /**
     * Recherche des bornes amont et aval les plus proches.
     *
     * @param refLinear
     * @param targetSR
     * @param toGetPRFor
     * @param borneRepo
     * @return [0] distance<->Borne amont
     *         [1] distance<->Borne aval
     */
    public static Map.Entry<Double, SystemeReperageBorne>[] findNearest(final SegmentInfo[] refLinear, final SystemeReperage targetSR, final Point toGetPRFor, final AbstractSIRSRepository<BorneDigue> borneRepo) {
        ArgumentChecks.ensureNonNull("Reference linear", refLinear);
        ArgumentChecks.ensureNonNull("Target SR", targetSR);
        ArgumentChecks.ensureNonNull("Point to compute PR for", toGetPRFor);
        ArgumentChecks.ensureNonNull("Database connection", borneRepo);

        final ProjectedPoint prjPt = projectReference(refLinear, toGetPRFor);

        final TreeMap<Double,SystemeReperageBorne> bornes = new TreeMap<>();
        for(SystemeReperageBorne srb : targetSR.systemeReperageBornes){
            final BorneDigue borne = borneRepo.get(srb.getBorneId());
            final ProjectedPoint projBorne = projectReference(refLinear, borne.getGeometry());
            bornes.put(projBorne.distanceAlongLinear, srb);
        }

        Map.Entry<Double, SystemeReperageBorne> under = bornes.floorEntry(prjPt.distanceAlongLinear);
        Map.Entry<Double, SystemeReperageBorne> above = bornes.ceilingEntry(prjPt.distanceAlongLinear);

        if(under!=null){
            under = new AbstractMap.SimpleImmutableEntry(prjPt.distanceAlongLinear-under.getKey(),under.getValue());
        }
        if(above!=null){
            above = new AbstractMap.SimpleImmutableEntry(above.getKey()-prjPt.distanceAlongLinear,above.getValue());
        }

        return new Map.Entry[]{under,above};
    }

    /**
     * Compute PR values (start and end point) for input {@link Positionable}.
     * @param targetPos The Positionable object to compute PR for.
     * @param session Connection to database, to retrieve SR and bornes.
     */
    public static void computePRs(final Positionable targetPos, final SessionCore session) {
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

        BorneDigueRepository borneRepo = (BorneDigueRepository) session.getRepositoryForClass(BorneDigue.class);
        SystemeReperage currentSR = session.getRepositoryForClass(SystemeReperage.class).get(srid);

        targetPos.setPrDebut(computePR(linearSegments, currentSR, objInfo.getGeoPointStart(), borneRepo));
        targetPos.setPrFin(computePR(linearSegments, currentSR, objInfo.getGeoPointEnd(), borneRepo));
    }

    /**
     * Calcul de la position avec un systeme de reperage et un PR.
     *
     * @param sr
     * @param pr
     * @return
     */
    public static Point computeCoordinate(SystemeReperage sr, double pr){
        final TronconDigueRepository tronconRepo = InjectorCore.getBean(TronconDigueRepository.class);
        final BorneDigueRepository borneRepo = InjectorCore.getBean(BorneDigueRepository.class);
        final TronconDigue troncon = tronconRepo.get(sr.getLinearId());

        final List<SystemeReperageBorne> srbs = sr.getSystemeReperageBornes();

        //on cherche les bornes les plus proche
        SystemeReperageBorne borneBasse = null;
        SystemeReperageBorne borneHaute = null;
        for(SystemeReperageBorne srb : srbs){
            if( (borneBasse==null || borneBasse.getValeurPR() < srb.getValeurPR() ) && srb.getValeurPR()<= pr){
                borneBasse = srb;
            }
            if( (borneHaute==null || borneHaute.getValeurPR() > srb.getValeurPR() ) && srb.getValeurPR()>= pr){
                borneHaute = srb;
            }
        }

        if(borneBasse == null) borneBasse = borneHaute;
        if(borneHaute == null) borneHaute = borneBasse;
        if(borneBasse == null) return null;

        final Geometry linear = troncon.getGeometry();
        Point pt;
        if(borneBasse == borneHaute){
            //une seule borne, on ne peut pas caluler la valeur réel des PR.
            final BorneDigue borne = borneRepo.get(borneBasse.getBorneId());
            pt = LinearReferencingUtilities.computeCoordinate(linear, borne.getGeometry(), 0.0, 0.0);
        }else{
            final BorneDigue borne0 = borneRepo.get(borneBasse.getBorneId());
            final BorneDigue borne1 = borneRepo.get(borneHaute.getBorneId());

            final SegmentInfo[] segments = LinearReferencingUtilities.buildSegments(LinearReferencing.asLineString(linear));

            final LinearReferencing.ProjectedPoint rel0 = LinearReferencingUtilities.projectReference(segments, borne0.getGeometry());
            final LinearReferencing.ProjectedPoint rel1 = LinearReferencingUtilities.projectReference(segments, borne1.getGeometry());

            //on converti le PR en distance le long du lineaire
            final double diffPr = borneHaute.getValeurPR()-borneBasse.getValeurPR();
            final double diffDist = rel1.distanceAlongLinear - rel0.distanceAlongLinear;
            final double ratio = (pr - borneBasse.getValeurPR()) / diffPr;
            final double distance = ratio*diffDist;

            pt = LinearReferencingUtilities.computeCoordinate(linear, borne0.getGeometry(), distance, 0.0);
        }

        pt.setSRID(linear.getSRID());
        pt.setUserData(linear.getUserData());
        return pt;

    }

    /**
     * Calcul de la position avec un systeme de reperage, une borne et une distance.
     *
     * @param sr
     * @param srBorne
     * @param distance
     * @return
     */
    public static Point computeCoordinate(SystemeReperage sr, SystemeReperageBorne srBorne, double distance){
        final TronconDigueRepository tronconRepo = InjectorCore.getBean(TronconDigueRepository.class);
        final BorneDigueRepository borneRepo = InjectorCore.getBean(BorneDigueRepository.class);

        final TronconDigue troncon = tronconRepo.get(sr.getLinearId());

        //une seule borne, on ne peut pas caluler la valeur réel des PR.
        final BorneDigue borne = borneRepo.get(srBorne.getBorneId());
        final Geometry linear = troncon.getGeometry();
        final Point pt = LinearReferencingUtilities.computeCoordinate(linear, borne.getGeometry(), distance, 0.0);

        pt.setSRID(linear.getSRID());
        pt.setUserData(linear.getUserData());
        return pt;
    }

    /**
     * Utility object for manipulation of spatial information of a {@link Positionable} object.
     */
    public static final class PosInfo {

        private final Positionable pos;
        private final SessionCore session;
        private TronconDigue troncon;
        private Geometry linear;
        private SegmentInfo[] linearSegments;

        public PosInfo(Positionable pos, SessionCore session) {
            this(pos,null,session);
        }

        public PosInfo(Positionable pos, TronconDigue troncon, SessionCore session) {
            this.pos = pos;
            this.troncon = troncon;
            this.session = session;
        }

        public PosInfo(Positionable pos, TronconDigue troncon, SegmentInfo[] linear, SessionCore session) {
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
                        troncon = session.getRepositoryForClass(TronconDigue.class).get(pos.getDocumentId());
                    } catch (Exception e) {
                        troncon = null;
                    }
                }
                // Last chance, we must try to get it from SR
                if (troncon == null && pos.getSystemeRepId() != null) {
                    SystemeReperage sr = session.getRepositoryForClass(SystemeReperage.class).get(pos.getSystemeRepId());
                    if (sr.getLinearId() != null) {
                        troncon = session.getRepositoryForClass(TronconDigue.class).get(sr.getLinearId());
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
                    final Point bornePoint = session.getRepositoryForClass(BorneDigue.class).get(pos.getBorneDebutId()).getGeometry();
                    double dist = pos.getBorne_debut_distance();
                    if (pos.getBorne_debut_aval()) {
                        dist *= -1;
                    }
                    point = LinearReferencingUtilities.computeCoordinate(getTronconSegments(false), bornePoint, dist, 0);

                } else if (pos.getPositionFin() != null) {
                    point = pos.getPositionFin();

                } else if (pos.getBorneFinId() != null) {
                    final Point bornePoint = session.getRepositoryForClass(BorneDigue.class).get(pos.getBorneFinId()).getGeometry();
                    double dist = pos.getBorne_fin_distance();
                    if (pos.getBorne_fin_aval()) {
                        dist *= -1;
                    }
                    point = LinearReferencingUtilities.computeCoordinate(getTronconSegments(false), bornePoint, dist, 0);

                } else {
                    //we extract point from the geometry
                    Geometry geom = pos.getGeometry();
                    if(!(geom instanceof LineString)){
                        geom = LinearReferencing.project(getTronconSegments(false), geom);
                    }
                    final Coordinate[] coords = geom.getCoordinates();
                    point = GO2Utilities.JTS_FACTORY.createPoint(new Coordinate(coords[0]));
                    final CoordinateReferenceSystem crs = session.getProjection();
                    JTS.setCRS(point, crs);
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
                    final Point bornePoint = session.getRepositoryForClass(BorneDigue.class).get(pos.getBorneFinId()).getGeometry();
                    double dist = pos.getBorne_fin_distance();
                    if (pos.getBorne_fin_aval()) {
                        dist *= -1;
                    }
                    point = LinearReferencingUtilities.computeCoordinate(getTronconSegments(false), bornePoint, dist, 0);

                } else if (pos.getPositionDebut() != null) {
                    point = pos.getPositionDebut();

                } else if (pos.getBorneDebutId() != null) {
                    final Point bornePoint = session.getRepositoryForClass(BorneDigue.class).get(pos.getBorneDebutId()).getGeometry();
                    double dist = pos.getBorne_debut_distance();
                    if (pos.getBorne_debut_aval()) {
                        dist *= -1;
                    }
                    point = LinearReferencingUtilities.computeCoordinate(getTronconSegments(false), bornePoint, dist, 0);

                } else {
                    //we extract point from the geometry
                    Geometry geom = pos.getGeometry();
                    if(!(geom instanceof LineString)){
                        geom = LinearReferencing.project(linearSegments, geom);
                    }
                    final Coordinate[] coords = geom.getCoordinates();
                    point = GO2Utilities.JTS_FACTORY.createPoint(new Coordinate(coords[coords.length-1]));
                    final CoordinateReferenceSystem crs = session.getProjection();
                    JTS.setCRS(point, crs);
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
                final SystemeReperage sr = session.getRepositoryForClass(SystemeReperage.class).get(srid);
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
                final BorneDigue bd = session.getRepositoryForClass(BorneDigue.class).get(bid);
                if(bd!=null){
                    bornes.add(bd);
                    references.add(bd.getGeometry());
                }
            }

            final PosSR possr = new PosSR();
            possr.srid = sr.getDocumentId();

            final Map.Entry<Integer, Double> startRef = computeRelative(getTronconSegments(false), references.toArray(new Point[0]), startPoint);
            final BorneDigue startBorne = bornes.get(startRef.getKey());
            possr.borneDigueStart = startBorne;
            possr.borneStartId = startBorne.getDocumentId();
            possr.distanceStartBorne = startRef.getValue();
            possr.startAval = possr.distanceStartBorne < 0;
            possr.distanceStartBorne = Math.abs(possr.distanceStartBorne);

            final Map.Entry<Integer, Double> endRef = computeRelative(getTronconSegments(false), references.toArray(new Point[0]), endPoint);
            final BorneDigue endBorne = bornes.get(endRef.getKey());
            possr.borneDigueEnd = endBorne;
            possr.borneEndId = endBorne.getDocumentId();
            possr.distanceEndBorne = endRef.getValue();
            possr.endAval = possr.distanceEndBorne < 0;
            possr.distanceEndBorne = Math.abs(possr.distanceEndBorne);

            return possr;
        }
    }

    public static final class PosSR{
        public String srid = "";

        public BorneDigue borneDigueStart;
        public String borneStartId = "";
        public double distanceStartBorne;
        public boolean startAval;

        public BorneDigue borneDigueEnd;
        public String borneEndId = "";
        public double distanceEndBorne;
        public boolean endAval;
    }
}
