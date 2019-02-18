import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.EnglishGrammaticalStructure;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TypedDependency;

////方法3思想：根据介词来切割标题，切割出的部分最后一个词是名词，则保留成名词短语

public class titleParser3 {
    static String modelpath = "edu/stanford/nlp/models/lexparser/englishFactored.ser.gz";
    // 分词、词性标注
    static LexicalizedParser lp = LexicalizedParser.loadModel(modelpath);

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        String str = "A fast method for identifying worldwide scientific collaborations using the Scopus database";
        readFileTitle();
        // createTree(str);
        long endTime = System.currentTimeMillis();
        System.out.println("SpendTime : " + (endTime - startTime) + " MillSeconds");
    }

    public static void readFileTitle() {
        String lineTxt = null;
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(new File("D:\\标题关键字提取\\titleInfo.txt")), "UTF-8"));

            while ((lineTxt = br.readLine()) != null) {
                createTree(lineTxt);
                System.out.println("finish");
            }
            br.close();
        } catch (Exception e) {
            System.out.println("read errors: " + lineTxt);

        }
    }

    public static void createTree(String str) {// 构建树
        int startPRE=0;
        int endPRE=0;
        Tree t;
        if(str.substring(str.length() - 1, str.length()).equals("?")
                ||str.substring(str.length() - 1, str.length()).equals("!")
                ||str.substring(str.length() - 1, str.length()).equals(".")){
            str=str.substring(0,str.length()-1);
        }

        for(int i=0;i<str.length();i++) {
            if(str.charAt(i)=='(') {
                startPRE=i;
            }
            if(str.charAt(i)==')') {
                endPRE=i;
            }
        }
        if(startPRE!=endPRE) {
            String newStr=str.substring(0,startPRE)+" "+str.substring(endPRE+1,str.length());
            t = lp.parse(newStr);
        }else {
            t = lp.parse(str);
        }


        //t.pennPrint();// 输出树
        ArrayList al = (ArrayList) extractInfoFromTree(t);
        writeToFile(str,al);
    }



    public static void writeToFile(String str, ArrayList<String> al) {
        String filepath = "D:\\标题关键字提取\\Result3-1.txt";
        File targetFile = new File(filepath);

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile, true));
            writer.write(str);
            writer.write(";");
            int length = al.size();// length是一个标题名词短语的个数
            for (int i = 0; i < length; i++) {
                writer.write(al.get(i));
                if (i != length - 1) {
                    writer.write(";");
                }
            }
            writer.write("\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String[] createStopWordsList() {// 停用词表
        String[] stopwords = new String[896];// 停用词表共有896个词
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(new File("D:\\标题关键字提取\\停用词表.txt")), "UTF-8"));
            String lineTxt = null;
            int i = 0;
            while ((lineTxt = br.readLine()) != null) {
                stopwords[i] = lineTxt;
                i++;
            }
            br.close();
        } catch (Exception e) {
            System.err.println("read errors :" + e);
        }
        return stopwords;
    }

    public static boolean isStopWords(String str) {
        String[] stopWords = createStopWordsList();
        for (int i = 0; i < stopWords.length; i++) {
            if (stopWords[i].equals(str + "  "))
                return true;// true表示是停用词
        }
        return false;// false表示不是停用词
    }

    public static String[] createVBGWhiteList() {// 动名词白名单
        String[] whiteList = new String[27];// 白名单有八个词
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(new File("D:\\标题关键字提取\\动名词白名单.txt")), "UTF-8"));

            String lineTxt = null;
            int i = 0;
            while ((lineTxt = br.readLine()) != null) {
                whiteList[i] = lineTxt;
                i++;
            }
            br.close();
        } catch (Exception e) {
            System.err.println("read errors :" + e);
        }
        return whiteList;
    }

    public static boolean isInVBGWhiteList(String str) {
        String[] VBGwhiteList = createVBGWhiteList();
        for (int i = 0; i < VBGwhiteList.length; i++) {
            if (VBGwhiteList[i].equals(str)) {
                return true;
            }
        }
        return false;
    }

    public static List<String> extractInfoFromTree(Tree t) {// 从树中抽取信息
        List<Tree> tLeaves = t.getLeaves();// 树的叶子节点列表
        Vector<Tree> vt = new Vector(10, 2);
        List<String> al = new ArrayList();
        int startPoint = 0;
        int endPoint = 0;

        List<Coordinate> ls = new ArrayList<Coordinate>();// ls二维列表存储了每一个词和它对应的标签
        for (int i = 0; i < tLeaves.size(); i++) {
            String word = tLeaves.get(i).label().toString();
            String label = tLeaves.get(i).ancestor(1, t).label().toString();
            Coordinate co = new Coordinate(word, label);
            ls.add(co);
        }

        // 根据介词切割句子
        for (int k = 0; k < ls.size(); k++) {
            StringBuilder strBuilder = new StringBuilder();
            if (((ls.get(k).getLabel().equals("IN")||ls.get(k).getLabel().equals("TO"))&&k!=0)
                    ||ls.get(k).getWord().equals("?")
                    ||ls.get(k).getWord().equals(".")
                    ||ls.get(k).getWord().equals("!")
                    ||ls.get(k).getWord().equals(":")//这四个标点符号也可以进行分割
                    ||k == ls.size() - 1) {
                //遇到介词切割时，endPoint指针为介词所在的位置，遇到句尾结束时，endPoint指针为句尾最后一个词的位置+1，因为句尾最后一个词也需要分析（已经提前删除了句尾的符号）
                endPoint = (k == ls.size() - 1) ? k + 1 : k;

                if ((ls.get(endPoint - 1).getLabel().length()>1&&ls.get(endPoint - 1).getLabel().subSequence(0, 2).equals("NN"))
                        || (ls.get(endPoint - 1).getLabel().equals("VBG") && isInVBGWhiteList(ls.get(endPoint - 1).getWord().toLowerCase()))
                        ) {//介词前一个词是名词或者可以做名词的动名词
                    for (int z = startPoint; z < endPoint; z++) {
                        String word = ls.get(z).getWord();
                        if (!isStopWords(word.toLowerCase())) {// 过滤停用词
                            strBuilder.append(word + " ");
                        }
                    }
                    //System.out.println(strBuilder.toString());
                    if(strBuilder.toString().length()!=0) {
                        al.add(strBuilder.toString().substring(0, strBuilder.toString().length() - 1));
                    }
                    startPoint = endPoint + 1;
                    strBuilder.delete(0, strBuilder.length());
                } else if((ls.get(endPoint - 1).getLabel().equals("VBN")||ls.get(endPoint - 1).getLabel().equals("VBD"))
                        &&ls.get(endPoint - 1).getWord().subSequence(ls.get(endPoint-1).getWord().length()-2, ls.get(endPoint -1).getWord().length()).equals("ed")
                        &&ls.get(endPoint - 2).getLabel().length()>1
                        &&ls.get(endPoint - 2).getLabel().subSequence(0, 2).equals("NN")) {// 介词前面是过去分词，过去分词是和介词组成介词短语
                    for (int z = startPoint; z < endPoint - 1; z++) {
                        String word = ls.get(z).getWord();
                        if (!isStopWords(word.toLowerCase())) {// 过滤停用词
                            strBuilder.append(word + " ");
                        }
                    }
                    //System.out.println(strBuilder.toString());
                    if(strBuilder.toString().length()!=0) {
                        al.add(strBuilder.toString().substring(0, strBuilder.toString().length() - 1));
                    }
                    startPoint = endPoint + 1;
                    strBuilder.delete(0, strBuilder.length());
                } else {
                    startPoint = endPoint + 1;
                }
            }
        }
        return al;

		/*
		 * for(int k=0;k<ls.size();k++) {
		 * while(k<ls.size()&&!ls.get(k).getLabel().equals("IN")) { String word =
		 * ls.get(k).getWord(); if(!isStopWords(word.toLowerCase())) {
		 * strBuilder.append(word+" "); } k++; }
		 * System.out.println(strBuilder.toString());
		 * strBuilder.delete(0,strBuilder.length()); }
		 */

		/*EnglishGrammaticalStructure gs = new EnglishGrammaticalStructure(t);
		Collection<TypedDependency> tdl = gs.typedDependenciesCollapsed();

		String s1 = "";
		System.out.println(tdl.size());
		for (int i = 0; i < tdl.size(); i++) {
			TypedDependency td = (TypedDependency) tdl.toArray()[i];
			String age = td.dep().toString();
			s1 += age + "/";
			s1 += " ";
		}

		System.out.println(s1);*/
    }

}

