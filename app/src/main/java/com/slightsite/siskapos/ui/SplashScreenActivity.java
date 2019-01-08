package com.slightsite.siskapos.ui;

import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.slightsite.siskapos.R;
import com.slightsite.siskapos.domain.CurrencyController;
import com.slightsite.siskapos.domain.DateTimeStrategy;
import com.slightsite.siskapos.domain.LanguageController;
import com.slightsite.siskapos.domain.ParamsController;
import com.slightsite.siskapos.domain.ProfileController;
import com.slightsite.siskapos.domain.customer.CustomerService;
import com.slightsite.siskapos.domain.inventory.Inventory;
import com.slightsite.siskapos.domain.params.ParamService;
import com.slightsite.siskapos.domain.sale.Register;
import com.slightsite.siskapos.domain.sale.SaleLedger;
import com.slightsite.siskapos.technicalservices.AndroidDatabase;
import com.slightsite.siskapos.technicalservices.Database;
import com.slightsite.siskapos.technicalservices.DatabaseExecutor;
import com.slightsite.siskapos.technicalservices.customer.CustomerDao;
import com.slightsite.siskapos.technicalservices.customer.CustomerDaoAndroid;
import com.slightsite.siskapos.technicalservices.inventory.InventoryDao;
import com.slightsite.siskapos.technicalservices.inventory.InventoryDaoAndroid;
import com.slightsite.siskapos.technicalservices.params.ParamDao;
import com.slightsite.siskapos.technicalservices.params.ParamDaoAndroid;
import com.slightsite.siskapos.technicalservices.sale.SaleDao;
import com.slightsite.siskapos.technicalservices.sale.SaleDaoAndroid;

/**
 * This is the first activity page, core-app and database created here.
 * Dependency injection happens here.
 *
 * 
 */
public class SplashScreenActivity extends Activity {

	public static final String POS_VERSION = "Siska POS 1.0";
	private static final long SPLASH_TIMEOUT = 2000;
	private Button goButton;
	private boolean gone;
	
	/**
	 * Loads database and DAO.
	 */
	private void initiateCoreApp() {
		Database database = new AndroidDatabase(this);
		InventoryDao inventoryDao = new InventoryDaoAndroid(database);
		SaleDao saleDao = new SaleDaoAndroid(database);
		CustomerDao customerDao = new CustomerDaoAndroid(database);
		ParamDao paramDao = new ParamDaoAndroid(database);

		DatabaseExecutor.setDatabase(database);
		LanguageController.setDatabase(database);
		CurrencyController.setDatabase(database);
		ParamsController.setDatabase(database);
		ProfileController.setDatabase(database);

		Inventory.setInventoryDao(inventoryDao);
		Register.setSaleDao(saleDao);
		SaleLedger.setSaleDao(saleDao);
		CustomerService.setCustomerDao(customerDao);
		ParamService.setParamDao(paramDao);

		DateTimeStrategy.setLocale("id", "ID");
		setLanguage(LanguageController.getInstance().getLanguage());
		CurrencyController.setCurrency("idr");

		Log.d("Core App", "INITIATE");
	}
	
	/**
	 * Set language.
	 * @param localeString
	 */
	private void setLanguage(String localeString) {
		Locale locale = new Locale(localeString);
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;
		getBaseContext().getResources().updateConfiguration(config,
				getBaseContext().getResources().getDisplayMetrics());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		initiateUI(savedInstanceState);
		initiateCoreApp();
	}
	
	/**
	 * Go.
	 */
	private void go() {
		gone = true;
		Intent newActivity = new Intent(SplashScreenActivity.this,
				LoginActivity.class);
		startActivity(newActivity);
		SplashScreenActivity.this.finish();	
	}

	private ProgressBar progressBar;
	private int progressStatus = 0;
	private Handler handler = new Handler();

	/**
	 * Initiate this UI.
	 * @param savedInstanceState
	 */
	private void initiateUI(Bundle savedInstanceState) {
		setContentView(R.layout.layout_splashscreen);
		goButton = (Button) findViewById(R.id.goButton);
		goButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				go();
			}

		});

		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		// Start long running operation in a background thread
		new Thread(new Runnable() {
			public void run() {
				while (progressStatus < 100) {
					progressStatus += 5;
					// Update the progress bar and display the
					//current value in the text view
					handler.post(new Runnable() {
						public void run() {
							progressBar.setProgress(progressStatus);
						}
					});
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (!gone) go();
			}
		}, SPLASH_TIMEOUT);
	}
}