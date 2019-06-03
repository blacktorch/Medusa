package com.chidiebere.medusa;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CardProcessor {

    private Context mContext;
    private int nameCount;
    private int cardNumCount;
    private int expDateCount;

    private boolean isScanned;

    ArrayList<String> names;
    ArrayList<String> cardNumbers;
    String expDate;

    String cardName;
    String cardNumber;

    public static final String TAG = "CARD PROCESSOR";

    public CardProcessor(Context context){
        this.mContext = context;
        names = new ArrayList<>();
        cardNumbers = new ArrayList<>();
        isScanned = false;
    }

    public String processCard(SparseArray<TextBlock> cardTextBlock){

        String result = "";
        if (cardNumCount == 1 && expDateCount == 1){
            nameCount = 0;
            cardNumCount = 0;
            expDateCount = 0;

            isScanned = true;

            cardNumber = getCardNumber();
            cardName = getCardName();

            Log.e(TAG, "receiveDetections: " + cardNumber );
            Log.e(TAG, "receiveDetections: " + cardName );
            Log.e(TAG, "receiveDetections: " + expDate );
        }

        if (cardTextBlock.size() != 0 ){
            StringBuilder stringBuilder = new StringBuilder();
            ArrayList<TextHolder> textHolder = new ArrayList<>();

            for (int index = 0; index < cardTextBlock.size(); index++) {
                TextBlock tBlock = cardTextBlock.valueAt(index);
                if (tBlock != null) {                                         // block is valid, get its components (ie, lines)
                    for (Text line : tBlock.getComponents()) {    // step through each line in the block
                        float top = line.getBoundingBox().top;       // CAPTURE THE LINE'S BOUNDING BOX & ITS TOP COORD !!
                        TextHolder t = new TextHolder(top, line.getValue());  // STUFF top AND THIS LINE'S text INTO AN ELEMENT FOR YOUR LIST
                        textHolder.add(t);    // ADD THAT ELEMENT TO YOUR LIST
                    }
                }
            }

            // SORT YOUR LIST USING THE top VALUE OF EACH ELEMENT AS YOUR SORT FIELD.  THIS ASSURES THAT YOU WILL AT LEAST BE READING TOP TO BOTTOM.  BUT IT DOESN'T GUARANTEE MOBILE VISION SEES A LINE OF TEXT THE WAY WE DO
            Collections.sort(textHolder);

            for (int i = 0; i < textHolder.size(); i++) {

                TextHolder line = textHolder.get(i);

                //fetch and store expiry date
                if (line.getText().contains("/")){
                    String exprDateLine = line.getText();
                    Pattern pattern = Pattern.compile("(\\d\\d/\\d\\d)");
                    Matcher matcher = pattern.matcher(exprDateLine);
                    while (matcher.find()) {
                        expDate = matcher.group(1);
                        if (expDateCount<1) {
                            ++expDateCount;
                        }
                        stringBuilder.append(expDate);
                        stringBuilder.append("\n");
                    }
                }

                //Apply filter to remove unnecessary data
                if (line.getText().toLowerCase().contains("elect")||
                        line.getText().toLowerCase().contains("electronic use only")||
                        line.getText().toLowerCase().contains("use")||
                        line.getText().toLowerCase().contains("only")||
                        line.getText().toLowerCase().contains("mastercard")||
                        line.getText().toLowerCase().contains("bank")||
                        line.getText().toLowerCase().contains("access")||
                        line.getText().toLowerCase().contains("visa")||
                        line.getText().toLowerCase().contains("debit")||
                        line.getText().toLowerCase().contains("thru")||
                        line.getText().toLowerCase().contains("verve")||
                        line.getText().toLowerCase().contains("erve")||
                        line.getText().toLowerCase().contains("union")||
                        line.getText().toLowerCase().contains("first")||
                        line.getText().toLowerCase().contains("skye")||
                        line.getText().toLowerCase().contains("elec")||
                        line.getText().toLowerCase().contains("master")||
                        line.getText().toLowerCase().contains("valid")||
                        line.getText().toLowerCase().contains("prepaid")||
                        line.getText().toLowerCase().contains("card")||
                        line.getText().matches("\\D{3}")||
                        line.getText().matches("\\D{4}") ||
                        line.getText().matches("\\D{2}")){
                    continue;
                }

                if (line.getText().matches("\\d{4}\\s\\d{4}\\s\\d{4}\\s\\d{4}") ||
                        line.getText().replaceAll("\\s","").matches("\\d{12,19}")){
                    cardNumbers.add(line.getText());
                    if (cardNumCount<1) {
                        ++cardNumCount;
                    }
                    stringBuilder.append(line.getText());
                    stringBuilder.append("\n");
                }
                if (line.getText().matches("^[a-zA-Z\\s]+$")){
                    names.add(line.getText());
                    if (nameCount<1) {
                        ++nameCount;
                    }
                }


            }

            result = stringBuilder.toString();
        }

        return result;
    }

    public ArrayList<String> getNames() {
        return names;
    }

    public void setNames(ArrayList<String> names) {
        this.names = names;
    }

    public ArrayList<String> getCardNumbers() {
        return cardNumbers;
    }

    public void setCardNumbers(ArrayList<String> cardNumbers) {
        this.cardNumbers = cardNumbers;
    }

    public String getExpDate() {
        return expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

    public String getCardNumber(){
        String cardNumber = "0000";

        for(String number : cardNumbers){
            if (number.length() > cardNumber.length()){
                cardNumber = number;
            }
        }

        return cardNumber;
    }

    public String getCardName(){
        String cardName = "abc";

        for(String name : names){
            if (name.length() > cardName.length()){
                cardName = name;
            }
        }

        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public boolean isScanned() {
        return isScanned;
    }
}
