package com.chidiebere.medusa;

import android.app.Application;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.chidiebere.medusa.constants.CardType;
import com.chidiebere.medusa.constants.Constants;

public class ManualEditActivity extends AppCompatActivity {

    private EditText cardNumberEditText;
    private EditText expiryDateEditText;
    private EditText cvvEditText;
    private EditText nameEditText;
    private ImageView cardTypeImageView;
    private LinearLayout addCardButton;
    private boolean isExpInEdit;
    private boolean isManualEdit;
    private String cardNumber;
    private String cardExpDate;
    private String cardHolder;
    private String cvv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_edit);
        init();

        Intent intent = getIntent();
        if (intent != null){
            cardNumber = intent.getStringExtra(Constants.CARD_NUMBER);
            cardExpDate = intent.getStringExtra(Constants.CARD_EXP_DATE);
            cardHolder = intent.getStringExtra(Constants.CARD_NAME);
            isManualEdit = intent.getBooleanExtra(Constants.IS_MANUAL_EDIT, false);


            if (cardNumber != null && cardExpDate != null && cardHolder!= null) {
                cardNumberEditText.setText(cardNumber.replace(" ", ""));
                checkAndSetCardLogo(cardNumber.replace(" ", ""));
                expiryDateEditText.setText(cardExpDate);
                nameEditText.setText(cardHolder);
                cvvEditText.requestFocus();
            }
        }


    }

    private void init(){
        cardNumberEditText = findViewById(R.id.input_card_number);
        expiryDateEditText = findViewById(R.id.input_expiry_date);
        cvvEditText = findViewById(R.id.input_cvv);
        cardTypeImageView = findViewById(R.id.card_type_logo);
        addCardButton = findViewById(R.id.add_card_button);
        nameEditText = findViewById(R.id.input_name);
        cvvEditText.setFilters(new InputFilter[]{new CardNumberInputFilter(0,999)});
        isExpInEdit = false;

        addCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                cvv = cvvEditText.getText().toString();
                cardNumber = cardNumberEditText.getText().toString();
                cardExpDate = expiryDateEditText.getText().toString();
                cardHolder = nameEditText.getText().toString();


                if (cardNumber == null || cardNumber.trim().equals("")){
                    cardNumberEditText.requestFocus();
                    Toast.makeText(getApplicationContext(), "Input Card Number", Toast.LENGTH_LONG).show();
                    return;
                }
                if (cardExpDate == null || cardExpDate.trim().equals("")){
                    expiryDateEditText.requestFocus();
                    Toast.makeText(getApplicationContext(), "Input Card Expiry Date", Toast.LENGTH_LONG).show();
                    return;
                }
                if (cvvEditText.getText().toString().trim().equals("")){
                    cvvEditText.requestFocus();
                    Toast.makeText(getApplicationContext(), "Input Card CVV", Toast.LENGTH_LONG).show();
                    return;
                }
                if (cardHolder == null || cardHolder.trim().equals("")){
                    nameEditText.requestFocus();
                    Toast.makeText(getApplicationContext(), "Input Card Holder Name", Toast.LENGTH_LONG).show();
                    return;
                }

                Intent intent = new Intent(ManualEditActivity.this, CardActivity.class);
                intent.putExtra(Constants.CARD_NUMBER, cardNumber);
                intent.putExtra(Constants.CARD_EXP_DATE, cardExpDate);
                intent.putExtra(Constants.CARD_NAME, cardHolder);
                intent.putExtra(Constants.CARD_CVV, cvv);

                startActivity(intent);
            }
        });

        cardNumberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkAndSetCardLogo(s.toString());

                if (s.toString().length()== 19){
                    expiryDateEditText.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                cardNumberEditText.setFocusable(true);
                cardNumberEditText.invalidate();
            }
        });

        expiryDateEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (!s.toString().matches("\\d") || !s.toString().contains("/") || !s.toString().equals("") || !s.toString().contains(".") || !s.toString().contains("-")){
                    addCardButton.requestFocus();
                }
                if (s.toString().length() == 3){
                    isExpInEdit = true;
                }
                if (s.toString().length() > 5){
                    cvvEditText.requestFocus();
                }

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.toString().length() == 2 && !isExpInEdit){

                    expiryDateEditText.append("/");
                }
                else if (s.toString().length() < 2){
                    isExpInEdit = false;
                }

                if (s.toString().length() == 5 && s.toString().contains("/")){
                    cvvEditText.requestFocus();
                }
                else if (s.toString().length() == 4 && !s.toString().contains("/")){
                    cvvEditText.requestFocus();
                }


            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        expiryDateEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    isExpInEdit = false;
                    if (expiryDateEditText.getText().toString().length() == 4 && !expiryDateEditText.getText().toString().contains("/")){
                        String s = expiryDateEditText.getText().toString();
                        StringBuilder sb = new StringBuilder();
                        sb.append(s.substring(0,2) +"/");
                        sb.append(s.substring(2,4));
                        expiryDateEditText.setText(sb.toString());
                    }
                }
            }
        });

        cvvEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() == 3){
                    nameEditText.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void checkAndSetCardLogo(String cardNumber){
        if (cardNumber.matches(CardType.PATTERN_VISA)){
            cardTypeImageView.setImageDrawable(ContextCompat.getDrawable(ManualEditActivity.this, R.drawable.visa_logo));
            expiryDateEditText.requestFocus();
        }
        else if (cardNumber.matches(CardType.PATTERN_MASTER_CARD)){
            cardTypeImageView.setImageDrawable(ContextCompat.getDrawable(ManualEditActivity.this, R.drawable.mastercard_logo));
            expiryDateEditText.requestFocus();
        }
        else if (cardNumber.matches(CardType.PATTERN_VERVE)){
            cardTypeImageView.setImageDrawable(ContextCompat.getDrawable(ManualEditActivity.this, R.drawable.verve_logo));
            expiryDateEditText.requestFocus();
        }
        else if (cardNumber.matches(CardType.PATTERN_AMERICAN_EXPRESS)){
            cardTypeImageView.setImageDrawable(ContextCompat.getDrawable(ManualEditActivity.this, R.drawable.amex));
            expiryDateEditText.requestFocus();
        }
        else if (cardNumber.matches(CardType.PATTERN_DISCOVER)){
            cardTypeImageView.setImageDrawable(ContextCompat.getDrawable(ManualEditActivity.this, R.drawable.discover));
            expiryDateEditText.requestFocus();
        }
        else {
            cardTypeImageView.setImageDrawable(null);
        }
    }
}
