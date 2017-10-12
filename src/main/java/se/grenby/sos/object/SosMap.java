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
package se.grenby.sos.object;

import se.grenby.sos.byteblock.ByteBlockReadPointer;
import se.grenby.sos.byteblock.ByteBlockReader;
import se.grenby.sos.byteblock.uniquebuffer.UniqueBufferByteBlockReadPointer;
import se.grenby.sos.byteblock.sharedbuffer.SharedBufferByteBlockReadPointer;
import se.grenby.sos.json.JsonDataMap;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static se.grenby.sos.constant.SosConstants.*;

/**
 * Created by peteri on 07/02/16.
 */
public class SosMap extends SosObject implements Iterable<Map.Entry<String, Object>> {

    private final int mapStartPosition;
    private final int mapTotalLength;

    public SosMap(ByteBuffer byteBuffer) {
        this(new UniqueBufferByteBlockReadPointer(byteBuffer));
    }

    public SosMap(ByteBlockReader blockReader, int blockPointer) {
        this(new SharedBufferByteBlockReadPointer(blockReader, blockPointer));
    }

    public SosMap(ByteBlockReadPointer byteBlockReadPointer) {
        this(byteBlockReadPointer, 0);
    }

    public SosMap(ByteBlockReadPointer byteBlockReadPointer, int position) {
        super(byteBlockReadPointer, position);

        int blockPosition = objectStartPosition;

        byte valueType = byteBlockReadPointer.getByte(blockPosition);
        blockPosition += Byte.BYTES;
        if (valueType == MAP_VALUE) {
            mapTotalLength = byteBlockReadPointer.getShort(blockPosition);
            blockPosition += Short.BYTES;
            mapStartPosition = blockPosition;
        } else {
            throw new RuntimeException("This is not a map structure " + valueType);
        }
    }

    public SosMap getMapValue(String key) {
        return extractValueByKey(key, SosMap.class);
    }

    public SosList getListValue(String key) {
        return extractValueByKey(key, SosList.class);
    }

    public byte getByteValue(String key) {
        return extractValueByKey(key, Byte.class);
    }

    public short getShortValue(String key) {
        return extractValueByKey(key, Short.class);
    }

    public int getIntValue(String key) {
        return extractValueByKey(key, Integer.class);
    }

    public long getLongValue(String key) {
        return extractValueByKey(key, Long.class);
    }

    public float getFloatValue(String key) {
        return extractValueByKey(key, Float.class);
    }

    public double getDoubleValue(String key) {
        return extractValueByKey(key, Double.class);
    }

    public String getStringValue(String key) {
        return extractValueByKey(key, String.class);
    }

    private <T> T extractValueByKey(String key, Class<T> klass) {
        T value = null;
        SosPosition position = new SosPosition(mapStartPosition);

        while (position.position() < mapStartPosition + mapTotalLength) {
            String mk = getStringFromByteBuffer(position);
            if (key.equals(mk)) {
                int valuePosition = position.position();
                int valueType = byteBlockReadPointer.getByte(position.position());
                position.incByte();
                if (valueType == MAP_VALUE) {
                    value = klass.cast(new SosMap(byteBlockReadPointer, valuePosition));
                } else if (valueType == LIST_VALUE) {
                    value = klass.cast(new SosList(byteBlockReadPointer, valuePosition));
                } else {
                    value = extractValue(klass, valueType, position);
                }
                break;
            } else {
                skipValueTypeAndValueInByteBuffer(position);
            }
        }

        return value;
    }

    public JsonDataMap extractJSonDataMap() {
        SosPosition position = new SosPosition(mapStartPosition);
        Map<String, Object> map = new HashMap<>();

        while (position.position() < mapStartPosition + mapTotalLength) {
            SosMapEntry entry = extractEntry(position);
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof SosList) {
                value = ((SosList) value).extractJSonDataList();
            } else if (value instanceof SosMap) {
                value = ((SosMap) value).extractJSonDataMap();
            }

            map.put(key, value);
        }

        return new JsonDataMap(map);
    }

    private SosMapEntry extractEntry(SosPosition position) {
        String mk = getStringFromByteBuffer(position);
        int valuePosition = position.position();
        int valueType = byteBlockReadPointer.getByte(position.position());
        position.incByte();

        SosMapEntry entry;
        if (valueType == MAP_VALUE) {
            SosMap cdm = new SosMap(byteBlockReadPointer, valuePosition);
            entry = new SosMapEntry(mk, cdm);
            skipMapOrListValueInByteBuffer(position);
        } else if (valueType == LIST_VALUE) {
            SosList cdl = new SosList(byteBlockReadPointer, valuePosition);
            entry = new SosMapEntry(mk, cdl);
            skipMapOrListValueInByteBuffer(position);
        } else if (valueType == BYTE_VALUE) {
            entry = new SosMapEntry(mk, extractValue(Byte.class, valueType, position));
        } else if (valueType == SHORT_VALUE) {
            entry = new SosMapEntry(mk, extractValue(Short.class, valueType, position));
        } else if (valueType == INTEGER_VALUE) {
            entry = new SosMapEntry(mk, extractValue(Integer.class, valueType, position));
        } else if (valueType == LONG_VALUE) {
            entry = new SosMapEntry(mk, extractValue(Long.class, valueType, position));
        } else if (valueType == STRING_VALUE) {
            entry = new SosMapEntry(mk, extractValue(String.class, valueType, position));
        } else if (valueType == FLOAT_VALUE) {
            entry = new SosMapEntry(mk, extractValue(Float.class, valueType, position));
        } else if (valueType == DOUBLE_VALUE) {
            entry = new SosMapEntry(mk, extractValue(Double.class, valueType, position));
        } else {
            throw new IllegalStateException(valueType + " is not a correct value type.");
        }

        return entry;
    }

    @Override
    public Iterator<Map.Entry<String, Object>> iterator() {
        return new SosMapIterator();
    }

    private class SosMapEntry implements Map.Entry<String, Object>{

        private String key;
        private Object value;

        public SosMapEntry(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public Object setValue(Object value) {
            throw new UnsupportedOperationException();
        }
    }

    private class SosMapIterator implements Iterator<Map.Entry<String, Object>> {

        private final SosPosition position;

        public SosMapIterator() {
            position = new SosPosition(mapStartPosition);
        }

        @Override
        public boolean hasNext() {
            if (position.position() < mapStartPosition + mapTotalLength) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public Map.Entry<String, Object> next() {
            return extractEntry(position);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
