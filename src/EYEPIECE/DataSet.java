package EYEPIECE;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class DataSet {
    private static String dataName;
    private static String reqNamePath = "temp/reqName.txt";
    private static String classNamePath = "temp/className.txt";
    private static String RTMPath;

    private ArrayList<Requirement> reqList;//0~m-1
    private ArrayList<ClassFile> classList;//0~n-1
    private HashMap<String, Integer> reqName2Index;//0~m-1
    private HashMap<String, Integer> className2Index;//m~m+n-1

    private ArrayList<Rank> allRankList;

    private ArrayList<String> labeledList;

    private ArrayList<String> rtmClassList;

    int RTMSize = 0;

    public DataSet(String dataName) {
        DataSet.dataName = dataName;
        RTMPath = "dataset/" + dataName + "/" + dataName + "_RTM_CLASS.txt";
        loadFiles();
        labeledList = new ArrayList<>();
    }

    public DataSet(String dataName, ArrayList<String> list) {
        DataSet.dataName = dataName;
        RTMPath = "dataset/" + dataName + "/" + dataName + "_RTM_CLASS.txt";
        loadFiles();
        setLabeledList(list);
    }

    private void loadFiles() {
        try {
            //read requirements in RTM
            File file = new File(reqNamePath);
            if (!file.exists()) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(file.getPath());
            int id = 0;
            reqList = new ArrayList<>();
            reqName2Index = new HashMap<>();
            file = new File(RTMPath);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String strLine;
            while (null != (strLine = bufferedReader.readLine())) {
                String[] strArr = strLine.split(" ");
                String reqName = strArr[0];
                if (!reqName2Index.containsKey(reqName)) {
                    reqList.add(new Requirement(reqName));
                    reqName2Index.put(reqName, id);
                    id++;
                    fileWriter.write(reqName + "\n");
                }
            }
            bufferedReader.close();
            fileWriter.close();
            //System.out.println("Requirements: " + reqList.size());

            //read classes in RTM
            file = new File(classNamePath);
            if (!file.exists()) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            }
            fileWriter = new FileWriter(file.getPath());
            classList = new ArrayList<>();
            className2Index = new HashMap<>();
            rtmClassList = new ArrayList<>();
            file = new File(RTMPath);
            bufferedReader = new BufferedReader(new FileReader(file));
            int cnt = 0;
            while (null != (strLine = bufferedReader.readLine())) {
                String[] strArr = strLine.split(" ");
                String reqName = strArr[0];
                String className = strArr[1];
                if (!className2Index.containsKey(className)) {
                    classList.add(new ClassFile(className));
                    className2Index.put(className, id);
                    id++;
                    rtmClassList.add(className);
                    fileWriter.write(className + "\n");
                }
                int index = reqName2Index.get(reqName);
                reqList.get(index).addRTM(className);
                cnt++;
            }
            bufferedReader.close();
            fileWriter.close();
            RTMSize = cnt;
            //System.out.println("Classes: " + classList.size());
            //System.out.println("RTM: " + cnt);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getDataName() {
        return dataName;
    }

    public Requirement getReq(int index) {
        return reqList.get(index);
    }

    public int getReqSize() {
        return reqList.size();
    }

    public ClassFile getClass(int index) {
        return classList.get(index);
    }

    public int getClassSize() {
        return classList.size();
    }

    public void clearClassValue() {
        for (ClassFile classFile : classList) {
            classFile.setValue(0);
        }
    }

    public ArrayList<ClassFile> sortClassValue() {
        ArrayList<ClassFile> temp = (ArrayList<ClassFile>) classList.clone();
        Collections.sort(temp);
        return temp;
    }

    public int getReqIndex(String name) {
        return reqName2Index.getOrDefault(name, -1);
    }

    public int getClassIndex(String name) {
        return className2Index.getOrDefault(name, -1);
    }

    public void setReqRank(int index, ArrayList<Rank> rankList) {
        Collections.sort(rankList);
        reqList.get(index).setRankList(rankList);
    }

    public void setAllRankList(ArrayList<Rank> allRankList) {
        Collections.sort(allRankList);
        this.allRankList = allRankList;
    }

    public ArrayList<Rank> getAllRankList() {
        return allRankList;
    }

    public ArrayList<String> getLabeledList() {
        return labeledList;
    }

    private void setLabeledList(ArrayList<String> list) {
        this.labeledList = list;
    }

    public int getRTMSize() {
        return RTMSize;
    }

    public void copyRank(DataSet data0) {
        this.labeledList = data0.getLabeledList();
        allRankList = new ArrayList<>(data0.getAllRankList());
        for (int i = 0; i < reqList.size(); i++) {
            ArrayList<Rank> rankList = new ArrayList<>(data0.getReq(i).getRankList());
            setReqRank(i, rankList);
        }
    }

    public void deleteLabeled() {
        ArrayList<Rank> allRankList = getAllRankList();
        for (String str : labeledList) {
            String[] strArr = str.split(" ");
            int label = Integer.parseInt(strArr[3]);
            if (label == 1 || label == 0) {
                String reqName = strArr[0];
                int i = getReqIndex(reqName);
                String className = strArr[1];
                ArrayList<String> reqRTM = getReq(i).getRTM();
                for (String name : reqRTM) {
                    if (name.equals(className)) {
                        reqRTM.remove(className);
                        break;
                    }
                }
                if (reqRTM.size() > 0) {
                    ArrayList<Rank> rankList = getReq(i).getRankList();
                    for (Rank rank : rankList) {
                        if (rank.getName().equals(className)) {
                            rankList.remove(rank);
                            break;
                        }
                    }
                    for (Rank rank : allRankList) {
                        if (rank.getName().equals(reqName + " " + className)) {
                            allRankList.remove(rank);
                            break;
                        }
                    }
                } else {
                    ArrayList<Rank> rankList = getReq(i).getRankList();
                    rankList.clear();
                    ArrayList<Rank> newRank = new ArrayList<>();
                    for (Rank rank : allRankList) {
                        if (!rank.getName().contains(reqName + " ")) {
                            newRank.add(rank);
                        }
                    }
                    allRankList = newRank;
                }
            }
        }
        rtmClassList.clear();
        for (int i = 0; i < getReqSize(); i++) {
            ArrayList<String> result = getReq(i).getRTM();
            for (int j = 0; j < result.size(); j++) {
                String className = result.get(j);
                if (!rtmClassList.contains(className)) {
                    rtmClassList.add(className);
                }
            }
        }
        for (int i = 0; i < getReqSize(); i++) {
            ArrayList<Rank> rankList = getReq(i).getRankList();
            ArrayList<Rank> rankList1 = new ArrayList<>();
            for (Rank rank : rankList) {
                if (rtmClassList.contains(rank.getName())) {
                    rankList1.add(rank);
                }
            }
            setReqRank(i, rankList1);
        }
        ArrayList<Rank> newRank = new ArrayList<>();
        for (Rank rank : allRankList) {
            String str = rank.getName();
            String[] strArr = str.split(" ");
            if (rtmClassList.contains(strArr[1])) {
                newRank.add(rank);
            }
        }
        setAllRankList(newRank);
    }
}
