package org.codechimp.appraterdemo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import org.codechimp.apprater.AppRater;
import org.codechimp.apprater.AppRaterDialogCallbackDelegate;

public class MainActivity extends Activity {

	private Button buttonTest;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        AppRater.Builder builder = new AppRater.Builder();
        builder.setTitle("My Title");
        builder.setMessage("My message, yo!");
        builder.setPositiveButtonTitle("Positive");
        builder.setNeutralButtonTitle("Neutral");
        builder.setNegativeButtonTitle("Negative");
        builder.setDaysUntilPromptForRemindLater(7);
        builder.setLaunchesUntilPromptForRemindLater(2);
        builder.setPackageName("uk.co.example");

        AppRater appRater = builder.build(this);
        appRater.appLaunched();


//        buttonTest = (Button) findViewById(R.id.button1);
		
//		buttonTest.setOnClickListener(new OnClickListener() {
//			public void onClick(View v) {
//
//				// This forces display of the rate prompt.
//				// It should only be used for testing purposes
//                AppRater.Builder builder = new AppRater.Builder();
//                builder.setTitle("My Title");
//                builder.setMessage("My message, yo!");
//                builder.setPositiveButtonTitle("Positive");
//                builder.setNeutralButtonTitle("Neutral");
//                builder.setNegativeButtonTitle("Negative");
//                builder.setDaysUntilPromptForRemindLater(7);
//                builder.setLaunchesUntilPromptForRemindLater(2);
//                builder.setPackageName("uk.co.example");
//
//                builder.setCallbackDialogButtonDelegate(new AppRaterDialogCallbackDelegate() {
//                    @Override
//                    public void positiveButtonClicked() {
//                        Log.d("MainActivity", "positiveButtonClicked");
//                    }
//
//                    @Override
//                    public void neutralButtonClicked() {
//                        Log.d("MainActivity", "neutralButtonClicked");
//                    }
//
//                    @Override
//                    public void negativeButtonClicked() {
//                        Log.d("MainActivity", "negativeButtonClicked");
//                    }
//                });
//                AppRater appRater = builder.build(v.getContext());
//                appRater.showRateDialog();

//			}
//		});


        // Optionally you can set the Market you want to use prior to calling appLaunched
        // If setMarket not called it will default to Google Play
        // Current implementations are Google Play and Amazon App Store, you can add your own by implementing Market
        // AppRater.setMarket(new GoogleMarket());
        // AppRater.setMarket(new AmazonMarket());
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case (R.id.menu_ratenow): {
                AppRater appRater = new AppRater(this);
                appRater.rateNow();
                return true;
            }
        }
        return false;
    }
}
