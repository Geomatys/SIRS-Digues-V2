
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
import java.util.Iterator;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class TronconUtils {
 
    public static final String SR_ELEMENTAIRE = "Elémentaire";
    
    private static final GeometryFactory GF = GO2Utilities.JTS_FACTORY;
        
    /**
     * 
     * @param troncon troncon a decouper
     * @param cutLinear partie du troncon a garder
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

        final SystemeReperageRepository srRepo = session.getSystemeReperageRepository();
        final BorneDigueRepository bdRepo = session.getBorneDigueRepository();
        final TronconDigueRepository tdRepo = session.getTronconDigueRepository();

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
        for (final SystemeReperage sr : srRepo.getByTroncon(troncon)) {
            final SystemeReperage srCp = sr.copy();
            final ListIterator<SystemeReperageBorne> srBorneIt = srCp.getSystemeReperageBorne().listIterator();
            while (srBorneIt.hasNext()) {
                if (!keptBornes.contains(srBorneIt.next().getBorneId())) {
                    srBorneIt.remove();
                }
            }
            if (!srCp.systemeReperageBorne.isEmpty()) {
                newSRs.put(sr.getId(), srCp);
            }
        }

        /* On va maintenant trier les structures du tronçon. Ceux qui sont en dehors
         * du nouveau tronçon sont supprimés de ce dernier. Pour tout objet contenu,
         * on met simplement à jour ses positions linéaires pour rester cohérent
         * avec sa géométrie. Les objets qui intersectent le nouveau tronçons sont 
         * quand à eux découpés.
         */
        final HashMap<SystemeReperage, List<Objet>> needSRIDUpdate = new HashMap<>();
        ListIterator<Objet> structures = tronconCp.getStructures().listIterator();
        while (structures.hasNext()) {
            final Objet structure = structures.next();

            //on vérifie que cet objet intersecte le segment
            Geometry objGeom = structure.getGeometry();
            if (objGeom == null) {
                //on la calcule
                objGeom = buildGeometry(troncon.getGeometry(), structure, bdRepo);
                if (objGeom == null) {
                    throw new IllegalStateException("Impossible de déterminer la géométrie de l'objet suivant :\n" + structure);
                }
                structure.setGeometry(objGeom);
            }

            if (!cutLinear.intersects(objGeom)) {
                structures.remove();
                continue;
            } else if (!cutLinear.contains(objGeom)) {
                objGeom = cutLinear.intersection(objGeom);
                structure.setGeometry(objGeom);
            }

            if (objGeom instanceof Point) {
                structure.setPositionDebut((Point) objGeom);
                structure.setPositionFin((Point) objGeom);
            } else {
                final LineString structureLine = asLineString(objGeom);
                structure.setPositionDebut(structureLine.getStartPoint());
                structure.setPositionFin(structureLine.getEndPoint());
                structure.setGeometry(objGeom);
            }

            final SystemeReperage sr = newSRs.get(structure.getSystemeRepId());
            if (sr == null) {
                structure.setSystemeRepId(null);
                structure.setBorneDebutId(null);
                structure.setBorneFinId(null);
                structure.setPR_debut(Float.NaN);
                structure.setPR_fin(Float.NaN);
                structure.setBorne_debut_distance(Float.NaN);
                structure.setBorne_fin_distance(Float.NaN);
            } else {
                //le systeme de reperage existe toujours, on recalcule les positions relatives
                final PosInfo info = new PosInfo(structure, tronconCp, session);
                final PosSR posSr = info.getForSR(sr);

                // On garde la reference de l'objet, car on devra le lier au nouveau SR quand ce dernier aura été inséré.
                List<Objet> boundObjets = needSRIDUpdate.get(sr);
                if (boundObjets == null) {
                    boundObjets = new ArrayList<>();
                    needSRIDUpdate.put(sr, boundObjets);
                }
                boundObjets.add(structure);
                
                structure.setBorneDebutId(posSr.borneStartId);
                structure.setBorne_debut_distance((float) posSr.distanceStartBorne);
                structure.setBorne_debut_aval(posSr.startAval);
                structure.setBorneFinId(posSr.borneEndId);
                structure.setBorne_fin_distance((float) posSr.distanceEndBorne);
                structure.setBorne_fin_aval(posSr.endAval);
                structure.setPositionDebut(null);
                structure.setPositionFin(null);
            }
        }

        // On sauvegarde les modifications
        // TODO : make it transactional.
        tdRepo.add(tronconCp);
        // On essaye de trouver un SR par défaut pour notre nouveau tronçon.   
        final SystemeReperage newDefaultSR = newSRs.remove(troncon.getSystemeRepDefautId());
        if (newDefaultSR != null) {
            newDefaultSR.setTronconId(tronconCp.getDocumentId());
            srRepo.add(newDefaultSR, tronconCp, true);
        }
        for (final SystemeReperage newSR : newSRs.values()) {
            newSR.setTronconId(tronconCp.getDocumentId());
            srRepo.add(newSR, tronconCp, false);
        }
        // Maintenant que notre tronçon et nos SR sont enregistrés, on peut relier 
        // les objets du tronçon à leur SR.
        if (!needSRIDUpdate.isEmpty()) {
            Iterator<Map.Entry<SystemeReperage, List<Objet>>> it = needSRIDUpdate.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<SystemeReperage, List<Objet>> next = it.next();
                for (final Objet o : next.getValue()) {
                    o.setSystemeRepId(next.getKey().getId());
                }
            }
            
            tdRepo.update(tronconCp);
        }
        return tronconCp;
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
        
        //on ajoute les bornes. Pas de copie / modification ici, car les bornes 
        // indépendantes des tronçons.
        final Set<String> borneIds = new HashSet<>();
        borneIds.addAll(mergeResult.getBorneIds());
        borneIds.addAll(mergeParam.getBorneIds());
        mergeResult.setBorneIds(new ArrayList<>(borneIds));
                
        /* On fusionne les SR. On cherche les systèmes portant le même nom dans 
         * les deux tronçons originaux, puis en fait un seul comportant les bornes
         * des deux. Pour le reste, on fait une simple copie des SR.
         */
        final HashMap<String, String> modifiedSRs = new HashMap<>(); 
        for(SystemeReperage sr2 : srRepo.getByTroncon(mergeParam)){
            
            //on cherche le SR du meme nom
            SystemeReperage sibling = null;
            for(SystemeReperage sr1 : srRepo.getByTroncon(mergeResult)){
                if(sr1.getLibelle().equals(sr2.getLibelle())){
                    sibling = sr1;
                    break;
                }
            }
                 
            if(sibling==null){
                //on copy le SR
                final SystemeReperage srCp = sr2.copy();
                srCp.setTronconId(mergeResult.getDocumentId());
                //sauvegarde du sr
                srRepo.add(srCp, mergeResult);
                modifiedSRs.put(sr2.getId(), srCp.getId());
            }else{
                //on merge les bornes
                final List<SystemeReperageBorne> srbs1 = sibling.getSystemeReperageBorne();
                final List<SystemeReperageBorne> srbs2 = sr2.getSystemeReperageBorne();
                    
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
        
        // On ajoute les structures du tronçon paramètre. On les copie pour changer le SR associé.
        for (final Objet objet : mergeParam.structures) {
            Objet copy = objet.copy();
            final String srId = modifiedSRs.get(copy.getSystemeRepId());
            if (srId != null) {
                copy.setSystemeRepId(srId);
            }
            mergeResult.structures.add(copy);
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
     */
    public static void updateSRElementaire(TronconDigue troncon, SessionGen session){
                
        final SystemeReperageRepository srRepo = session.getSystemeReperageRepository();
        final BorneDigueRepository bdRepo = session.getBorneDigueRepository();
        
        final List<SystemeReperage> srs = srRepo.getByTroncon(troncon);
        
        SystemeReperage sr = null;
        for(SystemeReperage csr : srs){
            if(SR_ELEMENTAIRE.equalsIgnoreCase(csr.getLibelle())){
                sr = csr;
                break;
            }
        }
        
        //on le crée s'il n'existe pas
        if(sr==null){
            sr = new SystemeReperage();
            sr.setLibelle(SR_ELEMENTAIRE);
            sr.setTronconId(troncon.getDocumentId());
            srRepo.add(sr,troncon);
        }
        
        SystemeReperageBorne srbStart = null;
        SystemeReperageBorne srbEnd = null;
        
        if(sr.systemeReperageBorne.size()>0){
            srbStart = sr.systemeReperageBorne.get(0);
        }
        if(sr.systemeReperageBorne.size()>1){
            srbEnd = sr.systemeReperageBorne.get(sr.systemeReperageBorne.size()-1);
        }
        
        BorneDigue bdStart = null;
        BorneDigue bdEnd = null;
        if(srbStart==null){
            //creation de la borne de début
            bdStart = new BorneDigue();
            bdStart.setLibelle("Début du tronçon");
            bdStart.setGeometry(GO2Utilities.JTS_FACTORY.createPoint(new Coordinate()));
            bdRepo.add(bdStart);
            
            srbStart = new SystemeReperageBorne();
            srbStart.setBorneId(bdStart.getDocumentId());
            sr.systemeReperageBorne.add(srbStart);
        }else{
            bdStart = bdRepo.get(srbStart.getBorneId());
        }
        
        if(srbEnd==null){
            //creation de la borne de début
            bdEnd = new BorneDigue();
            bdEnd.setLibelle("Fin du tronçon");
            bdEnd.setGeometry(GO2Utilities.JTS_FACTORY.createPoint(new Coordinate()));
            bdRepo.add(bdEnd);
            
            srbEnd = new SystemeReperageBorne();
            srbEnd.setBorneId(bdEnd.getDocumentId());
            sr.systemeReperageBorne.add(srbEnd);
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
        for(Objet obj : troncon.getStructures()){
            final LineString structGeom = buildGeometry(
                    troncon.getGeometry(), obj, session.getBorneDigueRepository());
            obj.setGeometry(structGeom);
        }     
        session.getTronconDigueRepository().update(troncon);
    }
    
    
    public static final class PosInfo{

        private final Positionable pos;
        private final SessionGen session;
        private TronconDigue troncon;
        private Geometry linear;
        
        public PosInfo(Positionable pos, SessionGen session) {
            this(pos,null,session);
        }
        
        public PosInfo(Positionable pos, TronconDigue troncon, SessionGen session) {
            this.pos = pos;
            this.troncon = troncon;
            this.session = session;
        }

        public TronconDigue getTroncon() {
            if(troncon==null){
                troncon = session.getTronconDigueRepository().get(pos.getDocumentId());
            }
            return troncon;
        }
        
        public Geometry getTronconLinear(){
            if(linear==null){
                linear = getTroncon().getGeometry();
            }
            return linear;
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
                    if (!pos.getBorne_debut_aval()) {
                        dist *= -1;
                    }
                    point = computeCoordinate(getTronconLinear(), bornePoint, dist, 0);

                } else if (pos.getPositionFin() != null) {
                    point = pos.getPositionFin();

                } else if (pos.getBorneFinId() != null) {
                    final Point bornePoint = session.getBorneDigueRepository().get(pos.getBorneFinId()).getGeometry();
                    double dist = pos.getBorne_fin_distance();
                    if (!pos.getBorne_fin_aval()) {
                        dist *= -1;
                    }
                    point = computeCoordinate(getTronconLinear(), bornePoint, dist, 0);

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
                    if (!pos.getBorne_fin_aval()) {
                        dist *= -1;
                    }
                    point = computeCoordinate(getTronconLinear(), bornePoint, dist, 0);

                } else if (pos.getPositionDebut() != null) {
                    point = pos.getPositionDebut();
                    
                } else if (pos.getBorneDebutId() != null) {
                    final Point bornePoint = session.getBorneDigueRepository().get(pos.getBorneDebutId()).getGeometry();
                    double dist = pos.getBorne_debut_distance();
                    if (!pos.getBorne_debut_aval()) {
                        dist *= -1;
                    }
                    point = computeCoordinate(getTronconLinear(), bornePoint, dist, 0);
                    
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
            for(SystemeReperageBorne srb : sr.systemeReperageBorne){
                final String bid = srb.getBorneId();
                final BorneDigue bd = session.getBorneDigueRepository().get(bid);
                if(bd!=null){ 
                    bornes.add(bd);
                    references.add(bd.getGeometry());
                }
            }
            
            final PosSR possr = new PosSR();
            possr.srid = sr.getDocumentId();
            
            final Geometry tmpLinear = getTronconLinear();
            
            final Map.Entry<Integer, Double> startRef = computeRelative(tmpLinear, references.toArray(new Point[0]), startPoint);
            final BorneDigue startBorne = bornes.get(startRef.getKey());
            possr.borneStartId = startBorne.getDocumentId();
            possr.distanceStartBorne = startRef.getValue();
            possr.startAval = possr.distanceStartBorne>=0;
            possr.distanceStartBorne = Math.abs(possr.distanceStartBorne);
            
            final Map.Entry<Integer, Double> endRef = computeRelative(tmpLinear, references.toArray(new Point[0]), endPoint);
            final BorneDigue endBorne = bornes.get(endRef.getKey());
            possr.borneEndId = endBorne.getDocumentId();
            possr.distanceEndBorne = endRef.getValue();
            possr.endAval = possr.distanceEndBorne>=0;
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
