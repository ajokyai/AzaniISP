package model;

import java.time.LocalDate;

public class Bill {
    private int billId;
    private int institutionId;
    private String institutionName;
    private LocalDate billMonth;
    private double amountDue;
    private double fine;
    private double amountPaid;

    public Bill(int billId, int institutionId, String institutionName,
                LocalDate billMonth, double amountDue, double fine, double amountPaid) {
        this.billId = billId;
        this.institutionId = institutionId;
        this.institutionName = institutionName;
        this.billMonth = billMonth;
        this.amountDue = amountDue;
        this.fine = fine;
        this.amountPaid = amountPaid;
    }

    public int getBillId() { return billId; }
    public int getInstitutionId() { return institutionId; }
    public String getInstitutionName() { return institutionName; }
    public LocalDate getBillMonth() { return billMonth; }
    public double getAmountDue() { return amountDue; }
    public double getFine() { return fine; }
    public double getAmountPaid() { return amountPaid; }

    @Override
    public String toString() {
        return institutionName + " | " + billMonth.toString().substring(0, 7)
             + " | owes KSh " + String.format("%,.2f", amountDue + fine - amountPaid);
    }
}

