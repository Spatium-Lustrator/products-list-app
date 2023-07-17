package com.tapps.productlist;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;

import java.util.ArrayList;
import java.util.Arrays;

public class OftenProductsActivity extends AppCompatActivity implements onCheckBoxClick {

    /** Views **/
    RecyclerView rvOftenProducts;
    EditText editTextProductName;

    Button btnAdd;

    /** Variables **/
    ArrayList<String> listOftenProducts = new ArrayList<String>();

    RecyclerViewAdapter recyclerViewAdapter;

    Context context = this;

    private SharedPreferences settings;

    private ArrayList<String> nowSelectedItems = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_often_products);

        editTextProductName = findViewById(R.id.etOftenProductName);
        btnAdd = findViewById(R.id.btnAddOftenProduct);
        rvOftenProducts = findViewById(R.id.rvOftenProducts);

        recyclerViewAdapter = new RecyclerViewAdapter(this, listOftenProducts, this);

        /** Ini functions **/
        rvOftenProducts.setLayoutManager(new LinearLayoutManager(context));
        rvOftenProducts.setAdapter(recyclerViewAdapter);

        /** Shared preferences **/
        settings = getSharedPreferences(Consts.APP_PREFERENCES, Context.MODE_PRIVATE);
        String[] list = settings.getString(Consts.APP_PREFERENCES_OFTEN_PRODUCTS, "").split(";");
        listOftenProducts.addAll(Arrays.asList(list));
        listOftenProducts.remove("");
        recyclerViewAdapter.notifyDataSetChanged();

    }

    /** OnClicks **/
    public void onClickDeleteOftenProducts(View view) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (Integer index = 0; index < nowSelectedItems.size(); index++) {
                    listOftenProducts.remove(nowSelectedItems.get(index));
                }
                recyclerViewAdapter.notifyDataSetChanged();
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(Consts.APP_PREFERENCES_OFTEN_PRODUCTS, String.join(";", listOftenProducts));
                editor.apply();
            }
        });

    }
    public void onClickAddOftenProduct(View view) {
        String oftenProduct = String.valueOf(editTextProductName.getText());
        if (!TextUtils.isEmpty(oftenProduct)) {
            listOftenProducts.add(oftenProduct);
            recyclerViewAdapter.notifyDataSetChanged();
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(Consts.APP_PREFERENCES_OFTEN_PRODUCTS, String.join(";", listOftenProducts));
            editor.apply();
        }

    }
    public void onClickBackToMainPageMenu(View view) {

        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.inflate(R.menu.popup_menu_back_to_main);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (getString(R.string.backToMainPage).equals(String.valueOf(menuItem))) {
                    onBackPressed();
                }
                return false;
            }
        });

        popupMenu.show();

    }

    /** Implementations **/
    @Override
    public void onClick(ArrayList<String> selectedItems) {
        Log.i("DEVELOP-DELETING", "OFTEN PRODUCTS ACTIVITY: " + String.valueOf(selectedItems));
        nowSelectedItems = selectedItems;
    }



}