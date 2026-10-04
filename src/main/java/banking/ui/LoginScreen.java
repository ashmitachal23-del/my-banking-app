package banking.ui;

import banking.db.UserDAO;
import banking.model.User;
import banking.ui.admin.AdminDashboard;
import banking.ui.customer.CustomerDashboard;
import banking.ui.staff.StaffDashboard;
import banking.util.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Draggable, rounded-corner login window.
 * Selects dashboard based on user role after successful authentication.
 */
public class LoginScreen extends JFrame {

    private JTextField     usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleBox;
    private JLabel         statusLabel;
    private final int[]    dragOffset = {0, 0};

    public LoginScreen() {
        UITheme.applyTheme();
        setTitle("SecureBank");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(440, 580);
        setLocationRelativeTo(null);
        setUndecorated(true);
        try { setShape(new RoundRectangle2D.Double(0, 0, 440, 580, 18, 18)); }
        catch (Exception ignored) {}

        JPanel content = buildContent();
        setContentPane(content);
        
        // Make window draggable
        content.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                dragOffset[0] = e.getX();
                dragOffset[1] = e.getY();
            }
        });
        content.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                setLocation(getX() + e.getX() - dragOffset[0],
                            getY() + e.getY() - dragOffset[1]);
            }
        });

        setVisible(true);
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_DARK);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                // gold top accent
                g2.setColor(UITheme.ACCENT_GOLD);
                g2.fillRect(0, 0, getWidth(), 4);
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));

        // Close button top-right
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        topBar.setOpaque(false);
        JButton closeBtn = new JButton("✕");
        closeBtn.setForeground(UITheme.TEXT_MUT);
        closeBtn.setFont(UITheme.F_BODY);
        closeBtn.setBorderPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> System.exit(0));
        topBar.add(closeBtn);

        // Header section
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(20, 40, 16, 40));

        JLabel iconLbl = UITheme.label("🏦", new Font("Segoe UI Emoji", Font.PLAIN, 44), UITheme.ACCENT_GOLD);
        iconLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLbl = UITheme.label("SecureBank", new Font("Segoe UI", Font.BOLD, 28), UITheme.TEXT_PRI);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLbl = UITheme.label("Banking Management System", UITheme.F_BODY, UITheme.TEXT_MUT);
        subLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(iconLbl);
        header.add(Box.createVerticalStrut(6));
        header.add(titleLbl);
        header.add(Box.createVerticalStrut(4));
        header.add(subLbl);

        // Form section
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(0, 40, 30, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets  = new Insets(5, 0, 5, 0);

        roleBox      = UITheme.combo(new String[]{"ADMIN", "CUSTOMER", "STAFF"});
        usernameField = UITheme.textField(20);
        passwordField = UITheme.passwordField(20);
        statusLabel   = UITheme.label("", UITheme.F_SMALL, UITheme.ACCENT_RED);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton loginBtn = UITheme.primaryBtn("  LOGIN  ");
        loginBtn.setPreferredSize(new Dimension(180, 42));
        loginBtn.addActionListener(e -> doLogin());
        passwordField.addActionListener(e -> doLogin());

        JLabel hintLbl = UITheme.label("Default: admin / admin123", UITheme.F_SMALL, UITheme.TEXT_MUT);
        hintLbl.setHorizontalAlignment(SwingConstants.CENTER);

        int r = 0;
        gbc.gridy = r++; form.add(UITheme.label("Role",     UITheme.F_SMALL, UITheme.TEXT_MUT), gbc);
        gbc.gridy = r++; form.add(roleBox, gbc);
        gbc.gridy = r++; form.add(UITheme.label("Username", UITheme.F_SMALL, UITheme.TEXT_MUT), gbc);
        gbc.gridy = r++; form.add(usernameField, gbc);
        gbc.gridy = r++; form.add(UITheme.label("Password", UITheme.F_SMALL, UITheme.TEXT_MUT), gbc);
        gbc.gridy = r++; form.add(passwordField, gbc);
        gbc.gridy = r++; form.add(statusLabel, gbc);
        gbc.gridy = r++;
        gbc.insets = new Insets(14, 0, 6, 0);
        form.add(loginBtn, gbc);
        gbc.gridy = r;
        gbc.insets = new Insets(8, 0, 0, 0);
        form.add(hintLbl, gbc);

        root.add(topBar, BorderLayout.NORTH);
        root.add(header, BorderLayout.CENTER);
        root.add(form,   BorderLayout.SOUTH);
        return root;
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String role     = (String) roleBox.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please fill in all fields.");
            return;
        }

        User user = new UserDAO().login(username, password);

        if (user == null) {
            statusLabel.setText("Invalid username or password.");
            passwordField.setText("");
            return;
        }

        if (!user.getRole().equals(role)) {
            statusLabel.setText("Wrong role selected for this account.");
            return;
        }

        dispose();
        switch (role) {
            case "ADMIN"    -> new AdminDashboard(user);
            case "CUSTOMER" -> new CustomerDashboard(user);
            case "STAFF"    -> new StaffDashboard(user);
        }
    }
}
