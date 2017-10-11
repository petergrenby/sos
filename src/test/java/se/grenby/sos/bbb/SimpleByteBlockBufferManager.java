package se.grenby.sos.bbb;

import se.grenby.sos.readpointer.BufferReadPointer;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class SimpleByteBlockBufferManager implements ByteBlockBufferAllocator, ByteBlockBufferReader {
    private ArrayList<ByteBuffer> byteBlocks = new ArrayList<>();

    @Override
    public int allocate(int sizeOfPayload) {
        byteBlocks.add(ByteBuffer.allocate(sizeOfPayload));
        return byteBlocks.size() - 1;
    }

    @Override
    public int allocateAndClear(int sizeOfPayload) {
        return allocate(sizeOfPayload);
    }

    @Override
    public int allocateAndClone(ByteBuffer buffer) {
        ByteBuffer clone = ByteBuffer.allocate(buffer.capacity());
        buffer.rewind();
        clone.put(buffer);
        buffer.rewind();
        clone.flip();
        byteBlocks.add(clone);
        return byteBlocks.size() - 1;
    }

    public BufferReadPointer getBlock(int blockIndex) {
        final ByteBuffer block = byteBlocks.get(blockIndex);

        return new BufferReadPointer() {

            @Override
            public byte[] getBytes(int position, int length) {
                byte[] bs = new byte[length];
                block.position(position);
                block.get(bs, 0, length);
                return bs;
            }

            @Override
            public byte getByte(int position) {
                return block.get(position);
            }

            @Override
            public short getShort(int position) {
                return block.getShort(position);
            }

            @Override
            public int getInt(int position) {
                return block.getInt(position);
            }

            @Override
            public long getLong(int position) {
                return block.getLong(position);
            }

            @Override
            public float getFloat(int position) {
                return block.getFloat(position);
            }

            @Override
            public double getDouble(int position) {
                return block.getDouble(position);
            }

            @Override
            public int getAllocatedSize() {
                return block.position();
            }
        };
    }

    public void deallocate(int blockPointer) {
        byteBlocks.remove(blockPointer);
    }

    @Override
    public byte[] getBytes(int blockPointer, int position, int length) {
        byte[] bs = new byte[length];
        byteBlocks.get(blockPointer).position(blockPointer + position);
        byteBlocks.get(blockPointer).get(bs, 0, length);
        return bs;
    }

    @Override
    public byte getByte(int blockPointer, int position) {
        return byteBlocks.get(blockPointer).get(position);
    }

    @Override
    public short getShort(int blockPointer, int position) {
        return byteBlocks.get(blockPointer).getShort(position);
    }

    @Override
    public int getInt(int blockPointer, int position) {
        return byteBlocks.get(blockPointer).getInt(position);
    }

    @Override
    public long getLong(int blockPointer, int position) {
        return byteBlocks.get(blockPointer).getLong(position);
    }

    @Override
    public float getFloat(int blockPointer, int position) {
        return byteBlocks.get(blockPointer).getFloat(position);
    }

    @Override
    public double getDouble(int blockPointer, int position) {
        return byteBlocks.get(blockPointer).getDouble(position);
    }

    @Override
    public int allocatedSize(int blockPointer) {
        return byteBlocks.get(blockPointer).capacity();
    }
}
