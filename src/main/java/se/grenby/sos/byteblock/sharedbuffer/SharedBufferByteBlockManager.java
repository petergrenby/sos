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

import se.grenby.sos.byteblock.ByteBlockAllocator;
import se.grenby.sos.byteblock.ByteBlockReader;
import se.grenby.sos.util.BitUtil;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import static se.grenby.sos.constant.PrimitiveConstants.INT_VALUE_FOR_NULL;

/**
 * Created by peteri on 23/10/15.
 */
public class SharedBufferByteBlockManager implements ByteBlockAllocator, ByteBlockReader {

    private final static Logger logger = Logger.getLogger(SharedBufferByteBlockManager.class.getName());

    // The smallest block that is allowed after a split.
    // Some if it will taken for overhead block handling, 8 bytes at time of writing.
    private final static int SMALLEST_BLOCK_SIZE = Integer.BYTES*4;

    private final static byte BYTE_STATUS_FREE = 0;
    private final static byte BYTE_STATUS_OCCUPIED = 127;

    private final static int RELATIVE_POINTER_STATUS = 0;
    private final static int RELATIVE_POINTER_PAYLOAD = RELATIVE_POINTER_STATUS + Byte.BYTES;

    private final static int RELATIVE_POINTER_PREVIOUS = RELATIVE_POINTER_STATUS + Byte.BYTES;
    private final static int RELATIVE_POINTER_NEXT = RELATIVE_POINTER_PREVIOUS + Integer.BYTES;
    private final static int RELATIVE_POINTER_VOID_SPACE = RELATIVE_POINTER_NEXT + Byte.BYTES;

    public final static int MANAGED_BLOCK_OVERHEAD_IN_BYTES = RELATIVE_POINTER_PAYLOAD + SharedBufferByteBlocks.BLOCK_OVERHEAD_IN_BYTES;

    private final SharedBufferByteBlocks blockBuffer;
    private final int sizeOfBinBlock;
    private final int numberOfBins;
    private final int binBlockPointer;

    private int numberOfAllocatedBlocks = 0;
    private int totalSizeOfAllocatedBlocks = 0;
    private int numberOfBlocksInBins = 0;
    private int totalSizeOfBlocksInBins = 0;

    public SharedBufferByteBlockManager(final int capacity) {
        blockBuffer = new SharedBufferByteBlocks(capacity);

        numberOfBins = BitUtil.numberOfBitsNeeded(blockBuffer.getCapacity());
        binBlockPointer = blockBuffer.getFirstBlock();

        // Spilt first block to bin-block and void-block
        int voidPointer = blockBuffer.splitBlock(binBlockPointer, numberOfBins*Integer.BYTES);
        sizeOfBinBlock = numberOfBins*Integer.BYTES;

        // Set all bins to NULL
        for (int i=0; i<numberOfBins; i++) {
            blockBuffer.putInt(binBlockPointer, i*Integer.BYTES, INT_VALUE_FOR_NULL);
        }

        // Set values in empty block (size of block, pointer to previous block, pointer to next block)
        // Remove size value from size of block
        blockBuffer.putByte(voidPointer, RELATIVE_POINTER_STATUS, BYTE_STATUS_FREE);
        blockBuffer.putInt(voidPointer, RELATIVE_POINTER_PREVIOUS, INT_VALUE_FOR_NULL);
        blockBuffer.putInt(voidPointer, RELATIVE_POINTER_NEXT, INT_VALUE_FOR_NULL);

        // Attach void-block to bins
        attachBlockToBins(voidPointer);
        String details = getDetailsAsString();


        logger.info(details);
    }

    public String getDetailsAsString() {
        // Print manager details
        StringBuilder sb = new StringBuilder("BBB manager details \n");
        sb.append("Total capacity: " + blockBuffer.getCapacity() + "\n");
        sb.append("Number of bins: " + numberOfBins + "\n");
        sb.append("Bin-block pointer: " + binBlockPointer + "\n");
        sb.append("# allocated blocks: " + numberOfAllocatedBlocks + "\n");
        sb.append("Total allocated size: " + totalSizeOfAllocatedBlocks + "\n");
        sb.append("# blocks in bins: " + numberOfBlocksInBins + "\n");
        sb.append("Total size in bins: " + totalSizeOfBlocksInBins + "\n");
        return sb.toString();
    }

