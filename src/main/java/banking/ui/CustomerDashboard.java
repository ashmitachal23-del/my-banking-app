package banking.ui.customer;

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

public class CustomerDashboard extends JFrame {

    private final User           me;
    private final AccountDAO     accountDAO = new AccountDAO();
    private final TransactionDAO txDAO      = new TransactionDAO();
    private final LoanDAO        loanDAO    = new LoanDAO();
    private static final NumberFormat FMT   = NumberFormat.getCurrencyInstance(new Locale("en","IN"));

    private JTabbedPane tabs;

    public CustomerDashboard(User user) {
        this.me = user;
        setTitle("SecureBank — Customer");
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

    // ── Sidebar ────────────────────────────────────────────────────────────
    private JPanel sidebar() {
        JPanel p = new JPanel();
        p.setPreferredSize(new Dimension(200, 0));
        p.setBackground(UITheme.BG_CARD);
        p.setBorder(BorderFactory.createMatteBorder(0,0,0,1,UITheme.BORDER));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(Box.createVerticalStrut(20));
        p.add(UITheme.label("  🏦  SecureBank",   new Font("Segoe UI",Font.BOLD,15), UITheme.ACCENT_GOLD));
        p.add(UITheme.label("  Customer Portal",   UITheme.F_SMALL, UITheme.TEXT_MUT));
        p.add(Box.createVerticalStrut(16));

        String[] nav = {"🏠  My Accounts","💳  Transactions","🔄  Transfer","💰  Loans","📊  Mini Statement"};
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
        hdr.add(UITheme.label("Welcome, " + me.getFullName(), UITheme.F_TITLE, UITheme.TEXT_PRI));

        tabs = new JTabbedPane();
        tabs.setBackground(UITheme.BG_DARK);
        tabs.setForeground(UITheme.TEXT_PRI);
        tabs.setFont(UITheme.F_BODY);
        tabs.addTab("My Accounts",    tabMyAccounts());
        tabs.addTab("Transactions",   tabTransactions());
        tabs.addTab("Transfer",       tabTransfer());
        tabs.addTab("Loans",          tabLoans());
        tabs.addTab("Mini Statement", tabMiniStatement());

        p.add(hdr,  BorderLayout.NORTH);
        p.add(tabs, BorderLayout.CENTER);
        return p;
    }

    // ── Tab: My Accounts ───────────────────────────────────────────────────
    private JPanel tabMyAccounts() {
        JPanel p = new JPanel(new BorderLayout(0,10));
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(14,14,14,14));

        String[] cols = {"Account No","Type","Balance","Status","Opened"};
        DefaultTableModel mdl = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        List<Account> accounts = accountDAO.getByUserId(me.getUserId());

        Runnable reload = () -> {
            mdl.setRowCount(0);
            List<Account> list = accountDAO.getByUserId(me.getUserId());
            for (Account a : list)
                mdl.addRow(new Object[]{a.getAccountNumber(), a.getAccountType(),
                    FMT.format(a.getBalance()), a.getStatus(), a.getOpenedDate()});
        };
        reload.run();

        JTable tbl = new JTable(mdl);
        UITheme.styleTable(tbl);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);

        JButton histBtn     = UITheme.primaryBtn("📋 View History");
        JButton depositBtn  = UITheme.primaryBtn("+ Deposit");
        JButton withdrawBtn = UITheme.ghostBtn("- Withdraw");

        histBtn.addActionListener(e -> {
            int row = tbl.getSelectedRow();
            if (row < 0) { UITheme.err(this,"Select an account."); return; }
            String no  = (String) mdl.getValueAt(row, 0);
            Account acc = accountDAO.getByNumber(no);
            new TransactionHistoryPopup(this, acc);
        });

        depositBtn.addActionListener(e -> {
            List<Account> list = accountDAO.getByUserId(me.getUserId());
            if (list.isEmpty()) { UITheme.err(this,"No accounts."); return; }
            showDepositDialog(list, reload);
        });

        withdrawBtn.addActionListener(e -> {
            List<Account> list = accountDAO.getByUserId(me.getUserId());
            if (list.isEmpty()) { UITheme.err(this,"No accounts."); return; }
            showWithdrawDialog(list, reload);
        });

