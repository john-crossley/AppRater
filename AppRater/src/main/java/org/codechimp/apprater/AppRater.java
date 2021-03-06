package org.codechimp.apprater;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

public class AppRater {

    public static class Builder {

        private String title;
        private String message;
        private String negativeButtonTitle;
        private String neutralButtonTitle;
        private String positiveButtonTitle;
        private AppRaterDialogCallbackDelegate delegate;
        private String packageName;
        private int daysUntilPromptForRemindLater;
        private int launchesUntilPromptForRemindLater;

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setPositiveButtonTitle(String positiveButtonTitle) {
            this.positiveButtonTitle = positiveButtonTitle;
            return this;
        }

        public Builder setNeutralButtonTitle(String neutralButtonTitle) {
            this.neutralButtonTitle = neutralButtonTitle;
            return this;
        }

        public Builder setNegativeButtonTitle(String negativeButtonTitle) {
            this.negativeButtonTitle = negativeButtonTitle;
            return this;
        }

        public Builder setCallbackDialogButtonDelegate(AppRaterDialogCallbackDelegate delegate) {
            this.delegate = delegate;
            return this;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public void setDaysUntilPromptForRemindLater(int days) {
            this.daysUntilPromptForRemindLater = days;
        }

        public void setLaunchesUntilPromptForRemindLater(int launches) {
            this.launchesUntilPromptForRemindLater = launches;
        }

        public AppRater build(Context context) {
            AppRater appRater = new AppRater(context, this.delegate);
            appRater.setTitle(this.title);
            appRater.setMessage(this.message);
            appRater.setPositiveButtonTitle(this.positiveButtonTitle);
            appRater.setNeutralButtonTitle(this.neutralButtonTitle);
            appRater.setNegativeButtonTitle(this.negativeButtonTitle);
            appRater.setPackageName(this.packageName);
            appRater.setNumDaysForRemindLater(this.daysUntilPromptForRemindLater);
            appRater.setNumLaunchesForRemindLater(this.launchesUntilPromptForRemindLater);

            return appRater;
        }
    }

    // Preference Constants
    private final static String PREF_NAME = "apprater";
    private final static String PREF_LAUNCH_COUNT = "launch_count";
    private final static String PREF_FIRST_LAUNCHED = "date_firstlaunch";
    private final static String PREF_DONT_SHOW_AGAIN = "dontshowagain";
    private final static String PREF_REMIND_LATER = "remindmelater";
    private final static String PREF_APP_VERSION_NAME = "app_version_name";
    private final static String PREF_APP_VERSION_CODE = "app_version_code";

    private int daysUntilPromptForRemindLater = 3;
    private int launchesUntilPromptForRemindLater = 7;

    private Context context;

    private boolean isDark;
    private boolean themeSet;
    private boolean hideNoButton;
    private boolean isVersionNameCheckEnabled;
    private boolean isVersionCodeCheckEnabled;
    private boolean isCancelable = true;
    private String packageName;
    private Market market = new GoogleMarket();
    private AppRaterDialogCallbackDelegate delegate;

    private String title;
    private String message;
    private String negativeButtonTitle;
    private String neutralButtonTitle;
    private String positiveButtonTitle;

    public AppRater(Context context) {
        this(context, null);
    }

    public AppRater(Context context, AppRaterDialogCallbackDelegate delegate) {
        this.context = context;
        this.delegate = delegate;
    }

    /**
     * Decides if the version name check is active or not
     *
     * @param versionNameCheck
     */
    public void setVersionNameCheckEnabled(boolean versionNameCheck) {
        isVersionNameCheckEnabled = versionNameCheck;
    }

    /**
     * Decides if the version code check is active or not
     *
     * @param versionCodeCheck
     */
    public void setVersionCodeCheckEnabled(boolean versionCodeCheck) {
        isVersionCodeCheckEnabled = versionCodeCheck;
    }

    /**
     * sets number of day until rating dialog pops up for next time when remind
     * me later option is chosen
     *
     * @param daysUntilPromt
     */
    public void setNumDaysForRemindLater(int daysUntilPromt) {
        daysUntilPromptForRemindLater = daysUntilPromt;
    }

