package com.jhzhang.address.normalizer.prob.complete;

import java.util.Iterator;

/**
 * 形参 T 会自动的包装成链表形式
 *
 * @author jhZhang
 * @date 2018/6/12
 */
public class LinkList<T> implements Iterable<T> {
    /** 头结点 */
    private Node<T> head;
    /** 尾结点 */
    private Node<T> tail;

    LinkList() {
        this.head = null;
        this.tail = null;
    }

    /**
     * @return 返回头结点
     */
    public Node<T> getHead() {
        return head;
    }

    /**
     * 添加一个元素到链表中，尾插法
     *
     * @param item
     */
    public void put(T item) {
        Node<T> node = new Node(item);
        if (head == null) {
            head = tail = node;
        } else {
            // 尾结点指向当前结点
            tail.next = node;
            tail = tail.next;
        }
    }

    public int size() {
        int len = 0;
        Node cur = head;
        while (cur != null) {
            len++;
            cur = cur.next;
        }
        return len;
    }

    /**
     * 移除所有元素
     */
    public void reset() {
        this.head = null;
        this.tail = null;
    }


    @Override
    public Iterator<T> iterator() {
        return new LinkIterator<>(head);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        Iterator<T> it = iterator();
        while (it.hasNext()) {
            sb.append(it.next()).append("→");
        }
        if (size() > 0) {
            // 删除最后一个箭头
            sb.setLength(sb.length() - 1);
        }
        sb.append("]");
        return sb.toString();
    }
}
