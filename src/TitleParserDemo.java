import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.Tree;

//本程序考虑更复杂的规则来匹配名词短语，即不考虑叶子节点的二级上层词性标注，直接考虑叶子节点的词性组合
//名词
//名词+名词+......(名词会被标注为NN、NNS、NNP、NNPS)
//形容词+形容词+...+名词+名词+...(形容词会被标注为JJ、JJR比较级、JJS最高级)
//名词+动词过去分词+名词(动词过去分词VBD)
//名词+动词现在分词+名词(VBG：present participate)
//形容词+动名词+名词(VBG:gerund)
//动词过去分词+形容词+形容词+...+名词+名词+...
//动名词+名词+名词+...  动名词前面不是介词
public class TitleParserDemo {

    public static void main(String[] args) throws  Exception{
        String str = "Calibration of peripheral perception of shape with and without saccadic eye movements";
        createTree(str);
    }



    public static void createTree(String str) throws InterruptedException {
        String modelpath = "edu/stanford/nlp/models/lexparser/englishFactored.ser.gz";
        // 分词、词性标注
        LexicalizedParser lp = LexicalizedParser.loadModel(modelpath);
        Tree t = lp.parse(str);
        t.pennPrint();

        trr(t);
    }




    public static String[] createStopWordsList() {// 停用词表
        String[] stopwords = new String[897];// 停用词表共有895个词
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

    public static boolean isStopWords(String str){
        String[] stopWords=createStopWordsList();
        for(int i=0;i<stopWords.length;i++){
            if(stopWords[i].equals(str))
                return true;
        }
        return false;
    }

    public static ArrayList<String> trr(Tree t)  {
        Vector<Tree> vt = new Vector(10, 2);// 存储10个词的容器，若溢出，以一次2个宽度的速度增加
        List<Tree> tLeaves = t.getLeaves();
        ArrayList<String> al = new ArrayList();
        int senLen = tLeaves.size();// senLen是标题长度

        // System.out.println("senLen = " + senLen);
        int i = 0;// 用来遍历叶子节点的指针

        while (i < senLen) {
            boolean flag = false;
            StringBuilder strBuilder = new StringBuilder();
            // System.out.println("check point 1");
            //System.out.println("i = "+ i);
            if (tLeaves.get(i).ancestor(1, t).label().toString().length()>1
                    &&tLeaves.get(i).ancestor(1, t).label().toString().substring(0, 2).equals("NN")
                    && !isStopWords(tLeaves.get(i).label().toString())) {// 一个词是名词时
                vt.add(tLeaves.get(i));
                i++;

                // 下一个词可以是名词，动词过去分词，动词现在分词
                // 情况1：名词+名词+名词......
                if (i < senLen
                        &&tLeaves.get(i).ancestor(1, t).label().toString().length()>1
                        && tLeaves.get(i).ancestor(1, t).label().toString().substring(0, 2).equals("NN")) {
                    while (i < senLen
                            &&tLeaves.get(i).ancestor(1, t).label().toString().length()>1
                            &&tLeaves.get(i).ancestor(1, t).label().toString().substring(0, 2).equals("NN")
                            &&!isStopWords(tLeaves.get(i).label().toString())) {
                        vt.add(tLeaves.get(i));
                        i++;

                    }
                }
                // 情况2：名词+动词过去分词+名词
                else if (i < senLen -1
                        && tLeaves.get(i).ancestor(1, t).label().toString().length() > 2
                        && tLeaves.get(i + 1).ancestor(1, t).label().toString().length() > 1
                        && tLeaves.get(i).ancestor(1, t).label().toString().substring(0, 3).equals("VBD")
                        && tLeaves.get(i + 1).ancestor(1, t).label().toString().substring(0, 2).equals("NN")
                        && !isStopWords(tLeaves.get(i + 1).label().toString())) {
                    vt.add(tLeaves.get(i));
                    vt.add(tLeaves.get(i + 1));
                    i += 2;
                }
                // 情况3:名词+动词现在分词+名词
                else if (i < senLen -1
                        && tLeaves.get(i).ancestor(1, t).label().toString().length() > 2
                        &&tLeaves.get(i + 1).ancestor(1, t).label().toString().length() > 1
                        && tLeaves.get(i).ancestor(1, t).label().toString().substring(0, 3).equals("VBG")
                        && tLeaves.get(i + 1).ancestor(1, t).label().toString().substring(0, 2).equals("NN")
                        && !isStopWords(tLeaves.get(i + 1).label().toString())) {
                    vt.add(tLeaves.get(i));
                    vt.add(tLeaves.get(i + 1));
                    i += 2;
                } else {

                }
                // 输出basket的内容
                // System.out.println("Size = " + vt.size());
                for (int k = 0; k < vt.size(); k++) {
                    System.out.print(((Tree) vt.get(k)).label().toString() + " ");
                    strBuilder.append(((Tree) vt.get(k)).label().toString() + " ");
                }

                System.out.println();
                al.add(strBuilder.toString());
                vt.clear();
            }
            // 形容词开始
            else if (i < senLen
                    &&tLeaves.get(i).ancestor(1, t).label().toString().length()>1
                    &&tLeaves.get(i).ancestor(1, t).label().toString().substring(0, 2).equals("JJ")) {
                // System.out.println("check point 3");
                vt.add(tLeaves.get(i));
                i++;
                // 情况1：形容词+名词+名词+...
                if (i < senLen
                        &&tLeaves.get(i).ancestor(1, t).label().toString().length()>1
                        &&tLeaves.get(i).ancestor(1, t).label().toString().substring(0, 2).equals("NN")) {
                    while (i < senLen
                            &&tLeaves.get(i).ancestor(1, t).label().toString().length()>1
                            && tLeaves.get(i).ancestor(1, t).label().toString().substring(0, 2).equals("NN")
                            && !isStopWords(tLeaves.get(i).label().toString())) {
                        vt.add(tLeaves.get(i));
                        flag = true;
                        i++;
                    }
                }
                // 情况2：形容词+形容词+...+名词+名词...
                else if (i < senLen
                        &&tLeaves.get(i).ancestor(1, t).label().toString().length()>1
                        && tLeaves.get(i).ancestor(1, t).label().toString().substring(0, 2).equals("JJ")) {
                    while (i < senLen
                            &&tLeaves.get(i).ancestor(1, t).label().toString().length()>1
                            &&tLeaves.get(i).ancestor(1, t).label().toString().substring(0, 2).equals("JJ")) {
                        vt.add(tLeaves.get(i));
                        i++;
                    }
                    while (i < senLen
                            &&tLeaves.get(i).ancestor(1, t).label().toString().length()>1
                            && tLeaves.get(i).ancestor(1, t).label().toString().substring(0, 2).equals("NN")
                            && !isStopWords(tLeaves.get(i).label().toString())) {
                        vt.add(tLeaves.get(i));
                        flag = true;
                        i++;
                    }
                }
                // 情况3：形容词+动名词+名词
                else if (i < senLen
                        && tLeaves.get(i).ancestor(1, t).label().toString().length() > 2
                        && tLeaves.get(i + 1).ancestor(1, t).label().toString().length() > 1
                        && tLeaves.get(i).ancestor(1, t).label().toString().substring(0, 3).equals("VBG")
                        && tLeaves.get(i + 1).ancestor(1, t).label().toString().substring(0, 2).equals("NN")
                        && !isStopWords(tLeaves.get(i + 1).label().toString())) {
                    vt.add(tLeaves.get(i));
                    vt.add(tLeaves.get(i + 1));
                    flag = true;
                    i += 2;
                } else {
                    vt.clear();
                }
                // 输出basket内容
                if (!vt.isEmpty() && flag == true) {
                    for (int k = 0; k < vt.size(); k++) {
                        System.out.print(((Tree) vt.get(k)).label().toString() + " ");
                        strBuilder.append(((Tree) vt.get(k)).label().toString() + " ");
                    }
                    System.out.println();
                    al.add(strBuilder.toString());
                    // 清空basket
                    vt.clear();
                }
            } else if (i < senLen
                    &&tLeaves.get(i).ancestor(1, t).label().toString().length() > 2
                    && tLeaves.get(i).ancestor(1, t).label().toString().substring(0, 3).equals("VBD")) {
                vt.add(tLeaves.get(i));
                i++;
                if (i < senLen
                        && tLeaves.get(i).ancestor(1, t).label().toString().substring(0, 2).equals("JJ")) {
                    while (i < senLen
                            && tLeaves.get(i).ancestor(1, t).label().toString().substring(0, 2).equals("JJ")) {
                        vt.add(tLeaves.get(i));
                        i++;
                    }
                    while (i < senLen
                            && tLeaves.get(i).ancestor(1, t).label().toString().length()>1
                            && tLeaves.get(i).ancestor(1, t).label().toString().substring(0, 2).equals("NN")
                            && !isStopWords(tLeaves.get(i).label().toString())) {
                        vt.add(tLeaves.get(i));
                        i++;
                        flag = true;
                    }
                } else {
                    vt.clear();
                }
                if (!vt.isEmpty() && flag == true){
                    for (int k = 0; k < vt.size(); k++) {
                        System.out.print(((Tree) vt.get(k)).label().toString() + " ");
                        strBuilder.append(((Tree) vt.get(k)).label().toString() + " ");
                    }
                    System.out.println();
                    al.add(strBuilder.toString());
                }
                vt.clear();
            } else if (i < senLen
                    &&tLeaves.get(i).ancestor(1, t).label().toString().length() > 2
                    &&tLeaves.get(i).ancestor(1, t).label().toString().substring(0, 3).equals("VBG")) {
                if (i < senLen
                        && i > 0
                        && !tLeaves.get(i - 1).ancestor(1, t).label().toString().equals("IN")) {
                    vt.add(tLeaves.get(i));
                    i++;
                    while (i<senLen
                            &&tLeaves.get(i).ancestor(1, t).label().toString().length()>1
                            && tLeaves.get(i).ancestor(1, t).label().toString().substring(0, 2).equals("NN")
                            && !isStopWords(tLeaves.get(i).label().toString())) {
                        vt.add(tLeaves.get(i));
                        flag = true;
                        i++;
                    }
                } else if (i == 0) {
                    vt.add(tLeaves.get(i));
                    i++;
                    // System.out.println("i = " + i);
                    while (i < senLen
                            &&tLeaves.get(i).ancestor(1, t).label().toString().length()>1
                            && tLeaves.get(i).ancestor(1, t).label().toString().substring(0, 2).equals("NN")
                            && !isStopWords(tLeaves.get(i).label().toString())) {
                        vt.add(tLeaves.get(i));
                        i++;
                        flag = true;
                        // System.out.println("check point 5");
                    }
                } else {
                    vt.clear();
                    i++;
                }
                if (!vt.isEmpty() && flag == true) {
                    for (int k = 0; k < vt.size(); k++) {
                        System.out.print(((Tree) vt.get(k)).label().toString() + " ");
                        strBuilder.append(((Tree) vt.get(k)).label().toString() + " ");
                    }
                    al.add(strBuilder.toString());
                    System.out.println();
                }
                vt.clear();
            } else {
                i++;
                // System.out.println("check point 6");
            } // selective structure end
            // System.out.println("selective end");
        } // while loop end
        // System.out.println("loop end ") ;
        return al;
    }// method end
}// class end