    /**
     * sets the number of launches until the rating dialog pops up for next time
     * when remind me later option is chosen
     *
     * @param launchesUntilPrompt
     */
    public void setNumLaunchesForRemindLater(int launchesUntilPrompt) {
        launchesUntilPromptForRemindLater = launchesUntilPrompt;
    }

    /**
     * decides if No thanks button appear in dialog or not
     *
     * @param isNoButtonVisible
     */
    public void setDontRemindButtonVisible(boolean isNoButtonVisible) {
        hideNoButton = isNoButtonVisible;
    }

    /**
     * sets whether the rating dialog is cancelable or not, default is true.
     *
     * @param cancelable
     */
    public void setCancelable(boolean cancelable) {
        isCancelable = cancelable;
    }

    /**
     * Call this method at the end of your OnCreate method to determine whether
     * to show the rate prompt using the specified or default day, launch count
     * values and checking if the version is changed or not
     *
     */
    public void appLaunched() {
        appLaunched(this.daysUntilPromptForRemindLater, this.launchesUntilPromptForRemindLater);
    }

    /**
     * Call this method at the end of your OnCreate method to determine whether
     * to show the rate prompt
     *
     * @param daysUntilPrompt
     * @param launchesUntilPrompt
     */
    public void appLaunched(int daysUntilPrompt, int launchesUntilPrompt) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        ApplicationRatingInfo ratingInfo = ApplicationRatingInfo.createApplicationInfo(context);
        int days;
        int launches;
        if (isVersionNameCheckEnabled) {
            if (!ratingInfo.getApplicationVersionName().equals(prefs.getString(PREF_APP_VERSION_NAME, "none"))) {
                editor.putString(PREF_APP_VERSION_NAME, ratingInfo.getApplicationVersionName());
                resetData(context);
                commitOrApply(editor);
            }
        }
        if (isVersionCodeCheckEnabled) {
            if (ratingInfo.getApplicationVersionCode() != (prefs.getInt(PREF_APP_VERSION_CODE, -1))) {
                editor.putInt(PREF_APP_VERSION_CODE, ratingInfo.getApplicationVersionCode());
                resetData(context);
                commitOrApply(editor);
            }
        }
        if (prefs.getBoolean(PREF_DONT_SHOW_AGAIN, false)) {
            return;
        } else if (prefs.getBoolean(PREF_REMIND_LATER, false)) {
            days = daysUntilPromptForRemindLater;
            launches = launchesUntilPromptForRemindLater;
        } else {
            days = daysUntilPrompt;
            launches = launchesUntilPrompt;
        }

        // Increment launch counter
        long launch_count = prefs.getLong(PREF_LAUNCH_COUNT, 0) + 1;
        editor.putLong(PREF_LAUNCH_COUNT, launch_count);
        // Get date of first launch
        Long date_firstLaunch = prefs.getLong(PREF_FIRST_LAUNCHED, 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong(PREF_FIRST_LAUNCHED, date_firstLaunch);
        }
        // Wait for at least the number of launches or the number of days used
        // until prompt
        if (launch_count >= launches && (System.currentTimeMillis() >= date_firstLaunch + (days * 24 * 60 * 60 * 1000))) {
            showRateAlertDialog(context, editor);
        }
        commitOrApply(editor);
    }

    /**
     * Call this method directly if you want to force a rate prompt, useful for
     * testing purposes
     *
     */
    public void showRateDialog() {
        showRateAlertDialog(context, null);
    }

