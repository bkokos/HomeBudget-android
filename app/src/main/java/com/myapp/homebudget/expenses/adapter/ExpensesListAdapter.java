package com.myapp.homebudget.expenses.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.myapp.homebudget.R;
import com.myapp.homebudget.expenses.data.Expense;

import java.util.List;

public class ExpensesListAdapter extends BaseAdapter {

    private List<Expense> expenses;
    private Context context;

    private LayoutInflater layoutInflater;

    public ExpensesListAdapter(List<Expense> expenses, Context context) {
        this.expenses = expenses;
        this.context = context;

        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return expenses.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = layoutInflater.inflate(R.layout.expense_row, parent, false);

        TextView value = view.findViewById(R.id.expensesValueList);
        TextView desc = view.findViewById(R.id.expensesDescList);
        TextView user = view.findViewById(R.id.expensesUserList);

        value.setText(expenses.get(position).getValue());
        desc.setText(expenses.get(position).getDesc());
        user.setText(expenses.get(position).getUserName());

        return view;
    }
}
