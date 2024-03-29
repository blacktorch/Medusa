package com.chidiebere.medusa.views;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.chidiebere.medusa.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.regex.Pattern;

import static com.chidiebere.medusa.constants.CardNumberFormat.ALL_DIGITS;
import static com.chidiebere.medusa.constants.CardNumberFormat.MASKED_ALL;
import static com.chidiebere.medusa.constants.CardNumberFormat.MASKED_ALL_BUT_LAST_FOUR;
import static com.chidiebere.medusa.constants.CardNumberFormat.ONLY_LAST_FOUR;
import static com.chidiebere.medusa.constants.CardType.AMERICAN_EXPRESS;
import static com.chidiebere.medusa.constants.CardType.AUTO;
import static com.chidiebere.medusa.constants.CardType.DISCOVER;
import static com.chidiebere.medusa.constants.CardType.MASTERCARD;
import static com.chidiebere.medusa.constants.CardType.PATTERN_AMERICAN_EXPRESS;
import static com.chidiebere.medusa.constants.CardType.PATTERN_DISCOVER;
import static com.chidiebere.medusa.constants.CardType.PATTERN_MASTER_CARD;
import static com.chidiebere.medusa.constants.CardType.PATTERN_VERVE;
import static com.chidiebere.medusa.constants.CardType.PATTERN_VISA;
import static com.chidiebere.medusa.constants.CardType.VERVE;
import static com.chidiebere.medusa.constants.CardType.VISA;

public class CreditCardView extends FrameLayout {

    private static final int CARD_FRONT = 0;
    private static final int CARD_BACK = 1;
    private static final boolean DEBUG = false;
    private final Context mContext;
    private String mCardNumber = "";
    private String mCardName = "";
    private String mExpiryDate = "";
    private String mCvv = "";
    private int mCardNumberTextColor = Color.WHITE;
    private int mCardNumberFormat = ALL_DIGITS;
    private int mCardNameTextColor = Color.WHITE;
    private int mExpiryDateTextColor = Color.WHITE;
    private int mCvvTextColor = Color.BLACK;
    private int mValidTillTextColor = Color.WHITE;
    @CreditCardType
    private int mType = VISA;
    private int cardSide = CARD_FRONT;
    private boolean mPutChip = false;
    private boolean mIsEditable = false;
    private boolean mIsCardNumberEditable = false;
    private boolean mIsCardNameEditable = false;
    private boolean mIsExpiryDateEditable = false;
    private boolean mIsCvvEditable = false;
    private int mHintTextColor = Color.WHITE;
    private int mCvvHintColor = Color.WHITE;
    private int mCardFrontBackground;
    private int mCardBackBackground;
    private boolean mIsFlippable = false;
    private Typeface creditCardTypeFace;
    private ImageButton mFlipBtn;
    private EditText mCardNumberView;
    private EditText mCardNameView;
    private EditText mExpiryDateView;
    private EditText mCvvView;
    private ImageView mCardTypeView;
    private TextView mValidTill;
    private View mStripe;
    private View mAuthorizedSig;

    public CreditCardView(Context context) {
        this(context, null);
    }

    public CreditCardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        if (context != null) {
            this.mContext = context;
        } else {
            this.mContext = getContext();
        }

