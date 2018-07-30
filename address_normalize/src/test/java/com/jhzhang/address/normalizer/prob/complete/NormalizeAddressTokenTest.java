package com.jhzhang.address.normalizer.prob.complete;

import com.jhzhang.address.normalizer.AddressTagger;
import com.jhzhang.address.normalizer.Normalizer;
import com.jhzhang.address.normalizer.cache.AddressTokenMatcher;
import com.jhzhang.address.normalizer.cache.exception.InvalidAddressException;
import com.jhzhang.address.normalizer.common.Level;
import com.jhzhang.address.normalizer.prob.bean.AddressType;
import com.jhzhang.address.normalizer.prob.structure.AddressToken;
import com.jhzhang.address.normalizer.prob.task.*;
import com.jhzhang.address.normalizer.AddressTagger;
import com.jhzhang.address.normalizer.Normalizer;
import com.jhzhang.address.normalizer.cache.AddressTokenMatcher;
import com.jhzhang.address.normalizer.cache.exception.InvalidAddressException;
import com.jhzhang.address.normalizer.prob.bean.AddressType;
import com.jhzhang.address.normalizer.prob.structure.AddressToken;
import com.jhzhang.address.normalizer.prob.task.*;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NormalizeAddressTokenTest {
    static AddressTokenMatcher query = null;

    @BeforeClass
    public static void setUp() throws Exception {
        query = AddressTokenMatcher.getInstance();
        String addrPath = "src/main/resources/conf/address.xml";
        String suffixPath = "src/main/resources/conf/addressSuffixLevelData.xml";

        new Normalizer(addrPath,suffixPath);
    }

    @Test
    public void mount() throws Exception {
        NormalizeAddressToken tokens = new NormalizeAddressToken();
        tokens.mount(new AddressToken(0, 3, "江西省", AddressType.Province), Level.A);
    }

    @Test
    public void valueOf() {
        String address = "浙江省杭州市上城区小营街道小营社区上马街34号梅林二村503室";
        ArrayList<AddressToken> ret = AddressTagger.tag(address);
        List<AddressToken> rets = ret.subList(1, ret.size() - 1);
        NormalizeAddressToken addressToken = new NormalizeAddressToken();
        new LevelABCMountTask().task(addressToken, rets);
        System.out.println("ABC: " + addressToken);
        System.out.println(Arrays.toString(rets.toArray()));
        new LevelDMountTask().task(addressToken, rets);
        System.out.println("D: " + addressToken);
        System.out.println(Arrays.toString(rets.toArray()));
        new LevelEMountTask().task(addressToken, rets);
        System.out.println("E: " + addressToken);
        System.out.println(Arrays.toString(rets.toArray()));
        new LevelFGMountTask().task(addressToken, rets);
        System.out.println("FG: " + addressToken);
        System.out.println(Arrays.toString(rets.toArray()));
        new LevelHMountTask().task(addressToken, rets);
        System.out.println("H: " + addressToken);
        System.out.println(Arrays.toString(rets.toArray()));
        new LevelJKLMMountTask().task(addressToken, rets);
        System.out.println("JKLM: " + addressToken);
        System.out.println(Arrays.toString(rets.toArray()));
    }

    @Test
    public void fixABCDELevel() throws InvalidAddressException {
        String address = "江苏省杭州市南京市";
        NormalizeAddressToken addressTokens = NormalizeAddressToken.valueOf(address);
        addressTokens.fixABCDELevel();
        System.out.println(addressTokens.asList());
    }

}