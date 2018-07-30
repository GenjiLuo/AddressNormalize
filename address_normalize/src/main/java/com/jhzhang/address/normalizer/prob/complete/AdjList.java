package com.jhzhang.address.normalizer.prob.complete;

import java.util.Iterator;
import java.util.List;

/**
 * 构造一个邻接矩阵
 *
 * @author jhZhang
 * @date 2018/6/14
 */
public class AdjList<T> {
    public final int verticesNum;
    LinkList<T>[] values = null;

    public AdjList(int num) {
        verticesNum = num;
        // 初始化
        values = new LinkList[verticesNum];
        for (int i = 0; i < verticesNum; i++) {
            values[i] = new LinkList<>();
        }
    }

    /**
     * 增加一个结点
     *
     * @param item
     * @param index
     */
    public void addEdge(T item, int index) {
        if (index < 0 || index > verticesNum) {
            throw new IndexOutOfBoundsException("超出了插入的界限");
        }
        values[index].put(item);
    }

    /**
     * 增加多个结点
     *
     * @param items
     * @param index
     */
    public void putAll(List<T> items, int index) {
        if (index < 0 || index > verticesNum) {
            throw new IndexOutOfBoundsException("超出了插入的界限");
        }
        for (T item : items) {
            values[index].put(item);
        }
    }


    /**
     * @return 返回所有的元素
     */
    public LinkList<T>[] getValues() {
        return values;
    }

    /**
     * @param indexNum 级别
     * @return 返回某一级别元素
     */
    public LinkList<T> get(int indexNum) {
        if (indexNum < 0 || indexNum > verticesNum) {
            throw new IndexOutOfBoundsException("超出了插入的界限");
        }
        return values[indexNum];
    }

    /**
     * @param vertex 索引
     * @return 返回某一级别的迭代器
     */
    public Iterator<T> getIter(int vertex) {
        if (vertex < 0 || vertex > verticesNum) {
            throw new IndexOutOfBoundsException("超出了插入的界限");
        }
        LinkList<T> ll = values[vertex];
        if (ll == null) {
            return null;
        }
        return ll.iterator();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (LinkList<T> value : values) {
            if (value.size() > 0) {
                sb.append(value.toString());
            }
            sb.append(",");
        }
        sb.append("}");
        return sb.toString();
    }
}
