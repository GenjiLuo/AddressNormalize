package com.jhzhang.address.normalizer.model;

import com.jhzhang.address.normalizer.model.automaton.FiniteAutomaton;
import com.jhzhang.address.normalizer.model.automaton.StatusCode;
import com.jhzhang.address.normalizer.model.automaton.FiniteAutomaton;
import com.jhzhang.address.normalizer.model.automaton.StatusCode;

import java.util.Arrays;
import java.util.List;

/**
 * 对切分后的地址进行合法性检测.
 *
 * @author johntse
 */
public final class AddressChecker {
    private FiniteAutomaton automaton;

    public AddressChecker(FiniteAutomaton automaton) {
        this.automaton = automaton;
    }

    /**
     * 对指定的地址进行合法性检测.
     *
     * @param address 给定的地址
     * @return 给定的地址是否符合要求
     */
    public StatusCode check(List<String> address) {
        return automaton.test(address);
    }

    /**
     * 对指定的地址进行合法性检测.
     *
     * @param address   给定的地址
     * @param delimiter 地址内各个元素分隔符
     * @return 给定的地址是否符合要求
     */
    public StatusCode check(String address, String delimiter) {
        if (address.isEmpty() || delimiter.isEmpty()) {
            return automaton.test(null);
        } else {
            return automaton.test(Arrays.asList(address.split(delimiter, -1)));
        }
    }
}
