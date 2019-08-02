package com.myapp.homebudget.expenses;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.myapp.homebudget.R;
import com.myapp.homebudget.expenses.data.Expense;
import com.myapp.homebudget.expenses.handler.ExpensesHandler;
import com.myapp.homebudget.util.Constans;
import com.myapp.homebudget.util.DecimalDigitsInputFilter;
import com.myapp.homebudget.util.DialogType;

import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import static com.myapp.homebudget.expenses.util.ExpensesConstans.EXP_FIXED_MSG_CONFIRM;
import static com.myapp.homebudget.expenses.util.ExpensesConstans.EXP_MSG_CONFIRM;

public class ExpensesActivity extends AppCompatActivity {

    private ExpenseType expenseType;
    private EditText dialogDesc;
    private EditText dialogValue;
    private TextView dialogTitle;
    private AlertDialog dialog;
    private boolean valueFlag;
    private TabLayout tabLayout;
    private ExpensesHandler handler;
    private Handler refreshHandler;

    private CurrentExpenses currentExpenses;
    private FixedExpenses fixedExpenses;


    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenses);
        expenseType = ExpenseType.Current;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
        handler = new ExpensesHandler(this);

        refreshHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == 1){
                    currentExpenses.initCurrentExpensesList(new View(ExpensesActivity.this));
                }else if(msg.what == 2){
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_expenses, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
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

    private void initDialog(final DialogType type) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ExpensesActivity.this);
        View dView = getLayoutInflater().inflate(R.layout.dialog_add, null);
        dialogValue = dView.findViewById(R.id.et_outcome_value);
        dialogValue.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(8, 2)});
        dialogDesc = dView.findViewById(R.id.et_outcome_description);
        dialogTitle = dView.findViewById(R.id.dialog_title);
        dialogTitle.setText(type.getDialogTitle());
        Button btnConfirm = dView.findViewById(R.id.btnOutcomeConf);

        dialogValue.setHint("*Kwota");
        dialogDesc.setHint("Opis");
        dialogDesc.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialogDesc.setHint("");
                } else {
                    dialogDesc.setHint("Opis");
                }
            }
        });

        dialogValue.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialogValue.setHint("");
                } else if (!hasFocus && !valueFlag) {
                    dialogValue.setHint("*Kwota");
                }
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add(type);
            }
        });
        dialogBuilder.setView(dView);
        dialog = dialogBuilder.create();
        dialog.show();
    }

    private boolean validateField(EditText field) {
        String value = field.getText().toString();
        return value != null && value.length() > 0;
    }

    private boolean add(final DialogType type) {
        final String desc = dialogDesc.getText().toString();

        System.out.println(desc);

        if (validateField(dialogValue)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    BigDecimal value = new BigDecimal(dialogValue.getText().toString());
                    final String description = desc.length() != 0 ? desc : "Wydatek";

                    Expense expense = new Expense(value.toString(), description, "koko", 1l, null); //TODO user name
                    String endpoint = type == DialogType.Expense ? getString(R.string.add_expense_endpoint) : getString(R.string.add_fixed_expense_endpoint);
                    RestTemplate restTemplate = new RestTemplate();
                    Boolean isAdded = restTemplate.postForObject(Constans.hostName + endpoint, expense, Boolean.class);

                    Message message = new Message();
                    if (isAdded) {
                        message.what = type == DialogType.Expense ? EXP_MSG_CONFIRM : EXP_FIXED_MSG_CONFIRM;
                        message.obj = expense;
                        handler.sendMessage(message);

                        Message refreshMessage = new Message();
                        if(type == DialogType.Expense){
                            refreshMessage.what = 1;
                            refreshHandler.sendMessage(refreshMessage);
                        }else if(type == DialogType.FixedExpense){
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
