package com.gempukku.mtg.trader;

import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import com.gempukku.mtg.trader.dao.CardInfo;
import com.gempukku.mtg.trader.service.CardProvider;

public class ChooseCardScreen extends AppCompatActivity {

    public static final int MAXIMUM_CASH_IN_CENTS = 10000 * 100;
    private CardProvider _cardProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_card_screen);

        MtgTraderApplication mtgTraderApplication = (MtgTraderApplication) getApplication();
        _cardProvider = mtgTraderApplication.getCardProvider();

        String[] columnNames = {"cardId", "name", "description", "price"};
        int[] to = {0, R.id.name, R.id.info, R.id.price};

        final SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(this, R.layout.choose_card_list_layout, null, columnNames, to, 0);
        cursorAdapter.setViewBinder(
                new SimpleCursorAdapter.ViewBinder() {
                    @Override
                    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                        if (columnIndex == 2 || columnIndex == 3) {
                            ((TextView) view).setText(cursor.getString(columnIndex));
                        } else if (columnIndex == 4) {
                            ((TextView) view).setText(MtgTraderApplication.formatPrice(cursor.getInt(columnIndex)));
                        }
                        return true;
                    }
                });
        cursorAdapter.setCursorToStringConverter(
                new SimpleCursorAdapter.CursorToStringConverter() {
                    @Override
                    public CharSequence convertToString(Cursor cursor) {
                        return cursor.getString(2);
                    }
                }
        );
        FilterQueryProvider filterProvider = new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                // run in the background thread
                if (constraint == null) {
                    return null;
                }


                final String[] columnNames = {BaseColumns._ID, "cardId", "name", "description", "price"};

                MatrixCursor c = new MatrixCursor(columnNames);
                int index = 0;

                try {
                    float value = Float.parseFloat(constraint.toString());
                    int valueCents = Math.round(value * 100);
                    if (valueCents > 0 && valueCents <= MAXIMUM_CASH_IN_CENTS) {
                        c.newRow().add(index).add(CardInfo.createCashCardId(valueCents)).add("Cash").add("").add(valueCents);
                        index++;
                    }
                } catch (NumberFormatException exp) {

                }

                for (CardInfo cardInfo : _cardProvider.findCards(constraint.toString(), 10)) {
                    String cardId = cardInfo.getId();
                    int price = cardInfo.getPrice();
                    c.newRow().add(index).add(cardId).add(cardInfo.getName()).add(cardInfo.getVersionInfo()).add(price);
                    index++;
                }
                return c;
            }
        };
        cursorAdapter.setFilterQueryProvider(filterProvider);

        ListView listView = (ListView) findViewById(R.id.matchingCards);
        listView.setAdapter(cursorAdapter);

        final EditText cardNameEdit = (EditText) findViewById(R.id.cardName);
        cardNameEdit.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        cursorAdapter.getFilter().filter(cardNameEdit.getText());
                    }
                });

        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent();
                        intent.putExtra("selectedCard", ((Cursor) cursorAdapter.getItem(position)).getString(1));
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_choose_card, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
