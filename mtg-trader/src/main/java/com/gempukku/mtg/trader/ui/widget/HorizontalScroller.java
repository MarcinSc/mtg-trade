package com.gempukku.mtg.trader.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;
import com.gempukku.mtg.trader.R;

public class HorizontalScroller extends View {
    private static final float PEEK_MULTIPLIER = 0.7f;

    private int _minValue = 0;
    private int _maxValue = 100;

    private int _value;
    private float _displayWidth;

    private Paint _textPaint;
    private Formatter _formatter;

    private float _cellWidth;
    private float _middleCellPosition;
    private float _leftCellPosition;
    private float _rightCellPosition;
    private float _renderDisplacementInUnitsX;
    private float _verticalTextPosition;

    private float _scrollStartInUnitsX;

    private int _minimumFlingVelocity;
    private int _maximumFlingVelocity;
    private VelocityTracker _velocityTracker;
    private ScrollMode _scrollMode = ScrollMode.NONE;
    private Scroller _flingScroller;
    private Scroller _adjustScroller;

    private OnValueChangeListener _onValueChangeListener;

    private enum ScrollMode {
        NONE, FLING, ADJUST
    }

    public HorizontalScroller(Context context) {
        this(context, null);
    }

    public HorizontalScroller(Context context, AttributeSet attrs) {
        this(context, attrs, R.styleable.HorizontalScroller_scrollerStyle);
    }

    public HorizontalScroller(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HorizontalScroller(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        _minimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
        _maximumFlingVelocity = configuration.getScaledMaximumFlingVelocity();

        _flingScroller = new Scroller(context, null, true);
        _adjustScroller = new Scroller(context, new DecelerateInterpolator());

        if (getBackground() == null) {
            setBackgroundDrawable(getResources().getDrawable(R.drawable.horizontal_scroller_shape));
        }

        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.HorizontalScroller, defStyleAttr, 0);

        _displayWidth = a.getDimension(R.styleable.HorizontalScroller_displayWidth, _displayWidth);
        int color = a.getColor(R.styleable.HorizontalScroller_textColor, Color.BLACK);
        String fontType = a.getString(R.styleable.HorizontalScroller_textFont);
        Typeface typeface;
        if (fontType == null)
            typeface = Typeface.defaultFromStyle(Typeface.NORMAL);
        else
            typeface = Typeface.create(fontType, Typeface.NORMAL);
        float textSize = a.getDimension(R.styleable.HorizontalScroller_textSize, 12f);

        _textPaint = new Paint(0);
        _textPaint.setColor(color);
        _textPaint.setTextSize(textSize);
        _textPaint.setTypeface(typeface);
        _textPaint.setAntiAlias(true);

        setupVariablesFromPaint();
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
        if (value != _value) {
            if (value < _minValue || value > _maxValue)
                throw new IllegalArgumentException("value less than minValue or greater than maxValue");
            setValueWithNotify(value);
            _renderDisplacementInUnitsX = 0;
            _scrollMode = ScrollMode.NONE;
            _adjustScroller.forceFinished(true);
            _flingScroller.forceFinished(true);
        }
    }

    public void setFormatter(Formatter formatter) {
        _formatter = formatter;
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (_velocityTracker == null) {
            _velocityTracker = VelocityTracker.obtain();
        }
        _velocityTracker.addMovement(event);

        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP:
                upEvent(event);
                break;
            case MotionEvent.ACTION_MOVE:
                moveEvent(event);
                break;
            case MotionEvent.ACTION_DOWN:
                downEvent(event);
                break;
        }

