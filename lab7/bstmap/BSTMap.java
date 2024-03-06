package bstmap;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V>{

    private MapNode root;
    private int size;
    private LinkedHashSet<K> cachedKeySet;

    public BSTMap(){
        root = null;
        size = 0;
        cachedKeySet = new LinkedHashSet<>();
    }

    public void clear(){
        root = null;
        size = 0;
        cachedKeySet.clear();
    }

    public boolean containsKey(K key){
        return containsKey(root, key);
    }

    private boolean containsKey(MapNode n, K key){
        if (n == null) {
            return false;
        } else if (n.key.compareTo(key) == 0) {
            return true;
        } else {
            return containsKey(n.left, key) || containsKey(n.right, key);
        }
    }

    public V get(K key){
        return get(root, key);
    }

    private V get(MapNode n, K key){
        if (n == null) {
            return null;
        } else if (n.key.compareTo(key) == 0) {
            return n.value;
        } else if (n.key.compareTo(key) > 0) {
            return get(n.left, key);
        } else {
            return get(n.right, key);
        }
    }

    public int size(){
        return size;
    }

    public void put(K key, V value) {
        if (root == null) {
            root = new MapNode(key, value);
            size += 1;
            cachedKeySet.add(key);
        } else {
            put(root, key, value);
        }
    }

    private void put(MapNode n, K key, V value) {
        if (n.key == key) {
            n.value = value;
        } else if (n.key.compareTo(key) > 0) {
            if (n.left == null) {
                n.left = new MapNode(key, value);
                size += 1;
                cachedKeySet.add(key);
            } else {
                put(n.left, key, value);
            }
        } else {
            if (n.right == null) {
                n.right = new MapNode(key, value);
                size += 1;
                cachedKeySet.add(key);
            } else {
                put(n.right, key, value);
            }
        }
    }

    public Set<K> keySet(){
        return cachedKeySet;
    }

    public V remove(K key){
        throw new UnsupportedOperationException();
    }

    public V remove(K key, V value){
        throw new UnsupportedOperationException();
    }

    public Iterator<K> iterator(){
        throw new UnsupportedOperationException();
    }

    public void printInOrder(){
        printInOrder(root);
    }

    private void printInOrder(MapNode n) {
        if(n == null) {
            return;
        } else {
            printInOrder(n.left);
            System.out.println("Key: " + n.key + ", value: " + n.value);
            printInOrder(n.right);
        }
    }


    private class MapNode{
        public K key;
        public V value;
        public MapNode left;
        public MapNode right;

        public MapNode(K k, V v){
            key = k;
            value = v;
            left = right = null;
        }
    }
}
