package com.slightsite.siskapos.ui;

import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.slightsite.siskapos.R;
import com.slightsite.siskapos.domain.LanguageController;
import com.slightsite.siskapos.domain.MasterDataController;
import com.slightsite.siskapos.domain.ProfileController;
import com.slightsite.siskapos.domain.customer.Customer;
import com.slightsite.siskapos.domain.customer.CustomerCatalog;
import com.slightsite.siskapos.domain.customer.CustomerService;
import com.slightsite.siskapos.domain.inventory.Inventory;
import com.slightsite.siskapos.domain.inventory.Product;
import com.slightsite.siskapos.domain.inventory.ProductCatalog;
import com.slightsite.siskapos.domain.params.ParamCatalog;
import com.slightsite.siskapos.domain.params.ParamService;
import com.slightsite.siskapos.domain.sale.Register;
import com.slightsite.siskapos.technicalservices.NoDaoSetException;
import com.slightsite.siskapos.ui.component.UpdatableFragment;
import com.slightsite.siskapos.ui.customer.CustomerDetailActivity;
import com.slightsite.siskapos.ui.inventory.InventoryFragment;
import com.slightsite.siskapos.ui.inventory.ProductDetailActivity;
import com.slightsite.siskapos.ui.inventory.ProductServerActivity;
import com.slightsite.siskapos.ui.params.ParamsActivity;
import com.slightsite.siskapos.ui.profile.ProfileActivity;
import com.slightsite.siskapos.ui.sale.ReportFragment;
import com.slightsite.siskapos.ui.sale.SaleFragment;
import com.slightsite.siskapos.ui.customer.CustomerFragment;

@SuppressLint("NewApi")
public class MainActivity extends FragmentActivity {

    private ViewPager viewPager;
    private ProductCatalog productCatalog;
    private CustomerCatalog customerCatalog;
    private String productId;
    private Product product;
    private String customerId;
    private Customer customer;
    private static boolean SDK_SUPPORTED;
    private PagerAdapter pagerAdapter;
    private Resources res;

    private TextView customer_name_box;
    private TextView customer_id_box;
    private Register register;

    private Integer FRAGMENT_INVENTORY = 0;
    private Integer FRAGMENT_SALE = 1;
    private Integer FRAGMENT_REPORT = 2;
    private Integer FRAGMENT_CUSTOMER = 3;

    private ParamCatalog paramCatalog;

