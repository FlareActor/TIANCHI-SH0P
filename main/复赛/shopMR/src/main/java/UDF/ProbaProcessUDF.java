package UDF;

import Base.BaseUDF;

/**
 * Created by wangdexun on 2017/11/30.
 * 提取label为1的置信度
 */
public class ProbaProcessUDF extends BaseUDF {
    public String evaluate(String prediction_result, String prediction_score) {
        if (prediction_result.equals("1"))
            return prediction_score;
        else
            return String.valueOf(1 - Double.valueOf(prediction_score));
    }
}
