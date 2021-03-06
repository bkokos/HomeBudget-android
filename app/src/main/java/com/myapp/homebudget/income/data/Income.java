package com.myapp.homebudget.income.data;

import java.util.Date;

public class Income {
    private Long id;
    private String value;
    private String desc;
    private String userName;
    private String walletId;
    private Date addDate;


    public Income() {

    }

    public Income(String value, String desc, String userName, String walletId) {
        this.value = value;
        this.desc = desc;
        this.userName = userName;
        this.walletId = walletId;
    }

    public Income(Long id, String value, String desc, String userName, String walletId, Date addDate) {
        this.id = id;
        this.value = value;
        this.desc = desc;
        this.userName = userName;
        this.walletId = walletId;
        this.addDate = addDate;
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

    public Date getAddDate() {
        return addDate;
    }
}
