package com.myapp.homebudget.income.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.myapp.homebudget.R;
import com.myapp.homebudget.income.data.Income;

import java.util.List;

public class IncomesListAdapter extends BaseAdapter {

    private List<Income> incomes;
    private Context context;

    private LayoutInflater layoutInflater;

    public IncomesListAdapter(List<Income> incomes, Context context) {
        this.incomes = incomes;
        this.context = context;

        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return incomes.size();
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

        View view = layoutInflater.inflate(R.layout.income_row, parent, false);

        TextView value = view.findViewById(R.id.incomesValueList);
        TextView desc = view.findViewById(R.id.incomesDescList);
        TextView user = view.findViewById(R.id.incomesUserList);

        value.setText(incomes.get(position).getValue());
        desc.setText(incomes.get(position).getDesc());
        user.setText(incomes.get(position).getUserName());

        return view;
    }
}
