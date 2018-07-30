package com.jhzhang.address.normalizer.core.process;

import com.jhzhang.address.normalizer.common.Element;
import com.jhzhang.address.normalizer.common.Level;
import com.jhzhang.address.normalizer.common.Vector;
import com.jhzhang.address.normalizer.core.CommonOperation;
import com.jhzhang.address.normalizer.core.support.Position;
import com.jhzhang.address.normalizer.core.support.Position;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 等级G的处理过程
 * Created by Administrator on 2016/9/12.
 */
public class LevelGProcessor implements Processor {
    /**
     * 该类要处理的等级.
     */
    private static final Level LEVEL = Level.G;
    /**
     * 号前面为数字如（3号），数字-数字（2-201），字母数字（B4）.
     */
    private static final Pattern NUMBER =
            Pattern.compile("([0-9]+[-][0-9]+)|([a-zA-Z]*[0-9]+)");
    /**
     * 匹配以数字结尾的正则.
     */
    private static final Pattern END_BY_NUMBER =
            Pattern.compile(".*(([0-9]+[-][0-9]+)|([a-zA-Z]*[0-9]+))$");
    private Constraint[] constraintArray = new Constraint[]{
            new Constraint("号", "楼", Level.J),
            new Constraint("号", "房", Level.M),
            new Constraint("号", "门", Level.UNKNOWN),
    };

    @Override
    public Vector process(Vector vector) {

        //按约束条件合并
        CommonOperation.mergeByConstraint(vector, LEVEL, END_BY_NUMBER, NUMBER);
        //修正
        fix(vector);
        CommonOperation.rollBackFromBreakPoint(vector, LEVEL);
        //将多余的部分移动到等级F
        CommonOperation.moveToTagLevel(vector, LEVEL);

        LevelFProcessor levelF = new LevelFProcessor();
        levelF.normalize(vector);
        return vector;
    }

    /**
     * 一些修正操作，主要是如果“号”关键字后面如果接“楼”关键字，则整合到等级J中去
     * 如果“号”关键字后面如果接“房”关键字，则整合到等级M中去.
     *
     * @param vector 地址向量
     */
    private void fix(Vector vector) {
        List<Element> list = vector.indexOf(LEVEL.ordinal());
        if (list.size() <= 0) {
            return;
        }
        for (int i = list.size() - 1; i >= 0; i--) {
            Element element = list.get(i);
            if (element.getLevel().equals(LEVEL)) {
                //获取其回退位置上的元素
                Position position = vector.getRollBackPosition(element);
                Element rollBackElement = vector.indexOf(position);
                Constraint constraint = isMatchConstraint(element, rollBackElement);
                if (constraint != null) {
                    //删除后继地址元素
                    vector.removeElement(position);
                    //将当前位置上的元素删掉
                    vector.removeElement(new Position(LEVEL.ordinal(), i));
                    reMount(vector, element, constraint);

                    Element preElement;
                    while (i > 0 && (preElement = list.get(--i)).getLevel().equals(Level.UNKNOWN)) {
                        vector.rollBack(preElement, new Position(LEVEL.ordinal(), i));
                    }
                    i++;
                }
            }
        }
    }

    /**
     * 判断一个地址元素是否符合约束.
     *
     * @param element         要判断的地址元素
     * @param rollBackElement 它回退位置上的地址元素
     * @return 是否符合约束
     */
    private Constraint isMatchConstraint(Element element, Element rollBackElement) {
        if (rollBackElement == null) {
            return null;
        }
        String prefix = element.getSuffix();
        String suffix = rollBackElement.getName() + rollBackElement.getSuffix();
        for (Constraint constraint : constraintArray) {
            if (prefix.equals(constraint.prefix) && suffix.equals(constraint.suffix)) {
                return constraint;
            }
        }
        return null;
    }

    /**
     * 将符合约束的地址元素整合到对应等级中去.
     *
     * @param vector     地址向量
     * @param element    地址元素
     * @param constraint 约束关系
     */
    private void reMount(Vector vector, Element element, Constraint constraint) {
        if (constraint.level.equals(Level.UNKNOWN)) {
            return;
        }
        // TODO 没有完全的实现Element参数
        Element suffixElement = new Element("",
                constraint.prefix + constraint.suffix,
                constraint.level,
                element.getStart() + element.getName().length(), element.getEnd());
        vector.mount(constraint.level.ordinal(), suffixElement);
        Element prefixElement = new Element(element.getName(),
                "",
                Level.UNKNOWN,
                element.getStart(), element.getEnd());
        vector.mount(constraint.level.ordinal(), prefixElement);
    }

    class Constraint {
        String prefix;
        String suffix;
        Level level;

        Constraint(String prefix, String suffix, Level level) {
            this.prefix = prefix;
            this.suffix = suffix;
            this.level = level;
        }
    }

}
