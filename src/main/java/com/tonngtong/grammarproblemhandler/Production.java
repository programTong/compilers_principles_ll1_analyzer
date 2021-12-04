package com.tonngtong.grammarproblemhandler;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 产生式类，或者说文法规则
 */
@Setter
@Getter
public class Production {
    private static int count = 1;//id起始
    private int id; // 唯一标识
    //例如：S->Abc
    private char left;// S
    private String right; //Abc
    private int rightLen;
    public Production(char l,String r) {
        id = count++;
        left = l;
        right = r;
        rightLen = r.length();
    }


    /**
     * 根据字符串List生成产生式List
     * @param productionStrList
     * @return
     */
    public static List<Production> generateList(List<String> productionStrList){
        List<Production> res = new ArrayList<>();
        for (String s : productionStrList) {
            s = s.replaceAll(" ","");
            String[] split = s.split("->");
            if (split.length==2){
                String[] split1 = split[1].split("\\|");
                for (String s1 : split1) {
                    res.add(new Production(split[0].charAt(0),s1));
                }
            }
        }
        return res;
    }

    @Override
    public String toString() {
        return "Production{" +
                "id=" + id +
                ", left=" + left +
                ", right='" + right + '\'' +
                ", rightLen=" + rightLen +
                '}';
    }
}

