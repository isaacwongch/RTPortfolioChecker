package org.rtportfolio.ds;

import org.openjdk.jol.info.ClassLayout;
import sun.misc.Contended;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implement offer() and poll() only as this is what we need
 *
 * Run with -XX:-RestrictContended
 *
 * @param <E>
 */
public class SPSCQueue<E> implements Queue<E> {
    private final E[] buffer;
    @Contended
    private AtomicLong head = new AtomicLong(0);
    @Contended
    private AtomicLong tail = new AtomicLong(0);

    public SPSCQueue(final int capacity) {
        buffer = (E[]) new Object[findNextPositivePowerOfTwo(capacity)];
    }

    public static int findNextPositivePowerOfTwo(int value) {
        return 1 << 32 - Integer.numberOfLeadingZeros(value - 1);
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
        if (e == null) {
            throw new NullPointerException("Null is not a valid element");
        }
        final long currentTail = tail.get();
        final long wrapPoint = currentTail - buffer.length;
        if (head.get() <= wrapPoint) {
            return false;
        }

        buffer[(int) (currentTail & (buffer.length - 1))] = e;
        tail.lazySet(currentTail + 1);

        return true;
    }

    @Override
    public E remove() {
        return null;
    }

    @Override
    public E poll() {
        final long currentHead = head.get();
        if (currentHead >= tail.get()) {
            return null;
        }

        final int index = (int) (currentHead & (buffer.length - 1));
        final E e = buffer[index];
        buffer[index] = null;
        head.set(currentHead + 1);

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

    public static void main(String[] args) {
        System.out.println(ClassLayout.parseClass(SPSCQueue.class).toPrintable());
    }
}
