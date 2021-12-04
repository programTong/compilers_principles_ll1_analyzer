package com.tonngtong.grammarproblemhandler;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 分析树实现类，依据LL(1)分析表进行选择
 */
public class ParseTreeImpl2 extends ParseTree{

    /**
     * 给定一个文法和一个句子，构造相应分析树
     *
     * @param grammar
     * @param sentence
     */
    public ParseTreeImpl2(Grammar grammar, String sentence) {
        super(grammar, sentence);
    }

    /**
     * 根据LL(1)分析表进行选择
     * @param input
     * @param productions
     * @param grammar
     * @return
     */
    @Override
    protected Production choose0(char input, List<Production> productions, Grammar grammar) {
        Map<Character, Map<Character, Set<Production>>> table = grammar.getLl1ParseTable();
        Set<Production> set = table.get(productions.get(0).getLeft()).get(input);
        if (set!=null){
            for (Production pa : set) {
                return pa;
            }
        }
        throw new RuntimeException("句子不合法");
    }


}
