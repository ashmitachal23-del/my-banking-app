package banking.ui;

import banking.db.TransactionDAO;
import banking.model.Account;
import banking.model.Transaction;
import banking.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Popup dialog showing the full transaction history for one account.
 * Triggered from any screen via: new TransactionHistoryPopup(parent, account);
 */
public class TransactionHistoryPopup extends JDialog {

    private static final NumberFormat FMT =
        NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public TransactionHistoryPopup(Window parent, Account account) {
        super(parent, "Transaction History — " + account.getAccountNumber(),
              ModalityType.APPLICATION_MODAL);
        setSize(900, 560);
        setLocationRelativeTo(parent);
        buildUI(account);
        setVisible(true);
    }

    private void buildUI(Account acc) {
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(UITheme.BG_DARK);
        root.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        /* ── Account info bar ─────────────────────────────────────────── */
        JPanel infoBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 24, 8));
        infoBar.setBackground(UITheme.BG_CARD);
        infoBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER),
            BorderFactory.createEmptyBorder(2, 10, 2, 10)));

        infoBar.add(chip("Account No",  acc.getAccountNumber()));
        infoBar.add(chip("Account Holder", acc.getOwnerName()));
        infoBar.add(chip("Type",        acc.getAccountType()));
        infoBar.add(chip("Balance",     FMT.format(acc.getBalance())));
        infoBar.add(chip("Status",      acc.getStatus()));

        /* ── Table ────────────────────────────────────────────────────── */
        String[] cols = {"#", "Date & Time", "Type", "Amount (₹)", "Balance After (₹)", "Description", "Performed By"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        List<Transaction> list = new TransactionDAO().getByAccount(acc.getAccountId());
        for (Transaction tx : list) {
            model.addRow(new Object[]{
                tx.getTransactionId(),
                tx.getTransactionDate(),
                formatType(tx.getTransactionType()),
                FMT.format(tx.getAmount()),
                FMT.format(tx.getBalanceAfter()),
                tx.getDescription() != null ? tx.getDescription() : "—",
                tx.getPerformedBy() != null ? tx.getPerformedBy() : "System"
            });
        }

        JTable table = new JTable(model);
        UITheme.styleTable(table);

        // Color-code the Type column
        table.getColumnModel().getColumn(2).setCellRenderer(
            (tbl, val, sel, foc, row, col) -> {
                JLabel lbl = new JLabel(val != null ? val.toString() : "");
                lbl.setFont(UITheme.F_BODY);
                lbl.setOpaque(true);
                String t = model.getValueAt(row, 2).toString();
                lbl.setForeground(typeColor(t));
                lbl.setBackground(sel ? new Color(88,166,255,55)
                                      : row%2==0 ? UITheme.BG_CARD : UITheme.BG_ROW_ALT);
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return lbl;
            });

        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);

        /* ── Footer ───────────────────────────────────────────────────── */
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(UITheme.BG_DARK);
        footer.add(UITheme.label(list.size() + " transactions", UITheme.F_SMALL, UITheme.TEXT_MUT));
        JButton closeBtn = UITheme.primaryBtn("Close");
        closeBtn.addActionListener(e -> dispose());
        footer.add(closeBtn);

        root.add(infoBar,             BorderLayout.NORTH);
        root.add(UITheme.scroll(table), BorderLayout.CENTER);
        root.add(footer,              BorderLayout.SOUTH);
        setContentPane(root);
    }

    /* ── Helpers ──────────────────────────────────────────────────────── */
    private JPanel chip(String label, String value) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(UITheme.BG_CARD);
        p.add(UITheme.label(label, UITheme.F_SMALL, UITheme.TEXT_MUT));
        p.add(UITheme.label(value, UITheme.F_HEADING, UITheme.TEXT_PRI));
        return p;
    }

    private String formatType(String t) {
        return switch (t) {
            case "DEPOSIT"      -> "✅  Deposit";
            case "WITHDRAWAL"   -> "💸  Withdrawal";
            case "TRANSFER_IN"  -> "⬇   Transfer In";
            case "TRANSFER_OUT" -> "⬆   Transfer Out";
            default -> t;
        };
    }

    private Color typeColor(String t) {
        if (t.contains("Deposit") || t.contains("In"))    return UITheme.ACCENT_GREEN;
        if (t.contains("Withdraw") || t.contains("Out"))  return UITheme.ACCENT_RED;
        return UITheme.TEXT_PRI;
    }
}
