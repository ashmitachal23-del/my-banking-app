package banking.ui.staff;

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

public class StaffDashboard extends JFrame {

    private final User           me;
    private final AccountDAO     accountDAO = new AccountDAO();
    private final TransactionDAO txDAO      = new TransactionDAO();
    private final LoanDAO        loanDAO    = new LoanDAO();
    private static final NumberFormat FMT   = NumberFormat.getCurrencyInstance(new Locale("en","IN"));

    private JTabbedPane tabs;

    public StaffDashboard(User user) {
        this.me = user;
        setTitle("SecureBank — Staff");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1050, 680);
        setLocationRelativeTo(null);
        setContentPane(build());
        setVisible(true);
    }

    private JPanel build() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_DARK);
        root.add(sidebar(),  BorderLayout.WEST);
        root.add(mainArea(), BorderLayout.CENTER);
        return root;
    }

    private JPanel sidebar() {
        JPanel p = new JPanel();
        p.setPreferredSize(new Dimension(200, 0));
        p.setBackground(UITheme.BG_CARD);
        p.setBorder(BorderFactory.createMatteBorder(0,0,0,1,UITheme.BORDER));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(Box.createVerticalStrut(20));
        p.add(UITheme.label("  🏦  SecureBank", new Font("Segoe UI",Font.BOLD,15), UITheme.ACCENT_GOLD));
        p.add(UITheme.label("  Staff Portal",   UITheme.F_SMALL, UITheme.TEXT_MUT));
        p.add(Box.createVerticalStrut(16));

        String[] nav = {"🏦  Accounts","💰  Deposit / Withdraw","🔄  Transfer","📋  Loan Requests","📊  All Transactions"};
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

    private JPanel mainArea() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UITheme.BG_DARK);
        JPanel hdr = new JPanel(new FlowLayout(FlowLayout.LEFT));
        hdr.setBackground(UITheme.BG_DARK);
        hdr.setBorder(BorderFactory.createEmptyBorder(16,16,0,16));
        hdr.add(UITheme.label("Staff Dashboard — " + me.getFullName(), UITheme.F_TITLE, UITheme.TEXT_PRI));

        tabs = new JTabbedPane();
        tabs.setBackground(UITheme.BG_DARK);
        tabs.setForeground(UITheme.TEXT_PRI);
        tabs.setFont(UITheme.F_BODY);
        tabs.addTab("Accounts",          tabAccounts());
        tabs.addTab("Deposit/Withdraw",  tabDepositWithdraw());
        tabs.addTab("Transfer",          tabTransfer());
        tabs.addTab("Loan Requests",     tabLoanRequests());
        tabs.addTab("All Transactions",  tabAllTransactions());

        p.add(hdr,  BorderLayout.NORTH);
        p.add(tabs, BorderLayout.CENTER);
        return p;
    }

    // ── Tab: Accounts ──────────────────────────────────────────────────────
    private JPanel tabAccounts() {
        JPanel p = new JPanel(new BorderLayout(0,10));
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(14,14,14,14));

        String[] cols = {"ID","Account No","Owner","Type","Balance","Status","Opened"};
        DefaultTableModel mdl = new DefaultTableModel(cols,0) {
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

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        toolbar.setOpaque(false);

        JTextField searchF = UITheme.textField(16);
        searchF.setMaximumSize(new Dimension(160,32));
        JButton searchBtn = UITheme.ghostBtn("🔍 Search");
        JButton histBtn   = UITheme.primaryBtn("📋 View History");
        JButton popupBtn  = UITheme.ghostBtn("Popup Tx");

        searchBtn.addActionListener(e -> {
            String q = searchF.getText().trim().toLowerCase();
            mdl.setRowCount(0);
            for (Account a : accountDAO.getAll()) {
                if (a.getAccountNumber().toLowerCase().contains(q) ||
                    a.getOwnerName().toLowerCase().contains(q)) {
                    mdl.addRow(new Object[]{a.getAccountId(), a.getAccountNumber(), a.getOwnerName(),
                        a.getAccountType(), FMT.format(a.getBalance()), a.getStatus(), a.getOpenedDate()});
                }
            }
            if (mdl.getRowCount() == 0) reload.run();
        });

        histBtn.addActionListener(e -> {
            int row = tbl.getSelectedRow();
            if (row < 0) { UITheme.err(this,"Select an account."); return; }
            Account acc = accountDAO.getById((int)mdl.getValueAt(row,0));
            new TransactionHistoryPopup(this, acc);
        });

        popupBtn.addActionListener(e -> {
            String no = JOptionPane.showInputDialog(this, "Enter Account Number:");
            if (no == null || no.isBlank()) return;
            Account acc = accountDAO.getByNumber(no.trim());
            if (acc == null) { UITheme.err(this,"Account not found."); return; }
            new TransactionHistoryPopup(this, acc);
        });

        toolbar.add(searchF); toolbar.add(searchBtn); toolbar.add(histBtn); toolbar.add(popupBtn);
        p.add(toolbar,             BorderLayout.NORTH);
        p.add(UITheme.scroll(tbl), BorderLayout.CENTER);
        return p;
    }

    // ── Tab: Deposit / Withdraw ────────────────────────────────────────────
    private JPanel tabDepositWithdraw() {
        JPanel p = new JPanel(new GridLayout(1,2,16,0));
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        p.add(opCard("💰  Deposit",  true));
        p.add(opCard("💸  Withdraw", false));
        return p;
    }

    private JPanel opCard(String title, boolean isDeposit) {
        JPanel card = UITheme.card();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets  = new Insets(7,7,7,7);

        Color opColor = isDeposit ? UITheme.ACCENT_GREEN : UITheme.ACCENT_RED;

        JTextField accF  = UITheme.textField(16);
        JTextField amtF  = UITheme.textField(16);
        JTextField descF = UITheme.textField(16);
        JLabel ownerLbl  = UITheme.label("", UITheme.F_SMALL, UITheme.TEXT_MUT);
        JLabel statusLbl = UITheme.label("", UITheme.F_BODY, opColor);

        // Auto-lookup owner when pressing Enter in account field
        accF.addActionListener(e -> {
            Account a = accountDAO.getByNumber(accF.getText().trim());
            ownerLbl.setText(a != null ? "👤 " + a.getOwnerName() + "  |  " + a.getAccountType() : "Account not found");
        });

        int r = 0;
        gbc.gridy = r++; card.add(UITheme.label(title, new Font("Segoe UI",Font.BOLD,16), opColor), gbc);
        gbc.gridy = r++; card.add(UITheme.label("Account Number (press Enter to lookup)", UITheme.F_SMALL, UITheme.TEXT_MUT), gbc);
        gbc.gridy = r++; card.add(accF, gbc);
        gbc.gridy = r++; card.add(ownerLbl, gbc);
        gbc.gridy = r++; card.add(UITheme.label("Amount (₹)", UITheme.F_SMALL, UITheme.TEXT_MUT), gbc);
        gbc.gridy = r++; card.add(amtF, gbc);
        gbc.gridy = r++; card.add(UITheme.label("Description", UITheme.F_SMALL, UITheme.TEXT_MUT), gbc);
        gbc.gridy = r++; card.add(descF, gbc);

        JButton btn = isDeposit ? UITheme.primaryBtn("  DEPOSIT  ") : UITheme.dangerBtn("  WITHDRAW  ");
        btn.setPreferredSize(new Dimension(180, 40));
        gbc.gridy = r++; gbc.insets = new Insets(14,7,7,7); card.add(btn, gbc);
        gbc.gridy = r;   gbc.insets = new Insets(4,7,7,7);  card.add(statusLbl, gbc);

        btn.addActionListener(e -> {
            Account acc = accountDAO.getByNumber(accF.getText().trim());
            if (acc == null) { statusLbl.setForeground(UITheme.ACCENT_RED); statusLbl.setText("❌ Account not found."); return; }
            try {
                BigDecimal amt = new BigDecimal(amtF.getText().trim());
                if (isDeposit) {
                    if (txDAO.deposit(acc.getAccountId(), amt, descF.getText(), me.getUserId())) {
                        statusLbl.setForeground(UITheme.ACCENT_GREEN);
                        statusLbl.setText("✅ Deposited " + FMT.format(amt));
                        accF.setText(""); amtF.setText(""); descF.setText(""); ownerLbl.setText("");
                    } else { statusLbl.setForeground(UITheme.ACCENT_RED); statusLbl.setText("❌ Deposit failed."); }
                } else {
                    String result = txDAO.withdraw(acc.getAccountId(), amt, descF.getText(), me.getUserId());
                    if ("SUCCESS".equals(result)) {
                        statusLbl.setForeground(UITheme.ACCENT_GREEN);
                        statusLbl.setText("✅ Withdrawn " + FMT.format(amt));
                        accF.setText(""); amtF.setText(""); descF.setText(""); ownerLbl.setText("");
                    } else { statusLbl.setForeground(UITheme.ACCENT_RED); statusLbl.setText("❌ " + result); }
                }
            } catch (NumberFormatException ex) { statusLbl.setForeground(UITheme.ACCENT_RED); statusLbl.setText("❌ Invalid amount."); }
        });

        return card;
    }

    // ── Tab: Transfer ──────────────────────────────────────────────────────
    private JPanel tabTransfer() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(30,100,30,100));

        JPanel card = UITheme.card();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(7,7,7,7);

        JTextField fromF = UITheme.textField(20);
        JTextField toF   = UITheme.textField(20);
        JTextField amtF  = UITheme.textField(20);
        JTextField descF = UITheme.textField(20);
        JLabel fromOwner = UITheme.label("", UITheme.F_SMALL, UITheme.TEXT_MUT);
        JLabel toOwner   = UITheme.label("", UITheme.F_SMALL, UITheme.TEXT_MUT);
        JLabel statusLbl = UITheme.label("", UITheme.F_BODY, UITheme.ACCENT_GREEN);

        fromF.addActionListener(e -> {
            Account a = accountDAO.getByNumber(fromF.getText().trim());
            fromOwner.setText(a != null ? "👤 " + a.getOwnerName() : "Not found");
        });
        toF.addActionListener(e -> {
            Account a = accountDAO.getByNumber(toF.getText().trim());
            toOwner.setText(a != null ? "👤 " + a.getOwnerName() : "Not found");
        });

        int r = 0;
        gbc.gridy = r++; card.add(UITheme.label("🔄  Fund Transfer", UITheme.F_TITLE, UITheme.TEXT_PRI), gbc);
        gbc.gridy = r++; card.add(UITheme.label("From Account No. (Enter to lookup)", UITheme.F_SMALL, UITheme.TEXT_MUT), gbc);
        gbc.gridy = r++; card.add(fromF, gbc);
        gbc.gridy = r++; card.add(fromOwner, gbc);
        gbc.gridy = r++; card.add(UITheme.label("To Account No. (Enter to lookup)", UITheme.F_SMALL, UITheme.TEXT_MUT), gbc);
        gbc.gridy = r++; card.add(toF, gbc);
        gbc.gridy = r++; card.add(toOwner, gbc);
        gbc.gridy = r++; card.add(UITheme.label("Amount (₹)", UITheme.F_SMALL, UITheme.TEXT_MUT), gbc);
        gbc.gridy = r++; card.add(amtF, gbc);
        gbc.gridy = r++; card.add(UITheme.label("Description", UITheme.F_SMALL, UITheme.TEXT_MUT), gbc);
        gbc.gridy = r++; card.add(descF, gbc);

        JButton btn = UITheme.primaryBtn("  TRANSFER  ");
        btn.setPreferredSize(new Dimension(200, 40));
        gbc.gridy = r++; gbc.insets = new Insets(14,7,7,7); card.add(btn, gbc);
        gbc.gridy = r;   gbc.insets = new Insets(4,7,7,7);  card.add(statusLbl, gbc);

        btn.addActionListener(e -> {
            Account from = accountDAO.getByNumber(fromF.getText().trim());
            Account to   = accountDAO.getByNumber(toF.getText().trim());
            if (from == null || to == null) {
                statusLbl.setForeground(UITheme.ACCENT_RED);
                statusLbl.setText("❌ Invalid account number(s)."); return;
            }
            try {
                BigDecimal amt = new BigDecimal(amtF.getText().trim());
                String result  = txDAO.transfer(from.getAccountId(), to.getAccountId(), amt, descF.getText(), me.getUserId());
                if ("SUCCESS".equals(result)) {
                    statusLbl.setForeground(UITheme.ACCENT_GREEN);
                    statusLbl.setText("✅ Transferred " + FMT.format(amt));
                    fromF.setText(""); toF.setText(""); amtF.setText(""); descF.setText("");
                    fromOwner.setText(""); toOwner.setText("");
                } else { statusLbl.setForeground(UITheme.ACCENT_RED); statusLbl.setText("❌ " + result); }
            } catch (NumberFormatException ex) { statusLbl.setForeground(UITheme.ACCENT_RED); statusLbl.setText("❌ Invalid amount."); }
        });

        p.add(card, BorderLayout.NORTH);
        return p;
    }

    // ── Tab: Loan Requests ─────────────────────────────────────────────────
    private JPanel tabLoanRequests() {
        JPanel p = new JPanel(new BorderLayout(0,10));
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(14,14,14,14));

        String[] cols = {"Loan ID","Account","Owner","Type","Principal","Rate","EMI","Applied"};
        DefaultTableModel mdl = new DefaultTableModel(cols,0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        Runnable reload = () -> {
            mdl.setRowCount(0);
            for (Loan l : loanDAO.getPending())
                mdl.addRow(new Object[]{l.getLoanId(), l.getAccountNumber(), l.getOwnerName(),
                    l.getLoanType(), FMT.format(l.getPrincipal()),
                    l.getInterestRate()+"%", FMT.format(l.getEmiAmount()), l.getAppliedDate()});
        };
        reload.run();

        JTable tbl = new JTable(mdl);
        UITheme.styleTable(tbl);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        toolbar.setOpaque(false);
        toolbar.add(UITheme.label("Pending Applications", UITheme.F_HEADING, UITheme.TEXT_PRI));

        JButton approveBtn = UITheme.primaryBtn("✅ Approve");
        approveBtn.addActionListener(e -> {
            int row = tbl.getSelectedRow();
            if (row < 0) { UITheme.err(this,"Select a loan."); return; }
            int id = (int)mdl.getValueAt(row,0);
            if (UITheme.confirm(this,"Approve this loan?")) {
                loanDAO.updateStatus(id,"ACTIVE",me.getUserId());
                UITheme.ok(this,"Loan approved."); reload.run();
            }
        });

        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(approveBtn);
        p.add(toolbar,             BorderLayout.NORTH);
        p.add(UITheme.scroll(tbl), BorderLayout.CENTER);
        return p;
    }

    // ── Tab: All Transactions ──────────────────────────────────────────────
    private JPanel tabAllTransactions() {
        JPanel p = new JPanel(new BorderLayout(0,10));
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(14,14,14,14));

        String[] cols = {"ID","Account","Type","Amount","Bal After","Description","Date"};
        DefaultTableModel mdl = new DefaultTableModel(cols,0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        for (Transaction t : txDAO.getAll())
            mdl.addRow(new Object[]{t.getTransactionId(), t.getAccountId(), t.getTransactionType(),
                FMT.format(t.getAmount()), FMT.format(t.getBalanceAfter()),
                t.getDescription(), t.getTransactionDate()});

        JTable tbl = new JTable(mdl);
        UITheme.styleTable(tbl);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        toolbar.setOpaque(false);

        JTextField accF  = UITheme.textField(16);
        JButton popupBtn = UITheme.ghostBtn("Popup History for Account");
        popupBtn.addActionListener(e -> {
            String no = accF.getText().trim();
            if (no.isEmpty()) { UITheme.err(this,"Enter account number."); return; }
            Account acc = accountDAO.getByNumber(no);
            if (acc == null) { UITheme.err(this,"Account not found."); return; }
            new TransactionHistoryPopup(this, acc);
        });

        toolbar.add(UITheme.label("Account No:", UITheme.F_BODY, UITheme.TEXT_MUT));
        toolbar.add(accF); toolbar.add(popupBtn);

        p.add(toolbar,             BorderLayout.NORTH);
        p.add(UITheme.scroll(tbl), BorderLayout.CENTER);
        return p;
    }
}
