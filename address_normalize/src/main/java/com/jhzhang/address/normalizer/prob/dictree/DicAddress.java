/*
 * Created on 2004-9-12
 *
 */
package com.jhzhang.address.normalizer.prob.dictree;

import com.jhzhang.address.normalizer.prob.bean.AddTypes;
import com.jhzhang.address.normalizer.prob.bean.AddressType;
import com.jhzhang.address.normalizer.prob.common.DicAddressTool;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;


/**
 * basic dictionary
 *
 * @author Administrator
 */
@SuppressWarnings("ALL")
public class DicAddress {
    private static DicAddress dicCore = null;
    /**
     * 根结点
     */
    public TSTNode root;
    /**
     * 所有的字符的权重之和
     */
    public long n = 0;

    public static DicAddress getInstance() {
        if (dicCore == null) {
            dicCore = new DicAddress();
        }
        return dicCore;
    }


    /**
     * Constructs a Ternary Search Trie and loads dataType from a <code>File</code>
     * into the Trie. The file is a normal text document, where each line is of
     * the form word : integer.
     * <p>使用格式如下 <b>地址名:地址编码:权重:类型</b></p>
     *
     * @param filePath The <code>filePath</code> with the dataType path to load into the Trie.
     * @throws IOException A problem occured while reading the dataType.
     */
    public void load(String filePath, AddressType type, int weight) {
        String line = null;
        String key = null;
        long code = 0;
        AddressType parseType = type;
        int parseWeight = weight;
        BufferedReader in = DicAddressTool.getBufferedReader(filePath);
        if (in != null) {
            try {
                while ((line = in.readLine()) != null) {
                    // 设计以“#”开头的行不读取，注释
                    if (line.startsWith("#")) {
                        continue;
                    }
                    StringTokenizer st = new StringTokenizer(line, ":");
                    key = null;
                    if (st.hasMoreTokens()) {
                        key = st.nextToken();
                    }
                    // 地理编码，如果没有就显示0
                    if (st.hasMoreTokens()) {
                        try {
                            code = Long.parseLong(st.nextToken());
                        } catch (NumberFormatException e) {
                            code = 0;
                        }
                    }
                    // 权重，如果没有就使用默认的
                    parseWeight = weight;
                    if (st.hasMoreTokens()) {
                        try {
                            parseWeight = Integer.valueOf(st.nextToken());
                        } catch (IllegalArgumentException e) {
                            continue;
                        }
                    }
                    // 地址类型，如果没有就使用默认的
                    parseType = type;
                    if (st.hasMoreTokens()) {
                        try {
                            parseType = AddressType.valueOf(st.nextToken());
                        } catch (IllegalArgumentException e) {
                            continue;
                        }
                    }
                    addWord(key, code, parseType, parseWeight);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 添加单个词典
     *
     * @param key  关键词
     * @param code 编码
     * @param type 类型
     * @param w    权重
     */
    void addWord(String key, long code, AddressType type, int w) {
        if (key == null || "".equals(key)) {
            return;
        }
        if (root == null) {
            root = new TSTNode(key.charAt(0));
        }
        n += w;
        AddTypes.AddressTypeInf pi = new AddTypes.AddressTypeInf(type, w, code);

        TSTNode node = null;
        if (root != null) {
            TSTNode currentNode = root;
            int charIndex = 0;
            while (true) {
                if (currentNode == null) {
                    break;
                }
                int charComp = (key.charAt(charIndex) - currentNode.splitchar);
                if (charComp == 0) {
                    charIndex++;
                    if (charIndex == key.length()) {
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
            AddTypes occur2 = null;
            if (node != null) {
                occur2 = node.data;
            }
            if (occur2 != null) {
                occur2.insert(pi);
                return;
            }
            //TODO: 去掉对getOrCreateNode的调用
            currentNode = getOrCreateNode(key);

            AddTypes occur3 = currentNode.data;
            if (occur3 != null) {
                occur3.insert(pi);
            } else {
                AddTypes occur = new AddTypes();
                occur.put(pi);
                currentNode.data = occur;
            }
        }
    }

    /**
     * 匹配字符串在起始位置在地址树中状态
     *
     * @param key    关键词
     * @param offset 起始偏移
     * @return
     */
    public ArrayList<MatchRet> matchAll(String key, int offset) {
        ArrayList<MatchRet> matchRets = new ArrayList<>();
        if (key == null || root == null || "".equals(key)
                || offset >= key.length()) {
            return matchRets;
        }
        int ret = offset;
        AddressType retPOS = null;
        int retWeight = 0;

        ret = DicAddressTool.matchNum(offset, key);
        // 如果匹配到了数字
        if (ret > offset) {
            retPOS = AddressType.No;
            retWeight = 100;

            AddTypes.AddressTypeInf posInf = new AddTypes.AddressTypeInf(retPOS,
                    retWeight, 0);
            AddTypes addressData = new AddTypes();
            addressData.put(posInf);
            MatchRet matchRet = new MatchRet(ret, addressData);
            matchRets.add(matchRet);
        }

        int retEnglishNum = DicAddressTool.matchEnglish(offset, key);
        // 如果匹配中了字母
        if (retEnglishNum > ret) {
            ret = retEnglishNum;
            retPOS = AddressType.Symbol;
            retWeight = 100;
            AddTypes.AddressTypeInf posInf = new AddTypes.AddressTypeInf(retPOS,
                    retWeight, 0);
            AddTypes addressData = new AddTypes();
            addressData.put(posInf);
            MatchRet matchRet = new MatchRet(ret, addressData);
            matchRets.add(matchRet);
        }

        TSTNode currentNode = root;
        int charIndex = offset;
        while (true) {
            if (currentNode == null) {
                return matchRets;
            }
            int charComp = key.charAt(charIndex) - currentNode.splitchar;

            if (charComp == 0) {
                charIndex++;

                if (currentNode.data != null) {
                    ret = charIndex;
                    MatchRet matchRet = new MatchRet(ret, currentNode.data);
                    matchRets.add(matchRet);
                }
                if (charIndex == key.length()) {
                    return matchRets;
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
    protected TSTNode getOrCreateNode(String key) throws NullPointerException,
            IllegalArgumentException {
        if (key == null) {
            throw new NullPointerException(
                    "attempt to get or create node with null key");
        }
        if ("".equals(key)) {
            throw new IllegalArgumentException(
                    "attempt to get or create node with key of zero length");
        }
        if (root == null) {
            root = new TSTNode(key.charAt(0));
        }
        TSTNode currentNode = root;
        int charIndex = 0;
        while (true) {
            int charComp = (key.charAt(charIndex) - currentNode.splitchar);
            if (charComp == 0) {
                charIndex++;
                if (charIndex == key.length()) {
                    return currentNode;
                }
                if (currentNode.eqKID == null) {
                    currentNode.eqKID = new TSTNode(key.charAt(charIndex));
                }
                currentNode = currentNode.eqKID;
            } else if (charComp < 0) {
                if (currentNode.loKID == null) {
                    currentNode.loKID = new TSTNode(key.charAt(charIndex));
                }
                currentNode = currentNode.loKID;
            } else {
                if (currentNode.hiKID == null) {
                    currentNode.hiKID = new TSTNode(key.charAt(charIndex));
                }
                currentNode = currentNode.hiKID;
            }
        }
    }
}
