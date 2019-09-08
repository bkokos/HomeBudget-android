package com.myapp.homebudget.income;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.myapp.homebudget.R;
import com.myapp.homebudget.income.data.Income;
import com.myapp.homebudget.income.handler.IncomesHandler;
import com.myapp.homebudget.util.Constans;
import com.myapp.homebudget.util.DialogType;
import com.myapp.homebudget.wallet.WalletActivity;

import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.logging.Logger;

import static com.myapp.homebudget.income.util.IncomesConstans.CONFIRM;
import static com.myapp.homebudget.income.util.IncomesConstans.ERROR;

public class IncomeActivity extends WalletActivity {

    private Logger logger = Logger.getLogger("Przychody");

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private IncomeFragment incomes;
    private Handler refreshHandler;
    private ViewPager mViewPager;
    private TabLayout tabLayout;

    private IncomesHandler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContext(IncomeActivity.this);
        setContentView(R.layout.activity_income);

        Toolbar toolbar = findViewById(R.id.toolbar_income);
        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        handler = new IncomesHandler(this);

        refreshHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    incomes.initIncomeList(new View(IncomeActivity.this));
                }
            }
        };

        tabLayout = findViewById(R.id.tabs_income);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        FloatingActionButton fab = findViewById(R.id.fab_income);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });
    }

    @Override
    protected boolean add(final DialogType type) {
        final String desc = dialogDesc.getText().toString();

        logger.info(desc);

        if (validateField(dialogValue)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    BigDecimal value = new BigDecimal(dialogValue.getText().toString());
                    final String description = desc.length() != 0 ? desc : "Przychód";

                    Income income = new Income(value.toString(), description, "koko", 1l);//TODO user name
                    String endpoint = getString(R.string.add_income);
                    RestTemplate restTemplate = new RestTemplate();
                    Boolean isAdded = restTemplate.postForObject(Constans.HOST_NAME + endpoint, income, Boolean.class);

                    Message message = new Message();
                    if (isAdded) {
                        message.what = CONFIRM;
                        message.obj = income;
                        logger.info(income.getDesc());
                        handler.sendMessage(message);

                        Message refreshMessage = new Message();
                        refreshMessage.what = 1;
                        refreshHandler.sendMessage(refreshMessage);
                    } else {
                        message.what = ERROR;
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

    private void showDialog() {
        initDialog(DialogType.Income);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_expenses, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
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
                    incomes = new IncomeFragment();
                    return incomes;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 1;
        }
    }
}
