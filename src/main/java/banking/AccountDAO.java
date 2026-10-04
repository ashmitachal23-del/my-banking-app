package banking.db;

import banking.model.Account;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {

    private static final String JOIN =
        "SELECT a.*, u.full_name AS owner_name " +
        "FROM accounts a JOIN users u ON a.user_id = u.user_id ";

    /* ── Create ────────────────────────────────────────────────────────── */
    public boolean create(Account a) {
        String sql = "INSERT INTO accounts (account_number,user_id,account_type,balance) VALUES (?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, a.getAccountNumber());
            ps.setInt(2, a.getUserId());
            ps.setString(3, a.getAccountType());
            ps.setBigDecimal(4, a.getBalance());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /** Auto-generates next account number like ACC0000002 */
    public String nextAccountNumber() {
        String sql = "SELECT COUNT(*) FROM accounts";
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return String.format("ACC%07d", rs.getInt(1) + 1);
        } catch (SQLException e) { e.printStackTrace(); }
        return "ACC0000001";
    }

    /* ── Read ──────────────────────────────────────────────────────────── */
    public Account getByNumber(String accNo) {
        return queryOne(JOIN + "WHERE a.account_number=?", accNo);
    }

    public Account getById(int id) {
        String sql = JOIN + "WHERE a.account_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Account> getByUserId(int userId) {
        List<Account> list = new ArrayList<>();
        String sql = JOIN + "WHERE a.user_id=? ORDER BY a.opened_date DESC";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Account> getAll() {
        List<Account> list = new ArrayList<>();
        String sql = JOIN + "ORDER BY a.opened_date DESC";
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /* ── Update ────────────────────────────────────────────────────────── */
    public boolean updateBalance(int accountId, BigDecimal balance, Connection c) throws SQLException {
        PreparedStatement ps = c.prepareStatement("UPDATE accounts SET balance=? WHERE account_id=?");
        ps.setBigDecimal(1, balance);
        ps.setInt(2, accountId);
        return ps.executeUpdate() > 0;
    }

    public boolean updateStatus(int accountId, String status) {
        String sql = "UPDATE accounts SET status=? WHERE account_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, accountId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /* ── Helpers ───────────────────────────────────────────────────────── */
    private Account queryOne(String sql, String param) {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, param);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    private Account map(ResultSet rs) throws SQLException {
        Account a = new Account();
        a.setAccountId(rs.getInt("account_id"));
        a.setAccountNumber(rs.getString("account_number"));
        a.setUserId(rs.getInt("user_id"));
        a.setOwnerName(rs.getString("owner_name"));
        a.setAccountType(rs.getString("account_type"));
        a.setBalance(rs.getBigDecimal("balance"));
        a.setOpenedDate(rs.getTimestamp("opened_date"));
        a.setStatus(rs.getString("status"));
        return a;
    }
}
