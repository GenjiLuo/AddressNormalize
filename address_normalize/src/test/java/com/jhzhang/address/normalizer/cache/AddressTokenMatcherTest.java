package com.jhzhang.address.normalizer.cache;

import com.jhzhang.address.normalizer.cache.structure.AddressNode;
import com.jhzhang.address.normalizer.common.Level;
import com.jhzhang.address.normalizer.prob.bean.AddressType;
import com.jhzhang.address.normalizer.prob.structure.AddressToken;
import com.jhzhang.address.normalizer.cache.structure.AddressNode;
import com.jhzhang.address.normalizer.prob.bean.AddressType;
import com.jhzhang.address.normalizer.prob.structure.AddressToken;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

public class AddressTokenMatcherTest {
    static AddressTokenMatcher query = null;

    @BeforeClass
    public static void setUp() throws Exception {
        query = AddressTokenMatcher.getInstance();
        String dicPath = "src/main/resources/conf/address.xml";
        String suffixPath = "src/main/resources/conf/addressSuffixLevelData.xml";
        query.init(dicPath, suffixPath);
    }

    @Test
    public void keywordsMatch() throws Exception {
        AddressToken token = new AddressToken(0, 3, "江西省", AddressType.Province);
        List<AddressNode> lists1 = query.keywordsMatch(token.termText, Level.A);
        for (AddressNode node : lists1) {
            System.out.println(node.getKeyString());
        }

        List<AddressNode> lists2 = query.keywordsMatch("南京", Level.A);
        Assert.assertEquals(0,lists2.size());
    }
}