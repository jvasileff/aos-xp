package org.anodyneos.xpImpl.runtime;

import java.util.EmptyStackException;

final class IntStack {
    private int[] elementData;
    private int size = 0;

    public IntStack(int initialCapacity) {
        elementData = new int[initialCapacity];
    }

    public IntStack() {
        this(32);
    }

    public void push(int value) {
        ensureCapacity(size + 1);
        elementData[size++] = value;
    }

    public int pop() throws EmptyStackException {
        int ret = peek();
        size--;
        return ret;
    }

    public int peek() throws EmptyStackException {
        if (size == 0) {
            throw new EmptyStackException();
        } else {
            return elementData[size -1];
        }
    }

    public boolean empty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void ensureCapacity(int minCapacity) {
        int oldCapacity = elementData.length;
        if (minCapacity > oldCapacity) {
            int[] oldData = elementData;
            int newCapacity = (oldCapacity * 3)/2 + 1;
            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }
            elementData = new int[newCapacity];
            System.arraycopy(oldData, 0, elementData, 0, size);
        }
    }
}
