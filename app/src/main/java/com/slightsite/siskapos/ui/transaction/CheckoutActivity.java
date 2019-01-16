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

import com.balysv.materialripple.MaterialRippleLayout;
import com.slightsite.siskapos.R;
import com.slightsite.siskapos.domain.CurrencyController;
import com.slightsite.siskapos.domain.DateTimeStrategy;
import com.slightsite.siskapos.domain.customer.Customer;
import com.slightsite.siskapos.domain.customer.CustomerCatalog;
import com.slightsite.siskapos.domain.customer.CustomerService;
import com.slightsite.siskapos.domain.inventory.LineItem;
import com.slightsite.siskapos.domain.sale.Register;
import com.slightsite.siskapos.technicalservices.NoDaoSetException;
import com.slightsite.siskapos.ui.printer.PrinterActivity;

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
    private MaterialRippleLayout lyt_submit;

    private EditText customer_name;
    private EditText customer_phone;
    private EditText customer_email;
    private EditText customer_address;

    private CustomerCatalog customerCatalog;
    private Customer customer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_checkout);
        initToolbar();

        try {
            register = Register.getInstance();
            customerCatalog = CustomerService.getInstance().getCustomerCatalog();
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

        customer_name = (EditText) findViewById(R.id.customer_name);
        customer_phone = (EditText) findViewById(R.id.customer_phone);
        customer_email = (EditText) findViewById(R.id.customer_email);
        customer_address = (EditText) findViewById(R.id.customer_address);

        lyt_submit = (MaterialRippleLayout) findViewById(R.id.lyt_submit);
        lyt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitOrder();
            }
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

    private void submitOrder() {
        int cash = 0;
        try {
            cash = Integer.parseInt(colected_cash.getText().toString());
        } catch (Exception e) { e.printStackTrace(); }
        String c_name = customer_name.getText().toString();
        String c_email = customer_email.getText().toString();
        String c_phone = customer_phone.getText().toString();
        String c_address = customer_address.getText().toString();
        if (c_email.length() > 0) {
            customer = customerCatalog.getCustomerByEmail(c_email);
            //Toast.makeText(getApplicationContext(), "Customer name is required.", Toast.LENGTH_SHORT).show();
        }

        if (customer != null) {
            register.setCustomer(customer);
        }

        if (cash > 0) {
            register.endSale(DateTimeStrategy.getCurrentTime());
            print();
        } else {
            Toast.makeText(getApplicationContext(), "Please fill the colected cash.", Toast.LENGTH_SHORT).show();
        }
    }

    private void print(){
        int saleId = register.getCurrentSale().getId();

        Intent newActivity = new Intent(this, PrinterActivity.class);
        newActivity.putExtra("saleId", saleId);
        startActivity(newActivity);
    }
}

