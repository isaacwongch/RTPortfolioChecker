package org.rtportfolio.ds;

import org.rtportfolio.util.RTObjectCreator;

import java.util.ArrayDeque;
import java.util.Queue;

public class RTObjectPool<E> {
    private final Queue<E> objectPool;
    private final int maxAllocNum;
    private final RTObjectCreator<E> objectCreator;

    private int currentSize = 0;
    public RTObjectPool(final int preAllocateNum, final int maxAllocNum, final RTObjectCreator<E> objectCreator){
        this.maxAllocNum = maxAllocNum;
        this.objectPool = new ArrayDeque<>(maxAllocNum);
        this.objectCreator = objectCreator;
        for (int i = 0; i < preAllocateNum; i++){
            objectPool.offer(objectCreator.create());
            currentSize++;
        }
    }

    public E get(){
        E obj = objectPool.poll();
        if (obj != null){
            currentSize--;
            return obj;
        }
        return objectCreator.create();
    }

    public void free(E object){
        if (object != null && maxAllocNum > currentSize){
            objectPool.offer(object);
        }
    }
}
