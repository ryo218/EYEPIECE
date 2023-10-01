package EYEPIECE;

import org.ujmp.core.Matrix;

import java.util.ArrayList;

public class Propagation {
    private DataSet data;

    private int m, n;
    private Matrix weight, similarity, Y;

    public Propagation(DataSet data, Matrix weight, Matrix similarity) {
        this.data = data;
        this.weight = weight;
        this.similarity = similarity;
        m = data.getReqSize();
        n = data.getClassSize();
    }

    public void compute(double alpha) {
        getY();

        //F^* = (1-alpha)*(I−alphaW)^(−1)*Y
        Matrix I = Matrix.Factory.eye(n, n);
        Matrix optimalF = weight.times(alpha);
        optimalF = I.minus(optimalF);
        optimalF = optimalF.inv();
        optimalF = optimalF.mtimes(Y);
        optimalF = optimalF.times(1 - alpha);

        //To compare with the baseline approaches, we put the user-verified links at
        //the top or bottom of the list, as do the baseline approaches in the experiments.
        ArrayList<String> labeledList = data.getLabeledList();
        for (int k = 0; k < labeledList.size(); k++) {
            String str = labeledList.get(k);
            String[] strArr = str.split(" |\t");
            int i = data.getReqIndex(strArr[0]);
            int j = data.getClassIndex(strArr[1]);
            int label = Integer.parseInt(strArr[3]);
            if (label > 0) {
                optimalF.setAsInt(1, j - m, i);
            } else {
                optimalF.setAsInt(-1, j - m, i);
            }
        }

        getRank(optimalF);
    }

    private void getY() {
        Y = Matrix.Factory.zeros(n, m);
        //pre-assign labels
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                double value = similarity.getAsDouble(i, j);
                Y.setAsDouble(value, j, i);
            }
        }
        //use feedback
        ArrayList<String> labeledList = data.getLabeledList();
        for (int k = 0; k < labeledList.size(); k++) {
            String str = labeledList.get(k);
            String[] strArr = str.split(" ");
            int i = data.getReqIndex(strArr[0]);
            int j = data.getClassIndex(strArr[1]);
            int label = Integer.parseInt(strArr[3]);
            if (label > 0) {
                Y.setAsInt(1, j - m, i);
            } else {
                Y.setAsInt(0, j - m, i);
            }
        }
    }

    private void getRank(Matrix F) {
        ArrayList<Rank> allRankList = new ArrayList<>();
        for (int i = 0; i < m; i++) {
            String reqName = data.getReq(i).getName();
            ArrayList<Rank> rankList = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                String className = data.getClass(j).getName();
                double value = F.getAsDouble(j, i);
                Rank rank = new Rank(className, value);
                rankList.add(rank);
                rank = new Rank(reqName + " " + className, value);
                allRankList.add(rank);
            }
            data.setReqRank(i, rankList);
        }
        data.setAllRankList(allRankList);
    }

}
