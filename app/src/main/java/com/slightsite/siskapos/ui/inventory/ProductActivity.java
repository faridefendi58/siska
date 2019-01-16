package com.slightsite.siskapos.ui.inventory;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.TranslateAnimation;
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
import com.slightsite.siskapos.domain.transaction.ProductCategory;
import com.slightsite.siskapos.technicalservices.NoDaoSetException;
import com.slightsite.siskapos.ui.LoginActivity;
import com.slightsite.siskapos.ui.component.UpdatableFragment;
import com.slightsite.siskapos.ui.params.ParamsActivity;
import com.slightsite.siskapos.ui.profile.ProfileActivity;
import com.slightsite.siskapos.ui.sale.ReportFragment;
import com.slightsite.siskapos.ui.sale.SaleFragment;
import com.slightsite.siskapos.ui.transaction.AdapterListProduct;
import com.slightsite.siskapos.ui.transaction.CartActivity;
import com.slightsite.siskapos.ui.transaction.DataGenerator;
import com.slightsite.siskapos.ui.transaction.Tools;
import com.slightsite.siskapos.ui.transaction.TransactionActivity;

import java.util.List;

public class ProductActivity extends AppCompatActivity {

    private View parent_view;

    private RecyclerView recyclerView;
    private AdapterListProduct mAdapter;

    private ActionBar actionBar;
    private Toolbar toolbar;

    private ParamCatalog paramCatalog;

    public final static String TAG = "ProductActivity";
    private ProductCatalog productCatalog;
    private Register register;

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        parent_view = findViewById(R.id.parent_view);

        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        try {
            paramCatalog = ParamService.getInstance().getParamCatalog();
            actionBar.setTitle(R.string.title_product);
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }
        Tools.setSystemBarColor(this);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), TransactionActivity.class);
                finish();
                startActivity(intent);
            }
        });
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
                Intent intent = new Intent(getApplicationContext(), ProductDetailActivity.class);
                intent.putExtra("id", ""+ obj.getId());
                finish();
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_product, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);

        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if( ! searchView.isIconified()) {
                    searchView.setIconified(true);
                }
                searchItem.collapseActionView();
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                if (mAdapter != null){
                    mAdapter.getFilter().filter(s);
                }

                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            intent = new Intent(getApplicationContext(), TransactionActivity.class);
            finish();
            startActivity(intent);
        } else if (item.getItemId() == R.id.action_add) {
            Toast.makeText(getApplicationContext(), "Under Construction.", Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.action_syncron) {
            /*intent = new Intent(getApplicationContext(), ProductServerActivity.class);
            finish();
            startActivity(intent);*/
            Toast.makeText(getApplicationContext(), "Under Construction.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    public void addProduct() {
        UpdatableFragment reportFragment = new ReportFragment();
        UpdatableFragment saleFragment = new SaleFragment(reportFragment);
        UpdatableFragment inventoryFragment = new InventoryFragment(
                saleFragment);
        AddProductDialogFragment newFragment = new AddProductDialogFragment(inventoryFragment);
        newFragment.show(inventoryFragment.getFragmentManager(), "");
    }
}
