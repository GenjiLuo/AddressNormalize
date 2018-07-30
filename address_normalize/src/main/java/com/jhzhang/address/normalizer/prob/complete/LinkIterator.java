package com.jhzhang.address.normalizer.prob.complete;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author jhZhang
 * @date 2018/6/12
 */
public class LinkIterator<T> implements Iterator<T> {
    /**
     * 记录当前节点
     */
    Node<T> current;

    public LinkIterator(Node<T> node) {
        current = node;
    }

    @Override
    public boolean hasNext() {
        return current != null;
    }

    @Override
    public T next() {
        if (current == null) {
            throw new NoSuchElementException();
        }
        Node<T> cur = current;
        current = current.next;
        return cur.item;
    }

    @Override
    public void remove() {
        throw new NoSuchElementException();
    }
}
