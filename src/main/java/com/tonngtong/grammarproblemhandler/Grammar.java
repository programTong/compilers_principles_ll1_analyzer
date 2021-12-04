package com.tonngtong.grammarproblemhandler;

import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 文法类
 */
@Setter
@Getter
public class Grammar {

    private Set<Character> terminals = new HashSet<>();//终结符集合
    private Set<Character> nonTerminals = new HashSet<>();//非终结符集合
    /**
     * S->aB|cD
     * 产生式Map，S : {S->ab, S->cD}
     */
    private Map<Character, List<Production>> productions = new HashMap<>();
    // 产生式id 映射到 产生式
    private Map<Integer,Production> idToProduction = new HashMap<>();
    private char start; // 文法开始符号
    private Map<Character,Set<Character>> first = new HashMap<>(); //first集合
    private Map<Character,Set<Character>> follow = new HashMap<>(); //follow集合
    public static char epsilon = '@';
    Map<Character,Map<Character,Set<Production>>> ll1ParseTable = new HashMap<>(); //LL(1)分析表

    /**
     * 通过产生式List生成文法
     * @param productionList
     */
    public Grammar(List<Production> productionList) {
        start = productionList.get(0).getLeft();
        for (Production production : productionList) {
            idToProduction.put(production.getId(),production);
            char left = production.getLeft();

            //构建productions
            List<Production> pL = this.productions.get(left);
            if (pL==null){
                pL = new ArrayList<>();
                productions.put(left,pL);
            }
            pL.add(production);

            //构建nonTerminals、terminals
            if (Character.isUpperCase(left)){
                nonTerminals.add(left);
            } else {
                terminals.add(left);
            }
            String right = production.getRight();
            for (int i = 0; i < right.length(); i++) {
                if (Character.isUpperCase(right.charAt(i))){
                    nonTerminals.add(right.charAt(i));
                } else {
                    terminals.add(right.charAt(i));
                }
            }
        }
    }

    /**
     * LL(1)分析表的构造步骤
     * 为每个非终结符A和规则A→β重复以下两个步骤：
     * 1)对于First(β)中的每个记号a，都将A→β添加到分析表 M[A, a]中。
     * 2)若ε在First(β)中，则对于Follow(A)的每个元素a(记号或是$)，都将A→β添加到M[A, a]中。
     * 3)把分析表A中每个未定义元素置为ERROR。注意：通常用空白表示即可
     */
    public void ll1ParseTable(){
        //先构建first和follow集合，后面会用到
        first();
        follow();
        //初始化ll1ParseTable
        ll1ParseTable = new HashMap<>();
        nonTerminals.forEach(new Consumer<Character>() {
            @Override
            public void accept(Character character) {
                ll1ParseTable.put(character,new HashMap<>());
            }
        });
        //为每个非终结符A和规则A→β重复以下两个步骤：
        idToProduction.forEach(new BiConsumer<Integer, Production>() {
            @Override
            public void accept(Integer integer, Production production) {
                Map<Character, Set<Production>> mapA = ll1ParseTable.get(production.getLeft());
                char c = production.getRight().charAt(0);
                Set<Character> setA = first.get(c);
                //1)对于First(β)中的每个记号a，都将A→β添加到分析表 M[A, a]中。
                for (Character ch : setA) {
                    if (ch!=epsilon){
                        Set<Production> pS = mapA.get(ch);
                        if (pS==null){
                            pS = new HashSet<>();
                            mapA.put(ch,pS);
                        }
                        pS.add(production);
                    }
                }
                //2)若ε在First(β)中，则对于Follow(A)的每个元素a(记号或是$)，都将A→β添加到M[A, a]中。
                if (setA.contains(epsilon)) {
                    Set<Character> setB = follow.get(production.getLeft());
                    for (Character ca : setB) {
                        Set<Production> pS = mapA.get(ca);
                        if (pS==null){
                            pS = new HashSet<>();
                            mapA.put(ca,pS);
                        }
                        pS.add(production);
                    }

                }
            }
        });
    }

