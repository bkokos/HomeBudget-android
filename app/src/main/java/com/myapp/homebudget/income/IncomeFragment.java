package com.myapp.homebudget.income;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.myapp.homebudget.R;
import com.myapp.homebudget.income.adapter.IncomesListAdapter;
import com.myapp.homebudget.income.data.Income;
import com.myapp.homebudget.util.Constans;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Comparator;
import java.util.List;

public class IncomeFragment extends Fragment {

    private ListView incomeList;
    private Handler handler;

    private FirebaseUser firebaseUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_income, container, false);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        incomeList = rootView.findViewById(R.id.incomeLV);
        handler = new Handler() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    List<Income> incomes = (List<Income>) msg.obj;
                    incomes.sort(new Comparator<Income>() {
                        @Override
                        public int compare(Income o1, Income o2) {
                            return -o1.getAddDate().compareTo(o2.getAddDate());
                        }
                    });

                    IncomesListAdapter adapter = new IncomesListAdapter(incomes, getContext());

                    incomeList.setAdapter(adapter);
                }
            }
        };

        initIncomeList(rootView);

        return rootView;
    }

    public void initIncomeList(View rootView) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<List<Income>> income = restTemplate.exchange(Constans.HOST_NAME + "income/get-all?id=" + firebaseUser.getUid(), HttpMethod.GET, null, new ParameterizedTypeReference<List<Income>>() {
                });
                Message message = new Message();
                message.what = 1;
                message.obj = income.getBody();
                handler.sendMessage(message);
            }
        }).start();


    }
}
