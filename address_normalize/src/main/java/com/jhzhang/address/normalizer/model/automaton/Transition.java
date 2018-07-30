package com.jhzhang.address.normalizer.model.automaton;

import com.jhzhang.address.normalizer.common.Level;
import com.jhzhang.address.normalizer.common.Level;

/**
 * 地址模型 DFA 状态转移对象定义.
 *
 * @author johntse
 */
public class Transition {
    private final State state;
    private final Level level;

    public Transition(State state, Level level) {
        this.state = state;
        this.level = level;
    }

    public State getState() {
        return state;
    }

    public Level getLevel() {
        return level;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + level.hashCode();
        result = 31 * result + state.hashCode();

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof Transition) {
            Transition other = (Transition) obj;

            return other.level == level && other.state.equals(state);
        }

        return false;
    }

    @Override
    public String toString() {
        return level + "->" + state;
    }
}