    /**
     * Call this method directly to go straight to play store listing for rating
     *
     */
    public void rateNow() {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, market.getMarketURI(context)));
        } catch (ActivityNotFoundException activityNotFoundException1) {
            Log.e(AppRater.class.getSimpleName(), "Market Intent not found");
        }
    }

    public void setPackageName(String packageName) {
        market.overridePackageName(packageName);
    }

    /**
     * Set an alternate Market, defaults to Google Play
     *
     * @param market
     */
    public void setMarket(Market market) {
        this.market = market;
    }

    /**
     * Get the currently set Market
     *
     * @return market
     */
    public Market getMarket() {
        return market;
    }

    /**
     * Sets dialog theme to dark
     */
    @TargetApi(11)
    public void setDarkTheme() {
        isDark = true;
        themeSet = true;
    }

    /**
     * Sets dialog theme to light
     */
    @TargetApi(11)
    public void setLightTheme() {
        isDark = false;
        themeSet = true;
    }

    /**
     * The meat of the library, actually shows the rate prompt dialog
     */
    @SuppressLint("NewApi")
    private void showRateAlertDialog(final Context context, final SharedPreferences.Editor editor) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= 11 && themeSet) {
            builder = new AlertDialog.Builder(context, (isDark ? AlertDialog.THEME_HOLO_DARK : AlertDialog.THEME_HOLO_LIGHT));
        } else {
            builder = new AlertDialog.Builder(context);
        }

        builder.setTitle(this.getTitle());

        builder.setMessage(this.getMessage());

        builder.setCancelable(isCancelable);

        builder.setPositiveButton(this.getPositiveButtonTitle(),
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    rateNow();
                    if (editor != null) {
                        editor.putBoolean(PREF_DONT_SHOW_AGAIN, true);
                        commitOrApply(editor);
                    }
                    if (delegate != null) {
                        delegate.positiveButtonClicked();
                    }
                    dialog.dismiss();
                }
            });

        builder.setNeutralButton(this.getNeutralButtonTitle(),
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (editor != null) {
                        Long date_firstLaunch = System.currentTimeMillis();
                        editor.putLong(PREF_FIRST_LAUNCHED, date_firstLaunch);
                        editor.putLong(PREF_LAUNCH_COUNT, 0);
                        editor.putBoolean(PREF_REMIND_LATER, true);
                        editor.putBoolean(PREF_DONT_SHOW_AGAIN, false);
                        commitOrApply(editor);
                    }
                    if (delegate != null) {
                        delegate.neutralButtonClicked();
                    }
                    dialog.dismiss();
                }
            });
        if (!hideNoButton) {
            builder.setNegativeButton(this.getNegativeButtonTitle(),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (editor != null) {
                            editor.putBoolean(PREF_DONT_SHOW_AGAIN, true);
                            editor.putBoolean(PREF_REMIND_LATER, false);
                            long date_firstLaunch = System.currentTimeMillis();
                            editor.putLong(PREF_FIRST_LAUNCHED, date_firstLaunch);
                            editor.putLong(PREF_LAUNCH_COUNT, 0);
                            commitOrApply(editor);
                        }
                        if (delegate != null) {
                            delegate.negativeButtonClicked();
                        }
                        dialog.dismiss();
                    }
                });
        }
        builder.show();
    }

    @SuppressLint("NewApi")
    private void commitOrApply(SharedPreferences.Editor editor) {
        if (Build.VERSION.SDK_INT > 8) {
            editor.apply();
        } else {
            editor.commit();
        }
    }

    public void resetData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PREF_DONT_SHOW_AGAIN, false);
        editor.putBoolean(PREF_REMIND_LATER, false);
        editor.putLong(PREF_LAUNCH_COUNT, 0);
        long date_firstLaunch = System.currentTimeMillis();
        editor.putLong(PREF_FIRST_LAUNCHED, date_firstLaunch);
        commitOrApply(editor);
    }

    private String getPositiveButtonTitle() {
        if (this.positiveButtonTitle == null) {
            return context.getString(R.string.rate);
        }
        return this.positiveButtonTitle;
    }

    public void setPositiveButtonTitle(String positiveButtonTitle) {
        this.positiveButtonTitle = positiveButtonTitle;
    }

    private String getNeutralButtonTitle() {
        if (this.neutralButtonTitle == null) {
            return this.context.getString(R.string.later);
        }
        return this.neutralButtonTitle;
    }

    public void setNeutralButtonTitle(String neutralButtonTitle) {
        this.neutralButtonTitle = neutralButtonTitle;
    }

    private String getNegativeButtonTitle() {
        if (this.negativeButtonTitle == null) {
            return this.context.getString(R.string.no_thanks);
        }
        return this.negativeButtonTitle;
    }

    public void setNegativeButtonTitle(String negativeButtonTitle) {
        this.negativeButtonTitle = negativeButtonTitle;
    }

    private String getMessage() {
        if (this.message == null) {
            return context.getString(R.string.rate_message);
        }
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private String getTitle() {
        if (this.title == null) {
            ApplicationRatingInfo ratingInfo = ApplicationRatingInfo.createApplicationInfo(context);
            return String.format(this.context.getString(R.string.dialog_title), ratingInfo.getApplicationName());
        }
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
