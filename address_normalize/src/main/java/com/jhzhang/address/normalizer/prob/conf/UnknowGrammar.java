package com.jhzhang.address.normalizer.prob.conf;

import com.jhzhang.address.normalizer.prob.bean.AddressSpan;
import com.jhzhang.address.normalizer.prob.bean.AddressType;
import com.jhzhang.address.normalizer.prob.structure.AddressToken;
import com.jhzhang.address.normalizer.prob.structure.AddressToken;

import java.util.ArrayList;

/**
 * 未登录地名识别规则
 *
 * @author jhzhang
 * @date 2018/06/07 星期四
 */
public class UnknowGrammar {

    private static UnknowGrammar dicGrammar = null;
    /**
     * 根节点
     */
    public TSTNode root;

    /**
     * @param key    输入的地址数据
     * @param offset 偏移量
     * @param spans  匹配中的规则
     */
    public static void replace(ArrayList<AddressToken> key, int offset,
                               ArrayList<AddressSpan> spans) {
        int j = 0;
        for (int i = offset; i < key.size(); ++i) {
            AddressSpan span = spans.get(j);
            AddressToken token = key.get(i);
            StringBuilder newText = new StringBuilder();
            int newStart = token.start;
            int newEnd = token.end;
            AddressType newType = span.type;

            for (int k = 0; k < span.length; ++k) {
                token = key.get(i + k);
                newText.append(token.termText);
                newEnd = token.end;
            }
            AddressToken newToken = new AddressToken(newStart, newEnd, newText
                    .toString(), newType);

            for (int k = 0; k < span.length; ++k) {
                key.remove(i);
            }
            key.add(i, newToken);
            j++;
            if (j >= spans.size()) {
                return;
            }
        }
    }

    /**
     * @return the singleton of basic dictionary
     */
    public static UnknowGrammar getInstance() {
        if (dicGrammar == null) {
            dicGrammar = new UnknowGrammar();
        }
        return dicGrammar;
    }


    public void addProduct(ArrayList<AddressType> key, ArrayList<AddressSpan> lhs) {
        if (root == null) {
            root = new TSTNode(key.get(0));
        }
        TSTNode node = null;
        if (key.size() > 0 && root != null) {
            TSTNode currentNode = root;
            int charIndex = 0;
            while (true) {
                if (currentNode == null) {
                    break;
                }
                int charComp = key.get(charIndex).compareTo(
                        currentNode.splitChar);
                if (charComp == 0) {
                    charIndex++;
                    if (charIndex == key.size()) {
                        node = currentNode;
                        break;
                    }
                    currentNode = currentNode.eqKID;
                } else if (charComp < 0) {
                    currentNode = currentNode.loKID;
                } else {
                    currentNode = currentNode.hiKID;
                }
            }
            ArrayList<AddressSpan> occur2 = null;
            if (node != null) {
                occur2 = node.data;
            }
            if (occur2 != null) {
                return;
            }
            currentNode = getOrCreateNode(key);
            currentNode.data = lhs;
        }
    }

    /**
     * 从offset位置依次查找匹配的模式
     *
     * @param key    待匹配的内容
     * @param offset 起始偏移量
     * @return
     */
    public ArrayList<AddressSpan> matchLong(ArrayList<AddressToken> key, int offset) {
        // 列表的长度
        int len = key.size();
        if (key == null || root == null || "".equals(key)
                || offset >= len) {
            return null;
        }
        int ret = offset;
        // 返回结果
        ArrayList<AddressSpan> retPOS = null;

        TSTNode currentNode = root;
        int charIndex = offset;
        while (true) {
            if (currentNode == null) {
                return retPOS;
            }
            int charComp = key.get(charIndex).type.compareTo(currentNode.splitChar);

            if (charComp == 0) {
                charIndex++;

                if (currentNode.data != null && charIndex > ret) {
                    ret = charIndex;
                    retPOS = currentNode.data;
                }
                if (charIndex == len) {
                    return retPOS;
                }
                currentNode = currentNode.eqKID;
            } else if (charComp < 0) {
                currentNode = currentNode.loKID;
            } else {
                currentNode = currentNode.hiKID;
            }
        }
    }

    /**
     * Returns the node indexed by key, creating that node if it doesn't exist,
     * and creating any required intermediate nodes if they don't exist.
     *
     * @param key A <code>String</code> that indexes the node that is returned.
     * @return The node object indexed by key. This object is an instance of an
     * inner class named <code>TernarySearchTrie.TSTNode</code>.
     * @throws NullPointerException     If the key is <code>null</code>.
     * @throws IllegalArgumentException If the key is an empty <code>String</code>.
     */
    protected TSTNode getOrCreateNode(ArrayList<AddressType> key)
            throws NullPointerException, IllegalArgumentException {
        if (key == null) {
            throw new NullPointerException(
                    "attempt to get or create node with null key");
        }
        if ("".equals(key)) {
            throw new IllegalArgumentException(
                    "attempt to get or create node with key of zero length");
        }
        if (root == null) {
            root = new TSTNode(key.get(0));
        }
        TSTNode currentNode = root;
        int charIndex = 0;
        while (true) {
            int charComp = key.get(charIndex).compareTo(currentNode.splitChar);
            if (charComp == 0) {
                charIndex++;
                if (charIndex == key.size()) {
                    return currentNode;
                }
                if (currentNode.eqKID == null) {
                    currentNode.eqKID = new TSTNode(key.get(charIndex));
                }
                currentNode = currentNode.eqKID;
            } else if (charComp < 0) {
                if (currentNode.loKID == null) {
                    currentNode.loKID = new TSTNode(key.get(charIndex));
                }
                currentNode = currentNode.loKID;
            } else {
                if (currentNode.hiKID == null) {
                    currentNode.hiKID = new TSTNode(key.get(charIndex));
                }
                currentNode = currentNode.hiKID;
            }
        }
    }


    /**
     * An inner class of Ternary Search Trie that represents a node in the trie.
     */
    public final class TSTNode {

        /**
         * The key to the node.
         */
        public ArrayList<AddressSpan> data = null;

        /**
         * The relative nodes.
         */
        protected TSTNode loKID;
        protected TSTNode eqKID;
        protected TSTNode hiKID;

        /**
         * The char used in the split.
         */
        protected AddressType splitChar;

        /**
         * Constructor method.
         *
         * @param splitChar The char used in the split.
         */
        protected TSTNode(AddressType splitChar) {
            this.splitChar = splitChar;
        }

        @Override
        public String toString() {
            return "splitChar:" + splitChar;
        }
    }
}
