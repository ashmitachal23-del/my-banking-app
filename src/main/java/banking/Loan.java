package banking.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Loan {
    private int        loanId;
    private int        accountId;
    private String     accountNumber;
    private String     ownerName;
    private String     loanType;
    private BigDecimal principal;
    private BigDecimal interestRate;
    private int        tenureMonths;
    private BigDecimal emiAmount;
    private BigDecimal outstanding;
    private String     status;
    private Timestamp  appliedDate;
    private String     approvedBy;

    public Loan() {}

    public int        getLoanId()        { return loanId;        }
    public int        getAccountId()     { return accountId;     }
    public String     getAccountNumber() { return accountNumber; }
    public String     getOwnerName()     { return ownerName;     }
    public String     getLoanType()      { return loanType;      }
    public BigDecimal getPrincipal()     { return principal;     }
    public BigDecimal getInterestRate()  { return interestRate;  }
    public int        getTenureMonths()  { return tenureMonths;  }
    public BigDecimal getEmiAmount()     { return emiAmount;     }
    public BigDecimal getOutstanding()   { return outstanding;   }
    public String     getStatus()        { return status;        }
    public Timestamp  getAppliedDate()   { return appliedDate;   }
    public String     getApprovedBy()    { return approvedBy;    }

    public void setLoanId(int v)             { loanId        = v; }
    public void setAccountId(int v)          { accountId     = v; }
    public void setAccountNumber(String v)   { accountNumber = v; }
    public void setOwnerName(String v)       { ownerName     = v; }
    public void setLoanType(String v)        { loanType      = v; }
    public void setPrincipal(BigDecimal v)   { principal     = v; }
    public void setInterestRate(BigDecimal v){ interestRate  = v; }
    public void setTenureMonths(int v)       { tenureMonths  = v; }
    public void setEmiAmount(BigDecimal v)   { emiAmount     = v; }
    public void setOutstanding(BigDecimal v) { outstanding   = v; }
    public void setStatus(String v)          { status        = v; }
    public void setAppliedDate(Timestamp v)  { appliedDate   = v; }
    public void setApprovedBy(String v)      { approvedBy    = v; }
}
