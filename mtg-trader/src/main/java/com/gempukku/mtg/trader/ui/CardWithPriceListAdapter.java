package com.gempukku.mtg.trader.ui;

import android.content.Context;
import android.content.Intent;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import com.gempukku.mtg.trader.CardDetails;
import com.gempukku.mtg.trader.MtgTraderApplication;
import com.gempukku.mtg.trader.R;
import com.gempukku.mtg.trader.dao.CardInfo;
import com.gempukku.mtg.trader.dao.CardValueMultiplier;
import com.gempukku.mtg.trader.dao.CardWithCountAndMultiplier;
import com.gempukku.mtg.trader.ui.widget.HorizontalPicker;

import java.util.ArrayList;
import java.util.List;

public class CardWithPriceListAdapter extends ArrayAdapter<CardWithCountAndMultiplier> {
    private static final String[] DISPLAYED_MULTIPLIER_VALUES;

    static {
        List<String> displayedMultiplierValues = new ArrayList<String>(CardValueMultiplier.MULTIPLIERS.size());
        for (CardValueMultiplier multiplier : CardValueMultiplier.MULTIPLIERS) {
            displayedMultiplierValues.add(multiplier.getDisplayValue());
        }

        DISPLAYED_MULTIPLIER_VALUES = displayedMultiplierValues.toArray(new String[displayedMultiplierValues.size()]);
    }

    private TradeModifierCallback _tradeModifierCallback;

    public CardWithPriceListAdapter(TradeModifierCallback tradeModifierCallback,
                                    Context context, int layout) {
        super(context, layout);
        _tradeModifierCallback = tradeModifierCallback;
    }

    public void addCard(CardInfo cardInfo, int count, float multiplier) {
        if (CardInfo.isCash(cardInfo)) {
            for (int i = 0; i < getCount(); i++) {
                CardWithCountAndMultiplier item = getItem(i);
                if (CardInfo.isCash(item.getCardInfo())) {
                    remove(item);
                    break;
                }
            }
        }
        add(new CardWithCountAndMultiplier(cardInfo, count, multiplier));
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.trade_list_layout, null);

            final ViewSwitcher viewSwitcher = (ViewSwitcher) v.findViewById(R.id.switcher);
            viewSwitcher.setTag(position);
            final GestureDetector gestureDetector = new GestureDetector(getContext(), new MyGestureListener(viewSwitcher));

