package EYEPIECE;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class Evaluation {
    private DataSet data;
    private int m, n;

    public Evaluation(DataSet data) {
        this.data = data;
        m = data.getReqSize();
        n = data.getClassSize();
    }

    public void getPRF() {
        int TP = 0;
        ArrayList<String> labeledList = data.getLabeledList();
        for (String strLine : labeledList) {
            String[] strArr = strLine.split(" ");
            int label = Integer.parseInt(strArr[3]);
            if (label == 1) {
                TP++;
            }
        }
        double precision = TP * 1.0 / labeledList.size();
        double recall = TP * 1.0 / data.getRTMSize();
        double F1 = 2 * precision * recall / (precision + recall);
        double F2 = 5 * precision * recall / (4 * precision + recall);
        DecimalFormat df = new DecimalFormat("###0.00");
        System.out.println("Verification: " + labeledList.size());
        System.out.println("Verified Traces: " + TP);
        System.out.println("Precision: " + df.format(precision * 100));
        System.out.println("Recall: " + df.format(recall * 100));
        System.out.println("F1: " + df.format(F1 * 100));
        System.out.println("F2: " + df.format(F2 * 100));
    }

    public void getAP() {
        double ap = 0;
        double precision = 0;
        int isRelevant = 0;
        ArrayList<Rank> allList = data.getAllRankList();
        for (int i = 0; i < allList.size(); i++) {
            String str = allList.get(i).getName();
            String[] strArr = str.split(" ");
            int index = data.getReqIndex(strArr[0]);
            ArrayList<String> result = data.getReq(index).getRTM();
            if (result.size() > 0) {
                if (result.contains(strArr[1])) {
                    isRelevant++;
                    precision += (double) isRelevant / (i + 1);
                }
            }
        }
        int size = 0;
        for (int i = 0; i < m; i++) {
            size += data.getReq(i).getRTM().size();
        }
        ap = precision / size;
        DecimalFormat df = new DecimalFormat("###0.00");
        System.out.println("AP: " + df.format(ap * 100));
    }

    public void getMAP() {
        int cnt = m;
        double map = 0;
        for (int i = 0; i < m; i++) {
            ArrayList<Rank> rankList = data.getReq(i).getRankList();
            ArrayList<String> RTM = data.getReq(i).getRTM();
            if (RTM.size() > 0) {
                double avgP = 0;
                int isRelevant = 0;
                for (int j = 0; j < rankList.size(); j++) {
                    String className = rankList.get(j).getName();
                    if (RTM.contains(className)) {
                        isRelevant++;
                        double precision = (double) isRelevant / (j + 1);
                        avgP += precision;
                    }
                }
                map += avgP / RTM.size();
            } else {
                cnt--;
            }
        }
        map /= cnt;
        DecimalFormat df = new DecimalFormat("###0.00");
        System.out.println("MAP: " + df.format(map * 100));
    }
}
