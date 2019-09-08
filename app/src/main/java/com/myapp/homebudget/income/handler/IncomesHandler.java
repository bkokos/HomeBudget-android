package com.myapp.homebudget.income.handler;


import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.myapp.homebudget.income.data.Income;

import static com.myapp.homebudget.income.util.IncomesConstans.CONFIRM;
import static com.myapp.homebudget.income.util.IncomesConstans.ERROR;

public class IncomesHandler extends Handler {

    private Context context;

    public IncomesHandler(Context context) {
        super();
        this.context = context;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case CONFIRM:
                Income income = (Income) msg.obj;
                Toast.makeText(context, "Dodano przychód - " + income.getValue(), Toast.LENGTH_SHORT).show();
                break;
            case ERROR:
                Toast.makeText(context, "Nie udało się dodać przyhodu", Toast.LENGTH_SHORT).show();
                break;
        }

    }
}