    @Override
    public int allocate(final int sizeOfPayload) {
        // find smallest block that is big enough in the bin
        int blockPointer = findLeastSizedBlockBins(sizeOfPayload + RELATIVE_POINTER_PAYLOAD);
        if (blockPointer != INT_VALUE_FOR_NULL) {
            detachBlockFromBins(blockPointer);

            // allocate memory from the block and return excessive memory to bins
            int sizeOfBlock = blockBuffer.getBlockSize(blockPointer);
            if ((sizeOfBlock-sizeOfPayload) > (RELATIVE_POINTER_VOID_SPACE + RELATIVE_POINTER_PAYLOAD + SMALLEST_BLOCK_SIZE)) {
                sizeOfBlock = sizeOfPayload + RELATIVE_POINTER_PAYLOAD;
                int excessiveBlockPointer = blockBuffer.splitBlock(blockPointer, sizeOfBlock);
                attachBlockToBins(excessiveBlockPointer);
            }

            numberOfAllocatedBlocks++;
            totalSizeOfAllocatedBlocks += sizeOfBlock;

            blockBuffer.putByte(blockPointer, RELATIVE_POINTER_STATUS, BYTE_STATUS_OCCUPIED);
            return blockPointer;
        } else {
            return INT_VALUE_FOR_NULL;
        }
    }

    @Override
    public int allocateAndClear(final int sizeOfPayload) {
        int pointer = allocate(sizeOfPayload);
        if (pointer != INT_VALUE_FOR_NULL) {
            resetBlock(pointer);
        }
        return pointer;
    }

    @Override
    public int allocateAndClone(final ByteBuffer buffer) {
        int pointer = allocate(buffer.limit());
        if (pointer != INT_VALUE_FOR_NULL) {
            putByteBuffer(pointer, 0, buffer);
        }
        return pointer;
    }

    public void deallocate(int blockPointer) {
        if (blockPointer == INT_VALUE_FOR_NULL) {
            throw new RuntimeException("Block can not be deallocated: pointer " + blockPointer);
        }

        int sizeOfBlock = blockBuffer.getBlockSize(blockPointer);
        byte status = blockBuffer.getByte(blockPointer, RELATIVE_POINTER_STATUS);
        if (status == BYTE_STATUS_OCCUPIED) {
            int blockPointerPrevious = blockBuffer.previousBlock(blockPointer);
            if (blockPointerPrevious != binBlockPointer) {
                byte statusPrevious = blockBuffer.getByte(blockPointerPrevious, RELATIVE_POINTER_STATUS);
                if (statusPrevious == BYTE_STATUS_FREE) {
                    detachBlockFromBins(blockPointerPrevious);
                    blockBuffer.mergeBlocks(blockPointerPrevious, blockPointer);
                    blockPointer = blockPointerPrevious;
                }
            }

            int blockPointerNext = blockBuffer.nextBlock(blockPointer);
            if (blockPointerNext != INT_VALUE_FOR_NULL) {
                byte statusNext = blockBuffer.getByte(blockPointerNext, RELATIVE_POINTER_STATUS);
                if (statusNext == BYTE_STATUS_FREE) {
                    detachBlockFromBins(blockPointerNext);
                    blockBuffer.mergeBlocks(blockPointer, blockPointerNext);
                }
            }

            blockBuffer.putByte(blockPointer, RELATIVE_POINTER_STATUS, BYTE_STATUS_FREE);
            attachBlockToBins(blockPointer);

            numberOfAllocatedBlocks--;
            totalSizeOfAllocatedBlocks -= sizeOfBlock;

        } else {
            throw new RuntimeException("Block can not be deallocated: pointer " + blockPointer);
        }
    }

