package com.jhzhang.address.normalizer;

import org.junit.BeforeClass;
import org.junit.Test;

public class NormalizerTest {
    static Normalizer normalizer = null;

    @BeforeClass
    public static void setUp() {
        String dicPath = "E:\\software\\SVN\\address_normalize_dev\\address_normalize_dev\\src\\main\\resources\\conf\\address.xml";
        String unkown = "E:\\software\\SVN\\address_normalize_dev\\address_normalize_dev\\src\\main\\resources\\conf\\addressSuffixLevelData.xml";
        normalizer = new Normalizer(dicPath, unkown);
    }

    @Test
    public void normalizeAsStr() throws Exception {
        String address = "萧山区河庄街道御东湾8幢2单元401室";
        String nor = normalizer.normalizeAsStr(address);
        System.out.println(nor);
    }

}