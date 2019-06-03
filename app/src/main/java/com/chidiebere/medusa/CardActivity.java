package com.chidiebere.medusa;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.chidiebere.medusa.constants.Constants;
import com.chidiebere.medusa.views.CreditCardView;

public class CardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        CreditCardView creditCardView = findViewById(R.id.card_view);

        String cardNumber;
        String cardExpDate;
        String cardHolder;
        String cvv;

        Intent intent = getIntent();
        if (intent != null){
            cardNumber = intent.getStringExtra(Constants.CARD_NUMBER);
            cardExpDate = intent.getStringExtra(Constants.CARD_EXP_DATE);
            cardHolder = intent.getStringExtra(Constants.CARD_NAME);
            cvv = intent.getStringExtra(Constants.CARD_CVV);


            if (cardNumber != null && cardExpDate != null && cardHolder!= null && cvv != null) {
               creditCardView.setCardName(cardHolder);
               creditCardView.setCardNumber(cardNumber);
               creditCardView.setExpiryDate(cardExpDate);
               creditCardView.setCvv(cvv);
            }
        }
    }
}
