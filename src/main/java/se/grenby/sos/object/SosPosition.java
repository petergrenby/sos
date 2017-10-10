package se.grenby.sos.object;

/**
 * Created by peteri on 5/30/16.
 */
class SosPosition {

    private int position;

    public SosPosition(int position) {
        this.position = position;
    }

    public void incByte() {
        position += Byte.BYTES;
    }

    public void incShort() {
        position += Short.BYTES;
    }

    public void incInteger() {
        position += Integer.BYTES;
    }

    public void incLong() {
        position += Long.BYTES;
    }

    public void incFloat() {
        position += Float.BYTES;
    }

    public void incDouble() {
        position += Double.BYTES;
    }

    public void addLength(int length) {
        position += length;
    }

    public int position() {
        return position;
    }

    @Override
    public String toString() {
        return "SosPosition{" +
                "position=" + position +
                '}';
    }
}
