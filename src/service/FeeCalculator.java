
package service;

public class FeeCalculator {

    public static final double REGISTRATION_FEE = 8500;
    public static final double INSTALLATION_FEE = 10000;
    public static final double PC_PRICE = 40000;
    public static final double LATE_FINE_RATE = 0.15;
    public static final double UPGRADE_DISCOUNT = 0.10;
    public static final double RECONNECTION_FEE = 1000;

    // Table 2: LAN cost by number of nodes
    public double lanCost(int nodes) {
    if (nodes < 0 || nodes == 1 || nodes > 100)
        throw new IllegalArgumentException("Nodes must be 0 or 2-100");
    if (nodes == 0) return 0;
    if (nodes >= 2 && nodes <= 10) return 10000;
    if (nodes <= 20) return 20000;
    if (nodes <= 40) return 30000;
    if (nodes <= 100) return 40000;
    return 0; // unreachable, but Java needs a return
}

    // Task 4(b): PCs and LAN only
    public double pcAndLanCost(int pcs, int nodes) {
        return pcs * PC_PRICE + lanCost(nodes);
    }

    // Task 4(a): installation fee plus PCs and LAN
    public double installationTotal(int pcs, int nodes) {
        return INSTALLATION_FEE + pcAndLanCost(pcs, nodes);
    }

    // Task 4(c): 10% off the NEW bandwidth price
    public double upgradedMonthlyCharge(double newBandwidthCost) {
        return newBandwidthCost * (1 - UPGRADE_DISCOUNT);
    }

    // 15% fine on a late bill
    public double lateFine(double amountDue) {
        return amountDue * LATE_FINE_RATE;
    }
}