    @SuppressLint("NewApi")
    /**
     * Initiate this UI.
     */
    private void initiateActionBar() {
        if (SDK_SUPPORTED) {
            ActionBar actionBar = getActionBar();

            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

            try {
                paramCatalog = ParamService.getInstance().getParamCatalog();
                if (paramCatalog.getParamByName("store_name") != null)
                    actionBar.setTitle(paramCatalog.getParamByName("store_name").getValue());
            } catch (NoDaoSetException e) {
                e.printStackTrace();
            }

            ActionBar.TabListener tabListener = new ActionBar.TabListener() {
                @Override
                public void onTabReselected(Tab tab, FragmentTransaction ft) {
                }

                @Override
                public void onTabSelected(Tab tab, FragmentTransaction ft) {
                    viewPager.setCurrentItem(tab.getPosition());
                }

                @Override
                public void onTabUnselected(Tab tab, FragmentTransaction ft) {
                }
            };
            actionBar.addTab(actionBar.newTab().setIcon(R.drawable.ic_playlist_add_black_24dp)
                    .setTabListener(tabListener), 0, false);
            actionBar.addTab(actionBar.newTab().setIcon(R.drawable.ic_shopping_cart_black_24dp)
                    .setTabListener(tabListener), 1, true);
            actionBar.addTab(actionBar.newTab().setIcon(R.drawable.ic_receipt_black_24dp)
                    .setTabListener(tabListener), 2, false);
            actionBar.addTab(actionBar.newTab().setIcon(R.drawable.ic_person_pin_black_24dp)
                    .setTabListener(tabListener), 3, false);

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1ABC9C")));
                actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#e2e3e5")));
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        res = getResources();
        setContentView(R.layout.layout_main);
        viewPager = (ViewPager) findViewById(R.id.pager);
        SDK_SUPPORTED = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
        initiateActionBar();
        FragmentManager fragmentManager = getSupportFragmentManager();
        pagerAdapter = new PagerAdapter(fragmentManager, res);
        viewPager.setAdapter(pagerAdapter);
        viewPager
                .setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        if (SDK_SUPPORTED)
                            getActionBar().setSelectedNavigationItem(position);
                    }
                });
        viewPager.setCurrentItem(FRAGMENT_SALE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            openQuitDialog();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Open quit dialog.
     */
    private void openQuitDialog() {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                MainActivity.this);
        quitDialog.setTitle(res.getString(R.string.dialog_quit));
        quitDialog.setPositiveButton(res.getString(R.string.quit), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        quitDialog.setNegativeButton(res.getString(R.string.no), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        quitDialog.show();
    }

    /**
     * Option on-click handler.
     * @param view
     */
    public void optionOnClickHandler(View view) {
        viewPager.setCurrentItem(FRAGMENT_INVENTORY);
        String id = view.getTag().toString();
        productId = id;
        try {
            productCatalog = Inventory.getInstance().getProductCatalog();
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }
        product = productCatalog.getProductById(Integer.parseInt(productId));
        openDetailDialog();

    }

    /**
     * Open detail dialog.
     */
    private void openDetailDialog() {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(MainActivity.this);
        quitDialog.setTitle(product.getName());
        quitDialog.setPositiveButton(res.getString(R.string.remove), new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                openRemoveDialog();
            }
        });

        quitDialog.setNegativeButton(res.getString(R.string.product_detail), new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent newActivity = new Intent(MainActivity.this,
                        ProductDetailActivity.class);
                newActivity.putExtra("id", productId);
                startActivity(newActivity);
            }
        });

        quitDialog.show();
    }

    /**
     * Open remove dialog.
     */
    private void openRemoveDialog() {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                MainActivity.this);
        quitDialog.setTitle(res.getString(R.string.dialog_remove_product));
        quitDialog.setPositiveButton(res.getString(R.string.no), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        quitDialog.setNegativeButton(res.getString(R.string.remove), new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                productCatalog.suspendProduct(product);
                pagerAdapter.update(FRAGMENT_INVENTORY);
            }
        });

        quitDialog.show();
    }

    /**
     * Get view-pager
     * @return
     */
    public ViewPager getViewPager() {
        return viewPager;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

		/*menu.add(Menu.NONE, 0, Menu.NONE, "custom")
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);*/
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.lang_en:
                setLanguage("en");
                return true;
            case R.id.lang_id:
                setLanguage("in");
                return true;
            case R.id.params:
                setParams();
                return true;
            case R.id.logout:
                //ProfileController.buildDatabase();
                SharedPreferences sharedpreferences = getSharedPreferences(LoginActivity.my_shared_preferences, Context.MODE_PRIVATE);

                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(LoginActivity.session_status, false);
                editor.putString(LoginActivity.TAG_ID, null);
                editor.putString(LoginActivity.TAG_EMAIL, null);
                editor.commit();

                intent = new Intent(MainActivity.this, LoginActivity.class);
                finish();
                startActivity(intent);
                return true;
            case R.id.profile:
                intent = new Intent(MainActivity.this, ProfileActivity.class);
                finish();
                startActivity(intent);
                return true;
            case R.id.syncronize:
                intent = new Intent(MainActivity.this, ProductServerActivity.class);
                finish();
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Set language
     * @param localeString
     */
    private void setLanguage(String localeString) {
        Locale locale = new Locale(localeString);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        LanguageController.getInstance().setLanguage(localeString);

        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());

        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    public void optionOnClickHandlerCustomer(View view) {
        viewPager.setCurrentItem(FRAGMENT_CUSTOMER);
        String id = view.getTag().toString();
        customerId = id;
        try {
            customerCatalog = CustomerService.getInstance().getCustomerCatalog();
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }
        customer = customerCatalog.getCustomerById(Integer.parseInt(customerId));
        openDetailCustomerDialog();
    }

    private void openDetailCustomerDialog() {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(MainActivity.this);
        quitDialog.setTitle(customer.getName());
        quitDialog.setPositiveButton(res.getString(R.string.remove), new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                openConfirmRemoveDialog();
            }
        });

        quitDialog.setNegativeButton(res.getString(R.string.product_detail), new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent newActivity = new Intent(MainActivity.this,
                        CustomerDetailActivity.class);
                newActivity.putExtra("id", customerId);
                startActivity(newActivity);
            }
        });

        quitDialog.show();
    }

    public void customerOnClickHandler(View view) {
        viewPager.setCurrentItem(FRAGMENT_SALE);
        customer_name_box = (TextView) viewPager.findViewById(R.id.customer_name_box);
        customer_id_box = (TextView) viewPager.findViewById(R.id.customer_id_box);
        String id = customer_id_box.getText().toString();

        customerId = id;
        try {
            customerCatalog = CustomerService.getInstance().getCustomerCatalog();
            register = Register.getInstance();
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }
        customer = customerCatalog.getCustomerById(Integer.parseInt(customerId));
        openRemoveCustomerDialog();
    }

    private void openRemoveCustomerDialog() {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(MainActivity.this);
        quitDialog.setTitle(customer.getName());
        quitDialog.setPositiveButton(res.getString(R.string.remove), new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    register.removeCustomer();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                customer_id_box.setText("0");
                customer_name_box.setText(null);
                customer_name_box.setVisibility(View.GONE);
            }
        });

        quitDialog.setNegativeButton(res.getString(R.string.update), new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                viewPager.setCurrentItem(FRAGMENT_CUSTOMER);
            }
        });

        quitDialog.show();
    }

    private void openConfirmRemoveDialog() {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                MainActivity.this);
        quitDialog.setTitle(res.getString(R.string.dialog_remove_customer));
        quitDialog.setPositiveButton(res.getString(R.string.no), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        quitDialog.setNegativeButton(res.getString(R.string.remove), new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                customerCatalog.suspendCustomer(customer);
                pagerAdapter.update(FRAGMENT_CUSTOMER);
            }
        });

        quitDialog.show();
    }

    public void jumpToCustomer(View view) {
        viewPager.setCurrentItem(FRAGMENT_CUSTOMER);
    }

    public void backToTransaction(View view) {
        viewPager.setCurrentItem(FRAGMENT_SALE);
    }

    private void setParams() {
        Intent newActivity = new Intent(MainActivity.this,
                ParamsActivity.class);
        startActivity(newActivity);
    }
}

