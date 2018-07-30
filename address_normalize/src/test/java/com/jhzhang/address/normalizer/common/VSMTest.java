package com.jhzhang.address.normalizer.common;

import com.jhzhang.address.normalizer.cache.AddressQuery;
import com.jhzhang.address.normalizer.cache.AddressQuery;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

public class VSMTest {
    private static AddressQuery query;

    @BeforeClass
    public static void setUp() {
        if (query == null) {
            String parentPath = "./src/main/resources/conf/";
            AddressQuery.getInstance().init(parentPath + "address.xml", parentPath + "addressSuffixLevelData.xml");
        }
        query = AddressQuery.getInstance();
    }

    @Test
    public void removeDuplicateElement() throws Exception {
        // todo 失败的设计，只能去除连续的等级的重复元素
        Assert.assertNotNull(query);
        List<Element> lists = query.getSegmentAddressElementList("南京市思南路115弄1号103室");
        Assert.assertNotNull(lists);
        System.out.println(lists);
        List<Element> filter = VSM.removeDuplicateElement(lists);
        Assert.assertNotNull(filter);
        Assert.assertSame(lists.get(0), filter.get(0));
        System.out.println(filter);
    }

    @Test
    public void getTrans() throws Exception {
    }

}