package com.slightsite.siskapos.ui.transaction;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.slightsite.siskapos.R;
import com.slightsite.siskapos.domain.CurrencyController;
import com.slightsite.siskapos.domain.inventory.LineItem;
import com.slightsite.siskapos.domain.sale.Register;
import com.slightsite.siskapos.technicalservices.NoDaoSetException;

import java.util.ArrayList;
import java.util.List;

public class CheckoutActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdapterListCartCheckout mAdapter;
    private Register register;
    private View parent_view;
    private EditText colected_cash;
    private TextView cash_change;
    private TextView sub_total;
    private TextView total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_checkout);
        initToolbar();

        try {
            register = Register.getInstance();
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);

        parent_view = findViewById(R.id.parent_view);

        double total_cart = 0;
        if(register.hasSale()){
            if (register.getTotal() > 0) {
                showList(register.getCurrentSale().getAllLineItem());
                total_cart = register.getTotal();
            }
        } else{
            showList(new ArrayList<LineItem>());
        }

        cash_change = (TextView) findViewById(R.id.cash_change);
        sub_total = (TextView) findViewById(R.id.sub_total);
        sub_total.setText(CurrencyController.getInstance().moneyFormat(total_cart));
        total = (TextView) findViewById(R.id.total);
        total.setText(CurrencyController.getInstance().moneyFormat(total_cart));

        colected_cash = (EditText) findViewById(R.id.colected_cash);
        try {
            int i_total_cart = (int) total_cart;
            colected_cash.setHint(i_total_cart);
        } catch (Exception e) {
            e.printStackTrace();
        }

        colected_cash.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                double tot = register.getTotal();
                double col_tot = 0;
                try {
                    col_tot = Double.parseDouble(s.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                double change_val = 0;
                if (col_tot >= tot) {
                    change_val = col_tot - tot;
                }
                Log.e("CheckoutActivity", "change_val : "+ change_val);
                cash_change.setText(CurrencyController.getInstance().moneyFormat(change_val));
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.grey_60), PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Tools.setSystemBarColor(this, R.color.grey_5);
        Tools.setSystemBarLight(this);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CartActivity.class);
                finish();
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_checkout, menu);
        Tools.changeMenuIconColor(menu, getResources().getColor(R.color.grey_60));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.action_home) {
            intent = new Intent(getApplicationContext(), TransactionActivity.class);
            finish();
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showList(List<LineItem> list) {
        //set data and list adapter
        mAdapter = new AdapterListCartCheckout(this, list, register);
        recyclerView.setAdapter(mAdapter);
    }
}

