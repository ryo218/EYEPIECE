package EYEPIECE;

import org.apache.commons.collections.CollectionUtils;
import org.ujmp.core.Matrix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

public class Weight {
    private DataSet data;

    private int m, n;
    private Matrix weightCall, weightPath, weight, similarity;

    private static String closenessPath;
    private static String similarityPath;

    public Weight(DataSet data) {
        this.data = data;
        m = data.getReqSize();
        n = data.getClassSize();
        closenessPath = "dataset/" + data.getDataName() + "/" + data.getDataName() + "_closeness.txt";
        similarityPath = "dataset/" + data.getDataName() + "/" + data.getDataName() + "_similarity.txt";

        initWeightCall();
        initWeightPath();
        initWeightUS();
        initPathNum();
    }

    private void initWeightCall() {
        weightCall = Matrix.Factory.zeros(n, n);
        //int cnt = 0;
        try {
            File file = new File(closenessPath);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String strLine;
            while (null != (strLine = bufferedReader.readLine())) {
                String[] strArr = strLine.split(":|#");
                int source = data.getClassIndex(strArr[0]);
                int target = data.getClassIndex(strArr[1]);
                double value = Double.parseDouble(strArr[2]);
                weightCall.setAsDouble(value, source - m, target - m);
                //cnt++;
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println("call: " + cnt);
    }

    private void initWeightPath() {
        weightPath = Matrix.Factory.zeros(n, n);
        //int cnt = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < i; j++) {
                double value1 = getMaxPath(i, j);
                double value2 = getMaxPath(j, i);
                double value = Math.max(value1, value2);
                if (value > 0) {
                    //cnt++;
                    weightPath.setAsDouble(value, i, j);
                    weightPath.setAsDouble(value, j, i);
                }
            }
        }
        //System.out.println("path: " + cnt);
    }

    private double getMaxPath(int start, int end) {
        LinkedList<Integer> visited = new LinkedList<>();
        visited.add(start);
        LinkedList<Integer> path = new LinkedList<>();
        path.add(start);
        LinkedList<LinkedList<Integer>> allPaths = new LinkedList<>();
        dfs(start, end, visited, path, allPaths);
        double maxPath = 0;
        for (int i = 0; i < allPaths.size(); i++) {
            double temp = 1;
            for (int j = 0; j < allPaths.get(i).size() - 1; j++) {
                temp *= weightCall.getAsDouble(allPaths.get(i).get(j), allPaths.get(i).get(j + 1));
            }
            if (temp > maxPath) {
                maxPath = temp;
            }
        }
        return maxPath;
    }

    public void dfs(int start, int end, LinkedList<Integer> visited, LinkedList<Integer> path, LinkedList<LinkedList<Integer>> allPaths) {
        if (start == end) {
            allPaths.add(new LinkedList<Integer>(path));
            return;
        }
        if (path.size() >= 3) {
            return;
        }
        for (int i = 0; i < n; i++) {
            double dis = weightCall.getAsDouble(start, i);
            if (dis > 0 && !visited.contains(i)) {
                visited.add(i);
                path.add(i);
                dfs(i, end, visited, path, allPaths);
                path.removeLast();
                visited.removeLast();
            }
        }
    }

