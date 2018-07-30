package com.jhzhang.address.normalizer.prob.structure;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 单词类的线性列表类
 *
 * @author Administer
 * @2010-3-18
 */
public class AddressTokenLinkedList implements Iterable<AddTokenInf> {
    private Node head, tail;

    public AddressTokenLinkedList() {
        head = null;
        tail = null;
    }

    public void put(AddTokenInf item) {
        Node t = tail;
        tail = new Node(item);
        if (head == null) {
            head = tail;
        } else {
            t.next = tail;
        }
    }

    public Node getHead() {
        return head;
    }

    public int size() {
        int count = 0;

        Node t = head;
        while (t != null) {
            count++;
            t = t.next;
        }

        return count;
    }

    @Override
    public Iterator<AddTokenInf> iterator() {
        return new LinkIterator(head);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        Node pCur = head;

        while (pCur != null) {
            buf.append(pCur.item.toString());
            buf.append('\t');
            pCur = pCur.next;
        }

        return buf.toString();
    }

    public static class Node {
        public AddTokenInf item;
        public Node next;

        Node(AddTokenInf item) {
            this.item = item;
            next = null;
        }
    }

    private class LinkIterator implements Iterator<AddTokenInf> {
        Node itr;

        public LinkIterator(Node begin) {
            itr = begin;
        }

        @Override
        public boolean hasNext() {
            return itr != null;
        }

        @Override
        public AddTokenInf next() {
            if (itr == null) {
                throw new NoSuchElementException();
            }
            Node cur = itr;
            itr = itr.next;
            return cur.item;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