    private void attachBlockToBins(int attachPointer) {
        int attachSize = blockBuffer.getBlockSize(attachPointer);
        int binIndex = binIndexFromSize(attachSize);
        int blockPointer = blockBuffer.getInt(binBlockPointer, binIndex*Integer.BYTES);

        int pointerPreviousBlock = INT_VALUE_FOR_NULL;
        int pointerNextBlock = INT_VALUE_FOR_NULL;

        if (blockPointer == INT_VALUE_FOR_NULL) {
            // Attach block to array of buckets
            blockBuffer.putInt(binBlockPointer, binIndex*Integer.BYTES, attachPointer);
        } else {
            // Find place in block list
            while (blockBuffer.getBlockSize(blockPointer) < attachSize &&
                    blockPointer != INT_VALUE_FOR_NULL) {
                blockPointer = blockBuffer.getInt(blockPointer, RELATIVE_POINTER_NEXT);
            }

            if (blockPointer == INT_VALUE_FOR_NULL) {
                // Add block last in block list
                pointerPreviousBlock = blockBuffer.getInt(blockPointer, RELATIVE_POINTER_PREVIOUS);
                // Attach last block to empty block
                blockBuffer.putInt(pointerPreviousBlock, RELATIVE_POINTER_NEXT, attachPointer);
            } else {
                // Attach block between blocks in list
                pointerPreviousBlock = blockBuffer.getInt(blockPointer, RELATIVE_POINTER_PREVIOUS);
                pointerNextBlock = blockBuffer.getInt(blockPointer, RELATIVE_POINTER_NEXT);

                // Attach previous and next block to empty block
                if (pointerPreviousBlock == INT_VALUE_FOR_NULL) {
                    blockBuffer.putInt(binBlockPointer, binIndex*Integer.BYTES, attachPointer);
                } else {
                    blockBuffer.putInt(pointerPreviousBlock, RELATIVE_POINTER_NEXT, attachPointer);
                }
                if (pointerNextBlock != INT_VALUE_FOR_NULL) {
                    blockBuffer.putInt(pointerNextBlock, RELATIVE_POINTER_PREVIOUS, attachPointer);
                }
            }
        }

        // Set correct size and pointers into empty block
        blockBuffer.putInt(attachPointer, RELATIVE_POINTER_PREVIOUS, pointerPreviousBlock);
        blockBuffer.putInt(attachPointer, RELATIVE_POINTER_NEXT, pointerNextBlock);

        numberOfBlocksInBins++;
        totalSizeOfBlocksInBins += attachSize;
    }

    private static int binIndexFromSize(int size) {
        return BitUtil.numberOfBitsNeeded(size) - 1;
    }

    private void detachBlockFromBins(int detachPointer) {
        int pointerPreviousBlock = blockBuffer.getInt(detachPointer, RELATIVE_POINTER_PREVIOUS);
        int pointerNextBlock = blockBuffer.getInt(detachPointer, RELATIVE_POINTER_NEXT);
        int detachSize = blockBuffer.getBlockSize(detachPointer);

        // detach block from linked list
        if (pointerPreviousBlock == INT_VALUE_FOR_NULL) {
            // find index of smallest possible bin
            int binIndex = binIndexFromSize(detachSize);
            blockBuffer.putInt(binBlockPointer, binIndex*Integer.BYTES, pointerNextBlock);
        } else {
            blockBuffer.putInt(pointerPreviousBlock, RELATIVE_POINTER_NEXT, pointerNextBlock);
        }
        if (pointerNextBlock != INT_VALUE_FOR_NULL) {
            blockBuffer.putInt(pointerNextBlock, RELATIVE_POINTER_PREVIOUS, pointerPreviousBlock);
        }

        numberOfBlocksInBins--;
        totalSizeOfBlocksInBins -= detachSize;
    }

    private int findLeastSizedBlockBins(int requestedSize) {
        // find index of smallest possible bin
        int binIndex = binIndexFromSize(requestedSize);

        // find smallest block that is big enough in the bin
        int allocationPointer = INT_VALUE_FOR_NULL;
        while (binIndex < numberOfBins && allocationPointer == INT_VALUE_FOR_NULL) {
            allocationPointer = findLeastSizedBlockInBinList(blockBuffer.getInt(binBlockPointer, binIndex*Integer.BYTES), requestedSize);
            binIndex++;
        }
        return allocationPointer;
    }

