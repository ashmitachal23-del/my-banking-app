package banking.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Transaction {
    private int        transactionId;
    private int        accountId;
    private String     transactionType;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String     description;
    private Integer    refAccountId;
    private Timestamp  transactionDate;
    private String     performedBy;

    public Transaction() {}

    public int        getTransactionId()    { return transactionId;    }
    public int        getAccountId()        { return accountId;        }
    public String     getTransactionType()  { return transactionType;  }
    public BigDecimal getAmount()           { return amount;           }
    public BigDecimal getBalanceAfter()     { return balanceAfter;     }
    public String     getDescription()      { return description;      }
    public Integer    getRefAccountId()     { return refAccountId;     }
    public Timestamp  getTransactionDate()  { return transactionDate;  }
    public String     getPerformedBy()      { return performedBy;      }

    public void setTransactionId(int v)         { transactionId   = v; }
    public void setAccountId(int v)             { accountId       = v; }
    public void setTransactionType(String v)    { transactionType = v; }
    public void setAmount(BigDecimal v)         { amount          = v; }
    public void setBalanceAfter(BigDecimal v)   { balanceAfter    = v; }
    public void setDescription(String v)        { description     = v; }
    public void setRefAccountId(Integer v)      { refAccountId    = v; }
    public void setTransactionDate(Timestamp v) { transactionDate = v; }
    public void setPerformedBy(String v)        { performedBy     = v; }
}
