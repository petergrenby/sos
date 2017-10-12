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
package se.grenby.sos.byteblock.sharedbuffer;

import org.junit.Test;
import se.grenby.sos.constant.PrimitiveConstants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by peteri on 24/11/15.
 */
public class SharedBufferByteBlocksManagerTest {

    @Test
    public void testAlloction() {
        SharedByteBlockManager bbbm = new SharedByteBlockManager(1024);

        int orginalSpace = bbbm.getTotalAvailableSpace();

        final int allocationSize = 100;
        int p1 = bbbm.allocate(allocationSize);
        assertNotEquals("No block allocation pointer was returned", PrimitiveConstants.INT_VALUE_FOR_NULL, p1);
        assertTrue("Incorrect size of allocated block", allocationSize <= bbbm.allocatedSize(p1));
        assertEquals("Available space to manager is not correct after allocation", orginalSpace - allocationSize - SharedByteBlockManager.MANAGED_BLOCK_OVERHEAD_IN_BYTES, bbbm.getTotalAvailableSpace());
        assertTrue("Integrity of memory management and/or byte block blockbuffer has been compromised", bbbm.verfiyIntegrity());
    }

    @Test
    public void testAlloctionAndDeallocation() {
        SharedByteBlockManager bbbm = new SharedByteBlockManager(1024);

        int orginalSpace = bbbm.getTotalAvailableSpace();

        final int allocationSize = 100;
        int p1 = bbbm.allocate(allocationSize);
        assertNotEquals("No block allocation pointer was returned", PrimitiveConstants.INT_VALUE_FOR_NULL, p1);
        assertTrue("Incorrect size of allocated block", allocationSize <= bbbm.allocatedSize(p1));
        assertEquals("Available space to manager is not correct after allocation", orginalSpace - allocationSize - SharedByteBlockManager.MANAGED_BLOCK_OVERHEAD_IN_BYTES, bbbm.getTotalAvailableSpace());
        assertTrue("Integrity of memory management and/or byte block blockbuffer has been compromised", bbbm.verfiyIntegrity());


        bbbm.deallocate(p1);
        assertEquals("Available space to manager is not correct after allocation", orginalSpace, bbbm.getTotalAvailableSpace());
        assertTrue("Integrity of memory management and/or byte block blockbuffer has been compromised", bbbm.verfiyIntegrity());
    }

    @Test
    public void testBigAlloction() {
        SharedByteBlockManager bbbm = new SharedByteBlockManager(1100);

        int p1 = bbbm.allocate(1000);
        assertNotEquals("No block allocation pointer was returned", PrimitiveConstants.INT_VALUE_FOR_NULL, p1);
        assertTrue("Integrity of memory management and/or byte block blockbuffer has been compromised", bbbm.verfiyIntegrity());
    }

    @Test
    public void testTooBigAlloction() {
        SharedByteBlockManager bbbm = new SharedByteBlockManager(1024);

        int p1 = bbbm.allocate(1024);
        assertEquals("Allocation pointer was returned even though allocation should not have been possible", PrimitiveConstants.INT_VALUE_FOR_NULL, p1);
        assertTrue("Integrity of memory management and/or byte block blockbuffer has been compromised", bbbm.verfiyIntegrity());
    }

    @Test
    public void testMassiveAllocationAndDeallocation() {
        List<Integer> pointers = new ArrayList<>(2000);
        SharedByteBlockManager bbbm = new SharedByteBlockManager(1024*1024*1024);

        assertTrue("Integrity of memory management and/or byte block blockbuffer has been compromised", bbbm.verfiyIntegrity());

        final int allocationSize = 1024;
        for (int i=0; i<2000; i++) {
            int p = bbbm.allocate(allocationSize);
            assertNotEquals("No block allocation pointer was returned", PrimitiveConstants.INT_VALUE_FOR_NULL, p);
            int actualSize = bbbm.allocatedSize(p);
            assertTrue("Incorrect size of allocated block", allocationSize <= actualSize);
            assertTrue("Integrity of memory management and/or byte block blockbuffer has been compromised", bbbm.verfiyIntegrity());
            pointers.add(p);
        }

        int c = 0;
        for (Iterator<Integer> it = pointers.iterator(); it.hasNext() & (c < 1000); c++) {
            int p = it.next();
            bbbm.deallocate(p);
            assertTrue("Integrity of memory management and/or byte block blockbuffer has been compromised", bbbm.verfiyIntegrity());
            it.remove();
        }


        for (int i=0; i<1000; i++) {
            int p = bbbm.allocate(1024);
            assertNotEquals("No block allocation pointer was returned", PrimitiveConstants.INT_VALUE_FOR_NULL, p);
            int actualSize = bbbm.allocatedSize(p);
            assertTrue("Incorrect size of allocated block", allocationSize <= actualSize);
            assertTrue("Integrity of memory management and/or byte block blockbuffer has been compromised", bbbm.verfiyIntegrity());
            pointers.add(p);
        }

        for (int p : pointers) {
            bbbm.deallocate(p);
            assertTrue("Integrity of memory management and/or byte block blockbuffer has been compromised", bbbm.verfiyIntegrity());
        }
    }

}
