package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    private T[] items;
    private int size;
    private int nextFirst;
    private int nextLast;

    public ArrayDeque() {
        items = (T []) new Object[8];
        size = 0;
        nextFirst = 0;
        nextLast = 1;
    }

    @Override
    public void addFirst(T item) {
        if (size == items.length) {
            resizing(size * 2);
        }
        items[nextFirst] = item;
        nextFirst = getNewIndex(nextFirst, -1);
        size += 1;
    }

    @Override
    public void addLast(T item) {
        if (size == items.length) {
            resizing(size * 2);
        }
        items[nextLast] = item;
        nextLast = getNewIndex(nextLast, 1);
        size += 1;
    }

    @Override
    public T removeFirst() {
        if (size == 0) {
            return null;
        }
        if (size * 4 < items.length && items.length > 4) {
            resizing(items.length / 4);
        }
        nextFirst = getNewIndex(nextFirst, 1);
        size -= 1;
        return items[nextFirst];
    }

    @Override
    public T removeLast() {
        if (size == 0) {
            return null;
        }
        if (size * 4 < items.length && items.length > 4) {
            resizing(items.length / 4);
        }
        nextLast = getNewIndex(nextLast, -1);
        size -= 1;
        return items[nextLast];
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        for (int i = 0; i < size; i++) {
            int index = getNewIndex(nextFirst, 1 + i);
            System.out.print(items[index] + " ");
        }
        System.out.println();
    }

    @Override
    public T get(int index) {
        if (index >= size) {
            return null;
        }
        return items[getNewIndex(nextFirst, 1 + index)];
    }

    private void resizing(int capacity) {
        T[] a = (T []) new Object[capacity];
        int firstIndex = getNewIndex(nextFirst, 1);
        if (firstIndex + size > items.length) {
            int size1 = items.length - firstIndex;
            System.arraycopy(items, firstIndex, a, 0, size1);
            System.arraycopy(items, 0, a, size1, size - size1);
        } else {
            System.arraycopy(items, firstIndex, a, 0, size);
        }
        items = a;
        nextFirst = getNewIndex(0, -1);
        nextLast = size;

    }

    private int getNewIndex(int index, int offset) {
        if (index + offset >= items.length) {
            return index + offset - items.length;
        } else if (index + offset < 0) {
            return items.length + (index + offset);
        } else {
            return index + offset;
        }
    }

    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    private class ArrayDequeIterator implements Iterator<T> {
        private int nextPos;

        ArrayDequeIterator() {
            nextPos = 0;
        }

        @Override
        public boolean hasNext() {
            return nextPos < size;
        }

        @Override
        public T next() {
            if (hasNext()) {
                T next = get(nextPos);
                nextPos += 1;
                return next;
            }
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof Deque) {
            Deque other = (Deque) o;
            if (other.size() != this.size()) {
                return false;
            }
            for (int i = 0; i < size; i++) {
                if (!this.get(i).equals(other.get(i))) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

}

