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
 */
package se.grenby.sos.json;

import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by peteri on 6/1/16.
 */
public class JsonDataListTest {

    private JsonDataList jdl = null;

    @Before
    public void initialize() {
        jdl = new JsonDataList();
        jdl.addByte((byte) 1).addShort((short) 2).addInt(3).addString("elem");
        jdl.addLong(4).addFloat(6.6f).addDouble(7.7);
        jdl.addList(new JsonDataList());
        jdl.addMap(new JsonDataMap());
    }

    @Test
    public void testSize() {
        assertEquals("Size of outer list is incorrect", 9, jdl.size());
    }

    @Test
    public void testValues() {
        Iterator<Object> iterator = jdl.iterator();
        assertEquals("Incorrect value", (byte) 1, iterator.next());
        assertEquals("Incorrect value", (short) 2, iterator.next());
        assertEquals("Incorrect value", 3, iterator.next());
        assertEquals("Incorrect value", "elem", iterator.next());
        assertEquals("Incorrect value", (long) 4, iterator.next());
        assertEquals("Incorrect value", 6.6f, iterator.next());
        assertEquals("Incorrect value", 7.7, iterator.next());
        assertTrue("Incorrect value", iterator.next() instanceof JsonDataList);
        assertTrue("Incorrect value", iterator.next() instanceof JsonDataMap);
    }

    @Test
    public void testIterator() {
        int i = 0;
        for (Object o : jdl) {
            i++;
        }
        assertEquals("Number of iterated items was incorrect", 9, i);
    }


}
