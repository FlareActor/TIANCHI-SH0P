package RandomForest;

import java.util.*;

/**
 * 随机森林对象
 */
public class Rf {

    // 最小特征数
    public int minfeat = 80;
    // 树的棵树
    public int nTree;
    // 保存若干棵树（根结点）的列表
    public List<Node> treeList = new ArrayList<Node>();
    // 决策树的最大深度
    public int maxDepth = 150;

    public Rf() {
        // TODO Auto-generated constructor stub
    }

    /**
     * 按照某个特征的取值，将数据集分为两部分，大于该值的样本放入左集，小于放入右集
     *
     * @param samples
     * @param columnIndex
     * @param value
     * @return
     */
    public List<double[]>[] divideSet(List<double[]> samples, int columnIndex, double value) {
        List<double[]> tList = new ArrayList<double[]>();
        List<double[]> fList = new ArrayList<double[]>();
        for (double[] row : samples) {
            if (row[columnIndex] >= value) {
                tList.add(row);
            } else {
                fList.add(row);
            }
        }
        return new List[]{tList, fList};
    }

    /**
     * 统计数据集中label标签及其比率：["label1,ratio1","label2,ratio2"...]
     *
     * @param samples
     * @return
     */
    public List<String> uniqueCounts(List<double[]> samples) {
        Map<Double, Integer> res = new HashMap<Double, Integer>();
        int all = 0;
        for (double[] row : samples) {
            if (!res.containsKey(row[row.length - 1])) {
                res.put(row[row.length - 1], 0);
            }
            res.put(row[row.length - 1], res.get(row[row.length - 1]) + 1);
            all += 1;
        }
        List<String> reslist = new ArrayList<String>();
        for (Double key : res.keySet()) {
            reslist.add(key + "," + res.get(key) * 1.0 / all);
        }
        return reslist;
    }

    /**
     * 计算数据集的基尼指数
     * gini=1-(nb_cls1**2+nb_cls2**2+...+nb_clsN**2)/nb_total**2
     * =1-(p1**2+p2**2+...+pN**2)
     *
     * @param samples： 训练集
     * @return Gini Index
     */
    public double giniEstimate(List<double[]> samples) {
        if (samples.size() == 0)
            return 0;
        int total = samples.size();
        List<String> valuecounts = uniqueCounts(samples);
        double gini = 0.0;
        for (String value : valuecounts) {
            gini = gini + Math.pow(Double.valueOf(value.split(",", -1)[1]) * samples.size(), 2);
        }
        gini = 1 - gini / Math.pow(total, 2);
        return gini;
    }

