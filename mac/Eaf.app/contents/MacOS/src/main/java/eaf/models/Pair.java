
package eaf.models;

public class Pair<F, S> {
    private F first;
    private S second;

    // Constructor
    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    // Getter for first element
    public F getFirst() {
        return first;
    }

    // Setter for first element
    public void setFirst(F first) {
        this.first = first;
    }

    // Getter for second element
    public S getSecond() {
        return second;
    }

    // Setter for second element
    public void setSecond(S second) {
        this.second = second;
    }

    // Override toString method
    @Override
    public String toString() {
        return "Pair{" +
                "first=" + first +
                ", second=" + second +
                '}';
    }

    // Override equals method
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pair<?, ?> pair = (Pair<?, ?>) o;

        if (!first.equals(pair.first)) return false;
        return second.equals(pair.second);
    }

    // Override hashCode method
    @Override
    public int hashCode() {
        int result = first.hashCode();
        result = 31 * result + second.hashCode();
        return result;
    }
}