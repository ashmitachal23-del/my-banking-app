package banking.db;

import banking.model.Loan;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LoanDAO {

    private static final String JOIN =
        "SELECT l.*, a.account_number, u.full_name AS owner_name, " +
        "adm.full_name AS approved_by_name " +
        "FROM loans l " +
        "JOIN accounts a ON l.account_id = a.account_id " +
        "JOIN users u    ON a.user_id    = u.user_id " +
        "LEFT JOIN users adm ON l.approved_by = adm.user_id ";

    /* ── EMI Calculator ────────────────────────────────────────────────── */
    public static BigDecimal calcEMI(BigDecimal principal, BigDecimal annualRate, int months) {
        double p = principal.doubleValue();
        double r = annualRate.doubleValue() / 100.0 / 12.0;
        if (r == 0) return principal.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
        double emi = p * r * Math.pow(1 + r, months) / (Math.pow(1 + r, months) - 1);
        return BigDecimal.valueOf(emi).setScale(2, RoundingMode.HALF_UP);
    }

    /* ── Apply ─────────────────────────────────────────────────────────── */
    public boolean apply(Loan loan) {
        String sql = "INSERT INTO loans " +
            "(account_id,loan_type,principal,interest_rate,tenure_months,emi_amount,outstanding) " +
            "VALUES (?,?,?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, loan.getAccountId());
            ps.setString(2, loan.getLoanType());
            ps.setBigDecimal(3, loan.getPrincipal());
            ps.setBigDecimal(4, loan.getInterestRate());
            ps.setInt(5, loan.getTenureMonths());
            ps.setBigDecimal(6, loan.getEmiAmount());
            ps.setBigDecimal(7, loan.getPrincipal());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /* ── Approve / Reject ──────────────────────────────────────────────── */
    public boolean updateStatus(int loanId, String status, int approvedBy) {
        String sql = "UPDATE loans SET status=?, approved_by=? WHERE loan_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, approvedBy);
            ps.setInt(3, loanId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /* ── Pay EMI ───────────────────────────────────────────────────────── */
    public boolean payEMI(int loanId, BigDecimal amount) {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            Loan loan = getById(loanId);
            if (loan == null) return false;

            BigDecimal remaining = loan.getOutstanding().subtract(amount).max(BigDecimal.ZERO);
            String newStatus = remaining.compareTo(BigDecimal.ZERO) == 0 ? "CLOSED" : "ACTIVE";

            PreparedStatement ps1 = con.prepareStatement(
                "UPDATE loans SET outstanding=?, status=? WHERE loan_id=?");
            ps1.setBigDecimal(1, remaining);
            ps1.setString(2, newStatus);
            ps1.setInt(3, loanId);
            ps1.executeUpdate();

            PreparedStatement ps2 = con.prepareStatement(
                "INSERT INTO loan_payments (loan_id,amount_paid) VALUES (?,?)");
            ps2.setInt(1, loanId);
            ps2.setBigDecimal(2, amount);
            ps2.executeUpdate();

            con.commit();
            return true;
        } catch (Exception e) {
            try { if (con != null) con.rollback(); } catch (SQLException ignored) {}
            e.printStackTrace(); return false;
        } finally {
            try { if (con != null) con.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    /* ── Queries ───────────────────────────────────────────────────────── */
    public List<Loan> getAll()                 { return query(JOIN + "ORDER BY l.applied_date DESC", null, -1); }
    public List<Loan> getPending()             { return query(JOIN + "WHERE l.status='PENDING' ORDER BY l.applied_date", null, -1); }
    public List<Loan> getByAccount(int accId)  { return query(JOIN + "WHERE l.account_id=? ORDER BY l.applied_date DESC", "account", accId); }

    public Loan getById(int loanId) {
        List<Loan> r = query(JOIN + "WHERE l.loan_id=?", "loan", loanId);
        return r.isEmpty() ? null : r.get(0);
    }

    /* ── Helpers ───────────────────────────────────────────────────────── */
    private List<Loan> query(String sql, String col, int val) {
        List<Loan> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs;
            if (val >= 0) {
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setInt(1, val);
                rs = ps.executeQuery();
            } else {
                rs = con.createStatement().executeQuery(sql);
            }
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private Loan map(ResultSet rs) throws SQLException {
        Loan l = new Loan();
        l.setLoanId(rs.getInt("loan_id"));
        l.setAccountId(rs.getInt("account_id"));
        l.setAccountNumber(rs.getString("account_number"));
        l.setOwnerName(rs.getString("owner_name"));
        l.setLoanType(rs.getString("loan_type"));
        l.setPrincipal(rs.getBigDecimal("principal"));
        l.setInterestRate(rs.getBigDecimal("interest_rate"));
        l.setTenureMonths(rs.getInt("tenure_months"));
        l.setEmiAmount(rs.getBigDecimal("emi_amount"));
        l.setOutstanding(rs.getBigDecimal("outstanding"));
        l.setStatus(rs.getString("status"));
        l.setAppliedDate(rs.getTimestamp("applied_date"));
        l.setApprovedBy(rs.getString("approved_by_name"));
        return l;
    }
}
