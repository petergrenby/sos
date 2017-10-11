package se.grenby.sos;

import org.junit.Before;
import org.junit.Test;
import se.grenby.sos.bbb.SimpleByteBlockBufferManager;
import se.grenby.sos.json.JsonDataList;
import se.grenby.sos.json.JsonDataMap;
import se.grenby.sos.object.SosList;
import se.grenby.sos.object.SosMap;

import java.util.Iterator;

import static org.junit.Assert.*;

public class SosTest {

    private SosManager sosManager;


    @Before
    public void initializer() {
        sosManager = new SosManager(new SimpleByteBlockBufferManager());
    }

    @Test
    public void testMap() {
        JsonDataMap jdm = new JsonDataMap();
        jdm.putByte("by", (byte) 64);
        jdm.putShort("sh", (short) 312);
        jdm.putInt("in", 45);
        jdm.putLong("lo", 76);
        jdm.putString("st", "ing");
        jdm.putFloat("fl", 36.4f);
        jdm.putDouble("do", 789.45436);

        SosMap sos = sosManager.createSosMap(jdm);

        assertEquals("Incorrect byte in map", (byte) 64, sos.getByteValue("by"));
        assertEquals("Incorrect short in map", (short) 312, sos.getShortValue("sh"));
        assertEquals("Incorrect integer in map", 45, sos.getIntValue("in"));
        assertEquals("Incorrect long in map", 76, sos.getLongValue("lo"));
        assertEquals("Incorrect string in map", "ing", sos.getStringValue("st"));
        assertEquals("Incorrect float in map", 36.4f, sos.getFloatValue("fl"), 0.0f);
        assertEquals("Incorrect double in map", 789.45436, sos.getDoubleValue("do"),  0.0);

        sosManager.removeSosObject(sos);
    }

    @Test
    public void testList() {
        JsonDataList jdl = new JsonDataList();
        jdl.addByte((byte) 1).addShort((short) 2).addInt(3).addString("elem");
        jdl.addLong(4).addFloat(6.6f).addDouble(7.7);

        SosList sos = sosManager.createSosList(jdl);

        Iterator<Object> it = sos.iterator();
        assertEquals("Incorrect byte in list", new Byte((byte) 1), it.next());
        assertEquals("Incorrect short in list", new Short((short) 2), it.next());
        assertEquals("Incorrect integer in list", new Integer(3), it.next());
        assertEquals("Incorrect string in list", "elem", it.next());
        assertEquals("Incorrect long in list", new Long(4), it.next());
        assertEquals("Incorrect float in list", new Float(6.6f), it.next());
        assertEquals("Incorrect double in list", new Double(7.7), it.next());

        sosManager.removeSosObject(sos);
    }


    @Test
    public void testListAndMapInMap() {
        JsonDataMap jdm = new JsonDataMap();

        JsonDataList jdl = new JsonDataList();
        jdl.addString("here it is in a list");
        jdm.putList("list", jdl);

        JsonDataMap jdm2 = new JsonDataMap();
        jdm2.putString("string", "here it is in a map");
        jdm.putMap("map", jdm2);

        SosMap sos = sosManager.createSosMap(jdm);

        SosList sosl1 = sos.getListValue("list");
        assertNotNull("List should not be null", sosl1);
        Object o = sosl1.iterator().next();
        assertEquals("", "here it is in a list", o);
        SosMap sosm1 = sos.getMapValue("map");
        assertNotNull("Map should not be null", sosl1);
        assertEquals("Map should not be null", "here it is in a map", sosm1.getStringValue("string"));

        sosManager.removeSosObject(sos);
    }

}