    /**
     * 消除左递归
     * 举例：
     * A → Ba | Aa | c
     * B → Bb | Ab | d
     * @return
     */
    public Grammar removeLeftRecursion() {
        boolean hasChange = true;
        //文法有变化，则继续尝试消除左递归
        while (hasChange) {
            hasChange = false;
            Character[] nonTs = nonTerminals.toArray(new Character[0]);
            for (int i = 0; i < nonTs.length; i++) {
                char ch = nonTs[i];
                /*
                    下面这个for循环，是用前面已经消除了左递归的非终结符进行替换
                    例如：
                    A → Ba | Aa | c
                    B → Bb | Ab | d
                    第一次进入while时，当i=1,ch = 'B'，
                    A → BaA’ | cA’
                    A’→ aA’ | ε
                    B → Bb | Ab | d
                    经下面for循环处理后，变为
                    A → BaA’ | cA’
                    A’→ aA’ | ε
                    B → Bb | BaA’b | cA’b | d
                 */
                for (int j = 0; j <=i-1 ; j++) {
                    List<Production> pros = this.productions.get(ch);
                    List<Production> prosA = new ArrayList<>(pros);
                    for (Production pro : pros) {
                        if (pro.getRight().charAt(0)==nonTs[j]){
                            idToProduction.remove(pro.getId());
                            prosA.remove(pro);
                            List<Production> pLB = this.productions.get(nonTs[j]);
                            for (Production pb : pLB) {
                                Production ppp = new Production(ch, pb.getRight() + pro.getRight().substring(1));
                                idToProduction.put(ppp.getId(),ppp);
                                prosA.add(ppp);
                            }
                            hasChange = true;
                        }
                    }
                    productions.put(ch,prosA);
                }
                //消除Ai 规则中的直接左递归；
                //1、找到存在直接左递归的产生式
                Production ppb = null;
                for (Production pro : productions.get(ch)) {
                    if (pro.getRight().charAt(0)==ch){
                        ppb = pro;
                        break;
                    }
                }
                /*
                    2、
                        A->Aa|β|μ
                        消除直接左递归后：
                        A->βC|μC
                        C->aC|ε
                 */
                if (ppb!=null){
                    hasChange  = true;
                    productions.get(ch).remove(ppb);//删除A->Aa
                    idToProduction.remove(ppb.getId());//删除A->Aa
                    List<Production> pLC = new ArrayList<>();
                    char newNonT = availableNonTerminalSign();
                    nonTerminals.add(newNonT);//增加C
                    for (Production pro : productions.get(ch)) {//增加A->βC|μC
                        idToProduction.remove(pro.getId());
                        Production production = new Production(ch, pro.getRight() + newNonT);
                        idToProduction.put(production.getId(),production);
                        pLC.add(production);
                    }
                    productions.put(newNonT,new ArrayList<>());
                    Production ppaa = new Production(newNonT, ppb.getRight().substring(1)+newNonT);
                    productions.get(newNonT).add(ppaa);//C->aC
                    idToProduction.put(ppaa.getId(),ppaa);
                    Production ppbb = new Production(newNonT, epsilon + "");
                    idToProduction.put(ppbb.getId(),ppbb);//C->ε
                    productions.get(newNonT).add(ppbb);
                    productions.put(ch,pLC);
                }
            }

        }
        removeNoUsefulProduction();
        return this;
    }


    /**
     * 求follow集合
     * A→X1 X2 ...Xi Xi+1 ...Xn
     * 1.初始化：
     * 	1.1 Follow(开始符号)={ $ }
     * 	1.2 其他任何一个非终结符号A，则执行 Follow(A)={ }
     * 2.循环：反复执行
     * 	2.1 循环：对于文法中的每条规则 A->X1 X2 ... Xn 都执行
     * 		2.1.1 对于该规则中的每个属于非终结符号的Xi ，都执行
     * 			2.1.1.1 把 First(Xi+1 Xi+2 ... Xn ) - {ε} 添加到 Follow(Xi)
     * 			2.1.1.2 if ε in First(Xi+1 Xi+2 ... Xn ),则把Follow(A)添加到 Follow(Xi)
     * 直到任何一个Follow集合的值都没有发生变化为止。
     */
    public void follow(){
        first();
        follow = new HashMap<>();
        for (Character nonT : nonTerminals) {
            follow.put(nonT,new HashSet<>());
        }
        follow.put(start,new HashSet<>());
        follow.get(start).add('$');

        final Boolean[] hasChange = {true};
        while (hasChange[0]){
            hasChange[0] = false;
            idToProduction.forEach(new BiConsumer<Integer, Production>() {
                @Override
                public void accept(Integer integer, Production production) {
                    String right = production.getRight();
                    for (int i = 0; i < right.length(); i++) {
                        if (nonTerminals.contains(right.charAt(i))){
                            int j;
                            for (j = i+1; j < right.length(); j++) {
                                Set<Character> cs = first.get(right.charAt(j));
                                boolean hasEpsilon = cs.contains(epsilon);
                                Set<Character> setA = new HashSet<>(cs);
                                setA.remove(epsilon);
                                hasChange[0] |= follow.get(right.charAt(i)).addAll(setA);
                                if (!hasEpsilon){
                                    break;
                                }
                            }
                            if (j>=right.length()){
                                hasChange[0] |= follow.get(right.charAt(i)).addAll(follow.get(production.getLeft()));
                            }
                        }
                    }
                }
            });
        }
    }



