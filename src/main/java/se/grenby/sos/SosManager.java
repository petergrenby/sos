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
package se.grenby.sos;

import se.grenby.sos.byteblock.ByteBlockAllocator;
import se.grenby.sos.json.JsonDataList;
import se.grenby.sos.json.JsonDataMap;
import se.grenby.sos.object.SosList;
import se.grenby.sos.object.SosMap;
import se.grenby.sos.object.SosObject;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static se.grenby.sos.constant.SosConstants.*;

/**
 * Created by peteri on 30/01/16.
 */
public class SosManager {

    private static final int MAX_BYTES_SOS_OBJECT = Short.MAX_VALUE;
    private final ByteBlockAllocator allocator;
    private ThreadLocal<ByteBuffer> buffers = new ThreadLocal<>();
    private Map<SosObject, Integer> objectToBlockMap = new HashMap<>();

    public SosManager(ByteBlockAllocator allocator) {
        this.allocator = allocator;
    }

    public SosMap createSosMap(JsonDataMap map) {
        ByteBuffer buffer = getByteBuffer();

        buildSosMap(buffer, map);
        buffer.flip();

        int blockPointer = allocator.allocateAndClone(buffer);
        SosMap so = new SosMap(allocator, blockPointer);
        objectToBlockMap.put(so, blockPointer);
        return so;
    }

    public SosList createSosList(JsonDataList list) {
        ByteBuffer buffer = getByteBuffer();

        buildSosList(buffer, list);
        buffer.flip();

        int blockPointer = allocator.allocateAndClone(buffer);
        SosList so = new SosList(allocator, blockPointer);
        objectToBlockMap.put(so, blockPointer);
        return so;
    }

    public void removeSosObject(SosObject sosObject) {
        allocator.deallocate(objectToBlockMap.get(sosObject));
    }

    private ByteBuffer getByteBuffer() {
        ByteBuffer buffer = buffers.get();
        if (buffer == null) {
            buffer = ByteBuffer.allocate(MAX_BYTES_SOS_OBJECT);
            buffers.set(buffer);
        }
        buffer.clear();
        return buffer;
    }

    private void buildSosMap(ByteBuffer dst, JsonDataMap map) {
        dst.put(MAP_VALUE);
        // Move position so we can set size later on
        int mapSizePosition = dst.position();
        dst.position(mapSizePosition + Short.BYTES);
        for (Map.Entry<String, Object> o : map) {
            putStringInByteBuffer(dst, o.getKey());
            buildValue(dst, o.getValue());
        }

        dst.putShort(mapSizePosition, (short) (dst.position() - Short.BYTES - mapSizePosition));
    }

    private void buildSosList(ByteBuffer dst, JsonDataList list) {
        dst.put(LIST_VALUE);
        // Move position so we can set size later on
        int listSizePosition = dst.position();
        dst.position(listSizePosition + Short.BYTES);
        for (Object v : list) {
            buildValue(dst, v);
        }

        dst.putShort(listSizePosition, (short) (dst.position() - Short.BYTES - listSizePosition));
    }

    private void buildValue(ByteBuffer dst, Object value) {
        if (value instanceof JsonDataMap) {
            JsonDataMap map = (JsonDataMap) value;
            buildSosMap(dst, map);
        } else if (value instanceof JsonDataList) {
            JsonDataList list = (JsonDataList) value;
            buildSosList(dst, list);
        } else if (value instanceof Byte) {
            dst.put(BYTE_VALUE);
            dst.put((Byte) value);
        } else if (value instanceof Short) {
            dst.put(SHORT_VALUE);
            dst.putShort((Short) value);
        } else if (value instanceof Integer) {
            dst.put(INTEGER_VALUE);
            dst.putInt((Integer) value);
        } else if (value instanceof Long) {
            dst.put(LONG_VALUE);
            dst.putLong((Long) value);
        } else if (value instanceof String) {
            dst.put(STRING_VALUE);
            putStringInByteBuffer(dst, (String) value);
        } else if (value instanceof Float) {
            dst.put(FLOAT_VALUE);
            dst.putFloat((Float) value);
        } else if (value instanceof Double) {
            dst.put(DOUBLE_VALUE);
            dst.putDouble((Double) value);
        }
    }

    private void putStringInByteBuffer(ByteBuffer dst, String s) {
        byte[] bs = s.getBytes(StandardCharsets.UTF_8);
        dst.put((byte) bs.length);
        dst.put(bs);
    }

}
