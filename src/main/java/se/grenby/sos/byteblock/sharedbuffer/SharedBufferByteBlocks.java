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

import java.nio.ByteBuffer;

import static se.grenby.sos.constant.PrimitiveConstants.INT_VALUE_FOR_NULL;

/**
 * Created by peteri on 01/11/15.
 */
public class SharedBufferByteBlocks {
    private final static int MAX_ALLOWED_CAPACITY = 1073741824; // 2^30
    private final static int BLOCK_SIZE_IN_BYTES = Integer.BYTES;
    public final static int BLOCK_OVERHEAD_IN_BYTES = BLOCK_SIZE_IN_BYTES*2;

    private final int bufferCapacity;
    private final ByteBuffer buffer;
    private final int firstBlockPointer;

    private int numberOfBlocks;

    public SharedBufferByteBlocks(final int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Requested capacity can not be a negativ value");
        }

        if (capacity > MAX_ALLOWED_CAPACITY) {
            throw new IllegalArgumentException("Requested capacity can not be greater than 2 to the power of 30");
        }

        bufferCapacity = capacity;
        buffer = ByteBuffer.allocateDirect(capacity);
        firstBlockPointer = BLOCK_SIZE_IN_BYTES;
        putSizeInBlock(firstBlockPointer, capacity - BLOCK_SIZE_IN_BYTES*2);
        numberOfBlocks = 1;
    }

    public int getCapacity() {
        return bufferCapacity;
    }

    public int getFirstBlock() {
        return firstBlockPointer;
    }

    public int getBlockSize(int pointer) {
        return buffer.getInt(pointer - BLOCK_SIZE_IN_BYTES);
    }

    public int previousBlock(int pointer) {
        if (pointer == BLOCK_SIZE_IN_BYTES) {
            return INT_VALUE_FOR_NULL;
        }

        int previousSize = buffer.getInt(pointer - BLOCK_SIZE_IN_BYTES*2);
        int previousPointer = pointer - previousSize - BLOCK_SIZE_IN_BYTES*2;

        if (previousPointer >= BLOCK_SIZE_IN_BYTES) {
            return previousPointer;
        } else {
            throw new IllegalStateException("Integrity of block is compromised");
        }
    }

    public int nextBlock(int pointer) {
        if (!isCorrectBlock(pointer)) {
            throw new IllegalStateException("Integrity of block is compromised. Pointer = " + pointer + " size " + getBlockSize(pointer));
        }

        int size = getBlockSize(pointer);
        int nextPointer = pointer + size + BLOCK_SIZE_IN_BYTES*2;

        if (nextPointer < bufferCapacity) {
            return nextPointer;
        } else if (nextPointer == bufferCapacity + BLOCK_SIZE_IN_BYTES) {
            return INT_VALUE_FOR_NULL;
        } else {
            throw new IllegalStateException("Integrity of block is compromised");
        }
    }

    private void putSizeInBlock(int pointer, int size) {
        buffer.putInt(pointer - BLOCK_SIZE_IN_BYTES, size);
        buffer.putInt(pointer + size, size);
    }

    public int splitBlock(int pointer, int requestedSize) {
        int originalSize = getBlockSize(pointer);
        if (originalSize != buffer.getInt(pointer + originalSize)) {
            throw new IllegalStateException("Either pointer or block is corrupt");
        }

        if (requestedSize + BLOCK_SIZE_IN_BYTES*2 >= originalSize) {
            return INT_VALUE_FOR_NULL;
        }

        putSizeInBlock(pointer, requestedSize);

        int voidSize = originalSize - requestedSize - BLOCK_SIZE_IN_BYTES*2;
        int voidPointer = pointer + requestedSize + BLOCK_SIZE_IN_BYTES*2;

        putSizeInBlock(voidPointer, voidSize);

        numberOfBlocks++;

        return voidPointer;
    }

    public int mergeBlocks(int pointer1, int pointer2) {
        if (!isAscendingPointers(pointer1, pointer2)) {
            throw new IllegalArgumentException("Pointers should be in ascending order");
        }

        int size1 = buffer.getInt(pointer1 - BLOCK_SIZE_IN_BYTES);
        int size2 = buffer.getInt(pointer2 - BLOCK_SIZE_IN_BYTES);

        int size = size1 + size2 + BLOCK_SIZE_IN_BYTES*2;

        putSizeInBlock(pointer1, size);

        numberOfBlocks--;

        return pointer1;
    }

    private boolean isAscendingPointers(int pointer1, int pointer2) {
        if (pointer1 < pointer2) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isAdjacentBlocks(int pointer1, int pointer2) {
        if (!isAscendingPointers(pointer1, pointer2)) {
            throw new IllegalArgumentException("Pointers should be in ascending order");
        }

        int size1 = buffer.getInt(pointer1 - BLOCK_SIZE_IN_BYTES);
        if (pointer2 == (pointer1 + size1 + BLOCK_SIZE_IN_BYTES)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isCorrectBlock(int pointer) {
        int sizeStart = getBlockSize(pointer);
        int sizeEnd = buffer.getInt(pointer + sizeStart);
        if (pointer + sizeStart < bufferCapacity &&
                sizeStart == sizeEnd) {
            return true;
        } else {
            return false;
        }
    }

    public boolean verfiyIntegrity() {
        boolean correct = true;

        int num = 0;
        int p = firstBlockPointer;
        while (correct && p != INT_VALUE_FOR_NULL) {
            correct = isCorrectBlock(p);
            if (correct) {
                p = nextBlock(p);
                num++;
            }
        }

        if (numberOfBlocks != num) {
            correct = false;
        }

        return correct;
    }

    public int getNumberOfBlocks () {
        return numberOfBlocks;
    }

    public String blockStructureToString() {
        int p = firstBlockPointer;
        int i = 0;
        StringBuilder sb = new StringBuilder();
        while (p != INT_VALUE_FOR_NULL) {
            int s = buffer.getInt(p - BLOCK_SIZE_IN_BYTES);
            sb.append("block[").append(i++).append("] : p=").append(p).append(" s=").append(s).append("\n");
            p = nextBlock(p);
        }
        return sb.toString();
    }


    public void putByte(int blockPointer, int position, byte value) {
        buffer.put(blockPointer + position, value);
    }

    public byte getByte(int blockPointer, int position) {
        return buffer.get(blockPointer + position);
    }

    public void putInt(int blockPointer, int position, int value) {
        buffer.putInt(blockPointer + position, value);
    }

    public int getInt(int blockPointer, int position) {
        return buffer.getInt(blockPointer + position);
    }

    public void putBuffer(int blockPointer, int position, ByteBuffer src) {
        buffer.position(blockPointer + position);
        buffer.put(src);
    }

    public byte[] getBytes(int blockPointer, int position, int length) {
        byte[] bs = new byte[length];
        buffer.position(blockPointer + position);
        buffer.get(bs, 0, length);
        return bs;
    }

    public void putShort(int blockPointer, int position, short value) {
        buffer.putShort(blockPointer + position, value);
    }

    public short getShort(int blockPointer, int position) {
        return buffer.getShort(blockPointer + position);
    }

    public void putLong(int blockPointer, int position, long value) {
        buffer.putLong(blockPointer + position, value);
    }

    public long getLong(int blockPointer, int position) {
        return buffer.getLong(blockPointer + position);
    }

    public float getFloat(int blockPointer, int position) {
        return buffer.getFloat(blockPointer + position);
    }

    public void putFloat(int blockPointer, int position, float value) {
        buffer.putFloat(blockPointer + position, value);
    }

    public double getDouble(int blockPointer, int position) {
        return buffer.getDouble(blockPointer + position);
    }

    public void putDouble(int blockPointer, int position, double value) {
        buffer.putDouble(blockPointer + position, value);
    }
}
