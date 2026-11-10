package ui;

import dao.BillDAO;
import dao.InstitutionDAO;
import dao.PaymentDAO;
import model.BandwidthPackage;
import model.Bill;
import model.Institution;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;

public class BillingForm extends JPanel {

    private JComboBox<Institution> cmbInstitution = new JComboBox<>();
    private JComboBox<BandwidthPackage> cmbPackage = new JComboBox<>();
    private JButton btnAssign = new JButton("Connect / upgrade");

    private JTextField txtMonth = new JTextField(YearMonth.now().minusMonths(1).toString(), 15);
    private JButton btnGenerate = new JButton("Generate bills");
    private JButton btnPenalties = new JButton("Apply fines & disconnections");

    private JComboBox<Bill> cmbBill = new JComboBox<>();
    private JButton btnPay = new JButton("Pay full balance");

    private BillDAO billDAO = new BillDAO();

    public BillingForm() {
        setLayout(new GridLayout(0, 2, 8, 8));

        add(section("1. Connect or upgrade"));  add(new JLabel(""));
        add(new JLabel("Institution:"));        add(cmbInstitution);
        add(new JLabel("Bandwidth package:"));  add(cmbPackage);
        add(new JLabel(""));                    add(btnAssign);

        add(section("2. Monthly bills"));       add(new JLabel(""));
        add(new JLabel("Bill month (yyyy-mm):")); add(txtMonth);
        add(btnGenerate);                       add(btnPenalties);

        add(section("3. Pay a bill"));          add(new JLabel(""));
        add(new JLabel("Unpaid bill:"));        add(cmbBill);
        add(new JLabel(""));                    add(btnPay);

        btnAssign.addActionListener(e -> assign());
        btnGenerate.addActionListener(e -> generate());
        btnPenalties.addActionListener(e -> penalties());
        btnPay.addActionListener(e -> pay());

        refresh();
    }

    private JLabel section(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 14f));
        return l;
    }

    public void refresh() {
        try {
            cmbInstitution.removeAllItems();
            for (Institution i : new InstitutionDAO().findAll()) cmbInstitution.addItem(i);
            cmbPackage.removeAllItems();
            for (BandwidthPackage p : billDAO.findPackages()) cmbPackage.addItem(p);
            cmbBill.removeAllItems();
            for (Bill b : billDAO.findUnpaid()) cmbBill.addItem(b);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not load data: " + ex.getMessage());
        }
    }

    private void assign() {
        Institution inst = (Institution) cmbInstitution.getSelectedItem();
        BandwidthPackage pkg = (BandwidthPackage) cmbPackage.getSelectedItem();
        if (inst == null || pkg == null) {
            JOptionPane.showMessageDialog(this, "Choose an institution and a package.");
            return;
        }
        try {
            if (!new PaymentDAO().hasPayment(inst.getInstitutionId(), "INSTALLATION")) {
                JOptionPane.showMessageDialog(this, "Record the installation payment first.");
                return;
            }
            JOptionPane.showMessageDialog(this, billDAO.assignBandwidth(inst.getInstitutionId(), pkg));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not save: " + ex.getMessage());
        }
    }

    private void generate() {
        try {
            LocalDate month = YearMonth.parse(txtMonth.getText().trim()).atDay(1);
            int n = billDAO.generateBills(month);
            JOptionPane.showMessageDialog(this, n + " bill(s) created for " + txtMonth.getText().trim() + ".");
            refresh();
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Month must look like 2026-09.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not generate: " + ex.getMessage());
        }
    }

    private void penalties() {
        try {
            int[] r = billDAO.applyPenalties();
            JOptionPane.showMessageDialog(this, r[0] + " bill(s) fined, " + r[1] + " institution(s) disconnected.");
            refresh();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not apply: " + ex.getMessage());
        }
    }

    private void pay() {
        Bill bill = (Bill) cmbBill.getSelectedItem();
        if (bill == null) {
            JOptionPane.showMessageDialog(this, "No unpaid bill selected.");
            return;
        }
        try {
            JOptionPane.showMessageDialog(this, billDAO.payBill(bill, LocalDate.now()));
            refresh();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not pay: " + ex.getMessage());
        }
    }
}
