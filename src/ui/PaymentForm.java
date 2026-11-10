package ui;

import dao.InstitutionDAO;
import dao.PaymentDAO;
import model.Institution;
import model.Payment;
import service.FeeCalculator;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class PaymentForm extends JPanel {

    private JComboBox<Institution> cmbInstitution = new JComboBox<>();
    private JComboBox<String> cmbType = new JComboBox<>(new String[]{
        "REGISTRATION", "INSTALLATION", "PC", "LAN", "MONTHLY", "FINE", "RECONNECTION"});
    private JTextField txtAmount = new JTextField(15);
    private JTextField txtDate = new JTextField(LocalDate.now().toString(), 15);
    private JButton btnSave = new JButton("Save payment");

    private PaymentDAO paymentDAO = new PaymentDAO();

    public PaymentForm() {
        
        setLayout(new GridLayout(0, 2, 8, 8));

        add(new JLabel("Institution:"));       add(cmbInstitution);
        add(new JLabel("Payment type:"));      add(cmbType);
        add(new JLabel("Amount (KSh):"));      add(txtAmount);
        add(new JLabel("Date (yyyy-mm-dd):")); add(txtDate);
        add(new JLabel(""));                   add(btnSave);

        refresh();
        fillAmount();

        cmbType.addActionListener(e -> fillAmount());
        btnSave.addActionListener(e -> save());

        
    }

    public void refresh() {
    cmbInstitution.removeAllItems();
    try {
        for (Institution i : new InstitutionDAO().findAll()) {
            cmbInstitution.addItem(i);
        }
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Could not load institutions: " + ex.getMessage());
    }
}

    // Auto-fill fixed amounts so staff cannot mistype them
    private void fillAmount() {
        String type = (String) cmbType.getSelectedItem();
        if ("REGISTRATION".equals(type)) {
            txtAmount.setText(String.valueOf(FeeCalculator.REGISTRATION_FEE));
            txtAmount.setEditable(false);
        } else if ("INSTALLATION".equals(type)) {
            txtAmount.setText(String.valueOf(FeeCalculator.INSTALLATION_FEE));
            txtAmount.setEditable(false);
        } else if ("RECONNECTION".equals(type)) {
            txtAmount.setText(String.valueOf(FeeCalculator.RECONNECTION_FEE));
            txtAmount.setEditable(false);
        } else {
            txtAmount.setText("");
            txtAmount.setEditable(true);
        }
    }

    private void save() {
        Institution inst = (Institution) cmbInstitution.getSelectedItem();
        if (inst == null) {
            JOptionPane.showMessageDialog(this, "Register an institution first.");
            return;
        }
        String type = (String) cmbType.getSelectedItem();

        double amount;
        try {
            amount = Double.parseDouble(txtAmount.getText().trim());
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid amount.");
            return;
        }

        LocalDate date;
        try {
            date = LocalDate.parse(txtDate.getText().trim());
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Date must look like 2026-10-11.");
            return;
        }

        try {
            // Rule 1: registration is paid only once
            if ("REGISTRATION".equals(type) && paymentDAO.hasPayment(inst.getInstitutionId(), "REGISTRATION")) {
                JOptionPane.showMessageDialog(this, "Registration fee already paid.");
                return;
            }
            // Rule 2: installation needs registration first
            if ("INSTALLATION".equals(type) && !paymentDAO.hasPayment(inst.getInstitutionId(), "REGISTRATION")) {
                JOptionPane.showMessageDialog(this, "Pay the registration fee first.");
                return;
            }
            paymentDAO.save(new Payment(inst.getInstitutionId(), type, amount, date));
            JOptionPane.showMessageDialog(this, "Payment saved.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not save: " + ex.getMessage());
        }
    }
}