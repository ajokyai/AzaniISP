package ui;

import dao.InstitutionDAO;
import dao.SurveyDAO;
import java.awt.*;
import java.time.LocalDate;
import javax.swing.*;
import model.Institution;
import model.Survey;
import service.FeeCalculator;

public class SurveyForm extends JPanel {

    private JComboBox<Institution> cmbInstitution = new JComboBox<>();
    private JTextField txtUsers = new JTextField(15);
    private JTextField txtPcsExisting = new JTextField("0", 15);
    private JCheckBox chkLanReady = new JCheckBox("LAN is ready");
    private JTextField txtPcsToBuy = new JTextField("0", 15);
    private JTextField txtNodesToBuy = new JTextField("0", 15);
    private JLabel lblTotal = new JLabel("Total installation cost: KSh 0");
    private JButton btnCalc = new JButton("Calculate cost");
    private JButton btnSave = new JButton("Save survey");

    private FeeCalculator calc = new FeeCalculator();

    public SurveyForm() {
        setLayout(new GridLayout(0, 2, 8, 8));

        add(new JLabel("Institution:"));        add(cmbInstitution);
        add(new JLabel("Number of users:"));    add(txtUsers);
        add(new JLabel("PCs it already has:")); add(txtPcsExisting);
        add(new JLabel("LAN status:"));         add(chkLanReady);
        add(new JLabel("PCs to buy:"));         add(txtPcsToBuy);
        add(new JLabel("LAN nodes to buy:"));   add(txtNodesToBuy);
        add(btnCalc);                           add(lblTotal);
        add(new JLabel(""));                    add(btnSave);

        btnCalc.addActionListener(e -> calculate());
        btnSave.addActionListener(e -> save());
        refresh();
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

    // Returns the total, or -1 if the inputs are invalid
    private double calculate() {
        try {
            int pcs = Integer.parseInt(txtPcsToBuy.getText().trim());
            int nodes = Integer.parseInt(txtNodesToBuy.getText().trim());
            if (pcs < 0 || nodes < 0) throw new NumberFormatException();
            double total = calc.installationTotal(pcs, nodes);
            lblTotal.setText("Total installation cost: KSh " + String.format("%,.0f", total));
            return total;
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "PCs and nodes must be whole numbers, 0 or more.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "LAN nodes must be 0, or between 2 and 100.");
        }
        return -1;
    }

    private void save() {
        Institution inst = (Institution) cmbInstitution.getSelectedItem();
        if (inst == null) {
            JOptionPane.showMessageDialog(this, "Register an institution first.");
            return;
        }
        if (calculate() < 0) return;

        try {
            int users = Integer.parseInt(txtUsers.getText().trim());
            int pcsExisting = Integer.parseInt(txtPcsExisting.getText().trim());
            int pcsToBuy = Integer.parseInt(txtPcsToBuy.getText().trim());
            int nodes = Integer.parseInt(txtNodesToBuy.getText().trim());
            if (users < 0 || pcsExisting < 0) throw new NumberFormatException();

            Survey s = new Survey(inst.getInstitutionId(), users, pcsExisting,
                    pcsToBuy, chkLanReady.isSelected(), nodes, LocalDate.now());
            new SurveyDAO().save(s);
            JOptionPane.showMessageDialog(this, "Survey saved.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Users and PC counts must be whole numbers.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not save: " + ex.getMessage());
        }
    }
}
