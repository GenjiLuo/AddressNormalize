package com.jhzhang.address.normalizer.prob.common;

import com.jhzhang.address.normalizer.prob.bean.AddressType;
import com.jhzhang.address.normalizer.prob.bean.AddressType;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class UnKnowGrammarToolTest {
    @Test
    public void load() throws Exception {
        String rhsStr = "Municipality|SuffixCity";
        ArrayList<AddressType> rhs = new ArrayList<>();
        String[] rhsArray = rhsStr.split("\\|",-1);
        for (String s : rhsArray) {
            rhs.add(AddressType.valueOf(s));
        }
        Assert.assertEquals(rhs.get(0),AddressType.Municipality);
        Assert.assertEquals(rhs.get(1),AddressType.SuffixCity);
    }


}