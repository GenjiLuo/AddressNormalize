package com.jhzhang.address.normalizer.prob.bean;

import java.util.Iterator;

/**
 * @author Administrator
 */
public class AddTypes implements Iterable<AddTypes.AddressTypeInf> {
    private Node head, tail;

    public AddTypes() {
        head = null;
        tail = null;
    }

    public void put(AddressTypeInf item) {
        Node t = tail;
        tail = new Node(item);
        if (head == null) {
            head = tail;
        } else {
            t.next = tail;
        }
    }

    public void insert(AddressTypeInf item) {
        // one element
        if (head == tail) {
            if (head.item.pos.compareTo(item.pos) > 0) {
                Node t = head;
                head = new Node(item);
                head.next = t;
            } else {
                Node t = tail;
                tail = new Node(item);
                t.next = tail;
            }
            return;
        }
        Node t = head;

        while (t.next != null) {
            if (t.next.item.pos.compareTo(item.pos) > 0) {
                break;
            }
            t = t.next;
        }
        Node p = t.next;
        t.next = new Node(item);
        t.next.next = p;
    }

    public Node getHead() {
        return head;
    }

    public AddressTypeInf findType(AddressType toFind) {
        if (head == null) {
            return null;
        }
        Node t = head;
        while (t != null && t.item != null) {
            if (t.item.pos.equals(toFind)) {
                return t.item;
            }
            t = t.next;
        }
        return null;
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

    public int totalCost() {
        int cost = 0;
        Node t = head;
        while (t != null) {
            cost += t.item.weight;
            t = t.next;
        }
        return cost;
    }

    @Override
    public Iterator<AddressTypeInf> iterator() {
        return new LinkIterator(head);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        Node pCur = head;
        while (pCur != null) {
            buf.append(pCur.item.toString());
            buf.append(' ');
            pCur = pCur.next;
        }
        return buf.toString();
    }

    public static class Node {
        public AddressTypeInf item;
        public Node next;

        Node(AddressTypeInf item) {
            this.item = item;
            next = null;
        }
    }

    /**
     * 地址类型信息，记录每个地址级别的详细信息
     */
    public static class AddressTypeInf {
        public AddressType pos;
        public int weight = 0;
        public long code;

        public AddressTypeInf(AddressType p, int f, long semanticCode) {
            pos = p;
            weight = f;
            code = semanticCode;
        }

        @Override
        public String toString() {
            return pos + ":" + weight;
        }
    }

    /**
     * Adapter to provide descending iterators via ListItr.previous
     */
    private class LinkIterator implements Iterator<AddressTypeInf> {
        Node itr;

        public LinkIterator(Node begin) {
            itr = begin;
        }

        @Override
        public boolean hasNext() {
            return itr != null;
        }

        @Override
        public AddressTypeInf next() {
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
