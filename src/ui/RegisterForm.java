package ui;

import dao.InstitutionDAO;
import model.ContactPerson;
import model.Institution;

import javax.swing.*;
import java.awt.*;

public class RegisterForm extends JPanel {

    private JTextField txtName = new JTextField(20);
    private JComboBox<String> cmbCategory =
        new JComboBox<>(new String[]{"PRIMARY", "JUNIOR", "SENIOR", "COLLEGE"});
    private JTextField txtLocation = new JTextField(20);
    private JTextField txtContactName = new JTextField(20);
    private JTextField txtPhone = new JTextField(20);
    private JTextField txtEmail = new JTextField(20);
    private JTextField txtPosition = new JTextField(20);
    private JButton btnSave = new JButton("Save");
    private JButton btnClear = new JButton("Clear");

    public RegisterForm() {
        
        setLayout(new GridLayout(0, 2, 8, 8));

        add(new JLabel("Institution name:"));  add(txtName);
        add(new JLabel("Category:"));          add(cmbCategory);
        add(new JLabel("Location:"));          add(txtLocation);
        add(new JLabel("Contact full name:")); add(txtContactName);
        add(new JLabel("Phone:"));             add(txtPhone);
        add(new JLabel("Email:"));             add(txtEmail);
        add(new JLabel("Position:"));          add(txtPosition);
        add(btnSave);                          add(btnClear);

        btnSave.addActionListener(e -> save());
        btnClear.addActionListener(e -> clear());

        
    }

    private void save() {
        String name = txtName.getText().trim();
        String location = txtLocation.getText().trim();
        String contactName = txtContactName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String position = txtPosition.getText().trim();

        // Validation
        if (name.isEmpty() || contactName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Institution name and contact name are required.");
            return;
        }
        if (!phone.matches("\\d{10,12}")) {
            JOptionPane.showMessageDialog(this, "Phone must be 10 to 12 digits.");
            return;
        }
        if (!email.contains("@")) {
            JOptionPane.showMessageDialog(this, "Enter a valid email address.");
            return;
        }

        Institution inst = new Institution(name, (String) cmbCategory.getSelectedItem(), location);
        ContactPerson cp = new ContactPerson(contactName, phone, email, position);

        try {
            new InstitutionDAO().register(inst, cp);
            JOptionPane.showMessageDialog(this, "Institution registered.");
            clear();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not save: " + ex.getMessage());
        }
    }

    private void clear() {
        txtName.setText("");
        txtLocation.setText("");
        txtContactName.setText("");
        txtPhone.setText("");
        txtEmail.setText("");
        txtPosition.setText("");
        cmbCategory.setSelectedIndex(0);
    }
}