package com.myapp.homebudget.auth;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.myapp.homebudget.expenses.data.Expense;

public class AuthHandler extends Handler {

    private Context context;

    public AuthHandler(Context context){
        super();
        this.context = context;
    }

    @Override
    public void handleMessage(Message msg) {

    }
}
