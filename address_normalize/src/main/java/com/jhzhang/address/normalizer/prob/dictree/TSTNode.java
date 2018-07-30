package com.jhzhang.address.normalizer.prob.dictree;

import com.jhzhang.address.normalizer.prob.bean.AddTypes;

/**
 * An inner class of Ternary Search Trie that represents a node in the trie.
 * @author Administrator
 */
public final class TSTNode {
    /**
     * The key to the node.
     */
    public AddTypes data = null;

    /**
     * The relative nodes.
     */
    protected TSTNode loKID;
    protected TSTNode eqKID;
    protected TSTNode hiKID;

    /**
     * The char used in the split.
     */
    protected char splitchar;

    /**
     * Constructor method.
     *
     * @param splitchar The char used in the split.
     */
    protected TSTNode(char splitchar) {
        this.splitchar = splitchar;
    }

    @Override
    public String toString() {
        return "splitChar:" + splitchar;
    }
}