/**
 *
 * @author Farid Efendi
 *
 */
class PagerAdapter extends FragmentStatePagerAdapter {

    private UpdatableFragment[] fragments;
    private String[] fragmentNames;

    /**
     * Construct a new PagerAdapter.
     * @param fragmentManager
     * @param res
     */
    public PagerAdapter(FragmentManager fragmentManager, Resources res) {

        super(fragmentManager);

        UpdatableFragment reportFragment = new ReportFragment();
        UpdatableFragment saleFragment = new SaleFragment(reportFragment);
        UpdatableFragment inventoryFragment = new InventoryFragment(
                saleFragment);
        UpdatableFragment customerFragment = new CustomerFragment(saleFragment);

        fragments = new UpdatableFragment[] { inventoryFragment, saleFragment,
                reportFragment, customerFragment };
        fragmentNames = new String[] { res.getString(R.string.inventory),
                res.getString(R.string.sale),
                res.getString(R.string.report),
                res.getString(R.string.customer)
        };

    }

    @Override
    public Fragment getItem(int i) {
        return fragments[i];
    }

    @Override
    public int getCount() {
        return fragments.length;
    }

    @Override
    public CharSequence getPageTitle(int i) {
        return fragmentNames[i];
    }

    /**
     * Update
     * @param index
     */
    public void update(int index) {
        fragments[index].update();
    }

}
