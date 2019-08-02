package com.myapp.homebudget.util;

public enum DialogType {
    Expense("Nowy wydatek"),
    FixedExpense("Nowy stały wydatek"),
    Income("Dodaj przychód"),
    Savings("Zaoszczędź");

    DialogType(String title){
        dialogTitle = title;
    }

    private  String dialogTitle;

    public String getDialogTitle() {
        return dialogTitle;
    }

}
