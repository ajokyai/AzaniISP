package model;

import java.time.LocalDate;

public class Payment {
    private int paymentId;
    private int institutionId;
    private String paymentType;
    private double amount;
    private LocalDate paymentDate;

    public Payment(int institutionId, String paymentType, double amount, LocalDate paymentDate) {
        this.institutionId = institutionId;
        this.paymentType = paymentType;
        this.amount = amount;
        this.paymentDate = paymentDate;
    }

    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }
    public int getInstitutionId() { return institutionId; }
    public String getPaymentType() { return paymentType; }
    public double getAmount() { return amount; }
    public LocalDate getPaymentDate() { return paymentDate; }
}