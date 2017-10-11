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

import se.grenby.sos.readpointer.BufferReadPointer;

import java.nio.charset.StandardCharsets;

import static se.grenby.sos.constant.SosConstants.*;
import static se.grenby.sos.util.BitUtil.HEXES;

/**
 * Created by peteri on 30/01/16.
 */
public abstract class SosObject {
    protected final BufferReadPointer bufferReadPointer;
    protected final int startBlockPosition;

    SosObject(BufferReadPointer bufferReadPointer, int position) {
        this.bufferReadPointer = bufferReadPointer;
        this.startBlockPosition = position;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bufferReadPointer.getAllocatedSize(); i++) {
            byte b = bufferReadPointer.getByte(i);
            sb.append(HEXES[(b & 0xff) >>> 4]);
            sb.append(HEXES[(b & 0xf)]);
            sb.append(" ");
        }
        return sb.toString();
    }

    protected <T> T extractValue(Class<T> klass, int valueType, SosPosition position) {
        T value;
        if (valueType == BYTE_VALUE) {
            value = klass.cast(bufferReadPointer.getByte(position.position()));
            position.incByte();
        } else if (valueType == SHORT_VALUE) {
            value = klass.cast(bufferReadPointer.getShort(position.position()));
            position.incShort();
        } else if (valueType == INTEGER_VALUE) {
            value = klass.cast(bufferReadPointer.getInt(position.position()));
            position.incInteger();
        } else if (valueType == LONG_VALUE) {
            value = klass.cast(bufferReadPointer.getLong(position.position()));
            position.incLong();
        } else if (valueType == STRING_VALUE) {
            value = klass.cast(getStringFromByteBuffer(position));
        } else if (valueType == FLOAT_VALUE) {
            value = klass.cast(bufferReadPointer.getFloat(position.position()));
            position.incFloat();
        } else if (valueType == DOUBLE_VALUE) {
            value = klass.cast(bufferReadPointer.getDouble(position.position()));
            position.incDouble();
        } else {
            throw new RuntimeException("Value type " + valueType + " is unknown.");
        }
        return value;
    }

    protected void skipValueTypeAndValueInByteBuffer(SosPosition position) {
        byte valueType = bufferReadPointer.getByte(position.position());
        position.incByte();
        switch (valueType) {
            case BYTE_VALUE:
                position.incByte();
                break;
            case SHORT_VALUE:
                position.incShort();
                break;
            case INTEGER_VALUE:
                position.incInteger();
                break;
            case LONG_VALUE:
                position.incLong();
                break;
            case FLOAT_VALUE:
                position.incFloat();
                break;
            case DOUBLE_VALUE:
                position.incDouble();
                break;
            case MAP_VALUE:
            case LIST_VALUE:
                skipMapOrListValueInByteBuffer(position);
                break;
            case STRING_VALUE:
                skipStringValueInByteBuffer(position);
                break;
            default:
                throw new RuntimeException("Unknown value type " + valueType);
        }
    }

    private void skipStringValueInByteBuffer(SosPosition position) {
        int stringLength = bufferReadPointer.getByte(position.position());
        position.incByte();
        position.addLength(stringLength);
    }

    protected void skipMapOrListValueInByteBuffer(SosPosition position) {
        int mlLength = bufferReadPointer.getShort(position.position());
        position.incShort();
        position.addLength(mlLength);
    }

    protected String getStringFromByteBuffer(SosPosition position) {
        int length = bufferReadPointer.getByte(position.position());
        position.incByte();
        byte[] bs = bufferReadPointer.getBytes(position.position(), length);
        position.addLength(length);
        return new String(bs, StandardCharsets.UTF_8);
    }

    public int getStartBlockPosition() {
        return startBlockPosition;
    }
}
