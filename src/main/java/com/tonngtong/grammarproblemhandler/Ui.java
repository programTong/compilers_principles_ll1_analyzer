package com.tonngtong.grammarproblemhandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class Ui {

    private JFrame jFrame = new JFrame("文法问题处理器");
    private JButton openFBtn = new JButton("打开文件");
    private JButton saveFBtn = new JButton("保存文件");
    private JButton rmLCFBtn = new JButton("消除左公因子");
    private JButton rmLRBtn = new JButton("消除左递归");
    private JButton simBtn = new JButton("化简文法");
    private JButton firFollBtn = new JButton("求first、follow集合");
    private JButton ll1Btn = new JButton("构建LL(1)分析表");
    private JButton leftDerivation = new JButton("输入句子进行最左推导");
    private JButton[] btns = {openFBtn,saveFBtn,rmLCFBtn, rmLRBtn,simBtn,firFollBtn,ll1Btn,leftDerivation};
    private JTextArea jTextArea = new JTextArea("31231231231231");
    private List<String> productionStrList = new ArrayList<>();
    private Grammar grammar;

    public Ui() {
        jTextArea.setEditable(false);
        GridBagLayout gridBagLayout = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10,10,10,10);

        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        for (int i = 0; i < btns.length; i++) {
            gbc.gridy = 2+i;
            gridBagLayout.setConstraints(btns[i],gbc);
        }

        JPanel leftP = new JPanel(gridBagLayout);
        for (int i = 0; i < btns.length; i++) {
            leftP.add(btns[i]);
        }
        openFBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.showOpenDialog(jFrame);
                File selectedFile = fileChooser.getSelectedFile();
                if (selectedFile==null){
                    return;
                }
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(selectedFile.getAbsolutePath()), StandardCharsets.UTF_8));
                    productionStrList.clear();
                    jTextArea.setText("");
                    String s;
                    while ((s=bufferedReader.readLine())!=null){
                        if (!"".equals(s)){
                            productionStrList.add(s);
                        }
                        jTextArea.append(s+'\n');
                    }
                    bufferedReader.close();
                    grammar = new Grammar(Production.generateList(productionStrList));
                } catch (Exception fileNotFoundException) {
                    fileNotFoundException.printStackTrace();
                }
            }
        });
        saveFBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String path = JOptionPane.showInputDialog("请输入保存位置");
                File file = new File(path);
                try {
                    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
                    bufferedWriter.write(jTextArea.getText());
                    bufferedWriter.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });



        rmLCFBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (grammar==null){
                    return;
                }
                grammar.removeLeftCommonFactor();
                jTextArea.setText(grammar.printProduction());
            }
        });

        rmLRBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (grammar==null){
                    return;
                }
                grammar.removeLeftRecursion();
                jTextArea.setText(grammar.printProduction());
            }
        });

        simBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (grammar==null){
                    return;
                }
                grammar.removeNoUsefulProduction();
                jTextArea.setText(grammar.printProduction());
            }
        });

        firFollBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (grammar==null){
                    return;
                }
                grammar.first();
                grammar.follow();
                jTextArea.setText(grammar.showFirst()+"\n"+grammar.showFollow());
            }
        });

        leftDerivation.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (grammar==null){
                    return;
                }
                String sentence = JOptionPane.showInputDialog("请输入要分析的句子");
                if (sentence!=null&&!"".equals(sentence)){
                    ParseTree parseTree = new ParseTreeImpl2(grammar,sentence);
                    jTextArea.setText(parseTree.getDerivationProcess().toString());
                }
            }
        });

        ll1Btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (grammar==null){
                    return;
                }
                grammar.ll1ParseTable();
                Map<Character, Map<Character, Set<Production>>> table = grammar.getLl1ParseTable();
                //1、消除二义性
                table.forEach(new BiConsumer<Character, Map<Character, Set<Production>>>() {
                    @Override
                    public void accept(Character cha, Map<Character, Set<Production>> characterSetMap) {
                        characterSetMap.forEach(new BiConsumer<Character, Set<Production>>() {
                            @Override
                            public void accept(Character character, Set<Production> pS) {
                                if (pS.size()<=1){
                                    return;
                                }
                                StringBuilder stringBuilder = new StringBuilder();
                                stringBuilder.append(cha).append("与").append(character).append("确定的结果为{");
                                Production[] pL = pS.toArray(new Production[0]);
                                stringBuilder.append(pL[0].getRight());
                                for (int i = 1; i < pL.length; i++) {
                                    stringBuilder.append(", ").append(pL[i].getRight());
                                }
                                stringBuilder.append("}\n");
                                stringBuilder.append("请输入结果集合的index来选择保留哪一个，注意index从0开始");
                                String res = JOptionPane.showInputDialog(stringBuilder.toString());
                                if (res!=null&&!"".equals(res)){
                                    int i = Integer.parseInt(res);
                                    if (i<pL.length){
                                        pS.removeIf(new Predicate<Production>() {
                                            @Override
                                            public boolean test(Production production) {
                                                return production!=pL[i];
                                            }
                                        });
                                    }
                                }
                            }
                        });
                    }
                });
                //2、jtable展示
                JFrame frame = new JFrame("LL1分析表");
                frame.setBounds(200,200,800,800);
                frame.setVisible(true);
                Map<Character,Integer> nonTToIndex = new HashMap<>();
                Map<Character,Integer> tToIndex = new HashMap<>();
                int colS = grammar.getTerminals().contains(Grammar.epsilon)?grammar.getTerminals().size()+1:grammar.getTerminals().size()+2;
                String[][] data = new String[grammar.getNonTerminals().size()][colS];
                String[] colNames = new String[colS];
                colNames[0] = "";
                int i = 1;
                for (Character t : grammar.getTerminals()) {
                    if (t==Grammar.epsilon){
                        continue;
                    }
                    colNames[i] = t+"";
                    tToIndex.put(t,i);
                    i++;
                }
                tToIndex.put('$',i);
                colNames[i] = "$";
                i = 0;
                for (Character nonT : grammar.getNonTerminals()) {
                    nonTToIndex.put(nonT,i);
                    data[i][0] = nonT+"";
                    i++;
                }
                table.forEach(new BiConsumer<Character, Map<Character, Set<Production>>>() {
                    @Override
                    public void accept(Character cha, Map<Character, Set<Production>> characterSetMap) {
                        characterSetMap.forEach(new BiConsumer<Character, Set<Production>>() {
                            @Override
                            public void accept(Character chb, Set<Production> productions) {
                                Production pa = productions.toArray(new Production[0])[0];
                                data[nonTToIndex.get(cha)][tToIndex.get(chb)] = pa.getRight();
                            }
                        });
                    }
                });
                JTable jTable = new JTable(data, colNames);
                JScrollPane jsp = new JScrollPane(jTable);
                frame.add(jsp);
            }
        });

        JScrollPane jScrollPane = new JScrollPane(jTextArea);
        jFrame.setLayout(new BorderLayout());
        jFrame.add(leftP,BorderLayout.WEST);
        jFrame.add(jScrollPane,BorderLayout.CENTER);
        jFrame.setBounds(100,100,1000,800);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setVisible(true);
    }


    public static void main(String[] args) {
        Ui ui = new Ui();
    }


}
