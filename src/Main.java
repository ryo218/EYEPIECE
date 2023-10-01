import EYEPIECE.*;

import java.util.ArrayList;

public class Main {
    public static int CUMULATIVE = 5;
    public static int CONSECUTIVE = 3;

    public static void main(String[] args) {
        String[] dataList = {"iTrust", "Gantt", "Maven", "Groovy", "Seam", "Pig", "Drools", "Infinispan", "Derby"};
        for (int di = 0; di < dataList.length; di++) {
            String dataName = dataList[di];
            System.out.println(dataName);

            //parameters
            double alpha1 = 0, alpha2 = 0, alpha3 = 0;
            if (dataName.equals("iTrust") || dataName.equals("Gantt")) {//large scale
                alpha1 = 0.9;
                alpha2 = 0.7;
                alpha3 = 0.9;
            } else if (dataName.equals("Maven") || dataName.equals("Groovy") || dataName.equals("Seam")) {//small scale
                alpha1 = 0.3;
                alpha2 = 0.3;
                alpha3 = 0.9;
            } else {//medium scale
                alpha1 = 0.5;
                alpha2 = 0.5;
                alpha3 = 0.9;
            }

            //initialisation
            DataSet dataSet1 = new DataSet(dataName);
            Weight weight = new Weight(dataSet1);

            //0th label propagation (without user feedback)
            labelPropagation(dataSet1, weight, alpha1);
            DataSet dataSet2 = dataSet1;

            //user verification
            ArrayList<String> labeledList = new ArrayList<>();
            int m = dataSet1.getReqSize();
            for (int i = 0; i < m; i++) {
                //cnt[0]: the number of cumulative no-trace links
                //cnt[1]: the number of cumulative trace links
                //cnt[0]: the number of consecutive no-trace links
                int[] cnt = new int[3];
                //classes of trace links to the current requirement
                ArrayList<String> labeledList1 = new ArrayList<>();
                //single=false means that no single class is considered
                boolean single = false;
                //System.out.println("S1：");
                while (cnt[0] < CUMULATIVE) {
                    //the user verify a link
                    weight.initPathNum();
                    ArrayList<String> list = selectLabeledS1(dataSet1, dataSet2, i, cnt, labeledList1, single);
                    labeledList.addAll(list);
                    //label propagation
                    dataSet2 = new DataSet(dataName, labeledList);
                    labelPropagation(dataSet2, weight, alpha1);
                    //remove the verified links to facilitate
                    //the selection of the next recommendation link
                    dataSet1.copyRank(dataSet2);
                    dataSet2.deleteLabeled();
                    //whether all the classes corresponding to the current requirement have been found
                    if (dataSet2.getReq(i).getRTM().size() == 0) {
                        break;
                    }
                }
                if (dataSet2.getReq(i).getRTM().size() == 0) {
                    continue;
                }
                cnt[0] = 0;
                cnt[1] = 0;
                cnt[2] = 0;
                //System.out.println("S2：");
                while (cnt[0] < CUMULATIVE && cnt[2] != CONSECUTIVE) {
                    ArrayList<String> commonSet = weight.getCommon(dataSet2.getReq(i).getRankList());
                    ArrayList<String> list = selectLabeledS2(dataSet2, i, cnt, labeledList1, commonSet);
                    labeledList.addAll(list);
                    dataSet2 = new DataSet(dataName, labeledList);
                    labelPropagation(dataSet2, weight, alpha2);
                    dataSet1.copyRank(dataSet2);
                    dataSet2.deleteLabeled();
                    if (dataSet2.getReq(i).getRTM().size() == 0) {
                        break;
                    }
                }
                if (dataSet2.getReq(i).getRTM().size() == 0) {
                    continue;
                }
                cnt[0] = 0;
                cnt[1] = 0;
                cnt[2] = 0;
                //System.out.println("S3：");
                if (labeledList1.size() != 0) {
                    while (cnt[0] < CUMULATIVE * 2 && cnt[2] != CUMULATIVE + CONSECUTIVE) {
                        ArrayList<String> degreeSet = weight.getDegree(labeledList1, dataSet2.getReq(i).getRankList());
                        ArrayList<String> list = selectLabeledS3(dataSet2, i, cnt, labeledList1, degreeSet);
                        labeledList.addAll(list);
                        dataSet2 = new DataSet(dataName, labeledList);
                        labelPropagation(dataSet2, weight, alpha3);
                        dataSet1.copyRank(dataSet2);
                        dataSet2.deleteLabeled();
                        if (dataSet2.getReq(i).getRTM().size() == 0) {
                            break;
                        }
                    }
                } else {
                    single = true;
                    while (cnt[0] < CUMULATIVE * 2 && cnt[2] != CUMULATIVE + CONSECUTIVE) {
                        weight.initPathNum();
                        ArrayList<String> list = selectLabeledS1(dataSet1, dataSet2, i, cnt, labeledList1, single);
                        labeledList.addAll(list);
                        dataSet2 = new DataSet(dataName, labeledList);
                        labelPropagation(dataSet2, weight, alpha1);
                        dataSet1.copyRank(dataSet2);
                        dataSet2.deleteLabeled();
                        if (dataSet2.getReq(i).getRTM().size() == 0) {
                            break;
                        }
                    }
                }
            }
            getEvaluation(dataSet1);
        }
    }

