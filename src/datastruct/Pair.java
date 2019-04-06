package datastruct;

public class Pair<F, S> {
    private F first;
    private S second;

    Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    F getFirst() {
        return first;
    }

    S getSecond() {
        return second;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof  Pair)) {
            return false;
        }

        Pair other = (Pair) obj;
        return this.first == other.first && this.second == other.second;
    }
}