        init();
        loadAttributes(attrs);
        initDefaults();
        mFlipBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                flip();
            }
        });
    }

    /**
     * Initialize various views and variables
     */
    private void init() {
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        inflater.inflate(R.layout.creditcardview, this, true);

        // Added this check to fix the issue of custom view not rendering correctly in the layout
        // preview.
        if (!isInEditMode()) {
            // Font path
            final String fontPath = mContext.getString(R.string.font_path);
            // Loading Font Face
            creditCardTypeFace = Typeface.createFromAsset(mContext.getAssets(), fontPath);
        }

        mCardNumberView = findViewById(R.id.card_number);
        mCardNameView = findViewById(R.id.card_holder);
        mCardTypeView = findViewById(R.id.logo);
        mValidTill = findViewById(R.id.valid_till);
        mExpiryDateView = findViewById(R.id.expiry_date);
        mFlipBtn = findViewById(R.id.flip_btn);
        mCvvView = findViewById(R.id.cvv_et);
        mStripe = findViewById(R.id.stripe);
        mAuthorizedSig = findViewById(R.id.authorized_sig_tv);
    }

    private void loadAttributes(@Nullable AttributeSet attrs) {

        final TypedArray a = mContext.getTheme().obtainStyledAttributes(attrs,
                R.styleable.CreditCardView, 0, 0);

        try {
            mCardNumber = a.getString(R.styleable.CreditCardView_cardNumber);
            mCardName = a.getString(R.styleable.CreditCardView_cardName);
            mExpiryDate = a.getString(R.styleable.CreditCardView_expiryDate);
            mCardNumberTextColor = a.getColor(R.styleable.CreditCardView_cardNumberTextColor,
                    Color.WHITE);
            mCardNumberFormat = a.getInt(R.styleable.CreditCardView_cardNumberFormat, 0);
            mCardNameTextColor = a.getColor(R.styleable.CreditCardView_cardNumberTextColor,
                    Color.WHITE);
            mExpiryDateTextColor = a.getColor(R.styleable.CreditCardView_expiryDateTextColor,
                    Color.WHITE);
            mCvvTextColor = a.getColor(R.styleable.CreditCardView_cvvTextColor,
                    Color.BLACK);
            mValidTillTextColor = a.getColor(R.styleable.CreditCardView_validTillTextColor,
                    Color.WHITE);
            //noinspection WrongConstant
            mType = a.getInt(R.styleable.CreditCardView_type, VISA);
            mIsEditable = a.getBoolean(R.styleable.CreditCardView_isEditable, false);
            //For more granular control to the fields. Issue #7
            mIsCardNameEditable = a.getBoolean(R.styleable.CreditCardView_isCardNameEditable, mIsEditable);
            mIsCardNumberEditable = a.getBoolean(R.styleable.CreditCardView_isCardNumberEditable, mIsEditable);
            mIsExpiryDateEditable = a.getBoolean(R.styleable.CreditCardView_isExpiryDateEditable, mIsEditable);
            mIsCvvEditable = a.getBoolean(R.styleable.CreditCardView_isCvvEditable, mIsEditable);
            mHintTextColor = a.getColor(R.styleable.CreditCardView_hintTextColor, Color.WHITE);
            mIsFlippable = a.getBoolean(R.styleable.CreditCardView_isFlippable, mIsFlippable);
            mCvv = a.getString(R.styleable.CreditCardView_cvv);
            mCardBackBackground = a.getResourceId(R.styleable.CreditCardView_cardBackBackground, R.drawable.ic_card_back);

        } finally {
            a.recycle();
        }
    }

    private void initDefaults() {

        // Set default background if background attribute was not entered in the xml
        if (getBackground() == null) {
            mCardFrontBackground = R.drawable.card;
            setBackgroundResource(mCardFrontBackground);
        }

        if (!mIsEditable) {
            // If card is not set to be editable, disable the edit texts
            mCardNumberView.setEnabled(false);
            mCardNameView.setEnabled(false);
            mExpiryDateView.setEnabled(false);
            mCvvView.setEnabled(false);
        } else {
            // If the card is editable, set the hint text and hint values which will be displayed
            // when the edit text is blank
            mCardNumberView.setHintTextColor(mHintTextColor);

            mCardNameView.setHintTextColor(mHintTextColor);

            mExpiryDateView.setHintTextColor(mHintTextColor);

            mCvvView.setHintTextColor(mCvvTextColor);
        }

        //For more granular control of the editable fields. Issue #7
        if (mIsCardNameEditable != mIsEditable) {
            //If the mIsCardNameEditable is different than mIsEditable field, the granular
            //precedence comes into picture and the value needs to be checked and modified
            //accordingly
            if (mIsCardNameEditable) {
                mCardNameView.setHintTextColor(mHintTextColor);
            } else {
                mCardNameView.setHint("");
            }

            mCardNameView.setEnabled(mIsCardNameEditable);
        }

        if (mIsCardNumberEditable != mIsEditable) {
            //If the mIsCardNumberEditable is different than mIsEditable field, the granular
            //precedence comes into picture and the value needs to be checked and modified
            //accordingly
            if (mIsCardNumberEditable) {
                mCardNumberView.setHintTextColor(mHintTextColor);
            } else {
                mCardNumberView.setHint("");
            }
            mCardNumberView.setEnabled(mIsCardNumberEditable);
        }

        if (mIsExpiryDateEditable != mIsEditable) {
            //If the mIsExpiryDateEditable is different than mIsEditable field, the granular
            //precedence comes into picture and the value needs to be checked and modified
            //accordingly
            if (mIsExpiryDateEditable) {
                mExpiryDateView.setHintTextColor(mHintTextColor);
            } else {
                mExpiryDateView.setHint("");
            }
            mExpiryDateView.setEnabled(mIsExpiryDateEditable);
        }

        // If card number is not null, add space every 4 characters and format it in the appropriate
        // format
        if (mCardNumber != null) {
            mCardNumberView.setText(getFormattedCardNumber(addSpaceToCardNumber()));
        }

        // Set the user entered card number color to card number field
        mCardNumberView.setTextColor(mCardNumberTextColor);

        // Added this check to fix the issue of custom view not rendering correctly in the layout
        // preview.
        if (!isInEditMode()) {
            mCardNumberView.setTypeface(creditCardTypeFace);
        }

        // If card name is not null, convert the text to upper case
        if (mCardName != null) {
            mCardNameView.setText(mCardName.toUpperCase());
        }

        // This filter will ensure the text entered is in uppercase when the user manually enters
        // the card name
        mCardNameView.setFilters(new InputFilter[]{
                new InputFilter.AllCaps()
        });

        // Set the user entered card name color to card name field
        mCardNameView.setTextColor(mCardNumberTextColor);

        // Added this check to fix the issue of custom view not rendering correctly in the layout
        // preview.
        if (!isInEditMode()) {
            mCardNameView.setTypeface(creditCardTypeFace);
        }

        // Set the appropriate logo based on the type of card
        mCardTypeView.setBackgroundResource(getLogo());

        // If expiry date is not null, set it to the expiryDate TextView
        if (mExpiryDate != null) {
            mExpiryDateView.setText(mExpiryDate);
        }

        // Set the user entered expiry date color to expiry date field
        mExpiryDateView.setTextColor(mExpiryDateTextColor);

        // Added this check to fix the issue of custom view not rendering correctly in the layout
        // preview.
        if (!isInEditMode()) {
            mExpiryDateView.setTypeface(creditCardTypeFace);
        }

        // Set the appropriate text color to the validTill TextView
        mValidTill.setTextColor(mValidTillTextColor);

        // If CVV is not null, set it to the expiryDate TextView
        if (mCvv != null) {
            mCvvView.setText(mCvv);
        }

        // Set the user entered card number color to card number field
        mCvvView.setTextColor(mCvvTextColor);

        // Added this check to fix the issue of custom view not rendering correctly in the layout
        // preview.
        if (!isInEditMode()) {
            mCvvView.setTypeface(creditCardTypeFace);
        }

        if (mIsCvvEditable != mIsEditable) {

            if (mIsCvvEditable) {
                mCvvView.setHintTextColor(mCvvHintColor);
            } else {
                mCvvView.setHint("");
            }

            mCvvView.setEnabled(mIsCvvEditable);

        }

        if (mIsFlippable) {
            mFlipBtn.setVisibility(View.VISIBLE);
        }
        mFlipBtn.setEnabled(mIsFlippable);

    }

    public boolean isFlippable() {
        return mIsFlippable;
    }

    public void setIsFlippable(boolean flippable) {
        mIsFlippable = flippable;
        mFlipBtn.setVisibility(mIsFlippable ? View.VISIBLE : View.INVISIBLE);
        mFlipBtn.setEnabled(mIsFlippable);
    }

    public void flip() {
        if (mIsFlippable) {
            if (cardSide == CARD_FRONT) {
                rotateInToBack();
            } else if (cardSide == CARD_BACK) {
                rotateInToFront();
            }
        }
    }

    private void showFrontView() {
        mCardNumberView.setVisibility(View.VISIBLE);
        mCardNameView.setVisibility(View.VISIBLE);
        mCardTypeView.setVisibility(View.VISIBLE);
        mValidTill.setVisibility(View.VISIBLE);
        mExpiryDateView.setVisibility(View.VISIBLE);
    }

    private void hideFrontView() {
        mCardNumberView.setVisibility(View.GONE);
        mCardNameView.setVisibility(View.GONE);
        mCardTypeView.setVisibility(View.GONE);
        mValidTill.setVisibility(View.GONE);
        mExpiryDateView.setVisibility(View.GONE);
    }

    private void showBackView() {
        mStripe.setVisibility(View.VISIBLE);
        mAuthorizedSig.setVisibility(View.VISIBLE);
        mCvvView.setVisibility(View.VISIBLE);
    }

    private void hideBackView() {
        mStripe.setVisibility(View.GONE);
        mAuthorizedSig.setVisibility(View.GONE);
        mCvvView.setVisibility(View.GONE);
    }

    private void redrawViews() {
        invalidate();
        requestLayout();
    }

    public String getCardNumber() {
        return mCardNumber;
    }

    public void setCardNumber(String cardNumber) {
        if (cardNumber == null) {
            throw new NullPointerException("Card Number cannot be null.");
        }
        this.mCardNumber = cardNumber.replaceAll("\\s+", "");
        this.mCardNumberView.setText(getFormattedCardNumber(addSpaceToCardNumber()));
        redrawViews();
    }

    public String getCardName() {
        return mCardName;
    }

    public void setCardName(String cardName) {
        if (cardName == null) {
            throw new NullPointerException("Card Name cannot be null.");
        }
        this.mCardName = cardName.toUpperCase();
        this.mCardNameView.setText(mCardName);
        redrawViews();
    }

    @ColorInt
    public int getCardNumberTextColor() {
        return mCardNumberTextColor;
    }

    public void setCardNumberTextColor(@ColorInt int cardNumberTextColor) {
        this.mCardNumberTextColor = cardNumberTextColor;
        this.mCardNumberView.setTextColor(mCardNumberTextColor);
        redrawViews();
    }

    @CreditCardFormat
    public int getCardNumberFormat() {
        return mCardNumberFormat;
    }

    public void setCardNumberFormat(@CreditCardFormat int cardNumberFormat) {
        if (cardNumberFormat < 0 | cardNumberFormat > 3) {
            throw new UnsupportedOperationException("CardNumberFormat: " + cardNumberFormat + "  " +
                    "is not supported. Use `CardNumberFormat.*` or `CardType.ALL_DIGITS` if " +
                    "unknown");
        }
        this.mCardNumberFormat = cardNumberFormat;
        this.mCardNumberView.setText(getFormattedCardNumber(mCardNumber));
        redrawViews();
    }

    @ColorInt
    public int getCardNameTextColor() {
        return mCardNameTextColor;
    }

    public void setCardNameTextColor(@ColorInt int cardNameTextColor) {
        this.mCardNameTextColor = cardNameTextColor;
        this.mCardNameView.setTextColor(mCardNameTextColor);
        redrawViews();
    }

    public String getExpiryDate() {
        return mExpiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.mExpiryDate = expiryDate;
        this.mExpiryDateView.setText(mExpiryDate);
        redrawViews();
    }

    @ColorInt
    public int getExpiryDateTextColor() {
        return mExpiryDateTextColor;
    }

    public void setExpiryDateTextColor(@ColorInt int expiryDateTextColor) {
        this.mExpiryDateTextColor = expiryDateTextColor;
        this.mExpiryDateView.setTextColor(mExpiryDateTextColor);
        redrawViews();
    }

    @ColorInt
    public int getValidTillTextColor() {
        return mValidTillTextColor;
    }

    public void setValidTillTextColor(@ColorInt int validTillTextColor) {
        this.mValidTillTextColor = validTillTextColor;
        this.mValidTill.setTextColor(mValidTillTextColor);
        redrawViews();
    }

    @CreditCardType
    public int getType() {
        return mType;
    }

    public void setType(@CreditCardType int type) {
        if (type < 0 | type > 4) {
            throw new UnsupportedOperationException("CardType: " + type + "  is not supported. " +
                    "Use `CardType.*` or `CardType.AUTO` if unknown");
        }
        this.mType = type;
        this.mCardTypeView.setBackgroundResource(getLogo());
        redrawViews();
    }

    public boolean getIsEditable() {
        return mIsEditable;
    }

    public void setIsEditable(boolean isEditable) {
        this.mIsEditable = isEditable;
        redrawViews();
    }

    public boolean getIsCardNameEditable() {
        return mIsCardNameEditable;
    }

    public void setIsCardNameEditable(boolean isCardNameEditable) {
        this.mIsCardNameEditable = isCardNameEditable;
        redrawViews();
    }

    public boolean getIsCardNumberEditable() {
        return mIsCardNumberEditable;
    }

    public void setIsCardNumberEditable(boolean isCardNumberEditable) {
        this.mIsCardNumberEditable = isCardNumberEditable;
        redrawViews();
    }

    public boolean getIsExpiryDateEditable() {
        return mIsExpiryDateEditable;
    }

    public void setIsExpiryDateEditable(boolean isExpiryDateEditable) {
        this.mIsExpiryDateEditable = isExpiryDateEditable;
        redrawViews();
    }

    @ColorInt
    public int getHintTextColor() {
        return mHintTextColor;
    }

    public void setHintTextColor(@ColorInt int hintTextColor) {
        this.mHintTextColor = hintTextColor;
        this.mCardNameView.setHintTextColor(mHintTextColor);
        this.mCardNumberView.setHintTextColor(mHintTextColor);
        this.mExpiryDateView.setHintTextColor(mHintTextColor);

        redrawViews();
    }

    public boolean getIsCvvEditable() {
        return mIsCvvEditable;
    }

    public void setIsCvvEditable(boolean editable) {
        this.mIsCvvEditable = editable;
        redrawViews();
    }

    public void setCvv(String CVV) {
        this.mCvv = CVV;
        mCvvView.setText(this.mCvv);
    }

    public String getCvv() {
        return mCvv;
    }

    @DrawableRes
    public int getCardBackBackground() {
        return mCardBackBackground;
    }

    public void setCardBackBackground(@DrawableRes int cardBackBackground) {
        this.mCardBackBackground = cardBackBackground;
        setBackgroundResource(mCardBackBackground);
        redrawViews();
    }

    /**
     * Return the appropriate drawable resource based on the card type
     */
    @DrawableRes
    private int getLogo() {

        switch (mType) {
            case VISA:
                return R.drawable.visa_logo;

            case MASTERCARD:
                return R.drawable.mastercard_logo;

            case AMERICAN_EXPRESS:
                return R.drawable.amex;

            case DISCOVER:
                return R.drawable.discover;

            case VERVE:
                return R.drawable.verve_logo;

            case AUTO:
                return findCardType();

            default:
                throw new UnsupportedOperationException("CardType: " + mType + "  is not supported" +
                        ". Use `CardType.*` or `CardType.AUTO` if unknown");
        }

    }

    /**
     * Returns the formatted card number based on the user entered value for card number format
     *
     * @param cardNumber Card Number.
     */
    private String getFormattedCardNumber(String cardNumber) {

        if (DEBUG) {
            Log.e("Card Number", cardNumber);
        }

        switch (getCardNumberFormat()) {
            case MASKED_ALL_BUT_LAST_FOUR:
                cardNumber = "* * * *       * * * *       * * * *       " + cardNumber.substring(cardNumber.length() - 7);
                break;
            case ONLY_LAST_FOUR:
                cardNumber = cardNumber.substring(cardNumber.length() - 7);
                break;
            case MASKED_ALL:
                cardNumber = "* * * *       * * * *       * * * *       * * * *";
                break;
            case ALL_DIGITS:
                //do nothing.
                break;
            default:
                throw new UnsupportedOperationException("CreditCardFormat: " + mCardNumberFormat +
                        " is not supported. Use `CreditCardFormat.*`");
        }
        return cardNumber;
    }

    /**
     * Returns the appropriate card type drawable resource based on the regex pattern of the card
     * number
     */
    @DrawableRes
    private int findCardType() {
        this.mType = VISA;
        if (mCardNumber.length() > 0) {
            final String cardNumber = mCardNumber.replaceAll("\\s+", "");

            if (Pattern.compile(PATTERN_MASTER_CARD).matcher(cardNumber).matches()) {
                this.mType = MASTERCARD;
            } else if (Pattern.compile(PATTERN_AMERICAN_EXPRESS).matcher(cardNumber).matches()) {
                this.mType = AMERICAN_EXPRESS;
            } else if (Pattern.compile(PATTERN_DISCOVER).matcher(cardNumber).matches()) {
                this.mType = DISCOVER;
            } else if (Pattern.compile(PATTERN_VERVE).matcher(cardNumber).matches()) {
                this.mType = VERVE;
            }
            else if (Pattern.compile(PATTERN_VISA).matcher(cardNumber).matches()){
                this.mType = VISA;
            }
        }

        return getLogo();
    }

    /**
     * Adds space after every 4 characters to the card number if the card number is divisible by 4
     */
    private String addSpaceToCardNumber() {

        StringBuilder builder = new StringBuilder();
        for (int j = 0; j < mCardNumber.length(); j++){
            switch (j){
                case 4:
                    builder.append("      ");
                    break;
                case 8:
                    builder.append("      ");
                    break;
                case 12:
                    builder.append("      ");
                    break;
            }
            builder.append(mCardNumber.charAt(j) + " ");
        }

        //mCardNumber = builder.toString().trim();

//        final int splitBy = 7;
//        final int length = mCardNumber.length();
//
//        if (length == 49){
//            return mCardNumber;
//        }
//        else if (length % splitBy != 0 || length <= splitBy) {
//            return mCardNumber;
//        } else {
//            final StringBuilder result = new StringBuilder();
//            result.append(mCardNumber.substring(0, splitBy));
//            for (int i = splitBy; i < length; i++) {
//                if (i % splitBy == 0) {
//                    result.append("       ");
//                }
//                result.append(mCardNumber.charAt(i));
//            }
//            return result.toString();
//        }
        return builder.toString().trim();
    }

    @TargetApi(11)
    private void rotateInToBack() {
        AnimatorSet set = new AnimatorSet();
        final ObjectAnimator rotateIn = ObjectAnimator.ofFloat(this, "rotationY", 0, 90);
        final ObjectAnimator hideFrontView = ObjectAnimator.ofFloat(this, "alpha", 1, 0);
        rotateIn.setInterpolator(new AccelerateDecelerateInterpolator());
        rotateIn.setDuration(300);
        hideFrontView.setDuration(1);
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                rotateOutToBack();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        set.play(hideFrontView).after(rotateIn);
        set.start();
    }

    @TargetApi(11)
    private void rotateInToFront() {
        AnimatorSet set = new AnimatorSet();
        final ObjectAnimator rotateIn = ObjectAnimator.ofFloat(this, "rotationY", 0, 90);
        final ObjectAnimator hideBackView = ObjectAnimator.ofFloat(this, "alpha", 1, 0);
        rotateIn.setDuration(300);
        hideBackView.setDuration(1);
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                rotateOutToFront();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        set.play(hideBackView).after(rotateIn);
        set.start();
    }

    @TargetApi(11)
    private void rotateOutToBack() {
        hideFrontView();
        showBackView();
        CreditCardView.this.setRotationY(-90);
        setBackgroundResource(mCardBackBackground);
        AnimatorSet set = new AnimatorSet();
        final ObjectAnimator flipView = ObjectAnimator.ofInt(CreditCardView.this, "rotationY", 90, -90);
        final ObjectAnimator rotateOut = ObjectAnimator.ofFloat(CreditCardView.this, "rotationY", -90, 0);
        final ObjectAnimator showBackView = ObjectAnimator.ofFloat(CreditCardView.this, "alpha", 0, 1);
        flipView.setDuration(0);
        showBackView.setDuration(1);
        rotateOut.setDuration(300);
        showBackView.setStartDelay(150);
        rotateOut.setInterpolator(new AccelerateDecelerateInterpolator());
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                //Do nothing
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                cardSide = CARD_BACK;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                //Do nothing
            }
        });
        set.play(flipView).with(showBackView).before(rotateOut);
        set.start();
    }

    @TargetApi(11)
    private void rotateOutToFront() {
        showFrontView();
        hideBackView();
        CreditCardView.this.setRotationY(-90);
        setBackgroundResource(R.drawable.card);
        AnimatorSet set = new AnimatorSet();
        final ObjectAnimator flipView = ObjectAnimator.ofInt(CreditCardView.this, "rotationY", 90, -90);
        final ObjectAnimator rotateOut = ObjectAnimator.ofFloat(CreditCardView.this, "rotationY", -90, 0);
        final ObjectAnimator showFrontView = ObjectAnimator.ofFloat(CreditCardView.this, "alpha", 0, 1);
        showFrontView.setDuration(1);
        rotateOut.setDuration(300);
        showFrontView.setStartDelay(150);
        rotateOut.setInterpolator(new AccelerateDecelerateInterpolator());
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                //Do nothing
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                cardSide = CARD_FRONT;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                //Do nothing
            }
        });
        set.play(flipView).with(showFrontView).before(rotateOut);
        set.start();
    }


    @IntDef({VISA, MASTERCARD, AMERICAN_EXPRESS, DISCOVER, VERVE, AUTO})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CreditCardType {
    }

    @IntDef({ALL_DIGITS, MASKED_ALL_BUT_LAST_FOUR, ONLY_LAST_FOUR, MASKED_ALL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CreditCardFormat {
    }
}
