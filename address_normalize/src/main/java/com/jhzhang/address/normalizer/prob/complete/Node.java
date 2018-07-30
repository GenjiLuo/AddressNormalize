package com.jhzhang.address.normalizer.prob.complete;


/**
 * @author jhZhang
 * @date 2018/6/12
 */
class Node<T> {
    T item;
    Node next;

    Node(T item) {
        this.item = item;
        next = null;
    }
}