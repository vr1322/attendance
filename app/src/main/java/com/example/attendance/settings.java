package com.example.attendance;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class settings extends AppCompatActivity {
    private ImageView back;
    private TextView sp_text, wh_text, sc_text, df_text;
    private Button ap_bt,sp_bt, wh_bt, sc_bt, df_bt, cd_bt;
    private SharedPreferences sharedPreferences;
    private List<String[]> currencyList; // Stores country & currency
    private List<String> displayList; // For filtered currency display

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        back = findViewById(R.id.back);
        ap_bt = findViewById(R.id.ap_bt);
        sp_bt = findViewById(R.id.sp_bt);
        wh_bt = findViewById(R.id.wh_bt);
        sc_bt = findViewById(R.id.sc_bt);
        df_bt = findViewById(R.id.df_bt);
        cd_bt = findViewById(R.id.cd_bt);

        sp_text = findViewById(R.id.sp_text);
        wh_text = findViewById(R.id.wh_text);
        sc_text = findViewById(R.id.sc_text);
        df_text = findViewById(R.id.df_text);

        sharedPreferences = getSharedPreferences("SettingsPrefs", MODE_PRIVATE);
        loadSavedValues();

        back.setOnClickListener(v -> startActivity(new Intent(settings.this, home.class)));
        ap_bt.setOnClickListener(v -> startActivity(new Intent(settings.this, update_admin.class)));
        sp_bt.setOnClickListener(v -> showDialog("Select Parameter", new String[]{"Parameter 1", "Parameter 2", "Parameter 3"}, sp_text, "parameter"));
        wh_bt.setOnClickListener(v -> showDialog("Select Working Hours", new String[]{"4 hrs", "6 hrs", "8 hrs", "10 hrs"}, wh_text, "working_hours"));
        sc_bt.setOnClickListener(v -> showCurrencyDialog());
        df_bt.setOnClickListener(v -> showDialog("Select Date Format", new String[]{"MM/dd/yyyy", "dd/MM/yyyy", "yyyy-MM-dd"}, df_text, "date_format"));
        cd_bt.setOnClickListener(v -> clearAllSettings());
    }

    private void showDialog(String title, String[] options, TextView textView, String key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setItems(options, (dialog, which) -> {
            String selectedValue = options[which];
            textView.setText(selectedValue);
            savePreference(key, selectedValue);
        });
        builder.show();
    }

    private void showCurrencyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_currency_selector, null);
        builder.setView(view);

        EditText searchBar = view.findViewById(R.id.search_bar);
        ListView listView = view.findViewById(R.id.currency_list); // Ensure this ID matches your XML

        // ✅ Load the currency list before using it
        loadCurrencyList();

        // ✅ Create display list (Country + Currency)
        List<String> displayList = new ArrayList<>();
        for (String[] currency : currencyList) {
            displayList.add(currency[0] + " (" + currency[1] + ")");
        }

        // ✅ Set up the adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayList);
        listView.setAdapter(adapter);

        // ✅ Search functionality
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<String> filtered = new ArrayList<>();
                for (String[] currency : currencyList) {
                    if (currency[0].toLowerCase().contains(s.toString().toLowerCase()) ||
                            currency[1].toLowerCase().contains(s.toString().toLowerCase())) {
                        filtered.add(currency[0] + " (" + currency[1] + ")");
                    }
                }
                listView.setAdapter(new ArrayAdapter<>(settings.this, android.R.layout.simple_list_item_1, filtered));
            }

            @Override public void afterTextChanged(Editable s) {}
        });

        // ✅ Handle item selection
        AlertDialog dialog = builder.create();
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            String selected = displayList.get(position).split(" \\(")[1].replace(")", "");
            sc_text.setText(selected);
            savePreference("currency", selected);
            dialog.dismiss();
        });

        dialog.show();
    }


    private void loadCurrencyList() {
        if (currencyList == null) {
            currencyList = new ArrayList<>();
        } else {
            currencyList.clear(); // Clear existing data
        }

        // ✅ Add all countries with their respective currencies
        currencyList.add(new String[]{"Afghanistan", "AFN"});
        currencyList.add(new String[]{"Albania", "ALL"});
        currencyList.add(new String[]{"Algeria", "DZD"});
        currencyList.add(new String[]{"Andorra", "EUR"});
        currencyList.add(new String[]{"Angola", "AOA"});
        currencyList.add(new String[]{"Argentina", "ARS"});
        currencyList.add(new String[]{"Armenia", "AMD"});
        currencyList.add(new String[]{"Australia", "AUD"});
        currencyList.add(new String[]{"Austria", "EUR"});
        currencyList.add(new String[]{"Azerbaijan", "AZN"});
        currencyList.add(new String[]{"Bahamas", "BSD"});
        currencyList.add(new String[]{"Bahrain", "BHD"});
        currencyList.add(new String[]{"Bangladesh", "BDT"});
        currencyList.add(new String[]{"Barbados", "BBD"});
        currencyList.add(new String[]{"Belarus", "BYN"});
        currencyList.add(new String[]{"Belgium", "EUR"});
        currencyList.add(new String[]{"Belize", "BZD"});
        currencyList.add(new String[]{"Benin", "XOF"});
        currencyList.add(new String[]{"Bhutan", "BTN"});
        currencyList.add(new String[]{"Bolivia", "BOB"});
        currencyList.add(new String[]{"Bosnia and Herzegovina", "BAM"});
        currencyList.add(new String[]{"Botswana", "BWP"});
        currencyList.add(new String[]{"Brazil", "BRL"});
        currencyList.add(new String[]{"Brunei", "BND"});
        currencyList.add(new String[]{"Bulgaria", "BGN"});
        currencyList.add(new String[]{"Burkina Faso", "XOF"});
        currencyList.add(new String[]{"Burundi", "BIF"});
        currencyList.add(new String[]{"Cambodia", "KHR"});
        currencyList.add(new String[]{"Cameroon", "XAF"});
        currencyList.add(new String[]{"Canada", "CAD"});
        currencyList.add(new String[]{"Chad", "XAF"});
        currencyList.add(new String[]{"Chile", "CLP"});
        currencyList.add(new String[]{"China", "CNY"});
        currencyList.add(new String[]{"Colombia", "COP"});
        currencyList.add(new String[]{"Comoros", "KMF"});
        currencyList.add(new String[]{"Congo", "XAF"});
        currencyList.add(new String[]{"Costa Rica", "CRC"});
        currencyList.add(new String[]{"Croatia", "HRK"});
        currencyList.add(new String[]{"Cuba", "CUP"});
        currencyList.add(new String[]{"Cyprus", "EUR"});
        currencyList.add(new String[]{"Czech Republic", "CZK"});
        currencyList.add(new String[]{"Denmark", "DKK"});
        currencyList.add(new String[]{"Djibouti", "DJF"});
        currencyList.add(new String[]{"Dominican Republic", "DOP"});
        currencyList.add(new String[]{"Ecuador", "USD"});
        currencyList.add(new String[]{"Egypt", "EGP"});
        currencyList.add(new String[]{"El Salvador", "USD"});
        currencyList.add(new String[]{"Eritrea", "ERN"});
        currencyList.add(new String[]{"Estonia", "EUR"});
        currencyList.add(new String[]{"Eswatini", "SZL"});
        currencyList.add(new String[]{"Ethiopia", "ETB"});
        currencyList.add(new String[]{"Fiji", "FJD"});
        currencyList.add(new String[]{"Finland", "EUR"});
        currencyList.add(new String[]{"France", "EUR"});
        currencyList.add(new String[]{"Gabon", "XAF"});
        currencyList.add(new String[]{"Gambia", "GMD"});
        currencyList.add(new String[]{"Georgia", "GEL"});
        currencyList.add(new String[]{"Germany", "EUR"});
        currencyList.add(new String[]{"Ghana", "GHS"});
        currencyList.add(new String[]{"Greece", "EUR"});
        currencyList.add(new String[]{"Guatemala", "GTQ"});
        currencyList.add(new String[]{"Guinea", "GNF"});
        currencyList.add(new String[]{"Guyana", "GYD"});
        currencyList.add(new String[]{"Haiti", "HTG"});
        currencyList.add(new String[]{"Honduras", "HNL"});
        currencyList.add(new String[]{"Hungary", "HUF"});
        currencyList.add(new String[]{"Iceland", "ISK"});
        currencyList.add(new String[]{"India", "INR"});
        currencyList.add(new String[]{"Indonesia", "IDR"});
        currencyList.add(new String[]{"Iran", "IRR"});
        currencyList.add(new String[]{"Iraq", "IQD"});
        currencyList.add(new String[]{"Ireland", "EUR"});
        currencyList.add(new String[]{"Israel", "ILS"});
        currencyList.add(new String[]{"Italy", "EUR"});
        currencyList.add(new String[]{"Jamaica", "JMD"});
        currencyList.add(new String[]{"Japan", "JPY"});
        currencyList.add(new String[]{"Jordan", "JOD"});
        currencyList.add(new String[]{"Kazakhstan", "KZT"});
        currencyList.add(new String[]{"Kenya", "KES"});
        currencyList.add(new String[]{"Kuwait", "KWD"});
        currencyList.add(new String[]{"Kyrgyzstan", "KGS"});
        currencyList.add(new String[]{"Laos", "LAK"});
        currencyList.add(new String[]{"Latvia", "EUR"});
        currencyList.add(new String[]{"Lebanon", "LBP"});
        currencyList.add(new String[]{"Libya", "LYD"});
        currencyList.add(new String[]{"Lithuania", "EUR"});
        currencyList.add(new String[]{"Luxembourg", "EUR"});
        currencyList.add(new String[]{"Madagascar", "MGA"});
        currencyList.add(new String[]{"Malaysia", "MYR"});
        currencyList.add(new String[]{"Maldives", "MVR"});
        currencyList.add(new String[]{"Mexico", "MXN"});
        currencyList.add(new String[]{"Moldova", "MDL"});
        currencyList.add(new String[]{"Mongolia", "MNT"});
        currencyList.add(new String[]{"Morocco", "MAD"});
        currencyList.add(new String[]{"Mozambique", "MZN"});
        currencyList.add(new String[]{"Myanmar", "MMK"});
        currencyList.add(new String[]{"Namibia", "NAD"});
        currencyList.add(new String[]{"Nepal", "NPR"});
        currencyList.add(new String[]{"Netherlands", "EUR"});
        currencyList.add(new String[]{"New Zealand", "NZD"});
        currencyList.add(new String[]{"Nicaragua", "NIO"});
        currencyList.add(new String[]{"Nigeria", "NGN"});
        currencyList.add(new String[]{"North Korea", "KPW"});
        currencyList.add(new String[]{"Norway", "NOK"});
        currencyList.add(new String[]{"Oman", "OMR"});
        currencyList.add(new String[]{"Pakistan", "PKR"});
        currencyList.add(new String[]{"Panama", "PAB"});
        currencyList.add(new String[]{"Paraguay", "PYG"});
        currencyList.add(new String[]{"Peru", "PEN"});
        currencyList.add(new String[]{"Philippines", "PHP"});
        currencyList.add(new String[]{"Poland", "PLN"});
        currencyList.add(new String[]{"Portugal", "EUR"});
        currencyList.add(new String[]{"Qatar", "QAR"});
        currencyList.add(new String[]{"Romania", "RON"});
        currencyList.add(new String[]{"Russia", "RUB"});
        currencyList.add(new String[]{"Saudi Arabia", "SAR"});
        currencyList.add(new String[]{"Serbia", "RSD"});
        currencyList.add(new String[]{"Singapore", "SGD"});
        currencyList.add(new String[]{"South Africa", "ZAR"});
        currencyList.add(new String[]{"South Korea", "KRW"});
        currencyList.add(new String[]{"Spain", "EUR"});
        currencyList.add(new String[]{"Sri Lanka", "LKR"});
        currencyList.add(new String[]{"Sweden", "SEK"});
        currencyList.add(new String[]{"Switzerland", "CHF"});
        currencyList.add(new String[]{"Taiwan", "TWD"});
        currencyList.add(new String[]{"Thailand", "THB"});
        currencyList.add(new String[]{"Turkey", "TRY"});
        currencyList.add(new String[]{"Ukraine", "UAH"});
        currencyList.add(new String[]{"United Kingdom", "GBP"});
        currencyList.add(new String[]{"United States", "USD"});
        currencyList.add(new String[]{"Vietnam", "VND"});
    }


    private void savePreference(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }

    private void loadSavedValues() {
        sp_text.setText(sharedPreferences.getString("parameter", "Not Set"));
        wh_text.setText(sharedPreferences.getString("working_hours", "Not Set"));
        sc_text.setText(sharedPreferences.getString("currency", "Not Set"));
        df_text.setText(sharedPreferences.getString("date_format", "Not Set"));
    }

    private void clearAllSettings() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Clear Data");
        builder.setMessage("Are you sure you want to clear all settings?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            loadSavedValues();
        });
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
