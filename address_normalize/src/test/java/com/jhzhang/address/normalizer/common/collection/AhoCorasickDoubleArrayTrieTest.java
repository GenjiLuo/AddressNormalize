package com.jhzhang.address.normalizer.common.collection;

import com.jhzhang.address.normalizer.cache.structure.AddressTree;
import com.jhzhang.address.normalizer.cache.tool.ParseAddressXml;
import com.jhzhang.address.normalizer.common.Level;
import com.jhzhang.address.normalizer.cache.structure.AddressTree;
import com.jhzhang.address.normalizer.cache.tool.ParseAddressXml;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.TreeMap;

public class AhoCorasickDoubleArrayTrieTest {
    private static AhoCorasickDoubleArrayTrie<Level> trie = new AhoCorasickDoubleArrayTrie<>();
    private static AddressTree addressTree = new AddressTree();

    @BeforeClass
    public static void setUp() throws Exception {
        // 转载所有的等级关键词
        ParseAddressXml.initLevelWithSuffix("src/main/resources/conf/addressSuffixLevelData.xml");
        addressTree = ParseAddressXml.createAddressTree("src/main/resources/conf/address.xml");
    }

    @Test
    public void parseText1() throws Exception {
        // 组装trie树
        TreeMap<String, Level> treeMap = new TreeMap<>();
        for (Level l : Level.values()) {
            Assert.assertNotNull(l.getKeywords());
            for (String s : l.getKeywords()) {
                treeMap.put(s, l);
            }
        }
        trie.build(treeMap);
        List<AhoCorasickDoubleArrayTrie<Level>.Hit<Level>> hits = trie.parseText("雨润大街");
        Assert.assertNotNull(hits.size() > 0);
        for (AhoCorasickDoubleArrayTrie<Level>.Hit<Level> v : hits) {
            Assert.assertEquals(v.value, Level.F);
        }
        // 地址中只能命中4级元素后的特征词
        List<AhoCorasickDoubleArrayTrie<Level>.Hit<Level>> hits1 = trie.parseText("江苏省");
        Assert.assertTrue(hits1.size() == 0);
    }

    @Test
    public void parseText2() throws Exception {
        TreeMap treeMap = addressTree.getAllNodes();
        trie.build(treeMap);
        List<AhoCorasickDoubleArrayTrie<Level>.Hit<Level>> hits = trie.parseText("江苏省建邺区");
        Assert.assertNotNull(hits.size() > 0);
        System.out.println(hits);
    }
}