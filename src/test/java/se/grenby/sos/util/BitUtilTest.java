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
package se.grenby.sos.util;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created by peteri on 24/10/15.
 */
public class BitUtilTest {
    @Test
    public void testLeastNumberOfBits() {
        assertEquals(1, BitUtil.numberOfBitsNeeded(1));

        assertEquals(1, BitUtil.numberOfBitsNeeded(1));
        System.out.println(Integer.numberOfLeadingZeros(1));
        System.out.println(Integer.highestOneBit(15));

        assertEquals(2, BitUtil.numberOfBitsNeeded(2));
        assertEquals(2, BitUtil.numberOfBitsNeeded(3));

        assertEquals(4, BitUtil.numberOfBitsNeeded(15));

        assertEquals(7, BitUtil.numberOfBitsNeeded(120));

        assertEquals(9, BitUtil.numberOfBitsNeeded(500));

        assertEquals(16, BitUtil.numberOfBitsNeeded(((int) Math.pow(2, 16)) - 1));
    }
}
