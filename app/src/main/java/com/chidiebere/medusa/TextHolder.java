package com.chidiebere.medusa;

import android.support.annotation.NonNull;

class TextHolder implements Comparable<TextHolder>{ //
    private float Y;
    private String text;
    TextHolder(float Y, String text) {
        this.Y=Y;
        this.text = text;
    }

    String getText() {
        return text;
    }

    float getY() {
        return Y;
    }  public

    int compareTo(@NonNull TextHolder th) {
        return Float.compare(Y, th.getY());
    }

}