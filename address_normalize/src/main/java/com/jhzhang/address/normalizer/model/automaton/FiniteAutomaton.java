package com.jhzhang.address.normalizer.model.automaton;

import com.jhzhang.address.normalizer.common.Level;
import com.jhzhang.address.normalizer.common.Level;

import java.util.List;

/**
 * 有限状态自动机.
 *
 * @author johntse
 */
public class FiniteAutomaton {
    private final int maxLen;
    private final State root;

    public FiniteAutomaton(State root, int maxLen) {
        this.root = root;
        this.maxLen = maxLen;
    }

    /**
     * 测试给定的地址1是否符合要求.
     *
     * @param items 给定的地址
     * @return 状态码，标识地址检测状态
     */
    public StatusCode test(List<String> items) {
        if (items == null || items.isEmpty()) {
            return StatusCode.UNKNOWN;
        } else {
            return test(root, items, 0);
        }
    }

    private StatusCode test(State state, List<String> items, int index) {
        if (index < 0 || index + 1 > maxLen) {
            if (state.isTerminated()) {
                return StatusCode.ACCEPT;
            } else {
                return StatusCode.UNTERMINATED;
            }
        }

        while (index < items.size() && (items.get(index) == null || items.get(index).isEmpty())) {
            ++index;
        }

        if (index == items.size()) {
            if (state.isTerminated()) {
                return StatusCode.ACCEPT;
            } else {
                return StatusCode.UNTERMINATED;
            }
        }

        Level level = Level.values()[index];
        State next = state.getState(level);

        if (next == State.EMPTY_STATE) {
            return StatusCode.MISSED;
        }

        return test(next, items, index + 1);
    }
}
