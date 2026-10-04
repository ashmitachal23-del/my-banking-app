package banking.ui.admin;

import banking.db.*;
import banking.model.*;
import banking.ui.LoginScreen;
import banking.ui.TransactionHistoryPopup;
import banking.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class AdminDashboard extends JFrame {

    private final User           me;
    private final UserDAO        userDAO    = new UserDAO();
    private final AccountDAO     accountDAO = new AccountDAO();
    private final LoanDAO        loanDAO    = new LoanDAO();
    private final TransactionDAO txDAO      = new TransactionDAO();
    private static final NumberFormat FMT   = NumberFormat.getCurrencyInstance(new Locale("en","IN"));

    private JTabbedPane tabs;

    public AdminDashboard(User user) {
        this.me = user;
        setTitle("SecureBank — Admin");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1120, 720);
        setLocationRelativeTo(null);
        setContentPane(build());
        setVisible(true);
    }

    // ── Root layout ────────────────────────────────────────────────────────
    private JPanel build() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_DARK);
        root.add(sidebar(), BorderLayout.WEST);
        root.add(mainArea(), BorderLayout.CENTER);
        return root;
    }

    // ── Sidebar ────────────────────────────────────────────────────────────
    private JPanel sidebar() {
        JPanel p = new JPanel();
        p.setPreferredSize(new Dimension(210, 0));
        p.setBackground(UITheme.BG_CARD);
        p.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.BORDER));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        p.add(Box.createVerticalStrut(20));
        p.add(UITheme.label("  🏦  SecureBank",   new Font("Segoe UI", Font.BOLD, 15), UITheme.ACCENT_GOLD));
        p.add(UITheme.label("  Admin Portal",      UITheme.F_SMALL, UITheme.TEXT_MUT));
        p.add(Box.createVerticalStrut(16));
        addSep(p);

        String[] nav = {"📊  Overview", "👥  Users", "🏦  Accounts", "💰  Loans", "📋  Transactions"};
        for (int i = 0; i < nav.length; i++) {
            final int idx = i;
            JButton b = UITheme.sidebarBtn(nav[i]);
            b.addActionListener(e -> tabs.setSelectedIndex(idx));
            p.add(b);
        }

        p.add(Box.createVerticalGlue());
        p.add(UITheme.label("  👤 " + me.getFullName(), UITheme.F_SMALL, UITheme.TEXT_PRI));
        p.add(Box.createVerticalStrut(6));
        JButton logout = UITheme.sidebarBtn("🚪  Logout");
        logout.setForeground(UITheme.ACCENT_RED);
        logout.addActionListener(e -> { dispose(); new LoginScreen(); });
        p.add(logout);
        p.add(Box.createVerticalStrut(16));
        return p;
    }

    private void addSep(JPanel p) {
        JSeparator sep = new JSeparator();
        sep.setForeground(UITheme.BORDER);
        sep.setMaximumSize(new Dimension(210, 1));
        p.add(sep);
        p.add(Box.createVerticalStrut(10));
    }

    // ── Main area ──────────────────────────────────────────────────────────
    private JPanel mainArea() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UITheme.BG_DARK);

        JPanel hdr = new JPanel(new FlowLayout(FlowLayout.LEFT));
        hdr.setBackground(UITheme.BG_DARK);
        hdr.setBorder(BorderFactory.createEmptyBorder(16, 16, 0, 16));
        hdr.add(UITheme.label("Admin Dashboard", UITheme.F_TITLE, UITheme.TEXT_PRI));

        tabs = new JTabbedPane();
        tabs.setBackground(UITheme.BG_DARK);
        tabs.setForeground(UITheme.TEXT_PRI);
        tabs.setFont(UITheme.F_BODY);
        tabs.addTab("Overview",     tabOverview());
        tabs.addTab("Users",        tabUsers());
        tabs.addTab("Accounts",     tabAccounts());
        tabs.addTab("Loans",        tabLoans());
        tabs.addTab("Transactions", tabTransactions());

        p.add(hdr,  BorderLayout.NORTH);
        p.add(tabs, BorderLayout.CENTER);
        return p;
    }

    // ── Tab: Overview ──────────────────────────────────────────────────────
    private JPanel tabOverview() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Stat cards
        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 0));
        cards.setOpaque(false);
        cards.add(statCard("👥  Users",        String.valueOf(userDAO.getAll().size()),       UITheme.ACCENT_BLUE));
        cards.add(statCard("🏦  Accounts",      String.valueOf(accountDAO.getAll().size()),    UITheme.ACCENT_GREEN));
        cards.add(statCard("📋  Pending Loans", String.valueOf(loanDAO.getPending().size()),   UITheme.ACCENT_GOLD));
        cards.add(statCard("💳  Transactions",  String.valueOf(txDAO.getAll().size()),         UITheme.ACCENT_RED));

        // Recent transactions
        JLabel lbl = UITheme.label("Recent Transactions", UITheme.F_HEADING, UITheme.TEXT_PRI);
        String[] cols = {"ID", "Account", "Type", "Amount", "Date"};
        DefaultTableModel mdl = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        List<Transaction> recent = txDAO.getAll();
        int max = Math.min(20, recent.size());
        for (int i = 0; i < max; i++) {
            Transaction t = recent.get(i);
            mdl.addRow(new Object[]{t.getTransactionId(), t.getAccountId(),
                t.getTransactionType(), FMT.format(t.getAmount()), t.getTransactionDate()});
        }
        JTable tbl = new JTable(mdl);
        UITheme.styleTable(tbl);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(lbl,                    BorderLayout.NORTH);
        center.add(UITheme.scroll(tbl),    BorderLayout.CENTER);

        p.add(cards,  BorderLayout.NORTH);
        p.add(center, BorderLayout.CENTER);
        return p;
    }

    private JPanel statCard(String title, String value, Color accent) {
        JPanel c = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_CARD);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1,1,getWidth()-2,getHeight()-2,12,12);
                g2.fillRect(0, getHeight()-4, getWidth(), 4);
                g2.dispose();
            }
        };
        c.setOpaque(false);
        c.setBorder(BorderFactory.createEmptyBorder(16,14,16,14));
        c.add(UITheme.label(title, UITheme.F_BODY, UITheme.TEXT_MUT), BorderLayout.NORTH);
        c.add(UITheme.label(value, new Font("Segoe UI",Font.BOLD,30), accent), BorderLayout.CENTER);
        return c;
    }

    // ── Tab: Users ─────────────────────────────────────────────────────────
    private JPanel tabUsers() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(14,14,14,14));

        String[] cols = {"ID","Username","Full Name","Role","Email","Phone","Active","Created"};
        DefaultTableModel mdl = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        Runnable reload = () -> {
            mdl.setRowCount(0);
            for (User u : userDAO.getAll())
                mdl.addRow(new Object[]{u.getUserId(), u.getUsername(), u.getFullName(),
                    u.getRole(), u.getEmail(), u.getPhone(),
                    u.isActive() ? "✅ Yes" : "❌ No", u.getCreatedAt()});
        };
        reload.run();

        JTable tbl = new JTable(mdl);
        UITheme.styleTable(tbl);

        JPanel toolbar = toolbar();
        JButton addBtn    = UITheme.primaryBtn("+ Add User");
        JButton toggleBtn = UITheme.ghostBtn("Toggle Status");
        JButton delBtn    = UITheme.dangerBtn("Delete");

        addBtn.addActionListener(e -> showAddUser(reload));
        toggleBtn.addActionListener(e -> {
            int row = tbl.getSelectedRow();
            if (row < 0) { UITheme.err(this, "Select a user."); return; }
            int id  = (int) mdl.getValueAt(row, 0);
            boolean cur = mdl.getValueAt(row, 6).toString().startsWith("✅");
            userDAO.toggleStatus(id, !cur);
            reload.run();
        });
        delBtn.addActionListener(e -> {
            int row = tbl.getSelectedRow();
            if (row < 0) { UITheme.err(this, "Select a user."); return; }
            int id = (int) mdl.getValueAt(row, 0);
            if (id == me.getUserId()) { UITheme.err(this, "Cannot delete yourself."); return; }
            if (UITheme.confirm(this, "Delete this user permanently?")) { userDAO.delete(id); reload.run(); }
        });

        toolbar.add(addBtn); toolbar.add(toggleBtn); toolbar.add(delBtn);
        p.add(toolbar,             BorderLayout.NORTH);
        p.add(UITheme.scroll(tbl), BorderLayout.CENTER);
        return p;
    }

    private void showAddUser(Runnable reload) {
        JDialog dlg = dialog("Add New User", 400, 360);
        JPanel form = formPanel(6, 2);

        JTextField nameF  = UITheme.textField(14);
        JTextField userF  = UITheme.textField(14);
        JPasswordField passF = UITheme.passwordField(14);
        JComboBox<String> roleC = UITheme.combo(new String[]{"ADMIN","CUSTOMER","STAFF"});
        JTextField emailF = UITheme.textField(14);
        JTextField phoneF = UITheme.textField(14);

        addRow(form, "Full Name", nameF);
        addRow(form, "Username",  userF);
        addRow(form, "Password",  passF);
        addRow(form, "Role",      roleC);
        addRow(form, "Email",     emailF);
        addRow(form, "Phone",     phoneF);

        JButton save = UITheme.primaryBtn("Create User");
        save.addActionListener(e -> {
            User u = new User();
            u.setFullName(nameF.getText().trim());
            u.setUsername(userF.getText().trim());
            u.setPassword(new String(passF.getPassword()));
            u.setRole((String)roleC.getSelectedItem());
            u.setEmail(emailF.getText().trim());
            u.setPhone(phoneF.getText().trim());
            if (userDAO.create(u)) { UITheme.ok(dlg,"User created!"); reload.run(); dlg.dispose(); }
            else UITheme.err(dlg, "Failed. Username may already exist.");
        });

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(footerPanel(save), BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ── Tab: Accounts ──────────────────────────────────────────────────────
    private JPanel tabAccounts() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(14,14,14,14));

        String[] cols = {"ID","Account No","Owner","Type","Balance","Status","Opened"};
        DefaultTableModel mdl = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        Runnable reload = () -> {
            mdl.setRowCount(0);
            for (Account a : accountDAO.getAll())
                mdl.addRow(new Object[]{a.getAccountId(), a.getAccountNumber(), a.getOwnerName(),
                    a.getAccountType(), FMT.format(a.getBalance()), a.getStatus(), a.getOpenedDate()});
        };
        reload.run();

        JTable tbl = new JTable(mdl);
        UITheme.styleTable(tbl);

        JPanel toolbar = toolbar();
        JButton openBtn    = UITheme.primaryBtn("+ Open Account");
        JButton histBtn    = UITheme.ghostBtn("View History");
        JButton freezeBtn  = UITheme.ghostBtn("Freeze/Unfreeze");
        JButton closeAccBtn= UITheme.dangerBtn("Close Account");

        openBtn.addActionListener(e -> showOpenAccount(reload));
        histBtn.addActionListener(e -> {
            int row = tbl.getSelectedRow();
            if (row < 0) { UITheme.err(this,"Select an account."); return; }
            Account acc = accountDAO.getById((int)mdl.getValueAt(row,0));
            new TransactionHistoryPopup(this, acc);
        });
        freezeBtn.addActionListener(e -> {
            int row = tbl.getSelectedRow();
            if (row < 0) { UITheme.err(this,"Select an account."); return; }
            int id  = (int) mdl.getValueAt(row,0);
            String cur = mdl.getValueAt(row,5).toString();
            accountDAO.updateStatus(id, "FROZEN".equals(cur) ? "ACTIVE" : "FROZEN");
            reload.run();
        });
        closeAccBtn.addActionListener(e -> {
            int row = tbl.getSelectedRow();
            if (row < 0) { UITheme.err(this,"Select an account."); return; }
            if (UITheme.confirm(this,"Close this account?")) {
                accountDAO.updateStatus((int)mdl.getValueAt(row,0),"CLOSED");
                reload.run();
            }
        });

        toolbar.add(openBtn); toolbar.add(histBtn); toolbar.add(freezeBtn); toolbar.add(closeAccBtn);
        p.add(toolbar,             BorderLayout.NORTH);
        p.add(UITheme.scroll(tbl), BorderLayout.CENTER);
        return p;
    }

    private void showOpenAccount(Runnable reload) {
        JDialog dlg = dialog("Open New Account", 380, 240);
        JPanel form = formPanel(3, 2);

        List<User> customers = userDAO.getByRole("CUSTOMER");
        String[] names = customers.stream().map(u -> u.getUserId() + " - " + u.getFullName()).toArray(String[]::new);
        JComboBox<String> custCb = UITheme.combo(names.length > 0 ? names : new String[]{"No customers"});
        JComboBox<String> typeCb = UITheme.combo(new String[]{"SAVINGS","CURRENT","FIXED_DEPOSIT"});
        JTextField balF = UITheme.textField(12); balF.setText("0.00");

        addRow(form, "Customer",        custCb);
        addRow(form, "Account Type",    typeCb);
        addRow(form, "Initial Balance", balF);

        JButton save = UITheme.primaryBtn("Open Account");
        save.addActionListener(e -> {
            try {
                if (customers.isEmpty()) { UITheme.err(dlg,"No customers found."); return; }
                User cust = customers.get(custCb.getSelectedIndex());
                Account acc = new Account();
                acc.setUserId(cust.getUserId());
                acc.setAccountNumber(accountDAO.nextAccountNumber());
                acc.setAccountType((String)typeCb.getSelectedItem());
                acc.setBalance(new BigDecimal(balF.getText().trim()));
                if (accountDAO.create(acc)) {
                    UITheme.ok(dlg, "Account " + acc.getAccountNumber() + " opened!");
                    reload.run(); dlg.dispose();
                } else UITheme.err(dlg,"Failed to open account.");
            } catch (NumberFormatException ex) { UITheme.err(dlg,"Invalid balance."); }
        });

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(footerPanel(save), BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ── Tab: Loans ─────────────────────────────────────────────────────────
    private JPanel tabLoans() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(14,14,14,14));

        String[] cols = {"ID","Account","Owner","Type","Principal","EMI","Outstanding","Status","Applied"};
        DefaultTableModel mdl = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        Runnable reload = () -> {
            mdl.setRowCount(0);
            for (Loan l : loanDAO.getAll())
                mdl.addRow(new Object[]{l.getLoanId(), l.getAccountNumber(), l.getOwnerName(),
                    l.getLoanType(), FMT.format(l.getPrincipal()), FMT.format(l.getEmiAmount()),
                    FMT.format(l.getOutstanding()), l.getStatus(), l.getAppliedDate()});
        };
        reload.run();

        JTable tbl = new JTable(mdl);
        UITheme.styleTable(tbl);

        JPanel toolbar = toolbar();
        JButton approveBtn = UITheme.primaryBtn("✅ Approve");
        JButton rejectBtn  = UITheme.dangerBtn("❌ Reject");

        approveBtn.addActionListener(e -> {
            int row = tbl.getSelectedRow();
            if (row < 0) { UITheme.err(this,"Select a loan."); return; }
            int id = (int)mdl.getValueAt(row,0);
            if (loanDAO.updateStatus(id,"ACTIVE",me.getUserId()))
                { UITheme.ok(this,"Loan approved!"); reload.run(); }
        });
        rejectBtn.addActionListener(e -> {
            int row = tbl.getSelectedRow();
            if (row < 0) { UITheme.err(this,"Select a loan."); return; }
            if (UITheme.confirm(this,"Reject this loan?"))
                { loanDAO.updateStatus((int)mdl.getValueAt(row,0),"REJECTED",me.getUserId()); reload.run(); }
        });

        toolbar.add(approveBtn); toolbar.add(rejectBtn);
        p.add(toolbar,             BorderLayout.NORTH);
        p.add(UITheme.scroll(tbl), BorderLayout.CENTER);
        return p;
    }

    // ── Tab: Transactions ──────────────────────────────────────────────────
    private JPanel tabTransactions() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(14,14,14,14));

        String[] cols = {"Tx ID","Account","Type","Amount","Bal After","Description","Date","By"};
        DefaultTableModel mdl = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        for (Transaction t : txDAO.getAll())
            mdl.addRow(new Object[]{t.getTransactionId(), t.getAccountId(), t.getTransactionType(),
                FMT.format(t.getAmount()), FMT.format(t.getBalanceAfter()),
                t.getDescription(), t.getTransactionDate(),
                t.getPerformedBy() != null ? t.getPerformedBy() : "System"});

        JTable tbl = new JTable(mdl);
        UITheme.styleTable(tbl);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setOpaque(false);
        header.add(UITheme.label("Showing last 1000 transactions", UITheme.F_SMALL, UITheme.TEXT_MUT));

        p.add(header,          BorderLayout.NORTH);
        p.add(UITheme.scroll(tbl), BorderLayout.CENTER);
        return p;
    }

    // ── Dialog/form helpers ────────────────────────────────────────────────
    private JDialog dialog(String title, int w, int h) {
        JDialog dlg = new JDialog(this, title, true);
        dlg.setSize(w, h);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(UITheme.BG_CARD);
        return dlg;
    }

    private JPanel formPanel(int rows, int cols) {
        JPanel p = new JPanel(new GridLayout(rows, cols, 10, 10));
        p.setBackground(UITheme.BG_CARD);
        p.setBorder(BorderFactory.createEmptyBorder(20,20,10,20));
        return p;
    }

    private void addRow(JPanel form, String label, JComponent field) {
        form.add(UITheme.label(label, UITheme.F_BODY, UITheme.TEXT_PRI));
        form.add(field);
    }

    private JPanel footerPanel(JButton btn) {
        JPanel fp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        fp.setBackground(UITheme.BG_CARD);
        fp.add(btn);
        return fp;
    }

    private JPanel toolbar() {
        JPanel tb = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tb.setOpaque(false);
        return tb;
    }
}
