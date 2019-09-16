package com.myapp.homebudget.expenses;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.myapp.homebudget.R;
import com.myapp.homebudget.expenses.adapter.ExpensesListAdapter;
import com.myapp.homebudget.expenses.data.Expense;
import com.myapp.homebudget.util.Constans;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Comparator;
import java.util.List;

public class CurrentExpenses extends Fragment {

    private ListView expensesList;
    private Handler handler;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.current_expenses, container, false);

        expensesList = rootView.findViewById(R.id.currentExpensesLV);
        handler = new Handler() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    List<Expense> expenses = (List<Expense>) msg.obj;

                    expenses.sort(new Comparator<Expense>() {
                        @Override
                        public int compare(Expense o1, Expense o2) {
                            return -o1.getAddDate().compareTo(o2.getAddDate());
                        }
                    });

                    ExpensesListAdapter adapter = new ExpensesListAdapter(expenses, getContext());

                    expensesList.setAdapter(adapter);
                }
            }
        };

        initCurrentExpensesList(rootView);

        return rootView;
    }

    public void initCurrentExpensesList(View rootView) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<List<Expense>> expenses = restTemplate.exchange(Constans.HOST_NAME + "current-expense/get-all?id=" + FirebaseAuth.getInstance().getCurrentUser().getUid(),
                        HttpMethod.GET, null, new ParameterizedTypeReference<List<Expense>>() {
                        });
                Message message = new Message();
                message.what = 1;
                message.obj = expenses.getBody();

                handler.sendMessage(message);

            }
        }).start();


    }
}