        return true;
    }

    private float _lastDownX;

    private void downEvent(MotionEvent event) {
        _lastDownX = event.getX();
        _scrollStartInUnitsX = _value + _renderDisplacementInUnitsX;
        _scrollMode = ScrollMode.NONE;
        _flingScroller.forceFinished(true);
        _adjustScroller.forceFinished(true);
    }

    private void upEvent(MotionEvent event) {
        _velocityTracker.computeCurrentVelocity(1000, _maximumFlingVelocity);
        int xVelocity = Math.round(_velocityTracker.getXVelocity());
        if (Math.abs(xVelocity) >= _minimumFlingVelocity) {
            if (_scrollMode == ScrollMode.NONE) {
                _scrollStartInUnitsX = _value + _renderDisplacementInUnitsX;
            }
            _scrollMode = ScrollMode.FLING;
            if (xVelocity > 0) {
                _flingScroller.fling(0, 0, xVelocity, 0, 0, Integer.MAX_VALUE, 0, 0);
            } else {
                _flingScroller.fling(0, 0, xVelocity, 0, Integer.MIN_VALUE, 0, 0, 0);
            }
            Log.d("Scrolling", "Started flinging");
            invalidate();
        } else {
            startAdjusting();
        }
        _velocityTracker.recycle();
        _velocityTracker = null;
    }

    private void startAdjusting() {
        _scrollStartInUnitsX = _value + _renderDisplacementInUnitsX;

        _scrollMode = ScrollMode.ADJUST;
        _adjustScroller.startScroll(0, 0, Math.round(_renderDisplacementInUnitsX * _cellWidth), 0, 800);
        Log.d("Scrolling", "Started adjusting");
        invalidate();
    }

    private void moveEvent(MotionEvent event) {
        float diff = _lastDownX - event.getX();
        setDisplacement(_scrollStartInUnitsX + diff / _cellWidth);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY) {
            // Parent has told us how big to be. So be it.
            width = widthSize;
        } else {
            width = Math.round((1 + 2 * PEEK_MULTIPLIER) * _displayWidth);

            if (widthMode == MeasureSpec.AT_MOST) {
                width = Math.min(width, widthSize);
            }
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            // Parent has told us how big to be. So be it.
            height = heightSize;
        } else {
            Paint.FontMetrics fontMetrics = _textPaint.getFontMetrics();
            height = Math.round(fontMetrics.descent - fontMetrics.ascent);

            if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(height, heightSize);
            }
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        _cellWidth = w / (1 + 2 * PEEK_MULTIPLIER);

        _middleCellPosition = w / 2;
        _leftCellPosition = _middleCellPosition - _cellWidth;
        _rightCellPosition = _middleCellPosition + _cellWidth;

        _verticalTextPosition = (h - _textHeight) - _textAscent;
    }

    private float _textHeight;
    private float _textAscent;

    private void setupVariablesFromPaint() {
        Paint.FontMetrics fontMetrics = _textPaint.getFontMetrics();
        _textHeight = fontMetrics.descent - fontMetrics.ascent;
        _textAscent = fontMetrics.ascent;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawValue(canvas, _verticalTextPosition, _middleCellPosition, getDisplayText(_value));
        drawValue(canvas, _verticalTextPosition, _leftCellPosition, getPreviousDisplayText(_value));
        drawValue(canvas, _verticalTextPosition, _rightCellPosition, getNextDisplayText(_value));
    }

    @Override
    public void computeScroll() {
        Scroller scroller = _flingScroller;
        if (scroller.isFinished()) {
            scroller = _adjustScroller;
            if (scroller.isFinished()) {
                return;
            }
        }

        scroller.computeScrollOffset();

        int currX = scroller.getCurrX();
        setDisplacement(_scrollStartInUnitsX - currX / _cellWidth);

        if (scroller.isFinished()) {
            if (scroller == _flingScroller) {
                Log.d("Scrolling", "Finished flinging");
            } else {
                Log.d("Scrolling", "Finished adjusting");
            }
            _scrollMode = ScrollMode.NONE;
            if (scroller == _flingScroller) {
                startAdjusting();
            }
        } else {
            invalidate();
        }
    }

    private void drawValue(Canvas canvas, float verticalTextPosition, float cellPosition, String value) {
        float textWidth = _textPaint.measureText(value);
        canvas.drawText(value, -_renderDisplacementInUnitsX * _cellWidth + cellPosition - textWidth / 2, verticalTextPosition, _textPaint);
    }

    private String getPreviousDisplayText(int value) {
        return getDisplayText(getPreviousValue(value));
    }

    private int getPreviousValue(int value) {
        if (value == _minValue)
            return _maxValue;
        else
            return value - 1;
    }

    private String getNextDisplayText(int value) {
        return getDisplayText(getNextValue(value));
    }

    private int getNextValue(int value) {
        if (value == _maxValue)
            return _minValue;
        else
            return value + 1;
    }

    private String getDisplayText(int value) {
        if (_formatter == null)
            return String.valueOf(value);
        else
            return _formatter.format(value);
    }

    private void setDisplacement(float desiredValue) {
        _renderDisplacementInUnitsX = desiredValue - _value;
        int finalValue = _value;
        while (_renderDisplacementInUnitsX > 0.5) {
            finalValue = getNextValue(finalValue);
            _renderDisplacementInUnitsX -= 1;
        }
        while (_renderDisplacementInUnitsX < -0.5) {
            finalValue = getPreviousValue(finalValue);
            _renderDisplacementInUnitsX += 1;
        }
        if (finalValue != _value) {
            setValueWithNotify(finalValue);
        }
        invalidate();
    }

    private void setValueWithNotify(int newValue) {
        int oldValue = _value;
        _value = newValue;
        if (_onValueChangeListener != null) {
            _onValueChangeListener.onValueChange(this, oldValue, newValue);
        }
    }

    public void setOnValueChangedListener(OnValueChangeListener onValueChangeListener) {
        _onValueChangeListener = onValueChangeListener;
    }

    public interface Formatter {
        String format(int value);
    }

    public interface OnValueChangeListener {
        void onValueChange(HorizontalScroller picker, int oldVal, int newVal);
    }
}
