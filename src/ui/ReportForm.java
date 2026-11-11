package ui;

import dao.ReportDAO;
import java.awt.*;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.time.LocalDate;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class ReportForm extends JPanel {

    private static final String[] REPORTS = {
        "Registered institutions",
        "Defaulters",
        "Disconnection issues",
        "Infrastructure requirements",
        "Installation cost per institution",
        "PC and LAN cost (assorted services)",
        "Upgraded monthly charges",
        "Category summary",
        "Service totals per institution"
    };

    private JComboBox<String> cmbReport = new JComboBox<>(REPORTS);
    private JButton btnRun = new JButton("Generate");
    private JButton btnPrint = new JButton("Print");
    private JButton btnExport = new JButton("Download CSV");
    private JTextField txtSearch = new JTextField(15);
    private JTable table = new JTable();
    private JLabel lblInfo = new JLabel(" ");
    private ReportDAO dao = new ReportDAO();
    private TableRowSorter<DefaultTableModel> sorter;

    public ReportForm() {
        setLayout(new BorderLayout(8, 8));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        top.add(new JLabel("Report:"));
        top.add(cmbReport);
        top.add(btnRun);
        top.add(btnPrint);
        top.add(btnExport);
        top.add(new JLabel("Search:"));
        top.add(txtSearch);

        table.setFillsViewportHeight(true);
        table.setRowHeight(24);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(lblInfo, BorderLayout.SOUTH);

        btnRun.addActionListener(e -> run());
        btnPrint.addActionListener(e -> print());
        btnExport.addActionListener(e -> exportCsv());

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });
    }

    private void run() {
        int idx = cmbReport.getSelectedIndex();
        try {
            DefaultTableModel m;
            switch (idx) {
                case 0: m = dao.registered(); break;
                case 1: m = dao.defaulters(); break;
                case 2: m = dao.disconnections(); break;
                case 3: m = dao.infrastructure(); break;
                case 4: m = dao.installationCosts(); break;
                case 5: m = dao.pcAndLanCosts(); break;
                case 6: m = dao.upgradedCharges(); break;
                case 7: m = dao.categorySummary(); break;
                default: m = dao.serviceTotals(); break;
            }
            int rows = m.getRowCount();
            // Every report except the two plain lists gets a totals row
            if (idx != 0 && idx != 3) addTotals(m);
            table.setModel(m);
            setupSorter(m);
            txtSearch.setText("");
            lblInfo.setText(rows + " record(s) - " + REPORTS[idx] + " - " + LocalDate.now());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not generate report: " + ex.getMessage());
        }
    }

    // Search: filters rows as you type, keeps the TOTAL row visible
    private void setupSorter(DefaultTableModel m) {
        sorter = new TableRowSorter<>(m);
        for (int c = 0; c < m.getColumnCount(); c++) sorter.setSortable(c, false);
        table.setRowSorter(sorter);
    }

    private void applyFilter() {
        if (sorter == null) return;
        String text = txtSearch.getText().trim().toLowerCase();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }
        sorter.setRowFilter(new RowFilter<Object, Object>() {
            @Override
            public boolean include(Entry<? extends Object, ? extends Object> entry) {
                if ("TOTAL".equals(String.valueOf(entry.getValue(0)))) return true;
                for (int i = 0; i < entry.getValueCount(); i++) {
                    if (String.valueOf(entry.getValue(i)).toLowerCase().contains(text)) return true;
                }
                return false;
            }
        });
    }

    private void addTotals(DefaultTableModel m) {
        if (m.getRowCount() == 0) return;
        Object[] total = new Object[m.getColumnCount()];
        total[0] = "TOTAL";
        for (int c = 1; c < m.getColumnCount(); c++) {
            if (m.getValueAt(0, c) instanceof Number
                    && !m.getColumnName(c).contains("MBPS")) {
                double sum = 0;
                for (int r = 0; r < m.getRowCount(); r++) {
                    Object v = m.getValueAt(r, c);
                    if (v instanceof Number) sum += ((Number) v).doubleValue();
                }
                total[c] = sum;
            }
        }
        m.addRow(total);
    }

    private void print() {
        if (table.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Generate a report first.");
            return;
        }
        try {
            String title = cmbReport.getSelectedItem() + " - " + LocalDate.now();
            table.print(JTable.PrintMode.FIT_WIDTH,
                    new MessageFormat("Azani ISP: " + title), new MessageFormat("Page {0}"));
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this, "Could not print: " + ex.getMessage());
        }
    }

    private void exportCsv() {
        if (table.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Generate a report first.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("report.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return; // user cancelled
        }

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getAbsolutePath() + ".csv");
        }

        try (PrintWriter out = new PrintWriter(file)) {
            StringBuilder header = new StringBuilder();
            for (int c = 0; c < table.getColumnCount(); c++) {
                if (c > 0) header.append(",");
                header.append(csv(table.getColumnName(c)));
            }
            out.println(header);

            for (int r = 0; r < table.getRowCount(); r++) {
                StringBuilder line = new StringBuilder();
                for (int c = 0; c < table.getColumnCount(); c++) {
                    if (c > 0) line.append(",");
                    line.append(csv(table.getValueAt(r, c)));
                }
                out.println(line);
            }

            JOptionPane.showMessageDialog(this, "Report saved to:\n" + file.getAbsolutePath());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not save report: " + ex.getMessage());
        }
    }

    // Wraps a value in quotes and escapes any quotes inside it
    private String csv(Object value) {
        String s = (value == null) ? "" : value.toString();
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
}