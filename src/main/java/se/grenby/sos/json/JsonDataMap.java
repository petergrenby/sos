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
package se.grenby.sos.json;

import java.util.*;

/**
 * Created by peteri on 10/12/15.
 */
public class JsonDataMap implements Iterable<Map.Entry<String, Object>> {

    private Map<String, Object> map;

    public JsonDataMap() {
        map = new HashMap<>();
    }

    public JsonDataMap(Map<String, Object> map) {
        this.map = map;
    }

    public JsonDataMap putByte(String key, byte value) {
        map.put(key, value);
        return this;
    }

    public JsonDataMap putShort(String key, short value) {
        map.put(key, value);
        return this;
    }

    public JsonDataMap putInt(String key, int value) {
        map.put(key, value);
        return this;
    }

    public JsonDataMap putLong(String key, long value) {
        map.put(key, value);
        return this;
    }

    public JsonDataMap putString(String key, String value) {
        map.put(key, value);
        return this;
    }

    public JsonDataMap putFloat(String key, float value) {
        map.put(key, value);
        return this;
    }

    public JsonDataMap putDouble(String key, double value) {
        map.put(key, value);
        return this;
    }

    public JsonDataMap putMap(String key, JsonDataMap value) {
        map.put(key, value);
        return this;
    }

    public JsonDataMap putList(String key, JsonDataList value) {
        map.put(key, value);
        return this;
    }

    public byte getByte(String key) {
        return (byte) map.get(key);
    }


    public short getShort(String key) {
        return (short) map.get(key);
    }


    public int getInteger(String key) {
        return (Integer) map.get(key);
    }


    public long getLong(String key) {
        return (long) map.get(key);
    }

    public float getFloat(String key) {
        return (float) map.get(key);
    }

    public double getDouble(String key) {
        return (double) map.get(key);
    }

    public String getString(String key) {
        return (String) map.get(key);
    }

    public JsonDataMap getMap(String key) {
        return (JsonDataMap) map.get(key);
    }

    public JsonDataList getList(String key) {
        return (JsonDataList) map.get(key);
    }

    public int size() {
        return map.size();
    }

    @Override
    public Iterator<Map.Entry<String, Object>> iterator() {
        return new JsonDataMapIterator(map.entrySet().iterator());
    }

    private class JsonDataMapEntry implements Map.Entry<String, Object>{

        private String key;
        private Object value;

        public JsonDataMapEntry(String key, Object value) {
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

    private class JsonDataMapIterator implements Iterator<Map.Entry<String, Object>> {

        private Iterator<Map.Entry<String, Object>> mapIter;

        public JsonDataMapIterator(Iterator<Map.Entry<String, Object>> mapIter) {
            this.mapIter = mapIter;
        }

        @Override
        public boolean hasNext() {
            return mapIter.hasNext();
        }

        @Override
        public Map.Entry<String, Object> next() {
            Map.Entry<String, Object> entry = mapIter.next();
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map) {
                value = new JsonDataMap((Map) value);
            } else if (value instanceof List) {
                value = new JsonDataList((List) value);
            }

            return new JsonDataMapEntry(key, value);
        }

        @Override
        public void remove() {
            mapIter.remove();
        }
    }

}
