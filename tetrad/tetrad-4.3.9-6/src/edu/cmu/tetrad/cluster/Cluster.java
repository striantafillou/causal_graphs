package edu.cmu.tetrad.cluster;

import edu.cmu.tetrad.util.Point;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Cluster {
    private Set<Point> cluster = new HashSet<Point>();
    private double weight = 0.0;

    /**
     * Hidden constructor.
     */
    private Cluster() {
    }

    public static Cluster emptyCluster() {
        return new Cluster();
    }

    public static Cluster pointCluster(Point p) {
        Cluster cluster = new Cluster();
        cluster.addPoint(p);
        return cluster;
    }

    public static Cluster cluster(List<Point> points) {
        Cluster cluster = new Cluster();
        cluster.cluster.addAll(points);
        return cluster;
    }

    public static Cluster mergeClusters(Cluster a, Cluster b) {
        Cluster cluster = new Cluster();
        cluster.merge(a);
        cluster.merge(b);
        return cluster;
    }

    public void addPoint(Point point) {
        cluster.add(point);
    }

    public void merge(List<Cluster> clusters) {
        Cluster merge = new Cluster();

        for (Cluster cluster : clusters) {
            merge.addPoints(cluster.getPoints());
        }
    }

    private void addPoints(Collection<? extends Point> points) {
        cluster.addAll(points);
    }

    public Set<Point> getPoints() {
        return new HashSet<Point>(cluster);
    }

    public void merge(Cluster cluster) {
        this.cluster.addAll(cluster.getPoints());
    }

    public double size() {
        return this.cluster.size();
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public int hashCode() {
        return cluster.hashCode();
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        Cluster other = (Cluster) o;
        return other.cluster.equals(this.cluster);
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();

        buf.append("Cluster size = ").append(cluster.size()).append(": ");

        for (Point p : cluster) {
            buf.append(p);
        }

        return buf.toString();
    }
}