    /**
     * 去除左公因子
     * S->Ab|Ec|Cd|e
     * A->Df
     * D->af
     * E->afg|afh
     * C->afj
     * @return
     */
    public Grammar removeLeftCommonFactor(){
        boolean[] hasChanged = {true};
        while (hasChanged[0]){
            hasChanged[0] = false;
            Map<Character, List<Production>> newProMap = new HashMap<>(productions);

            /**
             * 每次只消除一个字符
             */
            productions.forEach(new BiConsumer<Character, List<Production>>() {
                @Override
                public void accept(Character character, List<Production> productionList) {
                    if (productionList.size()<=1){//两个及两个以上才有可能有左公因子
                        return;
                    }
                    if (hasChanged[0]){//消除了一个字符，退出本次遍历
                        return;
                    }
                    /**
                     * 第一次时，S->Ab|Ec|Cd|e
                     * 经下面的for循环处理后，
                     * proToLongTerminalSeq {
                     *  S->Ab : "affb"
                     *  S->Ec : "Ec"
                     *  S->Cd : "afjd"
                     *  S->e  : "e"
                     * }
                     * chToProSet {
                     *  'a' : {S->Ab, S->Cd}
                     *  'E' : {S->Ec}
                     *  'e' : {S->e}
                     * }
                     */
                    Map<Character,Set<Production>> chToProSet = new HashMap<>();
                    Map<Production,String> proToLongTerminalSeq = new HashMap<>();
                    for (int i = 0; i < productionList.size(); i++) {
                        StringBuilder stringBuilder = new StringBuilder();
                        longestSeq(productionList.get(i),stringBuilder);
                        String s = stringBuilder.toString();
                        proToLongTerminalSeq.put(productionList.get(i),s);
                        if (!"".equals(s)){
                            Set<Production> set = chToProSet.get(s.charAt(0));
                            if(set==null){
                                set = new HashSet<>();
                                chToProSet.put(s.charAt(0),set);
                            }
                            set.add(productionList.get(i));
                        }
                    }
                    /**
                     * chToProSet {
                     *  'a' : {S->Ab, S->Cd}
                     *  'e' : {S->e}
                     *  'E' : {S->Ec}
                     * }
                     * S->Cd : "afjd"
                     * S->Ab : "affb"
                     * 只有'a' : {S->Ab, S->Cd} 的size>1, 才执行
                     * {S->Ab, S->Cd} 变为
                     * S->aH
                     * H->fjd|ffb
                     */
                    chToProSet.forEach(new BiConsumer<Character, Set<Production>>() {
                        @Override
                        public void accept(Character cha, Set<Production> pros) {
                            if (pros.size()<=1){
                                return;
                            }
                            char newNonT = availableNonTerminalSign();
                            nonTerminals.add(newNonT);
                            newProMap.put(newNonT,new ArrayList<>());
                            Production paa = new Production(character, (cha+"") + (newNonT+""));
                            idToProduction.put(paa.getId(),paa);
                            hasChanged[0] |= newProMap.get(character).add(paa);
                            for (Production proAAA : pros) {
                                hasChanged[0] |= newProMap.get(character).removeIf(new Predicate<Production>() {
                                    @Override
                                    public boolean test(Production production) {
                                        return proAAA==production;
                                    }
                                });
                                idToProduction.remove(proAAA.getId());
                                if (proToLongTerminalSeq.get(proAAA).length()<=1){
                                    Production pbb = new Production(newNonT, "@");
                                    idToProduction.put(pbb.getId(),pbb);
                                    hasChanged[0] |= newProMap.get(newNonT).add(pbb);
                                } else {
                                    Production pbb = new Production(newNonT,proToLongTerminalSeq.get(proAAA).substring(1));
                                    idToProduction.put(pbb.getId(),pbb);
                                    hasChanged[0] |= newProMap.get(newNonT).add(pbb);
                                }
                            }
                        }
                    });

                }
            });
            productions = newProMap;
        }


        return this;
    }


