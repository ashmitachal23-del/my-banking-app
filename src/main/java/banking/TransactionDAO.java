package banking.db;

import banking.model.Account;
import banking.model.Transaction;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    private final AccountDAO accountDAO = new AccountDAO();

    /* ── Deposit ───────────────────────────────────────────────────────── */
    public boolean deposit(int accountId, BigDecimal amount, String desc, int performedBy) {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            Account acc = accountDAO.getById(accountId);
            if (acc == null || !"ACTIVE".equals(acc.getStatus()))
                throw new Exception("Account not found or not active.");

            BigDecimal newBal = acc.getBalance().add(amount);
            accountDAO.updateBalance(accountId, newBal, con);
            insertTx(con, accountId, "DEPOSIT", amount, newBal, desc, null, performedBy);

            con.commit();
            return true;
        } catch (Exception e) {
            rollback(con); e.printStackTrace(); return false;
        } finally { restoreAutoCommit(con); }
    }

    /* ── Withdraw ──────────────────────────────────────────────────────── */
    public String withdraw(int accountId, BigDecimal amount, String desc, int performedBy) {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            Account acc = accountDAO.getById(accountId);
            if (acc == null || !"ACTIVE".equals(acc.getStatus()))
                return "Account not found or inactive.";
            if (acc.getBalance().compareTo(amount) < 0)
                return "Insufficient balance! Available: ₹" + acc.getBalance();

            BigDecimal newBal = acc.getBalance().subtract(amount);
            accountDAO.updateBalance(accountId, newBal, con);
            insertTx(con, accountId, "WITHDRAWAL", amount, newBal, desc, null, performedBy);

            con.commit();
            return "SUCCESS";
        } catch (Exception e) {
            rollback(con); e.printStackTrace(); return "Error: " + e.getMessage();
        } finally { restoreAutoCommit(con); }
    }

    /* ── Transfer ──────────────────────────────────────────────────────── */
    public String transfer(int fromId, int toId, BigDecimal amount, String desc, int performedBy) {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            Account from = accountDAO.getById(fromId);
            Account to   = accountDAO.getById(toId);

            if (from == null || !"ACTIVE".equals(from.getStatus())) return "Source account invalid/inactive.";
            if (to   == null || !"ACTIVE".equals(to.getStatus()))   return "Destination account invalid/inactive.";
            if (from.getBalance().compareTo(amount) < 0)            return "Insufficient balance.";

            BigDecimal fromNew = from.getBalance().subtract(amount);
            BigDecimal toNew   = to.getBalance().add(amount);

            accountDAO.updateBalance(fromId, fromNew, con);
            accountDAO.updateBalance(toId,   toNew,   con);

            String note = desc.isBlank() ? "Transfer" : desc;
            insertTx(con, fromId, "TRANSFER_OUT", amount, fromNew, note + " → " + to.getAccountNumber(),  toId,   performedBy);
            insertTx(con, toId,   "TRANSFER_IN",  amount, toNew,   note + " ← " + from.getAccountNumber(), fromId, performedBy);

            con.commit();
            return "SUCCESS";
        } catch (Exception e) {
            rollback(con); e.printStackTrace(); return "Error: " + e.getMessage();
        } finally { restoreAutoCommit(con); }
    }

    /* ── Queries ───────────────────────────────────────────────────────── */
    public List<Transaction> getByAccount(int accountId) {
        return query(
            "SELECT t.*, u.full_name AS performed_by_name FROM transactions t " +
            "LEFT JOIN users u ON t.performed_by=u.user_id " +
            "WHERE t.account_id=? ORDER BY t.transaction_date DESC", accountId);
    }

    public List<Transaction> getMini(int accountId, int limit) {
        List<Transaction> list = new ArrayList<>();
        String sql =
            "SELECT t.*, u.full_name AS performed_by_name FROM transactions t " +
            "LEFT JOIN users u ON t.performed_by=u.user_id " +
            "WHERE t.account_id=? ORDER BY t.transaction_date DESC LIMIT ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapTx(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Transaction> getAll() {
        List<Transaction> list = new ArrayList<>();
        String sql =
            "SELECT t.*, u.full_name AS performed_by_name FROM transactions t " +
            "LEFT JOIN users u ON t.performed_by=u.user_id " +
            "ORDER BY t.transaction_date DESC LIMIT 1000";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapTx(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /* ── Internal helpers ──────────────────────────────────────────────── */
    private void insertTx(Connection con, int accId, String type, BigDecimal amt,
                          BigDecimal balAfter, String desc, Integer refId, int by)
            throws SQLException {
        String sql = "INSERT INTO transactions " +
            "(account_id,transaction_type,amount,balance_after,description,ref_account_id,performed_by) " +
            "VALUES (?,?,?,?,?,?,?)";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, accId);
        ps.setString(2, type);
        ps.setBigDecimal(3, amt);
        ps.setBigDecimal(4, balAfter);
        ps.setString(5, desc);
        if (refId != null) ps.setInt(6, refId); else ps.setNull(6, Types.INTEGER);
        ps.setInt(7, by);
        ps.executeUpdate();
    }

    private List<Transaction> query(String sql, int param) {
        List<Transaction> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, param);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapTx(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private void rollback(Connection con) {
        try { if (con != null) con.rollback(); } catch (SQLException ignored) {}
    }

    private void restoreAutoCommit(Connection con) {
        try { if (con != null) con.setAutoCommit(true); } catch (SQLException ignored) {}
    }

    private Transaction mapTx(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setTransactionId(rs.getInt("transaction_id"));
        t.setAccountId(rs.getInt("account_id"));
        t.setTransactionType(rs.getString("transaction_type"));
        t.setAmount(rs.getBigDecimal("amount"));
        t.setBalanceAfter(rs.getBigDecimal("balance_after"));
        t.setDescription(rs.getString("description"));
        t.setTransactionDate(rs.getTimestamp("transaction_date"));
        t.setPerformedBy(rs.getString("performed_by_name"));
        return t;
    }
}
