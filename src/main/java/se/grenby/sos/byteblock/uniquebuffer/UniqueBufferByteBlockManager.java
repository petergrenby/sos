package se.grenby.sos.byteblock.uniquebuffer;

import se.grenby.sos.byteblock.ByteBlockAllocator;
import se.grenby.sos.byteblock.ByteBlockReader;
import se.grenby.sos.byteblock.ByteBlockReadPointer;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class UniqueBufferByteBlockManager implements ByteBlockAllocator, ByteBlockReader {
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
        ByteBuffer clone = ByteBuffer.allocate(buffer.limit());
        buffer.rewind();
        clone.put(buffer);
        buffer.rewind();
        clone.flip();
        byteBlocks.add(clone);
        return byteBlocks.size() - 1;
    }

    public ByteBlockReadPointer getBlock(int blockIndex) {
        final ByteBuffer block = byteBlocks.get(blockIndex);

        return new ByteBlockReadPointer() {

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
                return block.limit();
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
        return byteBlocks.get(blockPointer).limit();
    }
}
