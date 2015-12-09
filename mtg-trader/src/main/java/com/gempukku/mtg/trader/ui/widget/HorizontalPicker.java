package com.gempukku.mtg.trader.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.gempukku.mtg.trader.R;

public class HorizontalPicker extends LinearLayout {
    private static final int DEFAULT_LAYOUT_RESOURCE_ID = R.layout.horizontal_picker_widget;

    private static final Formatter DEFAULT_FORMATTER = new DefaultFormatter();

    private int _minValue = Integer.MIN_VALUE;
    private int _maxValue = Integer.MAX_VALUE;
    private int _value;
    private Formatter _formatter;
    private OnValueChangeListener _onValueChangeListener;

    private TextView _textView;

    public HorizontalPicker(Context context) {
        this(context, null);
    }

    public HorizontalPicker(Context context, AttributeSet attrs) {
        this(context, attrs, R.styleable.HorizontalPicker_pickerStyle);
    }

    public HorizontalPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HorizontalPicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        setOrientation(LinearLayout.HORIZONTAL);

        // process style attributes
        final TypedArray attributesArray = context.obtainStyledAttributes(
                attrs, R.styleable.HorizontalPicker, defStyleAttr, 0);

        final int layoutResId = attributesArray.getResourceId(
                R.styleable.HorizontalPicker_internalLayout, DEFAULT_LAYOUT_RESOURCE_ID);

        LayoutInflater vi = LayoutInflater.from(getContext());

        if (getBackground() == null) {
            setBackgroundDrawable(getResources().getDrawable(R.drawable.horizontal_picker_shape));
        }

        vi.inflate(layoutResId, this, true);

        View leftButton = findViewById(R.id.leftButton);
        leftButton.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int oldValue = _value;
                        if (_value == _minValue) {
                            _value = _maxValue;
                        } else {
                            _value--;
                        }
                        updateText();
                        notifyListener(oldValue, _value);
                    }
                });

        _textView = (TextView) findViewById(R.id.display);
        _textView.setText(String.valueOf(0));

        int dimension = attributesArray.getDimensionPixelSize(R.styleable.HorizontalPicker_displayWidth, 0);
        _textView.setWidth(dimension);

        View rightButton = findViewById(R.id.rightButton);
        rightButton.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int oldValue = _value;
                        if (_value == _maxValue) {
                            _value = _minValue;
                        } else {
                            _value++;
                        }
                        updateText();
                        notifyListener(oldValue, _value);
                    }
                });
    }

    public void setMaxValue(int maxValue) {
        if (maxValue < _minValue)
            throw new IllegalArgumentException("maxValue is less than minValue");
        _maxValue = maxValue;
        if (_value > _maxValue)
            setValue(_maxValue);
    }

    public void setMinValue(int minValue) {
        if (minValue > _maxValue)
            throw new IllegalArgumentException("minValue is more than maxValue");
        _minValue = minValue;
        if (_value < _minValue)
            setValue(_minValue);
    }

    public void setValue(int value) {
        if (value < _minValue || value > _maxValue)
            throw new IllegalArgumentException("value less than minValue or greater than maxValue");
        _value = value;
        updateText();
    }

    public void setFormatter(Formatter formatter) {
        _formatter = formatter;
        updateText();
    }

    private void updateText() {
        String displayStr;
        if (_formatter != null) {
            displayStr = _formatter.format(_value);
        } else {
            displayStr = DEFAULT_FORMATTER.format(_value);
        }
        _textView.setText(displayStr);
    }

    public void setOnValueChangedListener(OnValueChangeListener onValueChangeListener) {
        _onValueChangeListener = onValueChangeListener;
    }

    private void notifyListener(int oldValue, int newValue) {
        if (_onValueChangeListener != null) {
            _onValueChangeListener.onValueChange(this, oldValue, newValue);
        }
    }

    public interface Formatter {
        String format(int value);
    }

    private static class DefaultFormatter implements Formatter {
        @Override
        public String format(int value) {
            return String.valueOf(value);
        }
    }

    public interface OnValueChangeListener {
        void onValueChange(HorizontalPicker picker, int oldVal, int newVal);
    }
}
