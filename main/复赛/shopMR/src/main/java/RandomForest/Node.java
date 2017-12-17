package RandomForest;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * 决策树的结点对象
 */
public class Node {

    // 该结点分裂的特征列索引
    public int col;
    // 该结点分裂的特征的值
    public double value;
    // 保存结点对应的数据集中各类label及其比例："label1,ratio1","label2,ratio2",..."labelN,ratioN"
    // 只有叶子结点才有多个值，中间结点size==0
    public List<String> results = new ArrayList<String>();
    // 左子树
    public Node trueB = null;
    // 右子树
    public Node falseB = null;
    // 该结点在树中的深度
    public int depth;

    public Node() {
        // TODO Auto-generated constructor stub
    }

    /**
     * 释放引用
     */
    public void clear() {
        trueB = null;
        falseB = null;
        results.clear();
        results = null;
    }

    public void sort() {
        Collections.sort(results, new Comparator<String>() {
            public int compare(String s0, String s1) {
                return Double.valueOf(s1.split(",", -1)[1]).compareTo(Double.valueOf(s0.split(",", -1)[1]));
            }
        });
    }

    /**
     * 将排序后的results复制到新的list并返回
     *
     * @return
     */
    public List<String> getLabel_prob() {
        sort();
        List<String> res = new ArrayList<String>();
        if (results.size() == 0) {
            return res;
        }
        for (int i = 0; i < results.size(); i++) {
            res.add(results.get(i));
        }
        return res;
    }

    /**
     * 序列化
     *
     * @return
     */
    public String toString() {
        String nodeinfo = col + "|" + value + "|";
        for (int i = 0; i < results.size(); i++) {
            nodeinfo += results.get(i) + ";";
        }
        nodeinfo += "|" + depth;
        return nodeinfo;
    }

    /**
     * Root节点调用该方法"非递归"遍历所有子节点并序列化
     * 没有采用递归的原因：可能栈溢出
     * <meta> 1:2,3|2:,,|3:4,5|...#index|nodeinfo@index|nodeinfo
     *
     * @return
     */
    public String save() {
        LinkedList<Node> needlist = new LinkedList<Node>();
        needlist.add(this); // 增加本结点
        Map<Integer, String> indexNode = new HashMap<Integer, String>();
        Map<Integer, String> treeStruct = new HashMap<Integer, String>();
        int pos = 0;
        // 广度优先搜索替代递归，避免了反复的函数调用入栈
        while (needlist.size() > pos) {
            Node temp = needlist.get(pos);
            String indextrue = "";
            String indexfalse = "";
            if (temp.trueB != null) {
                indextrue = "" + needlist.size();
                needlist.add(temp.trueB);
            }
            if (temp.falseB != null) {
                indexfalse = "" + needlist.size();
                needlist.add(temp.falseB);
            }
            String struct = indextrue + "," + indexfalse;
            treeStruct.put(pos, pos + ":" + struct);
            indexNode.put(pos, pos + "|" + temp.toString());
            pos++;
        }
        String res = StringUtils.join(treeStruct.values(), "|") + "#" + StringUtils.join(indexNode.values(), "@");
        return res;
    }

    /**
     * 反序列化生成决策树
     *
     * @param s
     * @return
     */
    public static Node load(String s) {
        Map<Integer, Node> indexNode = new HashMap<Integer, Node>();
        String[] array = s.split("#", -1);
        String meta = array[0];
        String info = array[1];
        for (String struct : info.split("@", -1)) {
            Node n = new Node();
            String[] array2 = struct.split("\\|", -1);
            int index = Integer.valueOf(array2[0]);
            n.col = Integer.valueOf(array2[1]);
            n.value = Double.valueOf(array2[2]);
            n.depth = Integer.valueOf(array2[4]);
            if (array2[3].length() > 0) {
                for (String resvalue : array2[3].split(";", -1)) {
                    if (resvalue.length() > 0) {
                        n.results.add(resvalue);
                    }
                }
            }
            indexNode.put(index, n);
        }

        for (String struct : meta.split("\\|", -1)) {
            String[] array2 = struct.split(":", -1);
            int index = Integer.valueOf(array2[0]);
            Node n = indexNode.get(index);
            if (array2[1].split(",", -1)[0].length() > 0) {
                int indext = Integer.valueOf(array2[1].split(",", -1)[0]);
                n.trueB = indexNode.get(indext);
            }
            if (array2[1].split(",", -1)[1].length() > 0) {
                int indexr = Integer.valueOf(array2[1].split(",", -1)[1]);
                n.falseB = indexNode.get(indexr);
            }
        }
        // 返回Root结点
        return indexNode.get(0);
    }

    public static void main(String[] args) {  //test
        Node n = new Node();
        n.results.add("21323,0.3");
        n.results.add("aaa,0.1");
        n.results.add("bbb,0.4");
        n.col = 213;
        n.value = 99.0;
        Node n2 = new Node();
        n.trueB = n2;
        n2.col = 000;
        Node n3 = new Node();
        n2.falseB = n3;
        n2.col = 020;
        Node n4 = new Node();
        n.falseB = n4;
        n4.col = 300;
        n4.value = -98;
        n4.results.add("as3dhuada,0.98");
        System.out.println(n.getLabel_prob());
        System.out.println(n.save());
        System.out.println(Node.load(n.save()).save());
        System.out.println(Math.floor(0.10 * 50.3));
    }
}
