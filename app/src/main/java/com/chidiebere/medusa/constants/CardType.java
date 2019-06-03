package com.chidiebere.medusa.constants;

public class CardType {

    public static final int VISA = 0;

    public static final int MASTERCARD = 1;

    public static final int AMERICAN_EXPRESS = 2;

    public static final int DISCOVER = 3;

    public static final int VERVE = 4;

    public static final int AUTO = 5;

    public static final String PATTERN_VISA = "^4[0-9]{12}(?:[0-9]{3})?$^5[1-5][0-9]{14}$";

    public static final String PATTERN_MASTER_CARD = "^5[1-5][0-9]{14}$";

    public static final String PATTERN_AMERICAN_EXPRESS = "^3[47][0-9]{13}$";

    public static final String PATTERN_VERVE = "^50610[0-9]{14}$";

    //@formatter:off
    public static final String PATTERN_DISCOVER = "^65[4-9][0-9]{13}|64[4-9][0-9]{13}|6011[0-9]{12}|(622(?:12[6-9]|1[3-9][0-9]|[2-8][0-9][0-9]|9[01][0-9]|92[0-5])[0-9]{10})$";
    //@formatter:on

}