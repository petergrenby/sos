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
package se.grenby.sos.readpointer;

import se.grenby.sos.bbb.ByteBlockBufferReader;

/**
 * Created by peteri on 5/31/16.
 */
public class ByteBlockBufferWrapperReadPointer implements BufferReadPointer {

    private final ByteBlockBufferReader blockReader;
    private final int blockPointer;

    public ByteBlockBufferWrapperReadPointer(ByteBlockBufferReader blockReader, int blockPointer) {
        this.blockReader = blockReader;
        this.blockPointer = blockPointer;
    }

    @Override
    public byte[] getBytes(int position, int length) {
        return blockReader.getBytes(blockPointer, position, length);
    }

    @Override
    public byte getByte(int position) {
        return blockReader.getByte(blockPointer, position);
    }

    @Override
    public short getShort(int position) {
        return blockReader.getShort(blockPointer, position);
    }

    @Override
    public int getInt(int position) {
        return blockReader.getInt(blockPointer, position);
    }

    @Override
    public long getLong(int position) {
        return blockReader.getLong(blockPointer, position);
    }

    @Override
    public float getFloat(int position) {
        return blockReader.getFloat(blockPointer, position);
    }

    @Override
    public double getDouble(int position) {
        return blockReader.getDouble(blockPointer, position);
    }

    @Override
    public int getAllocatedSize() {
        return blockReader.allocatedSize(blockPointer);
    }
}
