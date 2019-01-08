package com.slightsite.siskapos.ui.transaction;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.slightsite.siskapos.R;
import com.slightsite.siskapos.domain.params.ParamCatalog;
import com.slightsite.siskapos.domain.params.ParamService;
import com.slightsite.siskapos.technicalservices.NoDaoSetException;
import com.slightsite.siskapos.domain.transaction.ProductCategory;
import com.slightsite.siskapos.ui.LoginActivity;
import com.slightsite.siskapos.ui.MainActivity;
import com.slightsite.siskapos.ui.profile.ProfileActivity;

import java.util.List;

public class TransactionActivity extends AppCompatActivity {

    private View parent_view;

    private RecyclerView recyclerView;
    private AdapterListCategory mAdapter;

    private ActionBar actionBar;
    private Toolbar toolbar;

    private ParamCatalog paramCatalog;

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

        //set data and list adapter
        mAdapter = new AdapterListCategory(this, items);
        recyclerView.setAdapter(mAdapter);

        // on item list clicked
        mAdapter.setOnItemClickListener(new AdapterListCategory.OnItemClickListener() {
            @Override
            public void onItemClick(View view, ProductCategory obj, int position) {
                Snackbar.make(parent_view, "Item " + obj.title + " clicked", Snackbar.LENGTH_SHORT).show();
            }
        });

    }

    public void onButtonTabClick(View v) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cart_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
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

}

