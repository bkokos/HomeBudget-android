package com.myapp.homebudget.expenses;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.myapp.homebudget.R;
import com.myapp.homebudget.auth.User;
import com.myapp.homebudget.expenses.data.Expense;
import com.myapp.homebudget.expenses.handler.ExpensesHandler;
import com.myapp.homebudget.util.Constans;
import com.myapp.homebudget.util.DialogType;
import com.myapp.homebudget.wallet.WalletActivity;

import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import static com.myapp.homebudget.expenses.util.ExpensesConstans.EXP_FIXED_MSG_CONFIRM;
import static com.myapp.homebudget.expenses.util.ExpensesConstans.EXP_MSG_CONFIRM;

public class ExpensesActivity extends WalletActivity {

    private ExpenseType expenseType;
    private TabLayout tabLayout;
    private ExpensesHandler handler;
    private Handler refreshHandler;

    private CurrentExpenses currentExpenses;
    private FixedExpenses fixedExpenses;

    private FirebaseUser firebaseUser;
    private User user;


    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContext(ExpensesActivity.this);
        setContentView(R.layout.activity_expenses);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        user = User.of(firebaseUser.getEmail());

        expenseType = ExpenseType.Current;
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabLayout = findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
        handler = new ExpensesHandler(this);

        refreshHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    currentExpenses.initCurrentExpensesList(new View(ExpensesActivity.this));
                } else if (msg.what == 2) {
                    fixedExpenses.initFixedExpensesList(new View(ExpensesActivity.this));
                }
            }
        };

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });
    }

    private void showDialog() {
        expenseType = tabLayout.getSelectedTabPosition() == 0 ? ExpenseType.Current : ExpenseType.Fixed;
        switch (expenseType) {
            case Fixed:
                initDialog(DialogType.FixedExpense);
                break;
            case Current:
                initDialog(DialogType.Expense);
                break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_expenses, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    currentExpenses = new CurrentExpenses();
                    return currentExpenses;
                case 1:
                    fixedExpenses = new FixedExpenses();
                    return fixedExpenses;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    @Override
    protected boolean add(final DialogType type) {
        final String desc = dialogDesc.getText().toString();

        System.out.println(desc);

        if (validateField(dialogValue)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    BigDecimal value = new BigDecimal(dialogValue.getText().toString());
                    final String description = desc.length() != 0 ? desc : "Wydatek";

                    Expense expense = new Expense(value.toString(), description, user.getUserName(), firebaseUser.getUid(), null); //TODO user name
                    String endpoint = type == DialogType.Expense ? getString(R.string.add_expense_endpoint) : getString(R.string.add_fixed_expense_endpoint);
                    RestTemplate restTemplate = new RestTemplate();
                    Boolean isAdded = restTemplate.postForObject(Constans.HOST_NAME + endpoint, expense, Boolean.class);

                    Message message = new Message();
                    if (isAdded) {
                        message.what = type == DialogType.Expense ? EXP_MSG_CONFIRM : EXP_FIXED_MSG_CONFIRM;
                        message.obj = expense;
                        handler.sendMessage(message);

                        Message refreshMessage = new Message();
                        if (type == DialogType.Expense) {
                            refreshMessage.what = 1;
                            refreshHandler.sendMessage(refreshMessage);
                        } else if (type == DialogType.FixedExpense) {
                            refreshMessage.what = 2;
                            refreshHandler.sendMessage(refreshMessage);
                        }

                    } else {
                        message.what = EXP_FIXED_MSG_CONFIRM;
                        handler.sendMessage(message);
                    }
                }
            }).start();
            dialog.dismiss();
        } else {
            valueFlag = true;
            dialogValue.setHintTextColor(getResources().getColor(R.color.colorAccent));
            dialogValue.setHint("Kwota jest wymagana!");
        }
        return true;
    }
}
