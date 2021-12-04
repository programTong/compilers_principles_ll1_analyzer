package com.tonngtong.grammarproblemhandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * 分析树实现类，手工选择
 */
public class ParseTreeImpl0 extends ParseTree{

    /**
     * 给定一个文法和一个句子，构造相应分析树
     *
     * @param grammar
     * @param sentence
     */
    public ParseTreeImpl0(Grammar grammar, String sentence) {
        super(grammar, sentence);
    }

    /**
     * 手工选择，测试
     * @param input
     * @param productions
     * @param grammar
     * @return
     */
    @Override
    protected Production choose0(char input, List<Production> productions, Grammar grammar) {
        Scanner scanner = new Scanner(System.in);
        for (int i = 0; i < productions.size(); i++) {
            System.out.println(i+"  :"+productions.get(i));
        }
        System.out.print(":");
        int i = scanner.nextInt();
        System.out.println();
        return productions.get(i);
    }

    public static void main(String[] args) {
//        List<String> pSL = new ArrayList<String>(){{
//            add("E-> E+T");
//            add("E-> E- T");
//            add("E-> T");
//            add("T -> T*F");
//            add("T -> T/F");
//            add("T -> F");
//            add("F -> (E)");
//            add("F -> i");
//        }};
//        Grammar grammar = new Grammar(Production.generateList(pSL));
//        ParseTree parseTree = new ParseTreeImpl0(grammar,"i+i*i");


//        List<String> pSL = new ArrayList<String>(){{
//            add("S-> Be");
//            add("B-> Ce");
//            add("B-> Af");
//            add("A -> Ae");
//            add("A -> e");
//            add("C -> Cf");
//            add("D -> f");
//        }};
//        Grammar grammar = new Grammar(Production.generateList(pSL));
//        grammar.removeNoUsefulProduction();


//            List<String> pSL = new ArrayList<String>(){{
//            add("E-> E+T");
//            add("E-> E- T");
//            add("E-> T");
//            add("T -> T*F");
//            add("T -> T/F");
//            add("T -> F");
//            add("F -> (E)");
//            add("F -> i");
//        }};
//        Grammar grammar = new Grammar(Production.generateList(pSL));
//        grammar.removeNoUsefulProduction();
//        grammar.first();


//            List<String> pSL = new ArrayList<String>(){{
//            add("S->AB|CD");
//            add("A->aB|dD");
//            add("B->cC|bD");
//            add("C->ef|gh");
//            add("D->i|j");
//        }};
//        Grammar grammar = new Grammar(Production.generateList(pSL));
//        grammar.removeNoUsefulProduction();
//        grammar.first();



//        List<String> pSL = new ArrayList<String>(){{
//            add("S->cAd|cB");
//            add("A->ab|a");
//            add("B->aa");
//
//        }};
//        Grammar grammar = new Grammar(Production.generateList(pSL));
//        grammar.removeNoUsefulProduction();
//        grammar.removeLeftCommonFactor();


//        List<String> pSL = new ArrayList<String>(){{
//            add("S->ABC");
//            add("A->aB|bB");
//            add("B->cC|@");
//            add("C->ef|gh");
//
//        }};
//        Grammar grammar = new Grammar(Production.generateList(pSL));
//        grammar.removeNoUsefulProduction();
//        grammar.removeLeftCommonFactor();
//        grammar.follow();



//        List<String> pSL = new ArrayList<String>(){{
//            add("E->EOT|T");
//            add("O->+|-");
//            add("T->TMF|F");
//            add("M->*");
//            add("F->(E)|n");
//
//        }};
//        Grammar grammar = new Grammar(Production.generateList(pSL));
//        grammar.removeNoUsefulProduction();
//        grammar.removeLeftCommonFactor();
//        grammar.follow();


//        List<String> pSL = new ArrayList<String>(){{
//            add("S->Ab|Ec|Cd|e");
//            add("A->Df");
//            add("D->af");
//            add("E->afg|afh");
//            add("C->afj");
//        }};
//        Grammar grammar = new Grammar(Production.generateList(pSL));
//        grammar.removeNoUsefulProduction();
//        grammar.removeLeftCommonFactor();




//        List<String> pSL = new ArrayList<String>(){{
//            add("A->Aa|b");
//        }};
//        Grammar grammar = new Grammar(Production.generateList(pSL));
//        grammar.removeNoUsefulProduction();
//        grammar.removeLeftCommonFactor();
//        grammar.removeLeftRecursion();



        List<String> pSL = new ArrayList<String>(){{
            add("A->Ba|Aa|c");
            add("B->Bb|Ab|d");
        }};
        Grammar grammar = new Grammar(Production.generateList(pSL));
        grammar.removeNoUsefulProduction();
        grammar.removeLeftCommonFactor();
        grammar.removeLeftRecursion();


    }

}
