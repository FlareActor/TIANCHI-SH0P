package UDF;

import Base.BaseUDF;
import MapReduce.WifiFeats.WifiRFD;
import Utils.Serializer;

import java.util.Map;

/**
 * Created by ewrfcas on 2017/11/22.
 * 计算两条Wi-Fi之间的RFD值
 */
public class RFDCalculateUDF extends BaseUDF {
    public String evaluate(String userWifiStr, String shopWifiStr, String norm) {
        Map<String, Double> userWifiMap = Serializer.deserialize(userWifiStr, null);
        Map<String, Double> shopWifiMap = Serializer.deserialize(shopWifiStr, userWifiMap);
        WifiRFD wifiRFD = WifiRFD.getInstance(userWifiMap, shopWifiMap);
        if (norm.equals("l1"))
            return String.valueOf(wifiRFD.getRfd1());
        return String.valueOf(wifiRFD.getRfd2());
    }
}
