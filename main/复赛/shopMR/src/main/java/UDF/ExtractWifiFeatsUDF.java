package UDF;

import Base.BaseUDF;
import MapReduce.WifiFeats.WifiApk;
import MapReduce.WifiFeats.WifiDiff;
import MapReduce.WifiFeats.WifiRFD;
import Utils.Serializer;

import java.util.Map;

/**
 * Created by wangdexun on 2017/12/1.
 * 提取Wifi特征
 */
public class ExtractWifiFeatsUDF extends BaseUDF {
    public String evaluate(String userWifiStr, String shopMeanAggWifiStr, String shopMaxAggWifiStr) {
        Map<String, Double> userWifiMap = Serializer.deserialize(userWifiStr, null);
        Map<String, Double> shopMeanWifiMap = Serializer.deserialize(shopMeanAggWifiStr, userWifiMap);
        Map<String, Double> shopMaxWifiMap;
        if (shopMaxAggWifiStr != null)
            shopMaxWifiMap = Serializer.deserialize(shopMaxAggWifiStr, userWifiMap);
        else
            shopMaxWifiMap = shopMeanWifiMap;
        return WifiDiff.getInstance(userWifiMap, shopMeanWifiMap).serialize() +
                "," +
                WifiRFD.getInstance(userWifiMap, shopMeanWifiMap).serialize() +
                "," +
                WifiApk.getInstance(userWifiMap.keySet(), shopMaxWifiMap.keySet(), 4).serialize() +
                "," +
                WifiApk.getInstance(userWifiMap.keySet(), shopMaxWifiMap.keySet(), 10).serialize();
    }
}
