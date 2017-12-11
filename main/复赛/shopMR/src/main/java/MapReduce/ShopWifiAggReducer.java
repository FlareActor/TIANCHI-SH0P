package MapReduce;

import Utils.Serializer;
import Utils.Sorted;
import Utils.Statistic;
import com.aliyun.odps.data.Record;
import com.aliyun.odps.mapred.ReducerBase;

import java.io.IOException;
import java.util.*;

/**
 * Created by wangdexun on 2017/11/23.
 * 将每个shop的wifi记录整合，相同id求均值(最大值)，再按照强度降序
 */
public class ShopWifiAggReducer extends ReducerBase {

    private Record result;

    @Override
    public void setup(TaskContext context) throws IOException {
        result = context.createOutputRecord();
    }

    @Override
    public void reduce(Record key, Iterator<Record> values, TaskContext context) throws IOException {
        Map<String, List<Integer>> wifiMap = new HashMap<String, List<Integer>>();
        Record record;
        String bssid;
        Integer strength;
        while (values.hasNext()) {
            record = values.next();
            bssid = record.getString("bssid");
            strength = Integer.valueOf(record.getString("strength"));
            if (wifiMap.containsKey(bssid)) {
                wifiMap.get(bssid).add(strength);
            } else {
                List<Integer> strengthList = new ArrayList<Integer>();
                strengthList.add(strength);
                wifiMap.put(bssid, strengthList);
            }
        }
        Map<String, Integer> wifiMaxAggMap = new HashMap<String, Integer>();
        Map<String, Double> wifiMeanAggMap = new HashMap<String, Double>();
        for (String i : wifiMap.keySet()) {
            wifiMaxAggMap.put(i, Statistic.max(wifiMap.get(i)));
            wifiMeanAggMap.put(i, Statistic.mean(wifiMap.get(i)));
        }
        // 按照wifi强度降序排序
        List<Map.Entry<String, Integer>> wifiMaxList = Sorted.sort(wifiMaxAggMap);
        List<Map.Entry<String, Double>> wifiMeanList = Sorted.sort(wifiMeanAggMap);
        // 写入结果
        result.setString("shop_id", key.getString("shop_id"));
        result.setString("shop_max_agg_wifi", Serializer.serialize(wifiMaxList));
        result.setString("shop_mean_agg_wifi", Serializer.serialize(wifiMeanList));
        context.write(result);
    }

}
