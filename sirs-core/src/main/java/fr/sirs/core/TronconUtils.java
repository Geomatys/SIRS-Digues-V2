
package fr.sirs.core;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
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
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.referencing.CRS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

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
     * @param segment partie du troncon a garder
     * @return nouveau troncon découpé
     */
    public static TronconDigue cutTroncon(TronconDigue troncon, LineString segment, String newName, SessionGen session){
        
        final SystemeReperageRepository srRepo = session.getSystemeReperageRepository();
        final BorneDigueRepository bdRepo = session.getBorneDigueRepository(); 
        final TronconDigueRepository tdRepo = session.getTronconDigueRepository();
        
        final TronconDigue tronconCp = troncon.copy();
        tronconCp.setGeometry(segment);
        tronconCp.setLibelle(newName);
        
        
        final LinearReferencingUtilities.SegmentInfo[] parts = LinearReferencingUtilities.buildSegments((LineString) tronconCp.geometryProperty().get());
        
        //on recupere les bornes qui sont sur le troncon
        final Map<String,BorneDigue> newBornes = new HashMap<>();
        tronconCp.getBorneIds().clear();
        for(String borneId : troncon.getBorneIds()){
            final BorneDigue borne = bdRepo.get(borneId);
            final LinearReferencingUtilities.ProjectedReference proj = LinearReferencingUtilities.projectReference(parts, borne.getGeometry());
            if(proj.perpendicularProjection){
                //on copie la borne
                final BorneDigue cp = borne.copy();
                bdRepo.add(cp);
                tronconCp.getBorneIds().add(cp.getDocumentId());
                newBornes.put(borneId, cp);
            }
        }
        
        //on sauvegarde maintenant car on va avoir besoin de la reference du nouveau troncon
        tdRepo.add(tronconCp);
        
        //on copie et on enleve les bornes des SR et les SR vide
        final Map<String,SystemeReperage> mapSrs = new HashMap<>();
        for(SystemeReperage sr : srRepo.getByTroncon(troncon)){
            final SystemeReperage srCp = sr.copy();
            srCp.systemereperageborneId.clear();
            srCp.setTronconId(tronconCp.getDocumentId());
            final List<SystemeReperageBorne> lst = sr.getSystemereperageborneId();
            for(int i=lst.size()-1;i>=0;i--){
                final SystemeReperageBorne srb = lst.get(i);
                final BorneDigue borne = newBornes.get(srb.getBorneId());
                if(newBornes.containsKey(srb.getBorneId())){
                    final SystemeReperageBorne cp = srb.copy();
                    cp.setBorneId(borne.getDocumentId());
                    srCp.systemereperageborneId.add(cp);
                }
            }
            if(!srCp.systemereperageborneId.isEmpty()){
                srRepo.add(srCp);
                mapSrs.put(sr.getDocumentId(), srCp);
            }
        }
        
        //on coupe les differents objets
        tronconCp.structures.clear();
                
        Geometry linear = tronconCp.getGeometry();
        
        for(Objet obj : troncon.structures){
            
            //on vérifie que cet objet intersect le segment
            Geometry objGeom = obj.getGeometry();
            if(objGeom==null){
                //on la calcule
                objGeom = LinearReferencingUtilities.buildGeometry(troncon.getGeometry(), obj, bdRepo);
                obj.setGeometry(objGeom);
            }
            
            if(!linear.intersects(objGeom)){
                continue;
            }
            
            final Objet objCp = obj.copy();
            tronconCp.structures.add(objCp);
            
            try{
                if(mapSrs.containsKey(objCp.getSystemeRepId())){
                    //le systeme de reperage existe toujours, on recalcule les positions relatives
                    final SystemeReperage sr = mapSrs.get(objCp.getSystemeRepId());
                    objCp.setSystemeRepId(sr.getDocumentId());
                    
                    //on sort les positions geographique
                    final PosInfo infoOrig = new PosInfo(obj,troncon,session);                    
                    objCp.setBorneDebutId(null);
                    objCp.setBorneFinId(null);
                    objCp.setPositionDebut(infoOrig.getGeoPointStart(null));
                    objCp.setPositionFin(infoOrig.getGeoPointEnd(null));
                    
                    //on recalcule les positions relatives
                    final PosInfo info = new PosInfo(objCp,tronconCp,session);
                    final PosSR posSr = info.getForSR(sr);
                    objCp.setBorneDebutId(posSr.borneStartId);
                    objCp.setBorne_debut_distance((float)posSr.distanceStartBorne);
                    objCp.setBorne_debut_aval(posSr.startAval);
                    objCp.setBorneFinId(posSr.borneEndId);
                    objCp.setBorne_fin_distance((float)posSr.distanceEndBorne);
                    objCp.setBorne_fin_aval(posSr.endAval);
                    objCp.setPositionDebut(null);
                    objCp.setPositionFin(null);
                    
                }else{
                    //on utilise les coordonnées géo reprojetées afin de s'assurer qu'ils sont sur le troncon
                    final PosInfo info = new PosInfo(obj,troncon,session);
                    final Point start = info.getGeoPointStart(null);
                    final Point end = info.getGeoPointEnd(null);
                    
                    final LinearReferencingUtilities.ProjectedReference projectStart = LinearReferencingUtilities.projectReference(parts, start);
                    final LinearReferencingUtilities.ProjectedReference projectEnd = LinearReferencingUtilities.projectReference(parts, end);   
                    objCp.setSystemeRepId(null);              
                    objCp.setBorneDebutId(null);
                    objCp.setBorneFinId(null);
                    objCp.setPositionDebut(GF.createPoint(projectStart.nearests[0]));
                    objCp.setPositionFin(GF.createPoint(projectEnd.nearests[0]));
                }
                
                final LineString geom = LinearReferencingUtilities.buildGeometry(linear, objCp, bdRepo);
                objCp.setGeometry(geom);
                
            }catch(FactoryException | MismatchedDimensionException | TransformException ex){
                SirsCore.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            }
            
        }
        
        //on sauvegarde les modifications
        tdRepo.update(tronconCp);
        
        return tronconCp;
    }
    
    /**
     * On merge les propriétés du troncon2 (incluant les structures) dans le troncon1.
     * 
     * @param troncon1 a merger
     * @param troncon2 a merger
     * @return troncon1
     */
    public static TronconDigue mergeTroncon(TronconDigue troncon1, TronconDigue troncon2, SessionGen session){
        
        final SystemeReperageRepository srRepo = session.getSystemeReperageRepository();
        
        //on merge les bornes
        final Set<String> borneIds = new HashSet<>();
        borneIds.addAll(troncon1.getBorneIds());
        borneIds.addAll(troncon2.getBorneIds());
        troncon1.setBorneIds(new ArrayList<>(borneIds));
                
        //on merge les SR 
        for(SystemeReperage sr2 : srRepo.getByTroncon(troncon2)){
            
            //on cherche le SR du meme nom
            SystemeReperage sibling = null;
            for(SystemeReperage sr1 : srRepo.getByTroncon(troncon1)){
                if(sr1.getLibelle().equals(sr2.getLibelle())){
                    sibling = sr1;
                    break;
                }
            }
            
            if(sibling==null){
                //on copy le SR
                final SystemeReperage srCp = sr2.copy();
                srCp.setTronconId(troncon1.getDocumentId());
                //sauvegarde du sr
                srRepo.add(srCp);
            }else{
                //on merge les bornes
                final List<SystemeReperageBorne> srbs1 = sibling.getSystemereperageborneId();
                final List<SystemeReperageBorne> srbs2 = sr2.getSystemereperageborneId();
                    
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
                srRepo.update(sibling);
            }
        }
        
        //on ajoute les structures
        troncon1.structures.addAll(troncon2.structures);
        
        //on combine les geometries
        final LineString line1 = (LineString) troncon1.getGeometry();
        final LineString line2 = (LineString) troncon2.getGeometry();
        
        final List<Coordinate> coords = new  ArrayList<>();
        coords.addAll(Arrays.asList(line1.getCoordinates()));
        coords.addAll(Arrays.asList(line2.getCoordinates()));
        
        final LineString serie = GF.createLineString(coords.toArray(new Coordinate[0]));
        serie.setSRID(line1.getSRID());
        serie.setUserData(line1.getUserData());
        troncon1.setGeometry(serie);        
        
        return troncon1;
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
        
        if(sr.systemereperageborneId.size()>0){
            srbStart = sr.systemereperageborneId.get(0);
        }
        if(sr.systemereperageborneId.size()>1){
            srbEnd = sr.systemereperageborneId.get(sr.systemereperageborneId.size()-1);
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
            sr.systemereperageborneId.add(srbStart);
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
            sr.systemereperageborneId.add(srbEnd);
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
            final LineString structGeom = LinearReferencingUtilities.buildGeometry(
                    troncon.getGeometry(), obj, session.getBorneDigueRepository());
            obj.setGeometry(structGeom);
        }     
        session.getTronconDigueRepository().update(troncon);
    }
    
    
    public static final class PosInfo{

        private final Positionable pos;
        private final SessionGen session;
        private Map<String,BorneDigue> cacheBorne;
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
        
        public Map<String,BorneDigue> getBorneCache(){
            if(cacheBorne==null){
                cacheBorne = new HashMap<>();
                if(pos.getSystemeRepId()!=null && !pos.getSystemeRepId().isEmpty()){
                    final SystemeReperage sr = session.getSystemeReperageRepository().get(pos.getSystemeRepId());
                    if(sr!=null){
                        for(SystemeReperageBorne srb : sr.systemereperageborneId){
                            final String bid = srb.getBorneId();
                            final BorneDigue bd = session.getBorneDigueRepository().get(bid);
                            if(bd!=null){
                                cacheBorne.put(bid, bd);
                            }
                        }
                    }
                }
            }
            return cacheBorne;
        }
        
        public Point getGeoPointStart(CoordinateReferenceSystem crs) throws 
                FactoryException, MismatchedDimensionException, TransformException{
            //calcule de la position geographique
            Point point = pos.getPositionDebut();
            if(point==null){
                //calcule a partir des bornes
                final Map<String,BorneDigue> cacheBorneDigue = getBorneCache();
                final BorneDigue borne = cacheBorneDigue.get(pos.borneDebutIdProperty().get());
                final Point bornePoint = borne.getGeometry();
                double dist = pos.getBorne_debut_distance();
                if(!pos.getBorne_debut_aval()) dist *= -1;

                point = LinearReferencingUtilities.calculateCoordinate(getTronconLinear(), bornePoint, dist, 0);
            }
            
            final CoordinateReferenceSystem geomCrs = JTS.findCoordinateReferenceSystem(point);
            if(crs!=null && !CRS.equalsIgnoreMetadata(geomCrs,crs)){
                final MathTransform trs = CRS.findMathTransform(geomCrs, crs);
                point = (Point) JTS.transform(point, trs);
            }
            
            return point;
        }
        
        public Point getGeoPointEnd(CoordinateReferenceSystem crs) throws 
                FactoryException, MismatchedDimensionException, TransformException{
            //calcule de la position geographique
            Point point = pos.getPositionFin();
            if(point==null){
                //calcule a partir des bornes
                final Map<String,BorneDigue> cacheBorneDigue = getBorneCache();
                final BorneDigue borne = cacheBorneDigue.get(pos.borneFinIdProperty().get());
                final Point bornePoint = borne.getGeometry();
                double dist = pos.getBorne_fin_distance();
                if(!pos.getBorne_fin_aval()) dist *= -1;

                point = LinearReferencingUtilities.calculateCoordinate(linear, bornePoint, dist, 0);
            }
            
            final CoordinateReferenceSystem geomCrs = JTS.findCoordinateReferenceSystem(point);
            if(crs!=null && !CRS.equalsIgnoreMetadata(geomCrs,crs)){
                final MathTransform trs = CRS.findMathTransform(geomCrs, crs);
                point = (Point) JTS.transform(point, trs);
            }
            
            return point;
        }
        
        public PosSR getForSR() throws FactoryException, MismatchedDimensionException, TransformException{
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
        
        public PosSR getForSR(SystemeReperage sr) throws FactoryException, MismatchedDimensionException, TransformException{
            final Point startPoint = getGeoPointStart(null);
            final Point endPoint = getGeoPointEnd(null);
            
            final List<BorneDigue> bornes = new ArrayList<>();
            final List<Point> references = new ArrayList<>();
            for(SystemeReperageBorne srb : sr.systemereperageborneId){
                final String bid = srb.getBorneId();
                final BorneDigue bd = session.getBorneDigueRepository().get(bid);
                if(bd!=null){ 
                    bornes.add(bd);
                    references.add(bd.getGeometry());
                }
            }
            
            final PosSR possr = new PosSR();
            possr.srid = sr.getDocumentId();
            
            final Geometry linear = getTronconLinear();
            
            final Map.Entry<Integer,double[]> startRef = LinearReferencingUtilities.calculateRelative(linear, references.toArray(new Point[0]), startPoint);
            final BorneDigue startBorne = bornes.get(startRef.getKey());
            possr.borneStartId = startBorne.getDocumentId();
            possr.distanceStartBorne = startRef.getValue()[0];
            possr.startAval = possr.distanceStartBorne>=0;
            possr.distanceStartBorne = Math.abs(possr.distanceStartBorne);
            
            final Map.Entry<Integer,double[]> endRef = LinearReferencingUtilities.calculateRelative(linear, references.toArray(new Point[0]), endPoint);
            final BorneDigue endBorne = bornes.get(endRef.getKey());
            possr.borneEndId = endBorne.getDocumentId();
            possr.distanceEndBorne = endRef.getValue()[0];
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