    private int findLeastSizedBlockInBinList(int pointer, int requestedSize) {
        boolean foundBlock = false;
        while (!foundBlock && pointer != INT_VALUE_FOR_NULL) {

            int sizeOfBlock = blockBuffer.getBlockSize(pointer);
            if (sizeOfBlock >= requestedSize ) {
                foundBlock = true;
            } else {
                pointer = blockBuffer.getInt(pointer, RELATIVE_POINTER_NEXT);
            }

        }

        return pointer;
    }

    public String memStructureToString() {
        StringBuilder sb = new StringBuilder();

        int bi = 0;
        while (bi < numberOfBins) {
            sb.append("bin[" + bi + "] : ");
            int p = blockBuffer.getInt(binBlockPointer, bi*Integer.BYTES);
            while (p != INT_VALUE_FOR_NULL) {
                sb.append("{p=").append(p).append(" as=").append(blockBuffer.getBlockSize(p) - RELATIVE_POINTER_PAYLOAD).append(" bs=").append(blockBuffer.getBlockSize(p)).append("}");
                p = blockBuffer.getInt(p, RELATIVE_POINTER_NEXT);

            }
            sb.append("\n");
            bi++;
        }

        sb.append(blockBuffer.blockStructureToString());

        return sb.toString();
    }

    public boolean verfiyIntegrity() {
        boolean correct = blockBuffer.verfiyIntegrity();

        if (correct) {
            int vNum = 0;
            int vSize = 0;
            int bi = 0;
            while (bi < numberOfBins) {
                int p = blockBuffer.getInt(binBlockPointer, bi*Integer.BYTES);
                while (p != INT_VALUE_FOR_NULL) {
                    vNum++;
                    vSize += blockBuffer.getBlockSize(p);
                    p = blockBuffer.getInt(p, RELATIVE_POINTER_NEXT);

                }
                bi++;
            }

            int totalAmountOfBlockBufferOverhead = blockBuffer.getNumberOfBlocks() * blockBuffer.BLOCK_OVERHEAD_IN_BYTES;

            if (numberOfBlocksInBins != vNum) {
                logger.severe("Blocks have been lost from bins, expected " + numberOfBlocksInBins + " but found " + vNum + " number of blocks");
                correct = false;
            } else if (totalSizeOfBlocksInBins != vSize) {
                logger.severe("Space as bin lost from bins, expected " + totalSizeOfBlocksInBins + " actual " + vSize);
                correct = false;
            } else if (numberOfBlocksInBins + numberOfAllocatedBlocks + 1 != blockBuffer.getNumberOfBlocks()) {
                logger.severe("Total number of blocks are " + blockBuffer.getNumberOfBlocks() + ", these blocks should be in bins (" + numberOfBlocksInBins + ") or allocated blocks (" + numberOfAllocatedBlocks + ")");
                correct = false;
            } else if (totalSizeOfBlocksInBins + totalSizeOfAllocatedBlocks + sizeOfBinBlock + totalAmountOfBlockBufferOverhead != blockBuffer.getCapacity()) {
                logger.severe("Total blockbuffer capacity is " + blockBuffer.getCapacity() + ", this space should be in bins (" + totalSizeOfBlocksInBins + ") or in allocated blocks (" + totalSizeOfAllocatedBlocks + ") or the bin block (" + sizeOfBinBlock + " or in overhead (" + totalAmountOfBlockBufferOverhead + ")");
                correct = false;
            }
        }

        return correct;
    }

    private void resetBlock(int blockPointer) {
        int size = blockBuffer.getBlockSize(blockPointer) - RELATIVE_POINTER_PAYLOAD;
        for (int p = 0; p < size; p++) {
            blockBuffer.putByte(blockPointer, RELATIVE_POINTER_PAYLOAD + p, (byte) 0);
        }
    }

    public void putByteBuffer(int blockPointer, int position, ByteBuffer src) {
        src.position(0);
        int blockPosition = position + RELATIVE_POINTER_PAYLOAD;
        checkBoundsOfBlock(blockPointer, blockPosition, src.remaining());
        blockBuffer.putBuffer(blockPointer, blockPosition, src);
    }

    @Override
    public byte[] getBytes(int blockPointer, int position, int length) {
        int blockPosition = position + RELATIVE_POINTER_PAYLOAD;
        checkBoundsOfBlock(blockPointer, blockPosition, length);
        return blockBuffer.getBytes(blockPointer, blockPosition, length);
    }

