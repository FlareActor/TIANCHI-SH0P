import ml.dmlc.xgboost4j.java.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wangdexun on 2017/11/23.
 * 测试
 */
public class Test {
    public static void main(String[] args) throws XGBoostError {
        // Data
        float[] data = new float[]{1f, 12f, 1f, 2f, -3f, 4f, 5f, 6f, 3f, 4f, 5f, 6f};
        int nrow = 6;
        int ncol = 2;
        float missing = 0.0f;
        DMatrix dmat = new DMatrix(data, nrow, ncol, missing);
        dmat.setLabel(new float[]{1, 1, 1, 0, 0, 1});
        // Params
        HashMap<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("eta", 0.15);
        paramMap.put("max_depth", 3);
        paramMap.put("silent", 1);
        paramMap.put("objective", "binary:logistic");
//        paramMap.put("eval_metric", "logloss");
        // Train
        HashMap<String, DMatrix> watches = new HashMap<String, DMatrix>();
//        watches.put("train", dmat);
//        watches.put("eval", dmat);
        int round = 200;
        Booster booster = XGBoost.train(dmat, paramMap, round, watches, null, null, null, 40);

        //predict
        float[][] predicts = booster.predict(dmat);
        for (float[] f : predicts) {
            for (float i : f) {
                System.out.println(i);
            }
        }
    }

    public static class A implements IEvaluation {

        String evalMetric = "custom_error";

        public String getMetric() {
            return evalMetric;
        }

        public float eval(float[][] predicts, DMatrix dmat) {
            float error = 0f;
            float[] labels;
            try {
                labels = dmat.getLabel();
            } catch (XGBoostError ex) {
                return -1f;
            }
            int nrow = predicts.length;
            for (int i = 0; i < nrow; i++) {
                if (labels[i] == 0f && predicts[i][0] > 0.5) {
                    error++;
                } else if (labels[i] == 1f && predicts[i][0] <= 0.5) {
                    error++;
                }
            }
            return error / labels.length;
        }
    }
}