    private void initWeightUS() {
        similarity = Matrix.Factory.zeros(m, n);
        try {
            File file = new File(similarityPath);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String strLine;
            while (null != (strLine = bufferedReader.readLine())) {
                String[] strArr = strLine.split(":|#");
                int source = data.getReqIndex(strArr[0]);
                int target = data.getClassIndex(strArr[1]);
                double value = Double.parseDouble(strArr[2]);
                similarity.setAsDouble(value, source, target - m);
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initPathNum() {
        data.clearClassValue();
        for (int i = 0; i < n; i++) {
            int cnt = 0;
            for (int j = 0; j < n; j++) {
                double value = weightPath.getAsDouble(i, j);
                if (value > 0) {
                    cnt++;
                }
            }
            if (cnt > 0) {
                data.getClass(i).setValue(cnt);
            }
        }
    }

    private void normalizeClassValue() {
        ArrayList<ClassFile> classValueList = data.sortClassValue();
        double maxv = classValueList.get(0).getValue();
        double minv = classValueList.get(classValueList.size() - 1).getValue();
        for (int i = 0; i < n; i++) {
            double value = data.getClass(i).getValue();
            value = (value - minv) / (maxv - minv);
            data.getClass(i).setValue(value);
        }
    }

    public Matrix getWeight() {
        weight = weightPath.clone();
        normalization(weight);
        return weight;
    }

    private void normalization(Matrix matrix) {
        for (int i = 0; i < matrix.getRowCount(); i++) {
            double sum = 0;
            for (int j = 0; j < matrix.getColumnCount(); j++) {
                double value = matrix.getAsDouble(i, j);
                sum += value;
            }
            if (sum != 0) {
                for (int j = 0; j < matrix.getColumnCount(); j++) {
                    double value = matrix.getAsDouble(i, j);
                    matrix.setAsDouble(value / sum, i, j);
                }
            }
        }
    }

    public Matrix getSimilarity() {
        return similarity;
    }

    public ArrayList<String> getCommon(ArrayList<Rank> rankList) {
        ArrayList<String> commonSet = new ArrayList<>();
        initPathNum();
        normalizeClassValue();
        for (int i = 0; i < n; i++) {
            double value = data.getClass(i).getValue();
            boolean flag = false;
            for (Rank rank : rankList) {
                if (rank.getName().equals(data.getClass(i).getName())) {
                    flag = true;
                    value *= rank.getScore();
                    break;
                }
            }
            if (flag) {
                data.getClass(i).setValue(value);
            } else {
                data.getClass(i).setValue(0);
            }
        }
        ArrayList<ClassFile> classValueList = data.sortClassValue();
        for (ClassFile classFile : classValueList) {
            commonSet.add(classFile.getName());
        }
        return commonSet;
    }

    public ArrayList<String> getDegree(ArrayList<String> labeledList1, ArrayList<Rank> rankList) {
        ArrayList<String> degreeSet = new ArrayList<>();
        ArrayList<Integer> interestSet = new ArrayList<>();
        for (String className : labeledList1) {
            interestSet.add(data.getClassIndex(className) - m);
        }
        data.clearClassValue();
        //called by
        ArrayList<Integer> forwardSet = new ArrayList<>();
        for (int k = 0; k < interestSet.size(); k++) {
            int index = interestSet.get(k);
            for (int i = 0; i < n; i++) {
                if (weightCall.getAsDouble(i, index) > 0) {
                    if (!forwardSet.contains(i)) {
                        forwardSet.add(i);
                    }
                }
            }
        }
        for (int k = 0; k < forwardSet.size(); k++) {
            if (!interestSet.contains(forwardSet.get(k))) {
                int index = forwardSet.get(k);
                ArrayList<Integer> backwardSet = new ArrayList<>();
                for (int j = 0; j < n; j++) {
                    if (weightCall.getAsDouble(index, j) > 0) {
                        if (!backwardSet.contains(j)) {
                            backwardSet.add(j);
                        }
                    }
                }
                int a = CollectionUtils.intersection(forwardSet, interestSet).size();
                int b = CollectionUtils.intersection(backwardSet, interestSet).size();
                double degree = (1 + a) * 1.0 / forwardSet.size() * b / backwardSet.size();
                degree = Math.pow(degree, 0.25);
                data.getClass(index).setValue(degree);
            }
        }
        //calls
        forwardSet = new ArrayList<>();
        for (int k = 0; k < interestSet.size(); k++) {
            int index = interestSet.get(k);
            for (int j = 0; j < n; j++) {
                if (weightCall.getAsDouble(index, j) > 0) {
                    if (!forwardSet.contains(j)) {
                        forwardSet.add(j);
                    }
                }
            }
        }
        for (int k = 0; k < forwardSet.size(); k++) {
            if (!interestSet.contains(forwardSet.get(k))) {
                int index = forwardSet.get(k);
                ArrayList<Integer> backwardSet = new ArrayList<>();
                for (int i = 0; i < n; i++) {
                    if (weightCall.getAsDouble(i, index) > 0) {
                        if (!backwardSet.contains(i)) {
                            backwardSet.add(i);
                        }
                    }
                }
                int a = CollectionUtils.intersection(forwardSet, interestSet).size();
                int b = CollectionUtils.intersection(backwardSet, interestSet).size();
                double degree = (1 + a) * 1.0 / forwardSet.size() * b / backwardSet.size();
                degree = Math.pow(degree, 0.25);
                double value = data.getClass(index).getValue();
                if (value > 0) {
                    degree = value + degree - value * degree;
                }
                data.getClass(index).setValue(degree);
            }
        }
        for (int i = 0; i < n; i++) {
            double value = data.getClass(i).getValue();
            boolean flag = false;
            for (Rank rank : rankList) {
                if (rank.getName().equals(data.getClass(i).getName())) {
                    flag = true;
                    value *= rank.getScore();
                    break;
                }
            }
            if (flag) {
                data.getClass(i).setValue(value);
            } else {
                data.getClass(i).setValue(0);
            }
        }
        ArrayList<ClassFile> classValueList = data.sortClassValue();
        for (ClassFile classFile : classValueList) {
            degreeSet.add(classFile.getName());
        }
        return degreeSet;
    }
}
