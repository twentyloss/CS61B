package deque;

public class LinkedListDeque<T> {
    private class DeNode{
        public T item;
        public DeNode prev;
        public DeNode next;

        public DeNode(T value) {
            item = value;
            prev = null;
            next = null;
        }
        public DeNode(T value, DeNode p,DeNode n) {
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
    public LinkedListDeque(){
        Sentinel = new DeNode(null);
        Sentinel.prev = Sentinel;
        Sentinel.next = Sentinel;
        size = 0;
    }
    public LinkedListDeque(T value){
        Sentinel = new DeNode(null);
        Sentinel.next = new DeNode(value);
        Sentinel.prev = Sentinel.next;
        Sentinel.next.prev = Sentinel;
        size = 1;
    }
    public void addFirst(T item){
        DeNode first =  new DeNode(item,Sentinel ,Sentinel.next);
        size += 1;
    }

    public void addLast(T item){
        DeNode last = new DeNode(item,Sentinel.prev, Sentinel);
        size += 1;
    }

    public T removeFirst(){
        if (size == 0){
            return null;
        }
        DeNode first = Sentinel.next;
        Sentinel.next = first.next;
        first.next.prev = Sentinel;
        size -= 1;
        return first.item;
    }

    public T removeLast(){
        if (size == 0){
            return null;
        }
        DeNode last = Sentinel.prev;
        Sentinel.prev = last.prev;
        last.prev.next = Sentinel;
        size -= 1;
        return last.item;
    }

    public boolean isEmpty(){
        return size == 0;
    }

    public int size(){
        return size;
    }
    public void printDeque(){
        DeNode curr = Sentinel.next;
        while(curr != Sentinel){
            if(curr.next == Sentinel) {
                System.out.println(curr.item);
            }
            else{
                System.out.print(curr.item + " ");
            }
            curr = curr.next;
        }
    }

    public T get(int index){
        DeNode curr = Sentinel.next;
        for(int i = 0; i < index; i++){
            if(curr == Sentinel){
                return null;
            }
            curr = curr.next;
        }
        return curr.item;
    }
    public T getRecursive(int index){
        return getHelper(Sentinel.next, index);
    }
    private T getHelper(DeNode n, int index){
        if(n == Sentinel){
            return null;
        }else if (index == 0){
            return n.item;
        }else{
            return getHelper(n.next, index - 1);
        }
    }
}
