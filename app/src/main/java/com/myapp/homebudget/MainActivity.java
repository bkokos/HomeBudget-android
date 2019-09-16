package com.myapp.homebudget;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.firebase.client.Firebase;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.myapp.homebudget.auth.LoginActivity;
import com.myapp.homebudget.auth.User;
import com.myapp.homebudget.expenses.ExpensesActivity;
import com.myapp.homebudget.expenses.data.Expense;
import com.myapp.homebudget.expenses.handler.ExpensesHandler;
import com.myapp.homebudget.income.IncomeActivity;
import com.myapp.homebudget.util.Constans;
import com.myapp.homebudget.util.DecimalDigitsInputFilter;
import com.myapp.homebudget.util.DialogType;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Logger;

import az.plainpie.PieView;
import az.plainpie.animation.PieAngleAnimation;

import static com.myapp.homebudget.expenses.util.ExpensesConstans.EXP_MSG_CONFIRM;
import static com.myapp.homebudget.expenses.util.ExpensesConstans.EXP_MSG_ERROR;
import static com.myapp.homebudget.util.Constans.HOST_NAME;
import static java.math.BigDecimal.valueOf;
import static java.math.RoundingMode.HALF_DOWN;
import static java.math.RoundingMode.HALF_UP;


public class MainActivity extends AppCompatActivity {

    Logger log = Logger.getLogger("MainActivity");

    private PieView pieView;
    private FloatingActionButton expenseButton;
    private EditText dialogDesc;
    private EditText dialogValue;
    private TextView dialogTitle;
    private AlertDialog dialog;
    private ExpensesHandler expensesHandler;

    private Handler mainHandler;

    //panel values
    private TextView expensesValue;
    private TextView incomesValue;
    private TextView savingsValue;
    private TextView availableValues;
    private TextView dayLimit;

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;

    private User user;


    private boolean valueFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Firebase.setAndroidContext(this);
        mAuth = FirebaseAuth.getInstance();

        firebaseUser = mAuth.getCurrentUser();

        user = User.of(firebaseUser.getEmail());

        getWalletValues();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getWalletValues();

        mainHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        List<BigDecimal> walletValues = (List<BigDecimal>) msg.obj;
                        if (walletValues.size() == 3) {
                            BigDecimal expenses = walletValues.get(0);
                            BigDecimal incomes = walletValues.get(1);
                            BigDecimal savings = walletValues.get(2);
                            setPanelsValues(expenses, incomes, savings);
                        }
                        break;
                }
            }
        };

        valueFlag = false;

        expensesHandler = new ExpensesHandler(this);
        initializeGui();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWalletValues();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_expenses, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void initializeGui() {

        expensesValue = findViewById(R.id.expensesTV);
        incomesValue = findViewById(R.id.incomeTV);
        savingsValue = findViewById(R.id.savingsTV);
        availableValues = findViewById(R.id.walletValue);
        dayLimit = findViewById(R.id.dayLimitValue);

        initChart();
        initButtons();
        initPanels();


    }

    private void getWalletValues() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<List<BigDecimal>> walletValues = restTemplate
                        .exchange(Constans.HOST_NAME + "wallet/get-values?id=" + firebaseUser.getUid(), HttpMethod.GET, null, new ParameterizedTypeReference<List<BigDecimal>>() {
                        });

                Message message = new Message();
                message.what = 1;
                message.obj = walletValues.getBody();
                mainHandler.sendMessage(message);
            }
        }).start();

    }

    private void initPanels() {
        initPanel(R.id.expensesPanel, ExpensesActivity.class);
        initPanel(R.id.incomePanel, IncomeActivity.class);
    }

    private void setPanelsValues(BigDecimal expenses, BigDecimal incomes, BigDecimal savings) {
        availableValues.setText(incomes.subtract(expenses).toString());

        setDayLimit(incomes, expenses);

        expensesValue.setText(expenses.toString() + "zł");
        incomesValue.setText(incomes.toString() + "zł");
        savingsValue.setText(savings.toString() + "zł");

        setPieView(incomes, expenses);
    }

    private void setPieView(BigDecimal incomes, BigDecimal expenses) {
        float percenrtage = 0;
        if(incomes.compareTo(BigDecimal.ZERO) > 0){
            percenrtage= incomes.subtract(expenses).divide(incomes, 2, HALF_UP).multiply(valueOf(100)).setScale(0).floatValue();
        }

        pieView.setPercentage(percenrtage);
        animatePieView();
    }

    private void setDayLimit(BigDecimal incomes, BigDecimal expenses) {
        BigDecimal cash = incomes.subtract(expenses);

        MathContext mc = new MathContext(2, HALF_DOWN);

        Calendar calendar = new GregorianCalendar();
        int daysRemaining = calendar.getActualMaximum(Calendar.DAY_OF_MONTH) - calendar.get(Calendar.DATE);

        BigDecimal dayliLimit = cash.divide(valueOf(daysRemaining), mc);

        dayLimit.setText(dayliLimit.toPlainString() + "zł");
    }

    private void initPanel(@IdRes int id, final Class clazz) {
        findViewById(id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, clazz);
                startActivity(intent);
            }
        });
    }

    private void initChart() {
        pieView = findViewById(R.id.pieView);

        animatePieView();
    }

    private void animatePieView() {
        PieAngleAnimation animation = new PieAngleAnimation(pieView);
        animation.setDuration(1000);
        pieView.startAnimation(animation);
    }

    private void initButtons() {

        expenseButton = findViewById(R.id.expensesButton);

        expenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initDialog(DialogType.Expense);
            }
        });


    }

    private void initDialog(DialogType type) {
        final AlertDialog.Builder dialogBuider = new AlertDialog.Builder(MainActivity.this);
        View dView = getLayoutInflater().inflate(R.layout.dialog_add, null);
        dialogValue = dView.findViewById(R.id.fieldValue);
        dialogValue.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(8, 2)});
        dialogDesc = dView.findViewById(R.id.fieldDescription);
        dialogTitle = dView.findViewById(R.id.dialog_title);
        dialogTitle.setText(type.getDialogTitle());
        Button btnConfirm = dView.findViewById(R.id.btnConfirmation);

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
                add();
            }
        });
        dialogBuider.setView(dView);
        dialog = dialogBuider.create();
        dialog.show();
    }

    private boolean validateField(EditText field) {
        String value = field.getText().toString();
        return value != null && value.length() > 0;
    }

    private boolean add() {
        if (validateField(dialogValue)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String desc = dialogDesc.getText().toString();
                    BigDecimal value = new BigDecimal(dialogValue.getText().toString());
                    final String description = desc.length() != 0 ? desc : "Nowy wydatek";

                    System.out.println(description);

                    Expense expense = new Expense(value.toString(), description, user.getUserName(), firebaseUser.getUid(), null);

                    RestTemplate restTemplate = new RestTemplate();
                    Boolean isAdded = restTemplate.postForObject(HOST_NAME + "current-expense/add", expense, Boolean.class);

                    Message message = new Message();
                    if (isAdded) {
                        message.what = EXP_MSG_CONFIRM;
                        message.obj = expense;
                        expensesHandler.sendMessage(message);
                        getWalletValues();
                    } else {
                        message.what = EXP_MSG_ERROR;
                        expensesHandler.sendMessage(message);
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
