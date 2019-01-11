package com.slightsite.siskapos.ui.transaction;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.balysv.materialripple.MaterialRippleLayout;
import com.slightsite.siskapos.R;
import com.slightsite.siskapos.domain.CurrencyController;
import com.slightsite.siskapos.domain.inventory.Inventory;
import com.slightsite.siskapos.domain.inventory.Product;
import com.slightsite.siskapos.domain.inventory.ProductCatalog;
import com.slightsite.siskapos.domain.params.ParamCatalog;
import com.slightsite.siskapos.domain.params.ParamService;
import com.slightsite.siskapos.domain.sale.Register;
import com.slightsite.siskapos.technicalservices.NoDaoSetException;
import com.slightsite.siskapos.domain.transaction.ProductCategory;
import com.slightsite.siskapos.ui.LoginActivity;
import com.slightsite.siskapos.ui.MainActivity;
import com.slightsite.siskapos.ui.component.ButtonAdapter;
import com.slightsite.siskapos.ui.profile.ProfileActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TransactionActivity extends AppCompatActivity {

    private View parent_view;

    private RecyclerView recyclerView;
    private AdapterListProduct mAdapter;

    private ActionBar actionBar;
    private Toolbar toolbar;

    private ParamCatalog paramCatalog;

    public final static String TAG = "TransactionActivity";
    private ProductCatalog productCatalog;
    private Register register;

    private TextView textCartItemCount;
    private int mCartItemCount = 0;
    private TextView cart_subtotal;
    private MaterialRippleLayout bottom_cart;
    private TextView tot_cart_item;

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);
        parent_view = findViewById(R.id.parent_view);

        initToolbar();
        initComponent();
        initNavigationMenu();
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        try {
            paramCatalog = ParamService.getInstance().getParamCatalog();
            if (paramCatalog.getParamByName("store_name") != null)
                actionBar.setTitle(paramCatalog.getParamByName("store_name").getValue());
            else
                actionBar.setTitle(R.string.app_name);
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }
        Tools.setSystemBarColor(this);
    }

    private void initComponent() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);

        List<ProductCategory> items = DataGenerator.getShoppingCategory(this);
        try {
            productCatalog = Inventory.getInstance().getProductCatalog();
            register = Register.getInstance();
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }

        //set data and list adapter
        mAdapter = new AdapterListProduct(this, productCatalog.getAllProduct());
        recyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new AdapterListProduct.OnItemClickListener() {
            @Override
            public void onItemClick(View view, Product obj, int position) {
                register.addItem(productCatalog.getProductById(obj.getId()), 1);
                setupBadge();
                Snackbar.make(parent_view, obj.getName() + " added to cart.", Snackbar.LENGTH_SHORT).show();
            }
        });

        cart_subtotal = (TextView) findViewById(R.id.cart_subtotal);
        bottom_cart = (MaterialRippleLayout) findViewById(R.id.bottom_cart);

        bottom_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(getApplicationContext(), CartActivity.class);
                finish();
                startActivity(intent);
            }
        });

        tot_cart_item = (TextView) findViewById(R.id.tot_cart_item);
    }

    public void onButtonTabClick(View v) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_cart_setting, menu);
        getMenuInflater().inflate(R.menu.menu_cart_badged, menu);

        final MenuItem menuItem = menu.findItem(R.id.action_cart);

        View actionView = MenuItemCompat.getActionView(menuItem);
        textCartItemCount = (TextView) actionView.findViewById(R.id.cart_badge);

        setupBadge();

        actionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(menuItem);
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.action_cart) {
            intent = new Intent(getApplicationContext(), CartActivity.class);
            finish();
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initNavigationMenu() {
        NavigationView nav_view = (NavigationView) findViewById(R.id.nav_view);
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        nav_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(final MenuItem item) {
                /*Toast.makeText(getApplicationContext(), item.getTitle() + " Selected", Toast.LENGTH_SHORT).show();
                actionBar.setTitle(item.getTitle());*/
                Intent intent;

                switch (item.getItemId()) {
                    case R.id.nav_home: {
                        intent = new Intent(getApplicationContext(), TransactionActivity.class);
                        finish();
                        startActivity(intent);
                        break;
                    }
                    case R.id.nav_invoice: {
                        intent = new Intent(getApplicationContext(), MainActivity.class);
                        finish();
                        startActivity(intent);
                        break;
                    }
                    case R.id.nav_hold: {
                        Toast.makeText(getApplicationContext(), item.getTitle() + " Selected", Toast.LENGTH_SHORT).show();
                        actionBar.setTitle(item.getTitle());
                        break;
                    }
                    case R.id.nav_profile: {
                        intent = new Intent(getApplicationContext(), ProfileActivity.class);
                        finish();
                        startActivity(intent);
                        break;
                    }
                    case R.id.nav_signout: {
                        SharedPreferences sharedpreferences = getSharedPreferences(LoginActivity.my_shared_preferences, Context.MODE_PRIVATE);

                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putBoolean(LoginActivity.session_status, false);
                        editor.putString(LoginActivity.TAG_ID, null);
                        editor.putString(LoginActivity.TAG_EMAIL, null);
                        editor.commit();

                        intent = new Intent(getApplicationContext(), LoginActivity.class);
                        finish();
                        startActivity(intent);
                        break;
                    }
                    default: {
                        intent = new Intent(getApplicationContext(), TransactionActivity.class);
                        finish();
                        startActivity(intent);
                    }
                }
                drawer.closeDrawers();
                return true;
            }
        });

        // open drawer at start
        //drawer.openDrawer(GravityCompat.START);
    }

    private void setupBadge() {
        try {
            mCartItemCount = register.getCurrentSale().getOrders();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (textCartItemCount != null) {
            if (mCartItemCount == 0) {
                if (textCartItemCount.getVisibility() != View.GONE) {
                    textCartItemCount.setVisibility(View.GONE);
                }
            } else {
                textCartItemCount.setText(String.valueOf(Math.min(mCartItemCount, 99)));
                if (textCartItemCount.getVisibility() != View.VISIBLE) {
                    textCartItemCount.setVisibility(View.VISIBLE);
                }
                if (register.hasSale()) {
                    bottom_cart.setVisibility(View.VISIBLE);
                    cart_subtotal.setText("Subtotal "+ CurrencyController.getInstance().moneyFormat(register.getTotal()));
                    tot_cart_item.setText(String.valueOf(Math.min(mCartItemCount, 99)));
                }
            }
        }
    }
}

