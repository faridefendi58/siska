package com.slightsite.siskapos.ui.transaction;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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

    private LinearLayout cart_container;
    private RelativeLayout empty_cart_container;

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

        cart_container = findViewById(R.id.cart_container);
        empty_cart_container = (RelativeLayout) findViewById(R.id.empty_cart_container);

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
        mAdapter = new AdapterListCart(this, list, register, cart_total);
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
        } else if (item.getItemId() == R.id.action_remove) {
            showConfirmClearDialog();
        } else {
            Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    public void update() {
        if(register.hasSale()){
            if (register.getTotal() > 0) {
                showList(register.getCurrentSale().getAllLineItem());
                cart_total.setText(CurrencyController.getInstance().moneyFormat(register.getTotal()) + "");
                cart_container.setBackgroundColor(getResources().getColor(R.color.colorWhite));
            } else {
                cart_container.setVisibility(View.GONE);
                empty_cart_container.setVisibility(View.VISIBLE);
            }
        } else{
            showList(new ArrayList<LineItem>());
            cart_total.setText("0.00");
            cart_container.setVisibility(View.GONE);
            empty_cart_container.setVisibility(View.VISIBLE);
        }
    }

    private void showConfirmClearDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getResources().getString(R.string.dialog_clear_sale));
        dialog.setPositiveButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        dialog.setNegativeButton(getResources().getString(R.string.clear), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                register.cancleSale();
                update();
            }
        });

        dialog.show();
    }

    public void addQty(View view) {
        TextView qty = (TextView) view.findViewById(R.id.quantity);
        Toast.makeText(getApplicationContext(), qty.getText().toString(), Toast.LENGTH_SHORT).show();
    }
}
