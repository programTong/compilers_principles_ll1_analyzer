package com.tonngtong.grammarproblemhandler;

import java.util.*;

/**
 * 分析树实现类，依据first和follow进行选择
 */
public class ParseTreeImpl1 extends ParseTree{

    /**
     * 给定一个文法和一个句子，构造相应分析树
     *
     * @param grammar
     * @param sentence
     */
    public ParseTreeImpl1(Grammar grammar, String sentence) {
        super(grammar, sentence);
    }

    /**
     * 根据first集合和follow选择正确的产生式
     * @param input
     * @param productions
     * @param grammar
     * @return
     */
    @Override
    protected Production choose0(char input, List<Production> productions, Grammar grammar) {
        Map<Character, Set<Character>> first = grammar.getFirst();
        Map<Character, Set<Character>> follow = grammar.getFollow();
        for (Production production : productions) {
            if (first.get(production.getRight().charAt(0)).contains(input)){
                return production;
            }
        }
        for (Production production : productions) {
            char ch = production.getRight().charAt(0);
            if (ch==Grammar.epsilon){
                continue;
            }
            if (first.get(ch).contains(Grammar.epsilon)&&follow.get(ch).contains(input)){
                return production;
            }
        }
        if (follow.get(productions.get(0).getLeft()).contains(input)){
            for (Production production : productions) {
                if (production.getRight().equals(Grammar.epsilon+"")){
                    return production;
                }
            }
        }
        throw new RuntimeException("句子不合法");
    }

    public static void main(String[] args) {

//        List<String> pSL = new ArrayList<String>(){{
//            add("E->EAT|T");
//            add("A->+|-");
//            add("T->TMF|F");
//            add("M->*");
//            add("F->(E)|n");
//        }};
//        Grammar grammar = new Grammar(Production.generateList(pSL));
//        ParseTree parseTree = new ParseTreeImpl1(grammar,"n+n*n");
//        System.out.println(parseTree.getDerivationProcess());




        List<String> pSL = new ArrayList<String>(){{
            add("S->Ab|Bc");
            add("A->aA|dB");
            add("B->c|e");
        }};
        Grammar grammar = new Grammar(Production.generateList(pSL));
        grammar.ll1ParseTable();

    }

}