    private static DataSet labelPropagation(DataSet dataSet, Weight weight, double alpha) {
        Propagation propagation = new Propagation(dataSet, weight.getWeight(), weight.getSimilarity());
        propagation.compute(alpha);
        return dataSet;
    }

    private static ArrayList<String> selectLabeledS1(DataSet data1, DataSet data2, int reqIndex, int[] cnt, ArrayList<String> labeledList1, boolean single) {
        ArrayList<String> list = new ArrayList<>();
        String reqName = data2.getReq(reqIndex).getName();
        ArrayList<Rank> rankList = data2.getReq(reqIndex).getRankList();
        for (Rank rank : rankList) {
            String className = rank.getName();
            double score = rank.getScore();
            int classIndex = data1.getClassIndex(className) - data1.getReqSize();
            double value = data1.getClass(classIndex).getValue();
            if (single) {
                value = 1;
            }
            if (value > 0) {
                if (!data2.getReq(reqIndex).getRTM().contains(className)) {
                    //no-trace
                    cnt[0]++;
                    cnt[2]++;
                    list.add(reqName + " " + className + " " + score + " 0");
                    //System.out.println(reqName + "\t" + className + "\t" + score + "\t0");
                } else {
                    //is-trace
                    cnt[1]++;
                    cnt[2] = 0;
                    list.add(reqName + " " + className + " " + score + " 1");
                    labeledList1.add(className);
                    //System.out.println(reqName + "\t" + className + "\t" + score + "\t1");
                }
                break;
            }
        }
        return list;
    }

    private static ArrayList<String> selectLabeledS2(DataSet data2, int reqIndex, int[] cnt, ArrayList<String> labeledList1, ArrayList<String> commonSet) {
        ArrayList<String> list = new ArrayList<>();
        String reqName = data2.getReq(reqIndex).getName();
        ArrayList<Rank> rankList = data2.getReq(reqIndex).getRankList();
        boolean flag = false;
        for (String name : commonSet) {
            for (Rank rank : rankList) {
                String className = rank.getName();
                double score = rank.getScore();
                if (name.equals(className)) {
                    flag = true;
                    if (!data2.getReq(reqIndex).getRTM().contains(className)) {
                        //no-trace
                        cnt[0]++;
                        cnt[2]++;
                        list.add(reqName + " " + className + " " + score + " 0");
                        //System.out.println(reqName + "\t" + className + "\t" + score + "\t0");
                    } else {
                        //is-trace
                        cnt[1]++;
                        cnt[2] = 0;
                        list.add(reqName + " " + className + " " + score + " 1");
                        labeledList1.add(className);
                        //System.out.println(reqName + "\t" + className + "\t" + score + "\t1");
                    }
                    break;
                }
            }
            if (flag) {
                break;
            }
        }
        return list;
    }

    private static ArrayList<String> selectLabeledS3(DataSet data2, int reqIndex, int[] cnt, ArrayList<String> labeledList1, ArrayList<String> degreeSet) {
        ArrayList<String> list = new ArrayList<>();
        String reqName = data2.getReq(reqIndex).getName();
        ArrayList<Rank> rankList = data2.getReq(reqIndex).getRankList();
        boolean flag = false;
        for (String name : degreeSet) {
            for (Rank rank : rankList) {
                String className = rank.getName();
                double score = rank.getScore();
                if (name.equals(className)) {
                    flag = true;
                    if (!data2.getReq(reqIndex).getRTM().contains(className)) {
                        //no-trace
                        cnt[0]++;
                        cnt[2]++;
                        list.add(reqName + " " + className + " " + score + " 0");
                        //System.out.println(reqName + "\t" + className + "\t" + score + "\t0");
                    } else {
                        //is-trace
                        cnt[1]++;
                        cnt[2] = 0;
                        list.add(reqName + " " + className + " " + score + " 1");
                        labeledList1.add(className);
                        //System.out.println(reqName + "\t" + className + "\t" + score + "\t1");
                    }
                    break;
                }
            }
            if (flag) {
                break;
            }
        }
        return list;
    }

    public static void getEvaluation(DataSet data) {
        Evaluation evaluation = new Evaluation(data);
        evaluation.getPRF();
        evaluation.getAP();
        evaluation.getMAP();
    }
}
