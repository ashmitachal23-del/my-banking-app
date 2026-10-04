package banking.util;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;

/**
 * Central theme for SecureBank — dark navy + gold palette.
 * All Swing components are built here for consistency.
 */
public final class UITheme {

    private UITheme() {}

    // ── Palette ──────────────────────────────────────────────────────────
    public static final Color BG_DARK      = new Color(13,  17,  23);
    public static final Color BG_CARD      = new Color(22,  27,  34);
    public static final Color BG_INPUT     = new Color(30,  37,  46);
    public static final Color BG_ROW_ALT   = new Color(26,  31,  38);
    public static final Color ACCENT_GOLD  = new Color(212, 175,  55);
    public static final Color ACCENT_GREEN = new Color( 63, 185, 128);
    public static final Color ACCENT_RED   = new Color(248,  81,  73);
    public static final Color ACCENT_BLUE  = new Color( 88, 166, 255);
    public static final Color TEXT_PRI     = new Color(230, 237, 243);
    public static final Color TEXT_MUT     = new Color(110, 118, 129);
    public static final Color BORDER       = new Color( 48,  54,  61);

    // ── Fonts ─────────────────────────────────────────────────────────────
    public static final Font F_TITLE   = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font F_HEADING = new Font("Segoe UI", Font.BOLD,  14);
    public static final Font F_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font F_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font F_MONO    = new Font("Consolas",  Font.PLAIN, 13);

    // ── Global look ────────────────────────────────────────────────────────
    public static void applyTheme() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
    }

    // ── Labels ─────────────────────────────────────────────────────────────
    public static JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    // ── Inputs ─────────────────────────────────────────────────────────────
    public static JTextField textField(int cols) {
        JTextField f = new JTextField(cols);
        styleInput(f);
        return f;
    }

    public static JPasswordField passwordField(int cols) {
        JPasswordField f = new JPasswordField(cols);
        styleInput(f);
        return f;
    }

    private static void styleInput(JTextComponent c) {
        c.setBackground(BG_INPUT);
        c.setForeground(TEXT_PRI);
        c.setCaretColor(ACCENT_GOLD);
        c.setFont(F_BODY);
        c.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
    }

    public static JComboBox<String> combo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setBackground(BG_INPUT);
        cb.setForeground(TEXT_PRI);
        cb.setFont(F_BODY);
        return cb;
    }

    // ── Buttons ─────────────────────────────────────────────────────────────
    public static JButton primaryBtn(String text) {
        return makeBtn(text, ACCENT_GOLD, BG_DARK);
    }

    public static JButton dangerBtn(String text) {
        return makeBtn(text, ACCENT_RED, Color.WHITE);
    }

    public static JButton ghostBtn(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(212, 175, 55, 35));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.setColor(ACCENT_GOLD);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(ACCENT_GOLD);
        styleBtn(btn);
        return btn;
    }

    private static JButton makeBtn(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = bg;
                if (getModel().isPressed())  base = base.darker();
                if (getModel().isRollover()) base = base.brighter();
                g2.setColor(base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(fg);
        styleBtn(btn);
        return btn;
    }

    private static void styleBtn(JButton btn) {
        btn.setFont(F_HEADING);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
    }

    // ── Table ───────────────────────────────────────────────────────────────
    public static void styleTable(JTable t) {
        t.setBackground(BG_CARD);
        t.setForeground(TEXT_PRI);
        t.setFont(F_BODY);
        t.setRowHeight(30);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(new Color(88, 166, 255, 55));
        t.setSelectionForeground(TEXT_PRI);

        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
                setBackground(sel ? new Color(88, 166, 255, 55)
                                  : row % 2 == 0 ? BG_CARD : BG_ROW_ALT);
                setForeground(TEXT_PRI);
                setFont(F_BODY);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        });

        JTableHeader h = t.getTableHeader();
        h.setBackground(BG_INPUT);
        h.setForeground(ACCENT_GOLD);
        h.setFont(F_HEADING);
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_GOLD));
        ((DefaultTableCellRenderer) h.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);
    }

    // ── Scroll pane ─────────────────────────────────────────────────────────
    public static JScrollPane scroll(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBackground(BG_CARD);
        sp.getViewport().setBackground(BG_CARD);
        sp.setBorder(BorderFactory.createLineBorder(BORDER));
        return sp;
    }

    // ── Card panel ───────────────────────────────────────────────────────────
    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        return p;
    }

    // ── Dialogs ──────────────────────────────────────────────────────────────
    public static void ok(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void err(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirm(Component parent, String msg) {
        return JOptionPane.showConfirmDialog(parent, msg, "Confirm",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    // ── Sidebar button ───────────────────────────────────────────────────────
    public static JButton sidebarBtn(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                if (getModel().isRollover()) {
                    g.setColor(new Color(212, 175, 55, 22));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
                super.paintComponent(g);
            }
        };
        b.setForeground(TEXT_PRI);
        b.setFont(F_BODY);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setMaximumSize(new Dimension(220, 40));
        b.setPreferredSize(new Dimension(220, 40));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        return b;
    }
}
