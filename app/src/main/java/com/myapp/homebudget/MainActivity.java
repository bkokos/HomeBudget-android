package com.myapp.homebudget;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.design.widget.FloatingActionButton;
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

import com.myapp.homebudget.auth.User;
import com.myapp.homebudget.expenses.ExpensesActivity;
import com.myapp.homebudget.expenses.data.Expense;
import com.myapp.homebudget.expenses.handler.ExpensesHandler;
import com.myapp.homebudget.income.IncomeActivity;
import com.myapp.homebudget.util.DecimalDigitsInputFilter;
import com.myapp.homebudget.util.DialogType;

import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import az.plainpie.PieView;
import az.plainpie.animation.PieAngleAnimation;

import static com.myapp.homebudget.expenses.util.ExpensesConstans.EXP_MSG_CONFIRM;
import static com.myapp.homebudget.expenses.util.ExpensesConstans.EXP_MSG_ERROR;
import static com.myapp.homebudget.util.Constans.HOST_NAME;


public class MainActivity extends AppCompatActivity {

    private PieView pieView;
    private FloatingActionButton expenseButton;
    private EditText dialogDesc;
    private EditText dialogValue;
    private TextView dialogTitle;
    private AlertDialog dialog;
    private User user;
    private ExpensesHandler expensesHandler;


    private boolean valueFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        user = new User("kokos", "email", "pass");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        valueFlag = false;
        initializeGui();
        expensesHandler = new ExpensesHandler(this);
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


    private void initializeGui() {
        initChart();
        initButtons();
        initPanels();
    }

    private void initPanels() {
        initPanel(R.id.expensesPanel, ExpensesActivity.class);
        initPanel(R.id.incomePanel, IncomeActivity.class);
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

                    Expense expense = new Expense(value.toString(), description, user.getUserName(), 1l, null);

                    RestTemplate restTemplate = new RestTemplate();
                    Boolean isAdded = restTemplate.postForObject(HOST_NAME + "current-expense/add", expense, Boolean.class);

                    Message message = new Message();
                    if (isAdded) {
                        message.what = EXP_MSG_CONFIRM;
                        message.obj = expense;
                        expensesHandler.sendMessage(message);
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
