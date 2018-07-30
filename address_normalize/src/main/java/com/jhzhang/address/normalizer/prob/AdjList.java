/*
 * Created on 2005-9-7
 *
 */
package com.jhzhang.address.normalizer.prob;

/**
 * 邻接矩阵表示的切分词图
 *
 * @author luogang
 * @2010-3-18
 */

import com.jhzhang.address.normalizer.prob.structure.AddTokenInf;
import com.jhzhang.address.normalizer.prob.structure.AddressTokenLinkedList;
import com.jhzhang.address.normalizer.prob.structure.AddTokenInf;
import com.jhzhang.address.normalizer.prob.structure.AddressTokenLinkedList;

import java.util.Iterator;

public class AdjList {
    public int verticesNum;
    /** AdjList 的邻接列表 */
    private AddressTokenLinkedList[] list;

    public AdjList(int verticesNum) {
        this.verticesNum = verticesNum;
        list = new AddressTokenLinkedList[verticesNum];
        // 初始化数组
        for (int index = 0; index < verticesNum; index++) {
            list[index] = new AddressTokenLinkedList();
        }
    }

    /**
     * 往有向图中增加一条边
     */
    public void addEdge(AddTokenInf newEdge) {
        list[newEdge.end].put(newEdge);
    }

    /**
     * Returns an iterator that contains the Edges leading to adjacent nodes
     */
    public Iterator<AddTokenInf> getPrev(int vertex) {
        AddressTokenLinkedList ll = list[vertex];
        if (ll == null) {
            return null;
        }
        return ll.iterator();
    }

    /**
     * Feel free to make this prettier, if you'd like
     */
    @Override
    public String toString() {
        StringBuilder temp = new StringBuilder();
        for (int index = 0; index < verticesNum; index++) {
            if (list[index] == null) {
                continue;
            }
            temp.append("node:");
            temp.append(index);
            temp.append(": ");
            temp.append(list[index].toString());
            temp.append("\n");
        }
        return temp.toString();
    }
}