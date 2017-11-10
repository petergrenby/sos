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
package se.grenby.sos.byteblock.uniquebuffer;

import se.grenby.sos.byteblock.ByteBlockReadPointer;

import java.nio.ByteBuffer;

/**
 * Created by peteri on 5/31/16.
 */
public class UniqueBufferByteBlockReadPointer implements ByteBlockReadPointer {

    private final ByteBuffer byteBuffer;

    public UniqueBufferByteBlockReadPointer(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    @Override
    public byte[] getBytes(int position, int length) {
        byte[] bs = new byte[length];
        byteBuffer.position(position);
        byteBuffer.get(bs, 0, length);
        return bs;
    }

    @Override
    public byte getByte(int position) {
        return byteBuffer.get(position);
    }

    @Override
    public short getShort(int position) {
        return byteBuffer.getShort(position);
    }

    @Override
    public int getInt(int position) {
        return byteBuffer.getInt(position);
    }

    @Override
    public long getLong(int position) {
        return byteBuffer.getLong(position);
    }

    @Override
    public float getFloat(int position) {
        return byteBuffer.getFloat(position);
    }

    @Override
    public double getDouble(int position) {
        return byteBuffer.getDouble(position);
    }

    @Override
    public int getAllocatedSize() {
        return byteBuffer.limit();
    }
}