    public void putByte(int blockPointer, int position, byte value) {
        int blockPosition = position + RELATIVE_POINTER_PAYLOAD;
        checkBoundsOfBlock(blockPointer, blockPosition, Byte.BYTES);
        blockBuffer.putByte(blockPointer, blockPosition, value);
    }

    @Override
    public byte getByte(int blockPointer, int position) {
        int blockPosition = position + RELATIVE_POINTER_PAYLOAD;
        checkBoundsOfBlock(blockPointer, blockPosition, Byte.BYTES);
        return blockBuffer.getByte(blockPointer, blockPosition);
    }

    public void putShort(int blockPointer, int position, short value) {
        int blockPosition = position + RELATIVE_POINTER_PAYLOAD;
        checkBoundsOfBlock(blockPointer, blockPosition, Short.BYTES);
        blockBuffer.putShort(blockPointer, blockPosition, value);
    }

    @Override
    public short getShort(int blockPointer, int position) {
        int blockPosition = position + RELATIVE_POINTER_PAYLOAD;
        checkBoundsOfBlock(blockPointer, blockPosition, Short.BYTES);
        return blockBuffer.getShort(blockPointer, blockPosition);
    }

    public void putInt(int blockPointer, int position, int value) {
        int blockPosition = position + RELATIVE_POINTER_PAYLOAD;
        checkBoundsOfBlock(blockPointer, blockPosition, Integer.BYTES);
        blockBuffer.putInt(blockPointer, blockPosition, value);
    }

    @Override
    public int getInt(int blockPointer, int position) {
        int blockPosition = position + RELATIVE_POINTER_PAYLOAD;
        checkBoundsOfBlock(blockPointer, blockPosition, Integer.BYTES);
        return blockBuffer.getInt(blockPointer, blockPosition);
    }

    public void putLong(int blockPointer, int position, long value) {
        int blockPosition = position + RELATIVE_POINTER_PAYLOAD;
        checkBoundsOfBlock(blockPointer, blockPosition, Long.BYTES);
        blockBuffer.putLong(blockPointer, blockPosition, value);
    }


    @Override
    public long getLong(int blockPointer, int position) {
        int blockPosition = position + RELATIVE_POINTER_PAYLOAD;
        checkBoundsOfBlock(blockPointer, blockPosition, Long.BYTES);
        return blockBuffer.getLong(blockPointer, blockPosition);
    }


    @Override
    public float getFloat(int blockPointer, int position) {
        int blockPosition = position + RELATIVE_POINTER_PAYLOAD;
        checkBoundsOfBlock(blockPointer, blockPosition, Float.BYTES);
        return blockBuffer.getFloat(blockPointer, blockPosition);
    }

    public void putFloat(int blockPointer, int position, float value) {
        int blockPosition = position + RELATIVE_POINTER_PAYLOAD;
        checkBoundsOfBlock(blockPointer, blockPosition, Float.BYTES);
        blockBuffer.putFloat(blockPointer, blockPosition, value);
    }


    @Override
    public double getDouble(int blockPointer, int position) {
        int blockPosition = position + RELATIVE_POINTER_PAYLOAD;
        checkBoundsOfBlock(blockPointer, blockPosition, Double.BYTES);
        return blockBuffer.getDouble(blockPointer, blockPosition);
    }

    public void putDouble(int blockPointer, int position, double value) {
        int blockPosition = position + RELATIVE_POINTER_PAYLOAD;
        checkBoundsOfBlock(blockPointer, blockPosition, Double.BYTES);
        blockBuffer.putDouble(blockPointer, blockPosition, value);
    }

    private void checkBoundsOfBlock(int blockPointer, int position, int numberOfBytes) {
        if (position < RELATIVE_POINTER_PAYLOAD ||
                position + numberOfBytes > blockBuffer.getBlockSize(blockPointer)) {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public int allocatedSize(int blockPointer) {
        return blockBuffer.getBlockSize(blockPointer) - RELATIVE_POINTER_PAYLOAD;
    }

    public int getTotalAvailableSpace() {
        return totalSizeOfBlocksInBins;
    }
}