    /**
     * 构建树
     *
     * @param samples:  训练集
     * @param colRange: 构造决策树时，需要考虑的特征的索引
     */
    public void buildTree(List<double[]> samples, List<Integer> colRange) {
        Node root = new Node();
        if (samples.size() == 0) {
            treeList.add(root);
            return;
        }
        // 保存每一个结点对应的数据集
        LinkedList<List<double[]>> samplelist = new LinkedList<List<double[]>>();
        samplelist.add(samples);
        root.depth = 0;
        LinkedList<Node> needlist = new LinkedList<Node>();
        needlist.add(root);
        int pos = 0;
        // 广度优先搜索代替递归
        while (needlist.size() > pos) {
            Node n = needlist.get(pos);
            List<double[]> currentSample = samplelist.get(pos);
            double cgini = giniEstimate(currentSample);
            double bestGain = 0;
            int bestcol = -1;
            double bestCriteria = 0.0;
            List<double[]> bestTList = null;
            List<double[]> bestFList = null;
            Collections.shuffle(colRange);
            List<Integer> colRange2 = new ArrayList<Integer>();
            int colnum = Math.min(Math.max(minfeat, (int) Math.ceil(Math.sqrt(colRange.size() * 1.0))), colRange.size());
            for (int i = 0; i < colnum; i++) {
                colRange2.add(colRange.get(i));
            }
            int count = 0;
            // 遍历每个待选特征
            for (int i : colRange2) {
                count += 1;
                Set<Double> values = new HashSet<Double>();
                List<Double> valuelist = new ArrayList<Double>();
                // 将数据集中该特征的所有可能取值（去重）保存下来
                for (double[] row : currentSample) {
                    if (!values.contains(row[i])) {
                        valuelist.add(row[i]);
                    }
                    values.add(row[i]);
                }
                // 对该特征排序
                Collections.sort(valuelist);
                // 取10%～90%的数据
                int leftlim = (int) Math.floor(0.10 * valuelist.size()); // 贪心优化
                int rightlim = (int) Math.ceil(0.90 * valuelist.size());
                List<Double> valuelist2 = new ArrayList<Double>();
                for (int j = leftlim; j < rightlim; j++) {
                    valuelist2.add(valuelist.get(j));
                }
                Collections.shuffle(valuelist2);
                // TODO 并不考虑该特征的所有取值，而是随机抽取一些
                for (int j = 0; j < Math.ceil(Math.min(25.0, 0.7 * valuelist2.size())); j++) {
                    List<double[]>[] reslist = divideSet(currentSample, i, valuelist2.get(j));
                    List<double[]> tlist = reslist[0];
                    List<double[]> flist = reslist[1];
                    if (flist.size() < 2 || tlist.size() < 2) {
                        continue;
                    }
                    double gain = cgini - (flist.size() * giniEstimate(flist) + tlist.size() * giniEstimate(tlist)) / (currentSample.size());
                    if (gain > bestGain) {
                        bestGain = gain;
                        bestCriteria = valuelist2.get(j);
                        bestcol = i;
                        bestFList = flist;
                        bestTList = tlist;
                    }
                }
                if (count % 20 == 0 && bestGain > 0) {  // 贪心优化
                    break;
                }
            }
            if (bestGain > 0 && n.depth < maxDepth) {
//				System.out.println(bestTList.size() + ","+ bestFList.size()+","+bestCriteria+","+cgini+","+(cgini - bestGain));
                Node tn = new Node();
                tn.depth = n.depth + 1;
                Node fn = new Node();
                fn.depth = n.depth + 1;
                n.col = bestcol;
                n.value = bestCriteria;
                n.trueB = tn;
                n.falseB = fn;
                // 入队
                samplelist.add(bestTList);
                needlist.add(tn);
                samplelist.add(bestFList);
                needlist.add(fn);
            } else {
                // 该结点为叶子结点，应该保存各类的置信度(比例)
                n.results = uniqueCounts(currentSample);
            }
            pos++;
        }
        for (List<double[]> sample : samplelist) {
            sample.clear();
        }
        samplelist.clear();
        needlist.clear();
        treeList.add(root);
    }

    /**
     * 通过BootStrap抽样法返回训练子集
     *
     * @param samples
     * @return
     */
    public List<double[]> generateBootstrapSamples(List<double[]> samples) {
        List<double[]> boostsample = new ArrayList<double[]>();
        for (int i = 0; i < Math.min(30000, (int) (Math.ceil(samples.size() * 0.7))); i++) {  // 样本max数目为30000
            boostsample.add(samples.get(new Random().nextInt(samples.size())));
        }
        return boostsample;
    }

    /**
     * 训练模型
     *
     * @param samples
     */
    public void fit(List<double[]> samples) {
        if (samples.size() == 0) {
            return;
        }
        for (int i = 0; i < nTree; i++) {
            // 抽样训练集
            List<double[]> sampleboots = generateBootstrapSamples(samples);
            int colCount = samples.get(0).length - 1;
            List<Integer> cols = new ArrayList<Integer>();
            for (int j = 0; j < colCount; j++) {
                cols.add(j);
            }
            // 打乱特征顺序
            Collections.shuffle(cols);
            List<Integer> colRange = new ArrayList<Integer>();
            for (int j = 0; j < Math.min(colCount * 0.7, 500); j++) {
                colRange.add(cols.get(j));
            }
            // 构造单棵决策树
            buildTree(sampleboots, colRange);
            sampleboots.clear();
            System.out.println("tree:" + i);
        }
    }

