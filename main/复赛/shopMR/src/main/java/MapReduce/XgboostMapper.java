package MapReduce;

import com.aliyun.odps.data.Record;
import com.aliyun.odps.mapred.MapperBase;

import java.io.IOException;

/**
 * Created by wangdexun on 2017/12/10.
 */
public class XgboostMapper extends MapperBase {
    private Record mall;
    private Record feats;

    @Override
    public void setup(TaskContext context) throws IOException {
        mall = context.createMapOutputKeyRecord();
        feats = context.createMapOutputValueRecord();
    }

    @Override
    public void map(long key, Record record, TaskContext context) throws IOException {
        mall.set("mall_id", record.getString(5));
//        price.set("price", record.getBigint(4));
        context.write(mall, feats);
    }

}
