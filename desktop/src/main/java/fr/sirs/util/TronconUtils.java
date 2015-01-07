
package fr.sirs.util;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.LinearReferencingUtilities;
import fr.sirs.core.component.SystemeReperageRepository;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.SystemeReperageBorne;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.theme.ui.FXPositionablePane;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.geotoolkit.display2d.GO2Utilities;
import org.opengis.geometry.MismatchedDimensionException;
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
    public static TronconDigue cutTroncon(TronconDigue troncon, LineString segment){
        
        final Session session = Injector.getSession();
        
        final TronconDigue tronconCp = troncon.copy();
        tronconCp.setGeometry(segment);
        
        
        final LinearReferencingUtilities.SegmentInfo[] parts = LinearReferencingUtilities.buildSegments((LineString) tronconCp.geometryProperty().get());
        
        //on recupere les bornes qui sont sur le troncon
        tronconCp.getBorneIds().clear();
        for(String borneId : troncon.getBorneIds()){
            final BorneDigue borne = session.getBorneDigueRepository().get(borneId);
            final LinearReferencingUtilities.ProjectedReference proj = LinearReferencingUtilities.projectReference(parts, borne.getGeometry());
            if(proj.perpendicularProjection){
                tronconCp.getBorneIds().add(borneId);
            }
        }
        
        //on sauvegarde maintenant car on va avoir besoin de la reference du nouveau troncon
        session.getTronconDigueRepository().add(tronconCp);
        
        //on copie et on enleve les bornes des SR et les SR vide
        final Map<String,SystemeReperage> mapSrs = new HashMap<>();
        for(SystemeReperage sr : session.getSystemeReperageRepository().getByTroncon(troncon)){
            final SystemeReperage srCp = sr.copy();
            srCp.systemereperageborneId.clear();
            srCp.setTronconId(tronconCp.getDocumentId());
            final List<SystemeReperageBorne> lst = sr.getSystemereperageborneId();
            for(int i=lst.size()-1;i>=0;i--){
                final SystemeReperageBorne srb = lst.get(i);
                if(tronconCp.getBorneIds().contains(srb.getBorneId())){
                    srCp.systemereperageborneId.add(srb);
                }
            }
            if(!srCp.systemereperageborneId.isEmpty()){
                session.getSystemeReperageRepository().add(srCp);
                mapSrs.put(sr.getDocumentId(), srCp);
            }
        }
        
        //on coupe les differents objets
        tronconCp.structures.clear();
                
        for(Objet obj : troncon.structures){
            
            //on vérifie que cet objet intersect le segment
            if(!tronconCp.getGeometry().intersects(obj.getGeometry())){
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
                    final FXPositionablePane.PosInfo infoOrig = new FXPositionablePane.PosInfo(obj,troncon);                    
                    objCp.setBorneDebutId(null);
                    objCp.setBorneFinId(null);
                    objCp.setPositionDebut(infoOrig.getGeoPointStart(null));
                    objCp.setPositionFin(infoOrig.getGeoPointEnd(null));
                    
                    //on recalcule les positions relatives
                    final FXPositionablePane.PosInfo info = new FXPositionablePane.PosInfo(objCp,tronconCp);
                    final FXPositionablePane.PosSR posSr = info.getForSR(sr);
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
                    final FXPositionablePane.PosInfo info = new FXPositionablePane.PosInfo(obj,troncon);
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
                
            }catch(FactoryException | MismatchedDimensionException | TransformException ex){
                SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            }
            
        }
        
        //on sauvegarde les modifications
        session.getTronconDigueRepository().update(tronconCp);
        
        return tronconCp;
    }
    
    /**
     * On merge les propriétés du troncon2 (incluant les structures) dans le troncon1.
     * 
     * @param troncon1 a merger
     * @param troncon2 a merger
     * @return troncon1
     */
    public static TronconDigue mergeTroncon(TronconDigue troncon1, TronconDigue troncon2){
        
        final Session session = Injector.getSession();
        
        //on merge les bornes
        final Set<String> borneIds = new HashSet<>();
        borneIds.addAll(troncon1.getBorneIds());
        borneIds.addAll(troncon2.getBorneIds());
        troncon1.setBorneIds(new ArrayList<>(borneIds));
        
        final SystemeReperageRepository srrepo = session.getSystemeReperageRepository();
        
        //on merge les SR 
        for(SystemeReperage sr2 : srrepo.getByTroncon(troncon2)){
            
            //on cherche le SR du meme nom
            SystemeReperage sibling = null;
            for(SystemeReperage sr1 : srrepo.getByTroncon(troncon1)){
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
                srrepo.add(srCp);
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
                srrepo.update(sibling);
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
    public static void updateSRElementaire(TronconDigue troncon){
        final Session session = Injector.getBean(Session.class);
                
        final List<SystemeReperage> srs = session.getSystemeReperageRepository().getByTroncon(troncon);
        
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
            session.getSystemeReperageRepository().add(sr);
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
            session.getBorneDigueRepository().add(bdStart);
            
            srbStart = new SystemeReperageBorne();
            srbStart.setBorneId(bdStart.getDocumentId());
            sr.systemereperageborneId.add(srbStart);
        }else{
            bdStart = session.getBorneDigueRepository().get(srbStart.getBorneId());
        }
        
        if(srbEnd==null){
            //creation de la borne de début
            bdEnd = new BorneDigue();
            bdEnd.setLibelle("Fin du tronçon");
            bdEnd.setGeometry(GO2Utilities.JTS_FACTORY.createPoint(new Coordinate()));
            session.getBorneDigueRepository().add(bdEnd);
            
            srbEnd = new SystemeReperageBorne();
            srbEnd.setBorneId(bdEnd.getDocumentId());
            sr.systemereperageborneId.add(srbEnd);
        }else{
            bdEnd = session.getBorneDigueRepository().get(srbEnd.getBorneId());
        }
        

        //calcul des nouvelles valeurs pour les bornes
        final double length = troncon.getGeometry().getLength();
        final Coordinate[] coords = troncon.getGeometry().getCoordinates();
        
        bdStart.setGeometry(GO2Utilities.JTS_FACTORY.createPoint(coords[0]));
        bdEnd.setGeometry(GO2Utilities.JTS_FACTORY.createPoint(coords[coords.length-1]));

        session.getBorneDigueRepository().update(bdStart);
        session.getBorneDigueRepository().update(bdEnd);
        
        srbStart.setValeurPR(0);
        srbEnd.setValeurPR((float)length);
        
        session.getSystemeReperageRepository().update(sr);
    }
    
    /**
     * Recalcule des geometries des differents positionnables apres que
     * la géometrie ou que les SR du troncon aient changés.
     * 
     * @param troncon 
     */
    public static void updatePositionableGeometry(TronconDigue troncon){
        for(Objet obj : troncon.getStructures()){
            final LineString structGeom = LinearReferencingUtilities.buildGeometry(
                    troncon.getGeometry(), obj, Injector.getSession().getBorneDigueRepository());
            obj.setGeometry(structGeom);
        }     
        Injector.getSession().getTronconDigueRepository().update(troncon);
    }
    
}