    /**
     * S->Ab|Ec|Cd|e
     * A->Df
     * D->af
     * E->afg|afh
     * C->afj
     *
     * 上述文法，
     *  对于产生式 S->Ab，返回 affb
     *  对于产生式 S->Ec，返回 Ec
     *  对于产生式 S->Cd，返回 afjd
     *  对于产生式 S->e，返回 e
     * @param pa
     * @param stringBuilder
     * @return
     */
    private boolean longestSeq(Production pa,StringBuilder stringBuilder) {
        String right = pa.getRight();
        for (int i = 0; i < right.length(); i++) {
            char ch = right.charAt(i);
            if (ch==epsilon){
                return true;
            }
            if (terminals.contains(ch)){
                stringBuilder.append(ch);
            } else {
                List<Production> pL = this.productions.get(ch);
                if (pL.size()==1){
                    longestSeq(pL.get(0),stringBuilder);
                } else {
                    stringBuilder.append(ch);
                }
            }
        }
        return true;
    }


    /**
     *  可用的非终结符号
     * @return
     */
    private char availableNonTerminalSign(){
        for (char i = 'A'; i <= 'Z';i++) {
            if (!nonTerminals.contains(i)){
                return i;
            }
        }
        return '#';
    }

    /**
     * 对于规则X->x1 x2 … xn，first(X)的计算算法如下：
     * First(X)={ };
     * K=1;
     * While (k<=n)
     * { if （xk 为终结符号或 ε ） first(xk)=xk;
     *   first(X)=first(X) + first(xk) - {ε}
     *   If ( ε 不属于 first(xk) ) break;
     *   k++;
     * }
     * If (k==n+1) first(X)=first(X) + ε
     */
    public void first(){
        first = new HashMap<>();
        terminals.forEach(new Consumer<Character>() {
            @Override
            public void accept(Character character) {
                first.put(character,new HashSet<>());
                first.get(character).add(character);
            }
        });
        nonTerminals.forEach(new Consumer<Character>() {
            @Override
            public void accept(Character character) {
                first.put(character,new HashSet<>());
            }
        });
        first.put(epsilon,new HashSet<>());
        first.get(epsilon).add(epsilon);

        boolean[] hasChange = {true};
        while (hasChange[0]){
            hasChange[0] = false;
            productions.forEach(new BiConsumer<Character, List<Production>>() {
                @Override
                public void accept(Character character, List<Production> productionList) {
                    for (Production proA : productionList) {
                        String right = proA.getRight();
                        int i;
                        for (i = 0; i < right.length(); i++) {
                            char ch = right.charAt(i);
                            Set<Character> csA = first.get(ch);
                            Set<Character> newcs = new HashSet<>(csA);
                            newcs.remove(epsilon);
                            hasChange[0] |= first.get(character).addAll(newcs);
                            if (!csA.contains(epsilon)){
                                break;
                            }
                        }
                        if (i>=right.length()){
                            hasChange[0] |= first.get(character).add(epsilon);
                        }
                    }

                }
            });
        }

    }



    /**
     * 删除不可到达的产生式
     * 删除未在产生式右边出现过的非终结符
     */
    private void removeCannotArriveProduction(){
        Set<Character> setA = new HashSet<>(nonTerminals); //在产生式右边没出现过的非终结符号，初始认为所有都未在右边出现
        setA.remove(start);
        idToProduction.forEach(new BiConsumer<Integer, Production>() {
            @Override
            public void accept(Integer integer, Production production) {
                String right = production.getRight();
                for (int i = 0; i < right.length(); i++) {
                    char ch = right.charAt(i);
                    if (nonTerminals.contains(ch)){//若在右边出现，从setA中删除
                        setA.remove(ch);
                    }
                }

            }
        });

        for (Character character : setA) {
            nonTerminals.remove(character);
            List<Production> pLA = this.productions.getOrDefault(character,Collections.EMPTY_LIST);
            for (Production pa : pLA) {
                idToProduction.remove(pa.getId());
            }
            this.productions.remove(character);
        }
    }

