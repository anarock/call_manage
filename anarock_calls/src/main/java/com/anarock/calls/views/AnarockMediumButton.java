package com.anarock.calls.views;

import android.content.Context;
import android.graphics.Typeface;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import android.util.AttributeSet;

public class AnarockMediumButton extends AppCompatButton {
    private static final String FONT = "fonts/anarock-medium.ttf";

    public AnarockMediumButton(Context context) {
        this(context, null, 0);
    }

    public AnarockMediumButton(Context context,
                               @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnarockMediumButton(Context context,
                               @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        Typeface type = Typeface.createFromAsset(getContext().getAssets(), FONT);
        setTypeface(type);
    }
}
