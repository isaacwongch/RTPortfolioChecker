package org.rtportfolio.ds;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

public class SPSCQueue<E> implements Queue<E> {
    private final E[] buffer;

    private volatile long tail = 0;
    private volatile long head = 0;

    public SPSCQueue(final int capacity){
        buffer = (E[]) new Object[capacity];
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public Iterator<E> iterator() {
        return null;
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return null;
    }

    @Override
    public boolean add(E e) {
        return false;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {

    }

    @Override
    public boolean offer(E e) {
        if (e == null){
            throw new NullPointerException("Null is not a valid element");
        }
        final long currentTail = tail;
        final long wrapPoint = currentTail - buffer.length;
        if (head <= wrapPoint) {
            return false;
        }

        buffer[(int) (currentTail % buffer.length)] = e;
        tail = currentTail + 1;

        return true;
    }

    @Override
    public E remove() {
        return null;
    }

    @Override
    public E poll() {
        final long currentHead = head;
        if (currentHead >= tail) {
            return null;
        }

        final int index = (int) (currentHead % buffer.length);
        final E e = buffer[index];
        buffer[index] = null;
        head = currentHead + 1;

        return e;
    }

    @Override
    public E element() {
        return null;
    }

    @Override
    public E peek() {
        return null;
    }
}
