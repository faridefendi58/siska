package com.slightsite.siskapos.ui.transaction;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.slightsite.siskapos.R;
import com.slightsite.siskapos.domain.CurrencyController;
import com.slightsite.siskapos.domain.inventory.LineItem;
import com.slightsite.siskapos.domain.sale.Register;
import com.slightsite.siskapos.technicalservices.NoDaoSetException;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("NewApi")
public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdapterListCart mAdapter;
    private Register register;
    private View parent_view;
    private TextView cart_total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_cart);
        initiateActionBar();

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
        cart_total = findViewById(R.id.cart_total);

        update();
    }

    @SuppressLint("NewApi")
    private void initiateActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Cart");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        Tools.setSystemBarColor(this, R.color.grey_5);
        Tools.setSystemBarLight(this);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), TransactionActivity.class);
                finish();
                startActivity(intent);
            }
        });
    }

    private void showList(List<LineItem> list) {

        //set data and list adapter
        mAdapter = new AdapterListCart(this, list);
        recyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new AdapterListCart.OnItemClickListener() {
            @Override
            public void onItemClick(View view, LineItem obj, int position) {
                Snackbar.make(parent_view, obj.getProduct().getName() + " choosen.", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cart_setting, menu);
        Tools.changeMenuIconColor(menu, getResources().getColor(R.color.grey_60));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        if (item.getItemId() == android.R.id.home) {
            intent = new Intent(getApplicationContext(), TransactionActivity.class);
            finish();
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    public void update() {
        if(register.hasSale()){
            showList(register.getCurrentSale().getAllLineItem());
            cart_total.setText(CurrencyController.getInstance().moneyFormat(register.getTotal()) + "");
        }
        else{
            showList(new ArrayList<LineItem>());
            cart_total.setText("0.00");
            //customer_name_box.setVisibility(View.GONE);
        }
    }
}
