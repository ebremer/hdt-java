package org.rdfhdt.hdt.util.disk;

import org.visnow.jlargearrays.LargeArray;
import org.visnow.jlargearrays.LargeArrayUtils;
import org.visnow.jlargearrays.LongLargeArray;

import java.io.IOException;

/**
 * Implementation of {@link LongArray} using a LargeArray
 *
 * @author Antoine Willerval
 */
public class LargeLongArray implements LongArray {
    private LargeArray array;

    /**
     * @param array large array
     */
    public LargeLongArray(LargeArray array) {
        this.array = array;
    }

    @Override
    public long get(long index) {
        return array.getLong(index);
    }

    @Override
    public void set(long index, long value) {
        array.setLong(index, value);
    }

    @Override
    public long length() {
        return array.length();
    }

    @Override
    public int sizeOf() {
        return (int) array.getType().sizeOf() * 8;
    }

    @Override
    public void resize(long newSize) throws IOException {
        if (newSize > 0) {
            if (array.length() != newSize) {
                LongLargeArray a = new LongLargeArray(newSize);
                LargeArrayUtils.arraycopy(array, 0, a, 0, Math.min(newSize, array.length()));
                array = a;
            }
        }
    }
}
