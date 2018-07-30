package com.jhzhang.address.normalizer.model.automaton;

import com.jhzhang.address.normalizer.common.Level;
import com.jhzhang.address.normalizer.common.Level;

import java.util.Arrays;

/**
 * 地址模型 DFA 状态定义.
 *
 * @author johntse
 */
public class State {
    public static final State EMPTY_STATE = new State("");

    private final String name;

    private boolean isTerminated = false;
    private Transition[] transitions;

    /**
     * 构造函数.
     *
     * @param name 状态名称
     */
    public State(String name) {
        this.name = name;
        this.transitions = new Transition[Level.size()];
    }

    /**
     * 构造函数.
     *
     * @param name         状态的名称
     * @param isTerminated 是否可终结状态
     */
    public State(String name, boolean isTerminated) {
        this(name);

        this.isTerminated = isTerminated;
    }

    public boolean isTerminated() {
        return isTerminated;
    }

    /**
     * 添加一个转移状态.
     *
     * @param state 下一个状态
     * @param level 转移方式
     * @return 添加是否成功
     */
    public boolean addTransition(State state, Level level) {
        Transition transition = new Transition(state, level);

        if (transitions[level.ordinal()] == null) {
            transitions[level.ordinal()] = transition;
            return true;
        } else {
            return false;
        }
    }

    /**
     * 添加一个转移状态.
     *
     * @param transition 待添加的转移状态
     * @return 添加是否成功
     */
    public boolean addTransition(Transition transition) {
        Level level = transition.getLevel();

        if (transitions[level.ordinal()] == null) {
            transitions[level.ordinal()] = transition;
            return true;
        } else {
            return false;
        }
    }

    /**
     * 根据给出的转移方式获取下一个状态.
     *
     * @param level 给出的转移方式
     * @return 获取成功返回下一个状态，否则返回空状态
     */
    public State getState(Level level) {
        Transition result = transitions[level.ordinal()];

        if (result != null) {
            return result.getState();
        } else {
            return EMPTY_STATE;
        }
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = result * 31 + name.hashCode();
        result = result * 31 + (isTerminated ? 1 : 0);
        result = result * 31 + Arrays.hashCode(transitions);

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return false;
        }

        if (obj instanceof State) {
            State other = (State) obj;

            return other.isTerminated == isTerminated && other.name.equals(name)
                    && Arrays.equals(other.transitions, transitions);
        }

        return false;
    }

    @Override
    public String toString() {
        return name + "[" + isTerminated + "]";
    }
}
