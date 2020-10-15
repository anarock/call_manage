package com.anarock.calls.views;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

public class AnarockMediumTextView extends AppCompatTextView {

    private static final String FONT = "fonts/anarock-medium.ttf";

    public AnarockMediumTextView(Context context) {
        this(context, null, 0);
    }

    public AnarockMediumTextView(Context context,
                                 @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnarockMediumTextView(Context context,
                                 @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        Typeface type = Typeface.createFromAsset(getContext().getAssets(), FONT);
        setTypeface(type);
    }
}
