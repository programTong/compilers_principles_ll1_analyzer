package com.tonngtong.grammarproblemhandler;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分析树
 */
@Getter
@Setter
public abstract class ParseTree {

    public int count = 1;
    public TreeNode root;//根节点
    public Map<Integer,TreeNode> idToNode = new HashMap<>();//节点集合
    public Grammar grammar;//文法
    public char[] sentence;//要分析的句子
    private int index = 0;
    private char input;
    private DerivationProcess derivationProcess = new DerivationProcess();//推导过程
    private DerivationProcess.LinkNode curLinkNode;//当前推导节点

    class TreeNode {
        public int id;
        public char sign;
        public int parentId;
        public int leftBrotherId;
        public int rightSonId;

        public TreeNode(int id, char sign) {
            this.id = id;
            this.sign = sign;
        }
    }

    /**
     * 给定一个文法和一个句子，构造相应分析树
     * @param grammar
     * @param sentence
     */
    public ParseTree(Grammar grammar,String sentence){
        grammar.removeLeftCommonFactor();
        grammar.removeLeftRecursion();
        grammar.removeNoUsefulProduction();
        grammar.first();
        grammar.follow();
        grammar.ll1ParseTable();
        this.grammar = grammar;
        this.sentence = sentence.toCharArray();
        root = new TreeNode(count++, grammar.getStart());
        idToNode.put(root.id,root);

        //第一次推导
        input = getNextInput();
        derivationProcess.head = new DerivationProcess.LinkNode(new DerivationProcess.SignNode(grammar.getStart()));
        curLinkNode = derivationProcess.head;
        Production production = choose0(input,
                grammar.getProductions().get(root.sign),
                grammar);
        generateDerivationProcess(production);

        generate(root,production.getRight().toCharArray());//递归推导
        //收尾工作，将最后剩下未推导的非终结符推导为epsilon
        DerivationProcess.SignNode p = curLinkNode.headOfSentencePattern;
        while (p!=null) {
            if (grammar.getNonTerminals().contains(p.sign)) {
                List<Production> pL = grammar.getProductions().get(p.sign);
                Production pb = null;
                for (Production pa : pL) {
                    if (pa.getRight().equals(Grammar.epsilon+"")){
                        pb = pa;
                    }
                }
                if (pb!=null){
                    generateDerivationProcess(pb);
                } else {
                    throw new RuntimeException("文法不合法，缺少"+p.sign+"->epsilion");
                }
            }
            p = p.next;
        }
    }


    /**
     * 构建树
     * @param node node对应非终结符号
     * @param currentSentencePattern 该非终结符号在当前输入下应该的产生式右部
     */
    private void generate(TreeNode node,char[] currentSentencePattern) {
        int j = 0;
        int leftBrotherId = 0;
        while (input!='\0'&&j<currentSentencePattern.length){
            TreeNode nodeA = new TreeNode(count++, currentSentencePattern[j]);
            idToNode.put(nodeA.id,nodeA);
            nodeA.parentId = node.id;
            nodeA.leftBrotherId = leftBrotherId;
            leftBrotherId = nodeA.id;
            if (input==currentSentencePattern[j]){
                input = getNextInput();
            } else {
                Production production = choose0(input,
                        grammar.getProductions().get(currentSentencePattern[j]),
                        grammar);
                generateDerivationProcess(production);
                if (!production.getRight().equals(Grammar.epsilon+"")){
                    generate(nodeA,production.getRight().toCharArray());
                }

            }
            j++;
        }
        node.rightSonId = leftBrotherId;
    }

    /**
     * 构建一个推导节点
     * @param production
     */
    private void generateDerivationProcess(Production production){
        DerivationProcess.LinkNode newL = DerivationProcess.copyFrom(curLinkNode);
        curLinkNode.next = newL;
        String right = production.getRight();

        DerivationProcess.SignNode p = newL.headOfSentencePattern;
        DerivationProcess.SignNode pre = null;
        while (p!=null){
            if (p.sign==production.getLeft()){
                break;
            }
            pre = p;
            p = p.next;
        }
        DerivationProcess.SignNode q = null;
        DerivationProcess.SignNode qhead = null;
        for (int i = 0; i < right.length(); i++) {
            if (q==null){
                q = new DerivationProcess.SignNode(right.charAt(i));
                qhead = q;
            } else {
                q.next = new DerivationProcess.SignNode(right.charAt(i));
                q = q.next;
            }
        }
        if (pre==null){
            newL.headOfSentencePattern = qhead;
        } else {
            pre.next = qhead;
        }
        q.next = p.next;
        curLinkNode = newL;
    }

    private char getNextInput(){
        return index>=sentence.length ? '\0': sentence[index++];
    }

    /**
     * 当前的非终结符号对应多条产生式，根据当前句子输入从多分支中选择正确的一条
     * @param input
     * @param productions
     * @param grammar
     * @return
     */
    protected abstract Production choose0(char input, List<Production> productions, Grammar grammar);




}
