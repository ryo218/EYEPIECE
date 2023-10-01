package EYEPIECE;

public class ClassFile implements Comparable {
    private String name;
    private double value;//number of paths, valueOfS2 or valueOfS3

    public ClassFile(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    @Override
    public int compareTo(Object o) {
        ClassFile classFile = (ClassFile) o;
        return Double.compare(classFile.value, value);
    }
}
