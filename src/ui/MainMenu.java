package ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class MainMenu extends JFrame {

    private static final Color HEADER_BG = new Color(88, 44, 131);
    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 22);
    private static final Font SUB_FONT = new Font("SansSerif", Font.PLAIN, 13);

    public MainMenu() {
        setTitle("Azani ISP Information System");
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);

        RegisterForm registerPanel = new RegisterForm();
        PaymentForm paymentPanel = new PaymentForm();

        SurveyForm surveyPanel = new SurveyForm();
        BillingForm billingPanel = new BillingForm();

        ReportForm reportPanel = new ReportForm();
        reportPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Register Institution", wrap(registerPanel));
        tabs.addTab("Record Payment", wrap(paymentPanel));
        tabs.addTab("Site Survey", wrap(surveyPanel));
        tabs.addTab("Billing", wrap(billingPanel));
        tabs.addTab("Reports", reportPanel);
        // Reload the institution list whenever the Payment tab is opened
        tabs.addChangeListener(e -> {
    int i = tabs.getSelectedIndex();
    if (i == 1) paymentPanel.refresh();
    if (i == 2) surveyPanel.refresh();
    if(i == 3 ) billingPanel.refresh();
        });

        add(tabs, BorderLayout.CENTER);

        setSize(950, 620);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    // Keeps the form at its natural size at the top of the tab
    private JPanel wrap(JPanel form) {
        JPanel holder = new JPanel(new BorderLayout());
        holder.setBorder(new EmptyBorder(20, 30, 20, 30));
        holder.add(form, BorderLayout.NORTH);
        return holder;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setBackground(HEADER_BG);
        header.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("AZANI ISP", SwingConstants.CENTER);
        title.setFont(TITLE_FONT);
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Information System", SwingConstants.CENTER);
        sub.setFont(SUB_FONT);
        sub.setForeground(new Color(200, 215, 240));

        header.add(title);
        header.add(sub);
        return header;
    }
}