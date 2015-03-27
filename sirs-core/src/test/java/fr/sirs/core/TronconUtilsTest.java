
package fr.sirs.core;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import fr.sirs.core.component.SessionGen;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Crete;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.SystemeReperageBorne;
import fr.sirs.core.model.TronconDigue;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class TronconUtilsTest extends CouchDBTestCase{
    
    private static final float DELTA = 0.00001f;
    private static final GeometryFactory GF = new GeometryFactory();
    
    /**
     * Test du decoupage d'un troncon.
     */
    @Test
    public void cutTest(){
        
        final SessionGen session = new SessionGen(connector);
        
        //creation du troncon
        TronconDigue troncon = new TronconDigue();
        troncon.setLibelle("TC");
        troncon.setGeometry(GF.createLineString(new Coordinate[]{new Coordinate(0, 0),new Coordinate(100, 0)}));
        session.getTronconDigueRepository().add(troncon);
        
        //create des bornes
        final BorneDigue borne0 = new BorneDigue();
        borne0.setLibelle("B0");
        borne0.setGeometry(GF.createPoint(new Coordinate(1, 1)));
        final BorneDigue borne1 = new BorneDigue();
        borne1.setLibelle("B1");
        borne1.setGeometry(GF.createPoint(new Coordinate(51, 2)));
        final BorneDigue borne2 = new BorneDigue();
        borne2.setLibelle("B2");
        borne2.setGeometry(GF.createPoint(new Coordinate(99, -3)));
        session.getBorneDigueRepository().add(borne0);
        session.getBorneDigueRepository().add(borne1);
        session.getBorneDigueRepository().add(borne2);
        
        //creation du systeme de reperage
        final SystemeReperage sr = new SystemeReperage();
        sr.setLibelle("SR");
        sr.setTronconId(troncon.getDocumentId());
        final SystemeReperageBorne srb0 = new SystemeReperageBorne();
        srb0.setBorneId(borne0.getDocumentId());
        srb0.setValeurPR(0);
        final SystemeReperageBorne srb1 = new SystemeReperageBorne();
        srb1.setBorneId(borne1.getDocumentId());
        srb1.setValeurPR(10);
        final SystemeReperageBorne srb2 = new SystemeReperageBorne();
        srb2.setBorneId(borne2.getDocumentId());
        srb2.setValeurPR(20);
        sr.getSystemeReperageBorne().add(srb0);
        sr.getSystemeReperageBorne().add(srb1);
        sr.getSystemeReperageBorne().add(srb2);
        session.getSystemeReperageRepository().add(sr, troncon);
        
        //le troncon doit etre a jour avec la liste des bornes
        troncon = session.getTronconDigueRepository().get(troncon.getDocumentId());
        final String[] tcbids = troncon.getBorneIds().toArray(new String[0]);
        assertEquals(3, tcbids.length);
        assertEquals(borne0.getId(), tcbids[0]);
        assertEquals(borne1.getId(), tcbids[1]);
        assertEquals(borne2.getId(), tcbids[2]);
        
        //on ajoute une crète
        final Crete crete = new Crete();
        crete.setSystemeRepId(sr.getDocumentId());
        crete.setBorneDebutId(borne0.getDocumentId());
        crete.setBorne_debut_aval(false);
        crete.setBorne_debut_distance(0.5f);
        crete.setBorne_fin_distance(0.3f);
        crete.setBorne_fin_aval(true);
        crete.setBorneFinId(borne2.getDocumentId());
        troncon.getStructures().add(crete);
        session.getTronconDigueRepository().update(troncon);
        
        //on vérifie que reconstruit bien la geometrie
        assertTrue(GF.createLineString(new Coordinate[]{new Coordinate(0.5, 0),new Coordinate(99.3, 0)}).equalsExact( 
                LinearReferencingUtilities.buildGeometry(troncon.getGeometry(), crete, session.getBorneDigueRepository())
                ,DELTA));
        
        //premiere decoupe -----------------------------------------------------
        final TronconDigue cut0 = TronconUtils.cutTroncon(troncon, 
                GF.createLineString(new Coordinate[]{new Coordinate(0, 0),new Coordinate(50, 0)}),
                "TC[0]", session);
        assertEquals("TC[0]", cut0.getLibelle());
        assertEquals(GF.createLineString(new Coordinate[]{new Coordinate(0, 0),new Coordinate(50, 0)}), cut0.getGeometry());
        
        //on verifie le systeme de reperage
        final String[] cut0brs = cut0.getBorneIds().toArray(new String[0]);
        assertEquals(1, cut0brs.length);
        assertEquals(borne0.getDocumentId(), cut0brs[0]);
        
        final List<SystemeReperage> cut0Srs = session.getSystemeReperageRepository().getByTroncon(cut0);
        assertEquals(1, cut0Srs.size());
        final SystemeReperage cut0sr = cut0Srs.get(0);
        assertNotEquals(sr.getDocumentId(), cut0sr.getDocumentId());
        assertEquals("SR", cut0sr.getLibelle());
        final List<SystemeReperageBorne> cut0srbs = cut0sr.getSystemeReperageBorne();
        assertEquals(1, cut0srbs.size());
        final SystemeReperageBorne cut0srb = cut0srbs.get(0);
        assertEquals(0.0f, cut0srb.getValeurPR(), DELTA);
        final BorneDigue cut0b0 = session.getBorneDigueRepository().get(cut0srb.getBorneId());
        assertEquals(borne0.getDocumentId(), cut0b0.getDocumentId());
        assertEquals("B0", cut0b0.getLibelle());
        assertTrue(GF.createPoint(new Coordinate(1, 1)).equals(cut0b0.getGeometry()));
        
        //on verifie que la crete a été coupée
        final List<Objet> cut0Strs = cut0.getStructures();
        assertEquals(1, cut0Strs.size());
        final Crete cut0Crete = (Crete)cut0Strs.get(0);
        assertEquals(cut0sr.getDocumentId(), cut0Crete.getSystemeRepId());
        assertEquals(cut0b0.getDocumentId(), cut0Crete.getBorneDebutId());
        assertEquals(cut0b0.getDocumentId(), cut0Crete.getBorneFinId());
        assertEquals(0.5f, cut0Crete.getBorne_debut_distance(), DELTA);
        assertEquals(49.0f, cut0Crete.getBorne_fin_distance(), DELTA); // troncon coupé
        assertEquals(false, cut0Crete.getBorne_debut_aval());
        assertEquals(true, cut0Crete.getBorne_fin_aval());
        
        //la geometrie doit etre a jour aussi
        assertTrue(GF.createLineString(new Coordinate[]{new Coordinate(0.5, 0),new Coordinate(50, 0)}).equalsExact( 
                cut0Crete.getGeometry(),DELTA));
        
        
        //le troncon d'origine ne doit pas avoir changé
        assertTrue(GF.createLineString(new Coordinate[]{new Coordinate(0.5, 0),new Coordinate(99.3, 0)}).equalsExact( 
                LinearReferencingUtilities.buildGeometry(troncon.getGeometry(), crete, session.getBorneDigueRepository())
                ,DELTA));
        
        //seconde decoupe -----------------------------------------------------
        final TronconDigue cut1 = TronconUtils.cutTroncon(troncon, 
                GF.createLineString(new Coordinate[]{new Coordinate(50, 0),new Coordinate(100, 0)}),
                "TC[1]", session);
        assertEquals("TC[1]", cut1.getLibelle());
        assertEquals(GF.createLineString(new Coordinate[]{new Coordinate(50, 0),new Coordinate(100, 0)}), cut1.getGeometry());
        
        //on verifie le systeme de reperage
        final List<String> cut1brs = cut1.getBorneIds();
        assertEquals(2, cut1brs.size());
        assertTrue("Cut troncon shares its bornes with its parent.", cut1brs.contains(borne1.getDocumentId()));
        assertTrue("Cut troncon shares its bornes with its parent.", cut1brs.contains(borne2.getDocumentId()));
        
        final List<SystemeReperage> cut1Srs = session.getSystemeReperageRepository().getByTroncon(cut1);
        assertEquals(1, cut1Srs.size());
        final SystemeReperage cut1sr = cut1Srs.get(0);
        assertNotEquals(sr.getDocumentId(), cut1sr.getDocumentId());
        assertEquals("SR", cut1sr.getLibelle());
        final List<SystemeReperageBorne> cut1srbs = cut1sr.getSystemeReperageBorne();
        cut1srbs.sort((SystemeReperageBorne first, SystemeReperageBorne second) -> {
            final float diff = first.getValeurPR() - second.getValeurPR();
            if (diff > 0) return -1;
            else if (diff < 0) return 1;
            else return 0;            
        });
        assertEquals(2, cut1srbs.size());
        final SystemeReperageBorne cut1srb0 = cut1srbs.get(1);
        final SystemeReperageBorne cut1srb1 = cut1srbs.get(0);
        assertEquals(10.0f, cut1srb0.getValeurPR(), DELTA);
        assertEquals(20.0f, cut1srb1.getValeurPR(), DELTA);
        final BorneDigue cut1b0 = session.getBorneDigueRepository().get(cut1srb0.getBorneId());
        final BorneDigue cut1b1 = session.getBorneDigueRepository().get(cut1srb1.getBorneId());
        assertEquals("Cut troncon shares its bornes with its parent.", borne1.getDocumentId(), cut1b0.getDocumentId());
        assertEquals("Cut troncon shares its bornes with its parent.", borne2.getDocumentId(), cut1b1.getDocumentId());
        assertEquals("B1", cut1b0.getLibelle());
        assertEquals("B2", cut1b1.getLibelle());
        assertTrue(GF.createPoint(new Coordinate(51, 2)).equals(cut1b0.getGeometry()));
        assertTrue(GF.createPoint(new Coordinate(99,-3)).equals(cut1b1.getGeometry()));
        
        //on verifie que la crete a été coupée
        final List<Objet> cut1Strs = cut1.getStructures();
        assertEquals(1, cut1Strs.size());
        final Crete cut1Crete = (Crete)cut1Strs.get(0);
        assertEquals(cut1sr.getDocumentId(), cut1Crete.getSystemeRepId());
        assertEquals(cut1b0.getDocumentId(), cut1Crete.getBorneDebutId());
        assertEquals(cut1b1.getDocumentId(), cut1Crete.getBorneFinId());
        assertEquals(1.0f, cut1Crete.getBorne_debut_distance(), DELTA); // troncon coupé
        assertEquals(0.3f, cut1Crete.getBorne_fin_distance(), DELTA); 
        assertEquals(false, cut1Crete.getBorne_debut_aval());
        assertEquals(true, cut1Crete.getBorne_fin_aval());
        
        //la geometrie doit etre a jour aussi
        assertTrue(GF.createLineString(new Coordinate[]{new Coordinate(50.0, 0),new Coordinate(99.3, 0)}).equalsExact( 
                cut1Crete.getGeometry(),DELTA));
        
        //le troncon d'origine ne doit pas avoir changé
        assertTrue(GF.createLineString(new Coordinate[]{new Coordinate(0.5, 0),new Coordinate(99.3, 0)}).equalsExact( 
                LinearReferencingUtilities.buildGeometry(troncon.getGeometry(), crete, session.getBorneDigueRepository())
                ,DELTA));
    }
    
}
