import java.util.ArrayList;
import java.util.List;


import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.FileWriter;
import java.io.IOException;

public class titleParserToFile {

    public static void main(String[] args) {
        Long startTime = System.currentTimeMillis();
        readTitleFile();
        Long endTime = System.currentTimeMillis();
        System.out.println("Spend Time :" +(endTime-startTime));
    }

    public static void createTree(String str) {
        String modelpath = "edu/stanford/nlp/models/lexparser/englishFactored.ser.gz";
        // 分词、词性标注
        LexicalizedParser lp = LexicalizedParser.loadModel(modelpath);
        Tree t = lp.parse(str);
        ArrayList<String> al = trr(t);
        write(str,al);
    }

    public static void write(String str,ArrayList<String> keywords) {
        String filepath="D:\\标题关键字提取\\Result1.txt";
        File targetFile = new File(filepath);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile,true));
            writer.write(str);

            writer.write(";");
            int length = keywords.size();
            for(int i=0;i<length;i++) {
                writer.write(keywords.get(i));
                if(i != length -1) {
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

    public static String[] createStopWordsList() {//停用词表
        String[] stopwords =new String[897];//停用词表共有897个词
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(new File("D:\\标题关键字提取\\停用词表.txt")), "UTF-8"));
            String lineTxt = null;
            int i=0;
            while ((lineTxt = br.readLine()) != null) {
                stopwords[i]=lineTxt;
                i++;
            }
            br.close();
        } catch (Exception e) {
            System.err.println("read errors :" + e);
        }
        return stopwords;
    }

    public static void readTitleFile() {
        String filepath="D:\\标题关键字提取\\savedrecs - 副本.txt";
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(new File(filepath)), "UTF-8"));
            String lineTxt = null;
            String lineNext = null;
            int i=0;
            while ((lineTxt = br.readLine()) != null) {
                if(lineTxt.startsWith("TI")) {
                    lineTxt = lineTxt.substring(3);
                    while((lineNext = br.readLine()).startsWith("   ")) {
                        lineTxt = lineTxt +" " + lineNext.substring(3);
                    }
                    System.out.println(lineTxt);
                    createTree(lineTxt);
                    //System.out.println("finish");
                }
            }
            br.close();
        } catch (Exception e) {
            System.err.println("read errors :" + e);
        }
    }



    @SuppressWarnings("null")
    public static ArrayList<String> trr(Tree t) {
        List<Tree> trLeaf = t.getLeaves();

        List<String> keywordsList = null;
        ArrayList<String> al = new ArrayList();
        String[] stopwords = createStopWordsList();//停用词表
        int stopwordsLen=stopwords.length;

        t.pennPrint();//输出树的层级结构
        Tree Tn = null;
        //System.out.println("check point 1");
        for (int i = 0; i < trLeaf.size(); i++) {
            StringBuilder strBuilder = new StringBuilder();
            Tn = trLeaf.get(i);
            boolean isStopWords = false;//停用词标志

            //如果Tn.label()不是停用词，则判断2级上位是不是NP
            for(int b=0;b<stopwordsLen;b++) {
                if(Tn.label().toString().toLowerCase().equals(stopwords[b].trim())) {//如果是停用词
                    isStopWords = true;//标志置为true；
                    //System.out.println("check point 2");
                    break;//跳出循环
                }
            }

            if(isStopWords==false
                    &&(Tn.ancestor(2, t).label().toString().equals("NP")
                    ||Tn.ancestor(2, t).label().toString().equals("NX"))
                    ) {//不是停用词时，需要匹配NP
                //System.out.println("execute to here");
                Tn = Tn.ancestor(2, t);
                int count = 0;
                List<Tree> TnLeaf = Tn.getLeaves();
                for(int j=0;j<TnLeaf.size();j++) {
                    boolean isStopWordsCheck2 = false;
                    for(int a = 0;a<stopwordsLen;a++) {
                        if(TnLeaf.get(j).label().toString().toLowerCase().equals(stopwords[a].trim())) {
                            isStopWordsCheck2 = true;
                            break;
                        }
                    }
                    if(Tn.depth(TnLeaf.get(j))==2) {
                        if(isStopWordsCheck2 ==false) {
                            strBuilder.append(TnLeaf.get(j).label().toString());
                            strBuilder.append(" ");
                        }
                        count++;
                    }
                }
                System.out.println(strBuilder.toString());
                //keywordsList.add(strBuilder.toString());
                al.add(strBuilder.toString());
                i = i + count;
            }
        }
        return al;
    }
}
