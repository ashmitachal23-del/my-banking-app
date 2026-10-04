package banking.model;

import java.sql.Timestamp;

public class User {
    private int       userId;
    private String    username;
    private String    password;
    private String    role;        // ADMIN | CUSTOMER | STAFF
    private String    fullName;
    private String    email;
    private String    phone;
    private Timestamp createdAt;
    private boolean   active;

    public User() {}

    // Getters
    public int       getUserId()    { return userId;    }
    public String    getUsername()  { return username;  }
    public String    getPassword()  { return password;  }
    public String    getRole()      { return role;      }
    public String    getFullName()  { return fullName;  }
    public String    getEmail()     { return email;     }
    public String    getPhone()     { return phone;     }
    public Timestamp getCreatedAt() { return createdAt; }
    public boolean   isActive()     { return active;    }

    // Setters
    public void setUserId(int v)       { userId    = v; }
    public void setUsername(String v)  { username  = v; }
    public void setPassword(String v)  { password  = v; }
    public void setRole(String v)      { role      = v; }
    public void setFullName(String v)  { fullName  = v; }
    public void setEmail(String v)     { email     = v; }
    public void setPhone(String v)     { phone     = v; }
    public void setCreatedAt(Timestamp v) { createdAt = v; }
    public void setActive(boolean v)   { active    = v; }

    @Override public String toString() { return fullName + " [" + role + "]"; }
}
