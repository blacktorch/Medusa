package com.chidiebere.medusa;

import android.text.InputFilter;
import android.text.Spanned;

public class CardNumberInputFilter implements InputFilter {
    private int min;
    private long max;

    public CardNumberInputFilter(int min, long max) {
        this.min = min;
        this.max = max;
    }

    public CardNumberInputFilter(String min, String max) {
        this.min = Integer.parseInt(min);
        this.max = Long.parseLong(max);
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            long input = Long.parseLong(dest.toString() + source.toString());
            if (isInRange(min, max, input))
                return null;
        } catch (NumberFormatException nfe) { }
        return "";
    }

    private boolean isInRange(int a, long b, long c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
    }
}