package com.example.currency_converter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;

import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Clears focus from EditText inputs when the user touches outside of them
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }
    //Initialization of variables
    TextView firstCurrencyPicker;
    EditText firstCurrencyValue;
    Currency firstPickedCurrency;
    TextView secondCurrencyPicker;
    EditText secondCurrencyValue;
    Currency secondPickedCurrency;
    TextView lastClickedCurrencyPicker;
    JSONArray exchangeRateTable;
    List<Currency> currencyList = new ArrayList<>();
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Assign references to the corresponding fields for further use and manipulation
        firstCurrencyPicker = findViewById(R.id.firstCurrencyPicker);
        firstCurrencyValue = findViewById(R.id.firstCurrencyValue);
        secondCurrencyPicker = findViewById(R.id.secondCurrencyPicker);
        secondCurrencyValue = findViewById(R.id.secondCurrencyValue);
        secondCurrencyValue.setFocusable(false);
        secondCurrencyValue.setClickable(false);
        secondCurrencyValue.setCursorVisible(false);

        //Sends a request to the NBP API
        sendRequestNBP();

        firstCurrencyValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                calculate();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }
    // Inflates the menu resource file and adds it to the ActionBar
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.converter_menu, menu);
        return true;
    }
    // Performs specific actions based on the selected menu item.
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        // Handle item selection
        switch (item.getItemId()){
            case R.id.action_info:
                Intent i=new Intent(this, InfoActivity.class);
                startActivity(i);
                return true;
            case R.id.action_exit:
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void sendRequestNBP() {
        String URL = "https://api.nbp.pl/api/exchangerates/tables/A/?format=json";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, response -> {
            try {
                // Assigns the retrieved table data to a variable for further use and manipulation
                exchangeRateTable = new JSONArray(response);
                // Retrieves the first object from the array
                JSONObject tableObject = exchangeRateTable.getJSONObject(0);
                // Retrieves the "rates" table from the tableObject
                JSONArray ratesArray = tableObject.getJSONArray("rates");
                // Iterates through the elements in the "rates" collection
                for (int i = 0; i < ratesArray.length(); i++) {
                    JSONObject rateObject = ratesArray.getJSONObject(i);
                    // Retrieves currency data from the rate object
                    //String currencyName = rateObject.getString("currency");
                    String currencyCode = rateObject.getString("code");
                    double currencyMid = rateObject.getDouble("mid");
                    // Retrieves the ID of the flag assigned to the currency
                    @SuppressLint("DiscouragedApi") int flagDrawableId = getResources().getIdentifier(currencyCode.toLowerCase(), "drawable", getPackageName());
                    // Creates a currency object
                    Currency itemCurrency = new Currency(currencyCode, flagDrawableId, currencyMid);
                    // Adds the itemCurrency to the currencyList
                    currencyList.add(itemCurrency);
                    // Assigns default selected currency (USD) to variable
                    if (currencyCode.equals("USD")){
                        firstPickedCurrency = itemCurrency;
                    }
                }
                // Adds PLN currency to the currencyList
                Currency PLN = new Currency("PLN", R.drawable.pln, 1);
                currencyList.add(PLN);
                // Assigns default selected currency (PLN) to variable
                secondPickedCurrency = PLN;
                // Initializes the adapter for the currency selection
                CurrencyAdapter adapter = new CurrencyAdapter(MainActivity.this, currencyList);
                // Sets click listeners for both currency pickers
                firstCurrencyPicker.setOnClickListener(v -> {
                    // Updates the helper variable to store the reference of the last clicked currency picker
                    lastClickedCurrencyPicker = (TextView) v;
                    // Displays the currency picker dialog using the provided adapter
                    showCurrencyPickerDialog(adapter);
                });
                secondCurrencyPicker.setOnClickListener(v -> {
                    // Updates the helper variable to store the reference of the last clicked currency picker
                    lastClickedCurrencyPicker = (TextView) v;
                    // Displays the currency picker dialog using the provided adapter
                    showCurrencyPickerDialog(adapter);
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Toast.makeText(getApplicationContext(),error.toString().trim(),Toast.LENGTH_SHORT).show());
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }
        private void showCurrencyPickerDialog(CurrencyAdapter adapter) {
                // Initializes the currency picker dialog
                dialog = new Dialog(MainActivity.this);
                // Sets the custom layout for the dialog
                dialog.setContentView(R.layout.dialog_searchable_spinner);
                // Sets custom height and width for the dialog window
                dialog.getWindow().setLayout(650, 800);
                // Sets a transparent background for the dialog window
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                // Displays the dialog on the screen
                dialog.show();

                // Initialize and assign variables for the search EditText and currency ListView
                EditText searchEditText = dialog.findViewById(R.id.searchEditText);
                ListView currencyListView = dialog.findViewById(R.id.currencyListView);

                // Sets the adapter for the currency ListView
                currencyListView.setAdapter(adapter);

                searchEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        // Apply the filter to the adapter based on the search query
                        adapter.getFilter().filter(s);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                // Sets item click listener for the currency ListView
                currencyListView.setOnItemClickListener((parent, view, position, id) -> {
                    // Retrieve the selected currency object from the ListView
                    Currency currency = (Currency) parent.getItemAtPosition(position);
                    // Assigns picked currency to variable
                    if (lastClickedCurrencyPicker == firstCurrencyPicker){
                        firstPickedCurrency = currency;
                    }else {
                        secondPickedCurrency = currency;
                    }
                    // Updates UI with selected currency information
                    lastClickedCurrencyPicker.setText(currency.getCurrencyCode());
                    int blackColor = ContextCompat.getColor(this, R.color.black);
                    lastClickedCurrencyPicker.setTextColor(blackColor);
                    // Gets the flag drawable from currency object
                    Drawable flagDrawable = ResourcesCompat.getDrawable(getResources(), currency.getCurrencyFlagDrawableId(), null);
                    if (flagDrawable != null) {
                        // Sets the bounds of the flag drawable
                        flagDrawable.setBounds(0, 0, flagDrawable.getIntrinsicWidth(), flagDrawable.getIntrinsicHeight());
                        // Gets the arrow drawable from resources
                        Drawable arrowDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.baseline_keyboard_arrow_down_24, null);
                        // Sets the flag drawable and arrow drawable to the left and right of the last clicked currency picker
                        lastClickedCurrencyPicker.setCompoundDrawablesWithIntrinsicBounds(flagDrawable, null, arrowDrawable, null);
                    }
                    // Sets the typeface of the last clicked currency picker to bold
                    lastClickedCurrencyPicker.setTypeface(null, Typeface.BOLD);

                    // Dismiss the dialog
                    dialog.dismiss();
                    calculate();
                });
                dialog.setOnDismissListener(dialogInterface -> adapter.getFilter().filter(""));
            }

            public void calculate (){
                if(firstPickedCurrency != null && secondPickedCurrency != null && firstCurrencyValue.getText().toString().isEmpty() == false) {
                    double result = Double.valueOf(firstCurrencyValue.getText().toString())*Double.valueOf(firstPickedCurrency.getCurrencyMid())/Double.valueOf(secondPickedCurrency.getCurrencyMid());
                    String resultString = String.valueOf(Math.round(result * 100.0) / 100.0);
                    secondCurrencyValue.setText(resultString);
                }else {
                    secondCurrencyValue.setText(null);
                }
            }
    }
