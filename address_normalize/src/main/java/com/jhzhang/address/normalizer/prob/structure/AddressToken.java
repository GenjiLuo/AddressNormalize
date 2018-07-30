/*
 * Created on 2005-9-7
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.jhzhang.address.normalizer.prob.structure;

import com.jhzhang.address.normalizer.prob.bean.AddTypes;
import com.jhzhang.address.normalizer.prob.bean.AddressType;

import java.util.Iterator;

/**
 * 有向边，边的权重不能是负数。
 * <p>
 * This class models an edge from vertex X to vertex Y -- pretty
 * straightforward.
 * <p>
 * It is a little wasteful to keep the starting vertex, since the adjacency list
 * will do this for us -- but it makes the code neater in other places (makes
 * the Edge independent of the Adj. List
 *
 * @author Administrator
 */
public class AddressToken {
    /** 词内容 */
    public String termText;
    /** 地址类型 */
    public AddressType type;
    /** 起始位置 */
    public int start;
    /** 终止位置 */
    public int end;
    /** 权重 */
    public int cost;
    /** 编码 */
    public long code;

    /**
     * @param vertexFrom 有向边起始位置
     * @param vertexTo   有向边终止位置
     * @param word       关键词
     * @param typ        地址类型
     */
    public AddressToken(int vertexFrom, int vertexTo, String word, AddressType typ) {
        start = vertexFrom;
        end = vertexTo;
        termText = word;
        type = typ;
    }

    /**
     * @param word 关键词
     * @param code 地址编码
     */
    public AddressToken(int vertexFrom, int vertexTo, int cost, String word, AddressType type, long code) {
        this(vertexFrom, vertexTo, word, type);
        this.cost = cost;
        this.code = code;
    }

    /**
     * 从一个<code>AddTokenInf</code>对象中，解析出指定类型的<code>AddressToken</code>对象
     *
     * @param addTokenInf 待解析的对象
     * @param type        指定的地址类型
     * @return 解析出的结果
     */
    public static AddressToken valueOf(AddTokenInf addTokenInf, AddressType type) {
        String termText = addTokenInf.termText;
        int start = addTokenInf.start;
        int end = addTokenInf.end;
        long code = 0L;
        int cost = 0;
        AddTypes addTypes = addTokenInf.dataType;
        // 找出标签中符合要求的地址片段信息
        for (Iterator<AddTypes.AddressTypeInf> it = addTypes.iterator(); it.hasNext(); ) {
            AddTypes.AddressTypeInf typeInf = it.next();
            if (typeInf.pos.equals(type)) {
                code = typeInf.code;
                cost = typeInf.weight;
                break;
            }
        }
        return new AddressToken(start, end, cost, termText, type, code);
    }


    @Override
    public String toString() {
        return termText + "|" + type;
    }
}