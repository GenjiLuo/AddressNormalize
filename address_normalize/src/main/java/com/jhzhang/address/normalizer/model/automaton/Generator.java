package com.jhzhang.address.normalizer.model.automaton;

import com.jhzhang.address.normalizer.common.Level;
import com.jhzhang.address.normalizer.common.Level;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * DFA生成器.
 *
 * @author johntse
 */
public class Generator {
    /**
     * 根据给定的配置，生成一个地址检测对象.
     *
     * @param automatonConfigPath 给定的配置
     * @return 生成的对象
     */
    public static FiniteAutomaton get(String automatonConfigPath) {
        return get(new File(automatonConfigPath));
    }

    /**
     * 根据给定的配置，生成一个地址检测对象.
     *
     * @param automatonConfigFile 给定的配置文件
     * @return 生成的对象
     */
    public static FiniteAutomaton get(File automatonConfigFile) {
        Document document;
        try {
            document = new SAXReader().read(new FileInputStream(automatonConfigFile));
        } catch (DocumentException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        Element root = document.getRootElement();

        Map<String, State> states = loadStates(root.element("states"));
        Map<String, List<Transition>> transitions = loadTransition(root.element("transitions"), states);

        return new FiniteAutomaton(build(states, transitions), 12);
    }

    static State build(Map<String, State> states, Map<String, List<Transition>> transitions) {
        State root = null;

        for (Map.Entry<String, State> entry : states.entrySet()) {
            State state = entry.getValue();

            String stateId = entry.getKey();

            if ("root".equals(stateId)) {
                root = state;
            }

            List<Transition> transitionList = transitions.get(stateId);
            if (transitionList == null || transitionList.isEmpty()) {
                if (state.isTerminated()) {
                    continue;
                } else {
                    throw new RuntimeException(
                            String.format("there is a single state[%s] without any transition!", state));
                }
            }

            for (Transition transition : transitionList) {
                if (!state.addTransition(transition)) {
                    throw new RuntimeException(
                            String.format("add a transition[%s] to state[%s] failed!", transition, state));
                }
            }
        }

        if (root == null) {
            throw new RuntimeException("there is no root state!");
        }

        return root;
    }

    static Map<String, State> loadStates(Element states) {
        if (states == null) {
            throw new RuntimeException("these is no any state defined!");
        }

        Map<String, State> stateMap = new HashMap<>();
        for (Iterator children = states.elementIterator(); children.hasNext(); ) {
            Element state = (Element) children.next();
            String id = getAttributeValue(state, "id");
            String name = getAttributeValue(state, "name");

            if (id.isEmpty() && name.isEmpty()) {
                throw new RuntimeException("id or name can't empty!");
            }

            boolean isTerminated = Boolean.valueOf(getAttributeValue(state, "isTerminated"));

            if (!stateMap.containsKey(id)) {
                stateMap.put(id, new State(name, isTerminated));
            } else {
                throw new RuntimeException("multi state defined! duplicate id: " + id);
            }
        }

        if (stateMap.isEmpty()) {
            throw new RuntimeException("these is no any state defined!");
        }

        return stateMap;
    }

    static Map<String, List<Transition>> loadTransition(Element transitions, Map<String, State> stateMap) {
        if (transitions == null) {
            throw new RuntimeException("these is no any transition defined!");
        }

        Map<String, List<Transition>> transitionMap = new HashMap<>();
        for (Iterator children = transitions.elementIterator(); children.hasNext(); ) {
            Element state = (Element) children.next();
            String from = getAttributeValue(state, "from");
            String to = getAttributeValue(state, "to");
            if (from.isEmpty() && to.isEmpty()) {
                throw new RuntimeException("from or to can't empty!");
            }

            Level when = Level.valueOf(getAttributeValue(state, "when"));

            if (stateMap.get(from) == null) {
                throw new RuntimeException(
                        String.format("the 'from' state not defined! from %s to %s when %s", from, to, when)
                );
            }

            State toState = stateMap.get(to);
            if (toState == null) {
                throw new RuntimeException(
                        String.format("the 'to' state not defined! from %s to %s when %s", from, to, when));
            }

            List<Transition> transitionList = transitionMap.get(from);

            if (transitionList == null) {
                transitionList = new ArrayList<>();
                transitionMap.put(from, transitionList);
            }

            Transition added = new Transition(toState, when);

            if (transitionList.contains(added)) {
                throw new RuntimeException("same transition found at state id: " + from);
            }

            transitionList.add(new Transition(toState, when));
        }

        if (transitionMap.isEmpty()) {
            throw new RuntimeException("these is no any transition defined!");
        }

        return transitionMap;
    }

    private static String getAttributeValue(Element element, String attributeName) {
        Attribute id = element.attribute(attributeName);
        if (id == null) {
            return "";
        } else {
            return id.getValue();
        }
    }
}
