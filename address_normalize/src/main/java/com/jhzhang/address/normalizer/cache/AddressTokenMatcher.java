package com.jhzhang.address.normalizer.cache;

import com.jhzhang.address.normalizer.cache.structure.AddressNode;
import com.jhzhang.address.normalizer.cache.structure.AddressTree;
import com.jhzhang.address.normalizer.cache.tool.ParseAddressXml;
import com.jhzhang.address.normalizer.common.Level;
import com.jhzhang.address.normalizer.common.collection.AhoCorasickDoubleArrayTrie;
import com.jhzhang.address.normalizer.common.collection.CommonAhoCorasickSegmentUtil;
import com.jhzhang.address.normalizer.common.collection.ResultTerm;
import com.jhzhang.address.normalizer.cache.structure.AddressNode;
import com.jhzhang.address.normalizer.cache.structure.AddressTree;
import com.jhzhang.address.normalizer.cache.tool.ParseAddressXml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

/**
 * 利用概率模型进行地址切分的查找器
 *
 * @author jhZhang
 * @date 2018/6/12
 */
public class AddressTokenMatcher {
    private static volatile AddressTokenMatcher instance = null;
    private AddressTree addressTree;
    private AhoCorasickDoubleArrayTrie<List<AddressNode>> trie;

    private AddressTokenMatcher() {
        addressTree = new AddressTree();
        trie = new AhoCorasickDoubleArrayTrie<>();
    }

    public static AddressTokenMatcher getInstance() {
        if (instance == null) {
            synchronized (AddressTokenMatcher.class) {
                if (instance == null) {
                    instance = new AddressTokenMatcher();
                }
            }
        }
        return instance;
    }

    /**
     * 判断两个AddressNode结点是不是在同一个trie树下
     *
     * @param pNode 父结点
     * @param cNode 子结点
     * @return
     */
    public static boolean isSameTrie(AddressNode pNode, AddressNode cNode) {
        if (cNode == null || pNode == null) {
            return false;
        }
        int levelDiff = cNode.getLevel().ordinal() - pNode.getLevel().ordinal();
        AddressNode pFindNode = cNode;
        while (levelDiff > 0) {
            pFindNode = cNode.getParentAddressNode();
            levelDiff--;
        }
        if (pFindNode.equals(pNode)) {
            return true;
        }
        return false;
    }

    public void init(String addressDataPath, String addressLevelPath) {
        try {
            init(new FileInputStream(new File(addressDataPath)), new FileInputStream(new File(addressLevelPath)));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void init(InputStream addressDataStream, InputStream addressLevelStream) {
        //读取地名库
        addressTree = ParseAddressXml.createAddressTree(addressDataStream);

        //初始化等级后缀库
        ParseAddressXml.initLevelWithSuffix(addressLevelStream);

        //初始化自动机，将包含地名关键字和地名对应地址节点的map当做参数传给自动机
        TreeMap<String, List<AddressNode>> keyWithNode = addressTree.getAllNodes();
        trie.build(keyWithNode);
    }

    /**
     * 根据输入的待确定的字符串，返回切词后的结果.
     *
     * @param text  待确定的字符串
     * @param level 过滤掉不同的等级
     * @return 所有符合条件的地址结点数量
     */
    public List<AddressNode> keywordsMatch(String text, Level level) {
        List<AddressNode> nodesList = new ArrayList<>();
        LinkedList<ResultTerm<List<AddressNode>>> segResultList = CommonAhoCorasickSegmentUtil
                .segment(text, trie);
        AddressNode node;
        for (ResultTerm<List<AddressNode>> result : segResultList) {
            if (result.label != null) {
                for (int i = 0; i < result.label.size(); i++) {
                    node = result.label.get(i);
                    if (node.getLevel().equals(level)) {
                        nodesList.add(node);
                    }
                }
            }
        }
        return nodesList;
    }
}