    /**
     * 单棵决策树对一条样本预测
     *
     * @param sample 一条样本
     * @param tree   单棵决策树
     * @return
     */
    public List<String> predict_tree_prob(double[] sample, Node tree) {
        Node temp = tree;
        while (true) {
            if (temp.results.size() != 0) {
                // 已经寻找到叶子结点了
                break;
            }
            double v = sample[temp.col];
            if (v >= temp.value) {
                temp = temp.trueB;
            } else {
                temp = temp.falseB;
            }
        }
        return temp.getLabel_prob();
    }

    /**
     * RF预测测试集(多棵决策树投票)
     *
     * @param sample
     * @param topn   每条样本取topN分类概率最大的类别
     * @return
     */
    public List<String> predict_probe(List<double[]> sample, int topn) {
        List<String> res = new ArrayList<String>();
        // 对每一个样本预测
        for (int i = 0; i < sample.size(); i++) {
            // Key:label,Value:probaSum
            Map<String, Double> prob = new HashMap<String, Double>();
            for (int j = 0; j < nTree; j++) {
                List<String> temp = predict_tree_prob(sample.get(i), treeList.get(j));
                for (String resAndProb : temp) {
                    String array[] = resAndProb.split(",", -1);
                    if (!prob.containsKey(array[0])) {
                        prob.put(array[0], 0.0);
                    }
                    prob.put(array[0], prob.get(array[0]) + Double.valueOf(array[1]) / nTree);
                }
            }
            List<String> problist = new ArrayList<String>();
            for (String key : prob.keySet()) {
                problist.add(key + "," + prob.get(key));
            }
            Collections.sort(problist, new Comparator<String>() {
                public int compare(String s0, String s1) {
                    return Double.valueOf(s1.split(",", -1)[1]).compareTo(Double.valueOf(s0.split(",", -1)[1]));
                }
            });
            String resstr = "";
            for (int j = 0; j < topn && j < problist.size(); j++) {
                resstr += problist.get(j) + "|";
            }
            res.add(resstr);
        }
        return res;
    }

    public List<String> save() {
        List<String> res = new ArrayList<String>();
        for (int i = 0; i < nTree; i++) {
            res.add(treeList.get(i).save());
        }
        return res;
    }

    public static Rf load(List<String> input, int depth) {  // 反序列化生成森林
        Rf r = new Rf();
        r.maxDepth = depth;
        for (int i = 0; i < input.size(); i++) {
            r.nTree += 1;
            r.treeList.add(Node.load(input.get(i)));
        }
        return r;
    }

    /**
     * 释放引用
     */
    public void clear() {
        for (Node n : treeList) {
            n.clear();
        }
        treeList.clear();
        treeList = null;
    }


    public static void main(String[] args) {  //test
        List<double[]> samples = new ArrayList<double[]>();
        for (int i = 0; i < 5000; i++) {
            samples.add(new double[]{-1.0, 1.5, 3.0, 4.0, -3.0});
            samples.add(new double[]{-5.0, 2.0, 3.0, 4.0, -1.0});
            samples.add(new double[]{1.0, 2.0, 3.0, 4.0, 1.0});
            samples.add(new double[]{1.0, 0.0, 3.0, 4.0, 2.0});
            samples.add(new double[]{5.0, 2.0, 3.0, 4.0, 5.0});
            samples.add(new double[]{1.0, 1.5, 3.0, 4.0, 2.0});
            samples.add(new double[]{5.0, 2.0, 3.0, 4.0, 5.0});
        }
        Rf r = new Rf();
        r.nTree = 2;
        r.fit(samples);
        List<double[]> samples2 = new ArrayList<double[]>();
        samples2.add(new double[]{5.0, 2.0, 3.0, 4.0});
        samples2.add(new double[]{4.0, 2.5, 3.0, 4.0});
        System.out.println(r.predict_probe(samples2, 2));
        for (String i : r.save()) {
            System.out.println(i);
        }
    }
}
