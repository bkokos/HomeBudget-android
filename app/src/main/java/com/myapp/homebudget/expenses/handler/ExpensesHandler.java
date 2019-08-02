package com.myapp.homebudget.expenses.handler;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import static com.myapp.homebudget.expenses.util.ExpensesConstans.EXP_MSG_CONFIRM;
import static com.myapp.homebudget.expenses.util.ExpensesConstans.EXP_FIXED_MSG_CONFIRM;
import static com.myapp.homebudget.expenses.util.ExpensesConstans.EXP_MSG_ERROR;


import com.myapp.homebudget.expenses.data.Expense;

public class ExpensesHandler extends Handler {
    private Context context;

    public ExpensesHandler(Context context) {
        super();
        this.context = context;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case EXP_MSG_CONFIRM:
                Expense expense = (Expense) msg.obj;
                Toast.makeText(context, "Dodano wydatek - " + expense.getValue(), Toast.LENGTH_SHORT).show();
                break;
            case EXP_FIXED_MSG_CONFIRM:
                Expense fixedExpense = (Expense) msg.obj;
                Toast.makeText(context, "Dodano stały wydatek - " + fixedExpense.getValue(), Toast.LENGTH_SHORT).show();
                break;
            case EXP_MSG_ERROR:
                Toast.makeText(context, "Nie udało się dodać wydatku", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
