package EYEPIECE;

public class Rank implements Comparable {
    private String name;
    private double score;

    public Rank(String name, double score) {
        this.name = name;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public double getScore() {
        return score;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public int compareTo(Object o) {
        Rank rank = (Rank) o;
        return Double.compare(rank.score, score);
    }
}