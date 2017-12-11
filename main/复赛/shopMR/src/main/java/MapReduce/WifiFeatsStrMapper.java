package MapReduce;

import MapReduce.WifiFeats.WifiApk;
import MapReduce.WifiFeats.WifiDiff;
import MapReduce.WifiFeats.WifiRFD;
import Utils.Serializer;
import com.aliyun.odps.data.Record;
import com.aliyun.odps.mapred.MapperBase;

import java.io.IOException;
import java.util.Map;

/**
 * Created by wangdexun on 2017/11/27.
 * Wi-Fi特征提取
 * rfd、large_sum、large_num、less_sum、less_num、apk_4、apk_10
 */
public class WifiFeatsStrMapper extends MapperBase {

    private Record wifiFeats;

    @Override
    public void setup(TaskContext context) throws IOException {
        wifiFeats = context.createOutputRecord();
    }

    @Override
    // TODO Wi-Fi强度改成Integer节约存储空间
    public void map(long key, Record record, TaskContext context) throws IOException {
        wifiFeats.setBigint("no", record.getBigint(0));
        String wifiFeatsStr = record.getString(1);
        String[] wifiFeatsArray = wifiFeatsStr.split(",");
        for (String i : wifiFeatsArray) {
            String[] wifiInfo = i.split(":");
            wifiFeats.setDouble(wifiInfo[0], Double.valueOf(wifiInfo[1]));
        }
        context.write(wifiFeats);
    }


}