    /**
     * 删除不可终止的产生式
     */
    private void removeCannotStopProduction(){
        Set<Production> setC = new HashSet<>();//可终止产生式集合
        Set<Production> setNC = new HashSet<>();//不可终止产生式集合
        idToProduction.forEach(new BiConsumer<Integer, Production>() {
            @Override
            public void accept(Integer integer, Production production) {
                generate0(setC,setNC,production,new HashSet<>());
            }
        });

        //重新构建一次
        setNC.clear();
        idToProduction.forEach(new BiConsumer<Integer, Production>() {
            @Override
            public void accept(Integer integer, Production production) {
                generate0(setC,setNC,production,new HashSet<>());
            }
        });


        Set<Character> hasOneCanStop = new HashSet<>();//决定是否从nonTerminals删除
        for (Production production : setC) {
            hasOneCanStop.add(production.getLeft());
        }

        for (Production production : setNC) {
            idToProduction.remove(production.getId());
            List<Production> pLA = productions.get(production.getLeft());
            if (pLA!=null){
                pLA.removeIf(new Predicate<Production>() {
                    @Override
                    public boolean test(Production pa) {
                        return pa==production;
                    }
                });
            }
            if (pLA!=null&&pLA.size()==0){
                productions.remove(production.getLeft());
            }
            if (!hasOneCanStop.contains(production.getLeft())){
                nonTerminals.remove(production.getLeft());
            }
        }
    }



    /**
     * 针对每条产生式，递归构建可终止非终结符、不可终止非终结符
     * @param setC  可终止产生式集合
     * @param setNC 不可终止产生式集合
     * @param production    当前产生式
     * @param used  已使用的产生式，须传入空集合
     * @return  该产生式是否能终止
     */
    private boolean generate0(Set<Production> setC,Set<Production> setNC, Production production, Set<Production> used){
        if (setC.contains(production)){
            return true;
        }
        if (setNC.contains(production)){
            return false;
        }
        if (used.contains(production)){
            return false;
        }
        used.add(production);//标记已使用，防止死递归
        String right = production.getRight();
        int i;
        for (i = 0; i < right.length(); i++) {
            char ch = right.charAt(i);
            if (nonTerminals.contains(ch)){
                List<Production> pL = this.productions.get(ch);
                boolean f = false;//ch是否可终止
                /*
                    例如，S->Ab|Ec|Cd|e
                    遍历S的所有产生式，若有一个产生式是可终结的，则S可终结
                 */
                for (Production pA : pL) {
                    f|=generate0(setC,setNC,pA,used);
                }
                if (!f){ //ch不可终止
                    break;
                }
            }
        }
        used.remove(production);
        if (i>=right.length()){//i
            setC.add(production);
            setNC.remove(production);
            return true;
        } else {
            setNC.add(production);
            return false;
        }
    }

    /**
     * 删除无效产生式，即删除有害产生式和多余产生式
     * @return
     */
    public Grammar removeNoUsefulProduction(){
        //0.删除有害产生式
        //...
        //1.删除不可到达产生式
        removeCannotArriveProduction();
        //2.删除不可终止产生式
        removeCannotStopProduction();
        return this;
    }

    /**
     * 产生式字符串化
     * @return
     */
    public String printProduction() {
        StringBuilder builder = new StringBuilder();
        productions.forEach(new BiConsumer<Character, List<Production>>() {
            @Override
            public void accept(Character character, List<Production> productionList) {
                builder.append(character+"->");
                for (Production production : productionList) {
                    builder.append(production.getRight()+"|");
                }
                builder.deleteCharAt(builder.length()-1);
                builder.append('\n');
            }
        });
        return builder.toString();
    }

    /**
     * first字符串化
     * @return
     */
    public String showFirst() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("first集合：\n");
        first.forEach(new BiConsumer<Character, Set<Character>>() {
            @Override
            public void accept(Character character, Set<Character> set) {
                stringBuilder.append(character).append('\t').append('\t').append("{");
                for (Character ch : set) {
                    stringBuilder.append(ch).append("  ,");
                }
                stringBuilder.deleteCharAt(stringBuilder.length()-1);
                stringBuilder.append("}\n");
            }
        });
        return stringBuilder.toString();
    }

    /**
     * follow字符串化
     * @return
     */
    public String showFollow() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("follow集合：\n");
        follow.forEach(new BiConsumer<Character, Set<Character>>() {
            @Override
            public void accept(Character character, Set<Character> set) {
                stringBuilder.append(character).append('\t').append('\t').append("{");
                for (Character ch : set) {
                    stringBuilder.append(ch).append("  ,");
                }
                stringBuilder.deleteCharAt(stringBuilder.length()-1);
                stringBuilder.append("}\n");
            }
        });
        return stringBuilder.toString();
    }



}
