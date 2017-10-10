/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Peter Grenby
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package se.grenby.sos.json;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by peteri on 6/1/16.
 */
public class JsonDataMapTest {

    private JsonDataMap jdm = null;

    @Before
    public void initialize() {
        jdm = new JsonDataMap();
        jdm.putByte("by", (byte) 64).putShort("sh", (short) 312).putInt("in", 45).putLong("lo", 76);
        jdm.putString("st", "ing").putFloat("fl", 36.4f).putDouble("do", 789.45436);

        JsonDataList jdl = new JsonDataList();
        jdl.addString("elem");
        jdm.putList("list", jdl);

        JsonDataMap jdm2 = new JsonDataMap();
        jdm2.putString("key", "value");
        jdm.putMap("map", jdm2);
    }

    @Test
    public void testSize() {
        assertEquals("Size of outer map is incorrect", 9, jdm.size());
    }

    @Test
    public void testValues() {
        assertEquals("Incorrect value for key", (byte) 64, jdm.getByte("by"));
        assertEquals("Incorrect value for key", (short) 312, jdm.getShort("sh"));
        assertEquals("Incorrect value for key", 45, jdm.getInteger("in"));
        assertEquals("Incorrect value for key", 76, jdm.getLong("lo"));
        assertEquals("Incorrect value for key", "ing", jdm.getString("st"));
        assertEquals("Incorrect value for key", 36.4f, jdm.getFloat("fl"), 0);
        assertEquals("Incorrect value for key", 789.45436, jdm.getDouble("do"), 0);
    }

    @Test
    public void testListInMap() {
        JsonDataList list = jdm.getList("list");
        assertNotNull("No list for the key", list);
        assertEquals("Incorrect value in list", "elem", (String) list.iterator().next());

    }

    @Test
    public void testMapInMap() {
        JsonDataMap map = jdm.getMap("map");
        assertNotNull("No map for the key", map);
        assertEquals("Incorrect value for key", "value", map.getString("key"));
    }

    @Test
    public void testIterator() {
        int i = 0;
        for (Object o : jdm) {
            i++;
        }
        assertEquals("Number of iterated items was incorrect", 9, i);
    }


}
