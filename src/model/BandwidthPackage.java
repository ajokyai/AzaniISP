package model;

public class BandwidthPackage {
    private int id;
    private int mbps;
    private double cost;

    public BandwidthPackage(int id, int mbps, double cost) {
        this.id = id;
        this.mbps = mbps;
        this.cost = cost;
    }

    public int getId() { return id; }
    public int getMbps() { return mbps; }
    public double getCost() { return cost; }

    @Override
    public String toString() {
        return mbps + " MBPS - KSh " + String.format("%,.0f", cost);
    }
}