package com.myapp.homebudget.expenses.data;

import java.util.Date;

public class Expense {
    private Long id;
    private String value;
    private String desc;
    private String userName;
    private String walletId;
    private Date addDate;


    public Expense() {
    }

    public Expense(String value, String desc, String userName, String walletId, Long id) {
        this.id = id;
        this.value = value;
        this.desc = desc;
        this.userName = userName;
        this.walletId = walletId;
    }

    public Long getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    public String getUserName() {
        return userName;
    }

    public String getWalletId() {
        return walletId;
    }

    public Date getAddDate(){
        return addDate;
    }
}