        toolbar.add(histBtn); toolbar.add(depositBtn); toolbar.add(withdrawBtn);
        p.add(toolbar,             BorderLayout.NORTH);
        p.add(UITheme.scroll(tbl), BorderLayout.CENTER);
        return p;
    }

    private void showDepositDialog(List<Account> accounts, Runnable reload) {
        JDialog dlg = makeDialog("Deposit Money", 360, 230);
        JPanel form = new JPanel(new GridLayout(3,2,10,10));
        form.setBackground(UITheme.BG_CARD);
        form.setBorder(BorderFactory.createEmptyBorder(20,20,10,20));

        String[] nos = accounts.stream().map(Account::getAccountNumber).toArray(String[]::new);
        JComboBox<String> accCb = UITheme.combo(nos);
        JTextField amtF  = UITheme.textField(12);
        JTextField descF = UITheme.textField(12);

        form.add(UITheme.label("Account",     UITheme.F_BODY, UITheme.TEXT_PRI)); form.add(accCb);
        form.add(UITheme.label("Amount (₹)",  UITheme.F_BODY, UITheme.TEXT_PRI)); form.add(amtF);
        form.add(UITheme.label("Description", UITheme.F_BODY, UITheme.TEXT_PRI)); form.add(descF);

        JButton ok = UITheme.primaryBtn("Deposit");
        ok.addActionListener(e -> {
            try {
                Account acc = accounts.get(accCb.getSelectedIndex());
                BigDecimal amt = new BigDecimal(amtF.getText().trim());
                if (txDAO.deposit(acc.getAccountId(), amt, descF.getText(), me.getUserId())) {
                    UITheme.ok(dlg, "Deposited " + FMT.format(amt) + " successfully!");
                    reload.run(); dlg.dispose();
                } else UITheme.err(dlg, "Deposit failed.");
            } catch (NumberFormatException ex) { UITheme.err(dlg,"Invalid amount."); }
        });

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(footer(ok), BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void showWithdrawDialog(List<Account> accounts, Runnable reload) {
        JDialog dlg = makeDialog("Withdraw Money", 360, 230);
        JPanel form = new JPanel(new GridLayout(3,2,10,10));
        form.setBackground(UITheme.BG_CARD);
        form.setBorder(BorderFactory.createEmptyBorder(20,20,10,20));

        String[] nos = accounts.stream().map(Account::getAccountNumber).toArray(String[]::new);
        JComboBox<String> accCb = UITheme.combo(nos);
        JTextField amtF  = UITheme.textField(12);
        JTextField descF = UITheme.textField(12);

        form.add(UITheme.label("Account",     UITheme.F_BODY, UITheme.TEXT_PRI)); form.add(accCb);
        form.add(UITheme.label("Amount (₹)",  UITheme.F_BODY, UITheme.TEXT_PRI)); form.add(amtF);
        form.add(UITheme.label("Description", UITheme.F_BODY, UITheme.TEXT_PRI)); form.add(descF);

        JButton ok = UITheme.dangerBtn("Withdraw");
        ok.addActionListener(e -> {
            try {
                Account acc = accounts.get(accCb.getSelectedIndex());
                BigDecimal amt = new BigDecimal(amtF.getText().trim());
                String result = txDAO.withdraw(acc.getAccountId(), amt, descF.getText(), me.getUserId());
                if ("SUCCESS".equals(result)) {
                    UITheme.ok(dlg, "Withdrawn " + FMT.format(amt));
                    reload.run(); dlg.dispose();
                } else UITheme.err(dlg, result);
            } catch (NumberFormatException ex) { UITheme.err(dlg,"Invalid amount."); }
        });

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(footer(ok), BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ── Tab: Transactions ──────────────────────────────────────────────────
    private JPanel tabTransactions() {
        JPanel p = new JPanel(new BorderLayout(0,10));
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(14,14,14,14));

        String[] cols = {"Date","Type","Amount","Balance After","Description"};
        DefaultTableModel mdl = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        List<Account> accounts = accountDAO.getByUserId(me.getUserId());
        for (Account acc : accounts)
            for (Transaction t : txDAO.getByAccount(acc.getAccountId()))
                mdl.addRow(new Object[]{t.getTransactionDate(), t.getTransactionType(),
                    FMT.format(t.getAmount()), FMT.format(t.getBalanceAfter()), t.getDescription()});

        JTable tbl = new JTable(mdl);
        UITheme.styleTable(tbl);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        toolbar.setOpaque(false);

        JButton popupBtn = UITheme.ghostBtn("📋 Popup History");
        popupBtn.addActionListener(e -> {
            if (accounts.isEmpty()) { UITheme.err(this,"No accounts."); return; }
            String[] opts = accounts.stream()
                .map(a -> a.getAccountNumber() + " | " + a.getAccountType()).toArray(String[]::new);
            String chosen = (String) JOptionPane.showInputDialog(this,
                "Select Account:", "Transaction History",
                JOptionPane.PLAIN_MESSAGE, null, opts, opts[0]);
            if (chosen != null) {
                int idx = java.util.Arrays.asList(opts).indexOf(chosen);
                new TransactionHistoryPopup(this, accounts.get(idx));
            }
        });
        toolbar.add(popupBtn);

        p.add(toolbar,             BorderLayout.NORTH);
        p.add(UITheme.scroll(tbl), BorderLayout.CENTER);
        return p;
    }

    // ── Tab: Transfer ──────────────────────────────────────────────────────
    private JPanel tabTransfer() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(30,80,30,80));

        JPanel card = UITheme.card();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(7,7,7,7);

        List<Account> myAccounts = accountDAO.getByUserId(me.getUserId());
        String[] myNos = myAccounts.stream().map(Account::getAccountNumber).toArray(String[]::new);

        JComboBox<String> fromCb = UITheme.combo(myNos.length > 0 ? myNos : new String[]{"No accounts"});
        JTextField toF   = UITheme.textField(20);
        JTextField amtF  = UITheme.textField(20);
        JTextField descF = UITheme.textField(20);
        JLabel statusLbl = UITheme.label("", UITheme.F_BODY, UITheme.ACCENT_GREEN);

        int r = 0;
        gbc.gridy = r++; card.add(UITheme.label("🔄  Fund Transfer", UITheme.F_TITLE, UITheme.TEXT_PRI), gbc);
        gbc.gridy = r++; card.add(UITheme.label("From Account", UITheme.F_SMALL, UITheme.TEXT_MUT), gbc);
        gbc.gridy = r++; card.add(fromCb, gbc);
        gbc.gridy = r++; card.add(UITheme.label("To Account Number", UITheme.F_SMALL, UITheme.TEXT_MUT), gbc);
        gbc.gridy = r++; card.add(toF, gbc);
        gbc.gridy = r++; card.add(UITheme.label("Amount (₹)", UITheme.F_SMALL, UITheme.TEXT_MUT), gbc);
        gbc.gridy = r++; card.add(amtF, gbc);
        gbc.gridy = r++; card.add(UITheme.label("Description (optional)", UITheme.F_SMALL, UITheme.TEXT_MUT), gbc);
        gbc.gridy = r++; card.add(descF, gbc);

        JButton transferBtn = UITheme.primaryBtn("  TRANSFER  ");
        transferBtn.setPreferredSize(new Dimension(200, 40));
        gbc.gridy = r++;
        gbc.insets = new Insets(14,7,7,7);
        card.add(transferBtn, gbc);
        gbc.gridy = r;
        gbc.insets = new Insets(4,7,7,7);
        card.add(statusLbl, gbc);

        transferBtn.addActionListener(e -> {
            if (myAccounts.isEmpty()) { UITheme.err(this,"No accounts available."); return; }
            Account from = myAccounts.get(fromCb.getSelectedIndex());
            Account to   = accountDAO.getByNumber(toF.getText().trim());
            if (to == null) { statusLbl.setForeground(UITheme.ACCENT_RED); statusLbl.setText("❌ Destination account not found."); return; }
            try {
                BigDecimal amt = new BigDecimal(amtF.getText().trim());
                String result = txDAO.transfer(from.getAccountId(), to.getAccountId(), amt, descF.getText(), me.getUserId());
                if ("SUCCESS".equals(result)) {
                    statusLbl.setForeground(UITheme.ACCENT_GREEN);
                    statusLbl.setText("✅ Transferred " + FMT.format(amt) + " successfully!");
                    toF.setText(""); amtF.setText(""); descF.setText("");
                } else { statusLbl.setForeground(UITheme.ACCENT_RED); statusLbl.setText("❌ " + result); }
            } catch (NumberFormatException ex) { statusLbl.setForeground(UITheme.ACCENT_RED); statusLbl.setText("❌ Invalid amount."); }
        });

        p.add(card, BorderLayout.NORTH);
        return p;
    }

    // ── Tab: Loans ─────────────────────────────────────────────────────────
    private JPanel tabLoans() {
        JPanel p = new JPanel(new BorderLayout(0,10));
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(14,14,14,14));

        String[] cols = {"Loan ID","Type","Principal","Rate","EMI","Outstanding","Status"};
        DefaultTableModel mdl = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        List<Account> accounts = accountDAO.getByUserId(me.getUserId());

        Runnable reload = () -> {
            mdl.setRowCount(0);
            for (Account acc : accounts)
                for (Loan l : loanDAO.getByAccount(acc.getAccountId()))
                    mdl.addRow(new Object[]{l.getLoanId(), l.getLoanType(),
                        FMT.format(l.getPrincipal()), l.getInterestRate() + "%",
                        FMT.format(l.getEmiAmount()), FMT.format(l.getOutstanding()), l.getStatus()});
        };
        reload.run();

        JTable tbl = new JTable(mdl);
        UITheme.styleTable(tbl);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        toolbar.setOpaque(false);

        JButton applyBtn = UITheme.primaryBtn("+ Apply for Loan");
        JButton payBtn   = UITheme.ghostBtn("Pay EMI");

        applyBtn.addActionListener(e -> {
            if (accounts.isEmpty()) { UITheme.err(this,"You need a bank account to apply."); return; }
            showApplyLoan(accounts, reload);
        });

        payBtn.addActionListener(e -> {
            int row = tbl.getSelectedRow();
            if (row < 0) { UITheme.err(this,"Select a loan."); return; }
            int lid = (int) mdl.getValueAt(row,0);
            Loan loan = loanDAO.getById(lid);
            if (!"ACTIVE".equals(loan.getStatus())) { UITheme.err(this,"Loan is not active."); return; }
            if (loanDAO.payEMI(lid, loan.getEmiAmount())) {
                UITheme.ok(this, "EMI of " + FMT.format(loan.getEmiAmount()) + " paid.");
                reload.run();
            }
        });

        toolbar.add(applyBtn); toolbar.add(payBtn);
        p.add(toolbar,             BorderLayout.NORTH);
        p.add(UITheme.scroll(tbl), BorderLayout.CENTER);
        return p;
    }

    private void showApplyLoan(List<Account> accounts, Runnable reload) {
        JDialog dlg = makeDialog("Apply for Loan", 420, 340);
        JPanel form = new JPanel(new GridLayout(6,2,10,10));
        form.setBackground(UITheme.BG_CARD);
        form.setBorder(BorderFactory.createEmptyBorder(20,20,10,20));

        String[] nos = accounts.stream().map(Account::getAccountNumber).toArray(String[]::new);
        JComboBox<String> accCb   = UITheme.combo(nos);
        JComboBox<String> typeCb  = UITheme.combo(new String[]{"HOME","CAR","PERSONAL","EDUCATION","BUSINESS"});
        JTextField amtF    = UITheme.textField(12);
        JTextField rateF   = UITheme.textField(12); rateF.setText("8.5");
        JTextField tenureF = UITheme.textField(12); tenureF.setText("12");
        JLabel emiLbl = UITheme.label("—", UITheme.F_HEADING, UITheme.ACCENT_GOLD);

        Runnable calcEMI = () -> {
            try {
                BigDecimal emi = LoanDAO.calcEMI(
                    new BigDecimal(amtF.getText().trim()),
                    new BigDecimal(rateF.getText().trim()),
                    Integer.parseInt(tenureF.getText().trim()));
                emiLbl.setText("EMI: " + FMT.format(emi));
            } catch (Exception ignored) { emiLbl.setText("—"); }
        };

        amtF.addActionListener(e -> calcEMI.run());
        rateF.addActionListener(e -> calcEMI.run());
        tenureF.addActionListener(e -> calcEMI.run());

        form.add(UITheme.label("Account",        UITheme.F_BODY, UITheme.TEXT_PRI)); form.add(accCb);
        form.add(UITheme.label("Loan Type",      UITheme.F_BODY, UITheme.TEXT_PRI)); form.add(typeCb);
        form.add(UITheme.label("Amount (₹)",     UITheme.F_BODY, UITheme.TEXT_PRI)); form.add(amtF);
        form.add(UITheme.label("Interest Rate %",UITheme.F_BODY, UITheme.TEXT_PRI)); form.add(rateF);
        form.add(UITheme.label("Tenure (months)",UITheme.F_BODY, UITheme.TEXT_PRI)); form.add(tenureF);
        form.add(UITheme.label("Estimated EMI",  UITheme.F_BODY, UITheme.TEXT_MUT)); form.add(emiLbl);

        JButton calcBtn  = UITheme.ghostBtn("Calculate EMI");
        JButton applyBtn = UITheme.primaryBtn("Apply");
        calcBtn.addActionListener(e -> calcEMI.run());
        applyBtn.addActionListener(e -> {
            try {
                Account acc = accounts.get(accCb.getSelectedIndex());
                BigDecimal principal = new BigDecimal(amtF.getText().trim());
                BigDecimal rate      = new BigDecimal(rateF.getText().trim());
                int tenure           = Integer.parseInt(tenureF.getText().trim());
                BigDecimal emi       = LoanDAO.calcEMI(principal, rate, tenure);

                Loan loan = new Loan();
                loan.setAccountId(acc.getAccountId());
                loan.setLoanType((String)typeCb.getSelectedItem());
                loan.setPrincipal(principal);
                loan.setInterestRate(rate);
                loan.setTenureMonths(tenure);
                loan.setEmiAmount(emi);

                if (loanDAO.apply(loan)) {
                    UITheme.ok(dlg,"Loan application submitted! Awaiting admin approval.");
                    reload.run(); dlg.dispose();
                } else UITheme.err(dlg,"Failed to submit application.");
            } catch (NumberFormatException ex) { UITheme.err(dlg,"Invalid input values."); }
        });

        JPanel fp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        fp.setBackground(UITheme.BG_CARD);
        fp.add(calcBtn); fp.add(applyBtn);

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(fp,   BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ── Tab: Mini Statement ────────────────────────────────────────────────
    private JPanel tabMiniStatement() {
        JPanel p = new JPanel(new BorderLayout(0,10));
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(14,14,14,14));

        List<Account> accounts = accountDAO.getByUserId(me.getUserId());
        String[] opts  = accounts.stream().map(a -> a.getAccountNumber()+" | "+a.getAccountType()).toArray(String[]::new);
        JComboBox<String> accCb   = UITheme.combo(opts.length > 0 ? opts : new String[]{"No accounts"});
        JComboBox<String> limitCb = UITheme.combo(new String[]{"5","10","20","50"});
        JButton viewBtn = UITheme.primaryBtn("View");
        JButton popupBtn = UITheme.ghostBtn("Open Popup");

        JTextArea area = new JTextArea();
        area.setBackground(UITheme.BG_CARD);
        area.setForeground(UITheme.TEXT_PRI);
        area.setFont(UITheme.F_MONO);
        area.setEditable(false);
        area.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        viewBtn.addActionListener(e -> {
            if (accounts.isEmpty()) { area.setText("No accounts found."); return; }
            Account acc = accounts.get(accCb.getSelectedIndex());
            int limit   = Integer.parseInt((String)limitCb.getSelectedItem());
            List<Transaction> list = txDAO.getMini(acc.getAccountId(), limit);

            StringBuilder sb = new StringBuilder();
            sb.append("═══════════════════════════════════════════════════════════\n");
            sb.append("  MINI STATEMENT  —  SecureBank\n");
            sb.append("═══════════════════════════════════════════════════════════\n");
            sb.append(String.format("  Account   : %s%n", acc.getAccountNumber()));
            sb.append(String.format("  Type      : %s%n", acc.getAccountType()));
            sb.append(String.format("  Balance   : %s%n", FMT.format(acc.getBalance())));
            sb.append(String.format("  Status    : %s%n", acc.getStatus()));
            sb.append("───────────────────────────────────────────────────────────\n");
            sb.append(String.format("  %-22s  %-16s  %15s%n","Date & Time","Type","Amount"));
            sb.append("───────────────────────────────────────────────────────────\n");
            for (Transaction t : list) {
                String type = t.getTransactionType();
                String sign = (type.contains("DEPOSIT") || type.contains("IN")) ? "+" : "-";
                sb.append(String.format("  %-22s  %-16s  %s%15s%n",
                    t.getTransactionDate().toString().substring(0,19),
                    type, sign, FMT.format(t.getAmount())));
            }
            sb.append("═══════════════════════════════════════════════════════════\n");
            area.setText(sb.toString());
            area.setCaretPosition(0);
        });

        popupBtn.addActionListener(e -> {
            if (accounts.isEmpty()) return;
            Account acc = accounts.get(accCb.getSelectedIndex());
            new TransactionHistoryPopup(this, acc);
        });

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        toolbar.add(UITheme.label("Account:", UITheme.F_BODY, UITheme.TEXT_PRI));
        toolbar.add(accCb);
        toolbar.add(UITheme.label("Last:", UITheme.F_BODY, UITheme.TEXT_PRI));
        toolbar.add(limitCb);
        toolbar.add(viewBtn);
        toolbar.add(popupBtn);

        p.add(toolbar,               BorderLayout.NORTH);
        p.add(UITheme.scroll(area),  BorderLayout.CENTER);
        return p;
    }

    // ── Helpers ────────────────────────────────────────────────────────────
    private JDialog makeDialog(String title, int w, int h) {
        JDialog dlg = new JDialog(this, title, true);
        dlg.setSize(w, h);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(UITheme.BG_CARD);
        return dlg;
    }

    private JPanel footer(JButton btn) {
        JPanel fp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        fp.setBackground(UITheme.BG_CARD);
        fp.add(btn);
        return fp;
    }
}
