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

import se.grenby.sos.reader.BufferReader;
import se.grenby.sos.json.JsonDataList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static se.grenby.sos.constant.SosConstants.*;
import static se.grenby.sos.constant.SosConstants.DOUBLE_VALUE;
import static se.grenby.sos.constant.SosConstants.FLOAT_VALUE;

/**
 * Created by peteri on 07/02/16.
 */
public class SosList extends SosObject implements Iterable<Object> {

    private final int listStartPosition;
    private final int listTotalLength;

    SosList(BufferReader bufferReader, int position) {
        super(bufferReader, position);

        int blockPosition = startBlockPosition;

        byte valueType = bufferReader.getByte(blockPosition);
        blockPosition += Byte.BYTES;
        if (valueType == LIST_VALUE) {
            listTotalLength = bufferReader.getShort(blockPosition);
            blockPosition += Short.BYTES;
            listStartPosition = blockPosition;
        } else {
            throw new RuntimeException("This is not a list structure " + valueType);
        }
    }

    private Object extractObject(SosPosition position) {
        Object obj;

        int valuePosition = position.position();
        int valueType = bufferReader.getByte(position.position());
        position.incByte();
        if (valueType == MAP_VALUE) {
            obj = new SosMap(bufferReader, valuePosition);
            skipMapOrListValueInByteBuffer(position);
        } else if (valueType == LIST_VALUE) {
            obj = new SosList(bufferReader, valuePosition);
            skipMapOrListValueInByteBuffer(position);
        } else if (valueType == BYTE_VALUE) {
            obj = extractValue(Byte.class, valueType, position);
        } else if (valueType == SHORT_VALUE) {
            obj = extractValue(Short.class, valueType, position);
        } else if (valueType == INTEGER_VALUE) {
            obj = extractValue(Integer.class, valueType, position);
        } else if (valueType == LONG_VALUE) {
            obj = extractValue(Long.class, valueType, position);
        } else if (valueType == STRING_VALUE) {
            obj = extractValue(String.class, valueType, position);
        } else if (valueType == FLOAT_VALUE) {
            obj = extractValue(Float.class, valueType, position);
        } else if (valueType == DOUBLE_VALUE) {
            obj = extractValue(Double.class, valueType, position);
        } else {
            throw new IllegalStateException(valueType + " is not a correct value type.");
        }

        return obj;
    }

    public JsonDataList extractJSonDataList() {
        SosPosition position = new SosPosition(listStartPosition);
        List<Object> list = new ArrayList<>();

        while (position.position() < listStartPosition + listTotalLength) {
            Object obj = extractObject(position);
            if (obj instanceof SosList) {
                obj = ((SosList) obj).extractJSonDataList();
            } else if (obj instanceof SosMap) {
                obj = ((SosMap) obj).extractJSonDataMap();
            }
            list.add(obj);
        }

        return new JsonDataList(list);
    }

    @Override
    public Iterator<Object> iterator() {
        return new SosListIterator();
    }

    private class SosListIterator implements Iterator<Object> {

        private final SosPosition position;

        public SosListIterator() {
            position = new SosPosition(listStartPosition);
        }

        @Override
        public boolean hasNext() {
            if (position.position() < listStartPosition + listTotalLength) {
                return true;
            }
            return false;
        }

        @Override
        public Object next() {
            return extractObject(position);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
