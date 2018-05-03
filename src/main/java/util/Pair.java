package util;

import com.google.common.base.MoreObjects;

import java.util.Objects;

/**
 * SPAN - Stochastic Protocol Analyzer
 * <p>
 * Created: 8/29/17
 *
 * @author Matthew S. Bauer
 * @version 1.0
 */
public class Pair<T1, T2> {

    private final T1 key;
    private final T2 value;


    public Pair(T1 key, T2 value) {

        this.key = key;
        this.value = value;
    }

    public T1 getKey() {
        return key;
    }

    public T2 getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {

        if (!(o instanceof Pair)) {
            return false;
        }

        if (!this.key.equals(((Pair) o).key)) return false;
        if (!this.value.equals(((Pair) o).value)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("key", key)
                .add("value", value)
                .toString();
    }
}
