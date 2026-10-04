package banking.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Account {
    private int        accountId;
    private String     accountNumber;
    private int        userId;
    private String     ownerName;
    private String     accountType;
    private BigDecimal balance;
    private Timestamp  openedDate;
    private String     status;

    public Account() {}

    public int        getAccountId()     { return accountId;     }
    public String     getAccountNumber() { return accountNumber; }
    public int        getUserId()        { return userId;        }
    public String     getOwnerName()     { return ownerName;     }
    public String     getAccountType()   { return accountType;   }
    public BigDecimal getBalance()       { return balance;       }
    public Timestamp  getOpenedDate()    { return openedDate;    }
    public String     getStatus()        { return status;        }

    public void setAccountId(int v)          { accountId     = v; }
    public void setAccountNumber(String v)   { accountNumber = v; }
    public void setUserId(int v)             { userId        = v; }
    public void setOwnerName(String v)       { ownerName     = v; }
    public void setAccountType(String v)     { accountType   = v; }
    public void setBalance(BigDecimal v)     { balance       = v; }
    public void setOpenedDate(Timestamp v)   { openedDate    = v; }
    public void setStatus(String v)          { status        = v; }

    @Override public String toString() {
        return accountNumber + " | " + accountType + " | ₹" + balance;
    }
}
