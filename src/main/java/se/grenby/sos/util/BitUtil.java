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

/**
 * Created by peteri on 24/10/15.
 */
public class BitUtil {

    public static final char[] HEXES = "0123456789ABCDEF".toCharArray();

    private final static int BITS_IN_SIGNED_INTEGER = 31;

    /**
     * Least number of needed bits to describe number
     * @param number
     * @return
     */
    public static int numberOfBitsNeeded(int number) {
        int ceiling = 1;
        int i = 1;
        while (i<BITS_IN_SIGNED_INTEGER) {
            if (number <= ceiling) {
                break;
            }
            ceiling = (ceiling << 1) + 1;
            i++;
        }
        return i;
    }
}
