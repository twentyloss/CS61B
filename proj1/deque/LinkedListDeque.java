package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {
    private class DeNode {
        private T item;
        private DeNode prev;
        private DeNode next;

        DeNode(T value) {
            item = value;
            prev = null;
            next = null;
        }
        DeNode(T value, DeNode p,DeNode n) {
            item = value;
            prev = p;
            next = n;
            p.next = this;
            n.prev = this;
        }
        public DeNode(DeNode p, T value) {
            item = value;
            prev = p;
            next = null;
            p.next = this;
        }
    }
    private DeNode Sentinel;
    private int size;
    public LinkedListDeque() {
        Sentinel = new DeNode(null);
        Sentinel.prev = Sentinel;
        Sentinel.next = Sentinel;
        size = 0;
    }

    @Override
    public void addFirst(T item) {
        DeNode first =  new DeNode(item, Sentinel, Sentinel.next);
        size += 1;
    }

    @Override
    public void addLast(T item) {
        DeNode last = new DeNode(item, Sentinel.prev, Sentinel);
        size += 1;
    }

    @Override
    public T removeFirst(){
        if (size == 0) {
            return null;
        }
        DeNode first = Sentinel.next;
        Sentinel.next = first.next;
        first.next.prev = Sentinel;
        size -= 1;
        return first.item;
    }

    @Override
    public T removeLast() {
        if (size == 0) {
            return null;
        }
        DeNode last = Sentinel.prev;
        Sentinel.prev = last.prev;
        last.prev.next = Sentinel;
        size -= 1;
        return last.item;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        DeNode curr = Sentinel.next;
        while (curr != Sentinel) {
            if (curr.next == Sentinel) {
                System.out.println(curr.item);
            }else {
                System.out.print(curr.item + " ");
            }
            curr = curr.next;
        }
    }

    @Override
    public T get(int index){
        DeNode curr = Sentinel.next;
        for (int i = 0; i < index; i++) {
            if (curr == Sentinel) {
                return null;
            }
            curr = curr.next;
        }
        return curr.item;
    }

    public T getRecursive(int index) {
        return getHelper(Sentinel.next, index);
    }

    private T getHelper(DeNode n, int index) {
        if (n == Sentinel) {
            return null;
        }else if (index == 0) {
            return n.item;
        }else {
            return getHelper(n.next, index - 1);
        }
    }

    public Iterator<T> iterator() {
        return new LinkedListDequeIterator();
    }

    private class LinkedListDequeIterator implements Iterator<T> {
        private int nextPos;

        public LinkedListDequeIterator() {
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
        }else if (o instanceof Deque) {
            Deque other = (Deque) o;
            if (other.size() != this.size()) {
                return false;
            }
            for (int i = 0; i < size; i++) {
                if (this.get(i).equals(other.get(i))) {
                    return false;
                }
            }
            return true;
        }

        return false;
     }

}
