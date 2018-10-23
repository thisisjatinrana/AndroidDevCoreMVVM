package rana.jatin.core.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.text.emoji.widget.EmojiTextView;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import rana.jatin.core.R;

public class BasicEmojiTextView extends EmojiTextView {
    private String typeFace = "";

    public BasicEmojiTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
        setTypeFace();
    }

    public BasicEmojiTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
        setTypeFace();
    }

    public BasicEmojiTextView(Context context) {
        super(context);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.BasicEmojiTextView);

        for (int i = 0; i < array.getIndexCount(); ++i) {
            int attr = array.getIndex(i);
            if (attr == R.styleable.BasicEmojiTextView_typeFace)
                typeFace = array.getString(attr);
        }
        array.recycle();
    }

    private void setTypeFace() {
        if (!isInEditMode()) {
            if(!typeFace.isEmpty()) {
                Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
                        "fonts/" + typeFace);
                setTypeface(tf);
            }
        }
    }
}
