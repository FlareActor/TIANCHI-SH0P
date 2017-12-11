package Utils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wangdexun on 2017/11/26.
 * 序列化
 */
public class Serializer {

    public static <T, R> String serialize(List<Map.Entry<T, R>> mapList) {
        if (mapList == null || mapList.size() == 0)
            return "";
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<T, R> i : mapList) {
            builder.append(i.getKey());
            builder.append(":");
            builder.append(i.getValue());
            builder.append(",");
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    /**
     * 将wifi字符串反序列化成(Linked)Map形式
     *
     * @param wifiStr    Wi-Fi字符串
     * @param anotherMap 求交集的另一个Map，减少内存使用
     * @return WifiMap
     */
    public static Map<String, Double> deserialize(String wifiStr, Map<String, Double> anotherMap) {
        Map<String, Double> wifiMap = new LinkedHashMap<String, Double>();
        String[] wifiArray = wifiStr.split(",");
        String[] wifiInfo;
        for (String i : wifiArray) {
            wifiInfo = i.split(":");
            if (anotherMap == null || anotherMap.containsKey(wifiInfo[0]))
                wifiMap.put(wifiInfo[0], Double.valueOf(wifiInfo[1]));
            else
                wifiMap.put(wifiInfo[0], null);
        }
        return wifiMap;
    }
}
