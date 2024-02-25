package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private Comparator<T> c;
    public MaxArrayDeque(Comparator<T> c) {
        this.c = c;
    }

    public T max() {
        T returnItem = get(0);
        for (int i = 0; i < size(); i += 1) {
            T currItem = get(i);
            if (c.compare(currItem, returnItem) > 0) {
                returnItem = currItem;
            }
        }
        return returnItem;
    }

    public T max(Comparator<T> ac) {
        T returnItem = get(0);
        for (int i = 0; i < size(); i += 1) {
            T currItem = get(i);
            if (ac.compare(currItem, returnItem) > 0) {
                returnItem = currItem;
            }
        }
        return returnItem;
    }
}