            final View priceView = v.findViewById(R.id.priceView);
            priceView.setOnTouchListener(
                    new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            return gestureDetector.onTouchEvent(event);
                        }
                    });

            View okButton = v.findViewById(R.id.done);
            okButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            viewSwitcher.showPrevious();
                        }
                    });

            View deleteButton = v.findViewById(R.id.delete);
            deleteButton.setTag(position);
            deleteButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int position = (Integer) v.getTag();
                            remove(getItem(position));
                            _tradeModifierCallback.tradeChanged();
                        }
                    });

            HorizontalPicker countPicker = (HorizontalPicker) v.findViewById(R.id.count);
            countPicker.setTag(position);
            final View finalV = v;
            countPicker.setOnValueChangedListener(
                    new HorizontalPicker.OnValueChangeListener() {
                        @Override
                        public void onValueChange(HorizontalPicker picker, int oldVal, int newVal) {
                            CardWithCountAndMultiplier cardWithCountAndMultiplier = getItem((Integer) picker.getTag());
                            cardWithCountAndMultiplier.setCount(newVal);
                            _tradeModifierCallback.tradeChanged();
                            updateText(finalV, cardWithCountAndMultiplier);
                        }
                    }
            );

            countPicker.setValue(1);
            countPicker.setMinValue(1);
            countPicker.setMaxValue(100);

            HorizontalPicker multiplierPicker = (HorizontalPicker) v.findViewById(R.id.multiplierPicker);
            multiplierPicker.setTag(position);
            multiplierPicker.setOnValueChangedListener(
                    new HorizontalPicker.OnValueChangeListener() {
                        @Override
                        public void onValueChange(HorizontalPicker picker, int oldVal, int newVal) {
                            CardWithCountAndMultiplier cardWithCountAndMultiplier = getItem((Integer) picker.getTag());
                            cardWithCountAndMultiplier.setMultiplier(CardValueMultiplier.MULTIPLIERS.get(newVal).getMultiplier());
                            _tradeModifierCallback.tradeChanged();
                            updateText(finalV, cardWithCountAndMultiplier);
                        }
                    }
            );

            multiplierPicker.setFormatter(
                    new HorizontalPicker.Formatter() {
                        @Override
                        public String format(int value) {
                            return DISPLAYED_MULTIPLIER_VALUES[value];
                        }
                    });
            multiplierPicker.setMinValue(0);
            multiplierPicker.setMaxValue(CardValueMultiplier.MULTIPLIERS.size() - 1);

        } else {
            View countPicker = v.findViewById(R.id.count);
            countPicker.setTag(position);

            View deleteButton = v.findViewById(R.id.delete);
            deleteButton.setTag(position);

            View multiplierPicker = v.findViewById(R.id.multiplierPicker);
            multiplierPicker.setTag(position);

            final ViewSwitcher viewSwitcher = (ViewSwitcher) v.findViewById(R.id.switcher);
            viewSwitcher.setTag(position);
            switchToFirstChild(viewSwitcher);
        }

        final CardWithCountAndMultiplier cardWithPrice = getItem(position);

        if (cardWithPrice != null) {
            updateText(v, cardWithPrice);

            int modifierVisibility = CardInfo.isCash(cardWithPrice.getCardInfo()) ? View.INVISIBLE : View.VISIBLE;

            View countPicker = v.findViewById(R.id.count);
            View multiplierPicker = v.findViewById(R.id.multiplierPicker);
            countPicker.setVisibility(modifierVisibility);
            multiplierPicker.setVisibility(modifierVisibility);
        }

        return v;
    }

    private void switchToFirstChild(ViewSwitcher viewSwitcher) {
        if (viewSwitcher.getDisplayedChild() != 0) {
            Animation inAnimation = viewSwitcher.getInAnimation();
            Animation outAnimation = viewSwitcher.getOutAnimation();

            viewSwitcher.setInAnimation(null);
            viewSwitcher.setOutAnimation(null);
            viewSwitcher.setDisplayedChild(0);

            viewSwitcher.setInAnimation(inAnimation);
            viewSwitcher.setOutAnimation(outAnimation);
        }
    }

    private void updateText(View v, CardWithCountAndMultiplier cardWithCountAndMultiplier) {
        TextView nameText = (TextView) v.findViewById(R.id.name);
        TextView infoText = (TextView) v.findViewById(R.id.info);
        TextView priceText = (TextView) v.findViewById(R.id.price);
        TextView multiplierText = (TextView) v.findViewById(R.id.multiplier);

        HorizontalPicker countPicker = (HorizontalPicker) v.findViewById(R.id.count);
        HorizontalPicker multiplierPicker = (HorizontalPicker) v.findViewById(R.id.multiplierPicker);

        CardInfo cardInfo = cardWithCountAndMultiplier.getCardInfo();

        int cardCount = cardWithCountAndMultiplier.getCount();

        int cardPrice = MtgTraderApplication.evaluatePrice(cardWithCountAndMultiplier);

        nameText.setText(MtgTraderApplication.formatCardCount(cardCount, cardInfo));
        infoText.setText(cardInfo.getVersionInfo());
        priceText.setText(MtgTraderApplication.formatPrice(cardPrice));

        if (CardInfo.isCash(cardInfo)) {
            multiplierText.setText("");
        } else {
            float multiplier = cardWithCountAndMultiplier.getMultiplier();

            multiplierText.setText(CardValueMultiplier.getClosest(multiplier).getDisplayValue());
            multiplierPicker.setValue(CardValueMultiplier.getClosestIndex(multiplier));
        }

        countPicker.setValue(cardCount);

        final View priceView = v.findViewById(R.id.priceView);
        priceView.invalidate();
    }

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        private ViewSwitcher _viewSwitcher;

        public MyGestureListener(ViewSwitcher viewSwitcher) {
            _viewSwitcher = viewSwitcher;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            _viewSwitcher.showNext();
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            CardWithCountAndMultiplier cardWithCountAndMultiplier = getItem((Integer) _viewSwitcher.getTag());
            CardInfo cardInfo = cardWithCountAndMultiplier.getCardInfo();
            if (!CardInfo.isCash(cardInfo)) {
                String cardId = cardInfo.getId();

                Context context = getContext();
                Intent openCardInfo = new Intent(context, CardDetails.class);
                openCardInfo.putExtra(CardDetails.CARD_ID_KEY, cardId);

                context.startActivity(openCardInfo);
            }
            return true;
        }
    }
}
