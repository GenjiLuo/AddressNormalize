package com.jhzhang.address.normalizer.prob.complete;

/**
 * @author jhZhang
 * @date 2018/6/20
 */
public final class ThreeSearchTrie<T> {
    /**
     * 结点的值
     */
    public T data;
    /**
     * 左结点
     */
    public ThreeSearchTrie<T> lNode;
    /**
     * 右结点
     */
    public ThreeSearchTrie<T> rNode;
    /**
     * 中间结点
     */
    public ThreeSearchTrie<T> eqNode;
    /**
     * 本结点所表示的字符
     */
    public char splitChar;

    public ThreeSearchTrie(char splitChar) {
        this.splitChar = splitChar;
    }

    @Override
    public String toString() {
        return "splitChar=" + splitChar + '}';
    }
}
