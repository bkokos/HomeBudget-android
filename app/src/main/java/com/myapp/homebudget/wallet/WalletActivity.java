package com.myapp.homebudget.wallet;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.myapp.homebudget.R;
import com.myapp.homebudget.util.DecimalDigitsInputFilter;
import com.myapp.homebudget.util.DialogType;

public abstract class WalletActivity extends AppCompatActivity {

    private Context context;

    protected EditText dialogDesc;
    protected EditText dialogValue;
    protected TextView dialogTitle;
    protected AlertDialog dialog;
    protected boolean valueFlag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void setContext(Context context){
        this.context = context;
    }


    protected void initDialog(final DialogType type) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
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
                } else if (!valueFlag) {
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

    protected boolean validateField(EditText field) {
        String value = field.getText().toString();
        return value != null && value.length() > 0;
    }

    protected abstract boolean add(final DialogType type);
}
