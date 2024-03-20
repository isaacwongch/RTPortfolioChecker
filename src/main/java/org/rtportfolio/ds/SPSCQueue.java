package org.rtportfolio.ds;

import org.openjdk.jol.info.ClassLayout;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

class AbstractSPSCQueue1 {
    protected long p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15;
}

class AbstractSPSCQueue2 extends AbstractSPSCQueue1 {
    protected AtomicLong head = new AtomicLong(0); //consumer
    protected long tailCache = 0L;
}

class AbstractSPSCQueue3 extends AbstractSPSCQueue2{
    protected long p16, p17, p18, p19, p20, p21, p22, p23, p24, p25, p26, p27, p28, p29, p30;
}

class AbstractSPSCQueue4 extends AbstractSPSCQueue3{
    protected AtomicLong tail = new AtomicLong(0); //producer
    protected long headCache = 0L;
}

class AbstractSPSCQueue5 extends AbstractSPSCQueue4{
    protected long p31, p32, p33, p34, p35, p36, p37, p38, p39, p40, p41, p42, p43, p44, p45;
}

public class SPSCQueue<E> extends AbstractSPSCQueue5 implements Queue<E> {
    private final E[] buffer;

    public SPSCQueue(final int capacity) {
        buffer = (E[]) new Object[findNextPositivePowerOfTwo(capacity)];
    }

    public static int findNextPositivePowerOfTwo(int value) {
        return 1 << 32 - Integer.numberOfLeadingZeros(value - 1);
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<E> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean offer(E e) {
        if (e == null) {
            throw new NullPointerException("Null is not a valid element");
        }
        final long currentTail = tail.get();
        final long wrapPoint = currentTail - buffer.length;
        if (headCache <= wrapPoint){
            headCache = head.get();
            if (headCache <= wrapPoint) {
                return false;
            }
        }
        buffer[(int) (currentTail & (buffer.length - 1))] = e;
        tail.lazySet(currentTail + 1);

        return true;
    }

    @Override
    public E remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E poll() {
        final long currentHead = head.get();
        if (currentHead >= tailCache){
            tailCache = tail.get();
            if (currentHead >= tailCache) {
                return null;
            }
        }

        final int index = (int) (currentHead & (buffer.length - 1));
        final E e = buffer[index];
        buffer[index] = null;
        head.lazySet(currentHead + 1);

        return e;
    }

    @Override
    public E element() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E peek() {
        throw new UnsupportedOperationException();
    }

    public static void main(String[] args) {
        System.out.println(ClassLayout.parseClass(SPSCQueue.class).toPrintable());
    }
}
