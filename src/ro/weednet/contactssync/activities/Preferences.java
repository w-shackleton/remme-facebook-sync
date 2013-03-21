/*
 * Copyright (C) 2012 Danut Chereches
 *
 * Contact: Danut Chereches <admin@weednet.ro>
 *
 * This file is part of Facebook Contact Sync.
 * 
 * Facebook Contact Sync is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Facebook Contact Sync.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 */
package ro.weednet.contactssync.activities;

import com.appbrain.AppBrain;

import ro.weednet.ContactsSync;
import ro.weednet.contactssync.Constants;
import ro.weednet.contactssync.R;
import ro.weednet.contactssync.authenticator.AuthenticatorActivity;
import ro.weednet.contactssync.client.RawContact;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;

public class Preferences extends Activity {
	public final static ContactsSync.SyncType DEFAULT_SYNC_TYPE = ContactsSync.SyncType.MEDIUM;
	public final static int DEFAULT_SYNC_FREQUENCY = 24;//hours
	public final static int DEFAULT_PICTURE_SIZE = RawContact.IMAGE_SIZES.MAX_SQUARE;
	public final static boolean DEFAULT_SYNC_ALL = false;
	public final static boolean DEFAULT_SYNC_WIFI_ONLY = false;
	public final static boolean DEFAULT_JOIN_BY_ID = false;
	public final static boolean DEFAULT_SYNC_BIRTHDAYS = false;
	public final static int DEFAULT_BIRTHDAY_FORMAT = RawContact.BIRTHDAY_FORMATS.GLOBAL;
	public final static boolean DEFAULT_SYNC_STATUSES = true;
	public final static boolean DEFAULT_SYNC_EMAILS = true;
	public final static boolean DEFAULT_SHOW_NOTIFICATIONS = false;
	public final static int DEFAULT_CONNECTION_TIMEOUT = 60;
	public final static boolean DEFAULT_DISABLE_ADS = false;
	
	private Dialog mAuthDialog;
	private Account[] mAccounts;
	
	//@Override
//	public void onBuildHeaders(List<Header> target) {
//		loadHeadersFromResource(R.xml.preferences_header, target);
//	}
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
	//	if (hasHeaders()) {
	//		TextView v = new TextView(this);
	//		v.setText("Some text ..");
	//		setListFooter(v);
	//	}
		
		setContentView(R.layout.preferences);
		
		FragmentTransaction ft = getFragmentManager().beginTransaction();
	//	ft.replace(R.id.content, new OtherFragment());
		ft.replace(R.id.settings, new AllFragment());
	//	ft.add(R.id.sync, new SyncFragment());
	//	ft.add(R.id.content, new TroubleshootFragment());
	//	ft.add(R.id.content, new OtherFragment());
	//	ft.add(R.id.content, new AboutFragment());
		ft.commit();
		
		//TODO: use current/selected account (not the first one)
		// Log.d("pref-bundle", icicle != null ? icicle.toString() : "null");
	//	addPreferencesFromResource(R.xml.preferences_sync);
	//	addPreferencesFromResource(R.xml.preferences_troubleshooting);
	//	addPreferencesFromResource(R.xml.preferences_other);
	//	addPreferencesFromResource(R.xml.preferences_about);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		ContactsSync app = ContactsSync.getInstance();
		
		AccountManager am = AccountManager.get(this);
		mAccounts = am.getAccountsByType(Constants.ACCOUNT_TYPE);
		
		if (mAccounts.length > 0) {
			if (ContentResolver.getSyncAutomatically(mAccounts[0], ContactsContract.AUTHORITY)) {
				if (app.getSyncFrequency() == 0) {
					app.setSyncFrequency(Preferences.DEFAULT_SYNC_FREQUENCY);
					app.savePreferences();
					ContentResolver.addPeriodicSync(mAccounts[0], ContactsContract.AUTHORITY, new Bundle(), Preferences.DEFAULT_SYNC_FREQUENCY * 3600);
				}
			} else {
				if (app.getSyncFrequency() > 0) {
					app.setSyncFrequency(0);
					app.savePreferences();
				}
			}
		} else {
			if (mAuthDialog != null) {
				mAuthDialog.dismiss();
			}
			
			mAuthDialog = new Dialog(this);
			mAuthDialog.setContentView(R.layout.not_account_actions);
			mAuthDialog.setTitle("Select option");
			((Button) mAuthDialog.findViewById(R.id.add_account_button)).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mAuthDialog.dismiss();
					Intent intent = new Intent(Preferences.this, AuthenticatorActivity.class);
					startActivity(intent);
				}
			});
			((Button) mAuthDialog.findViewById(R.id.exit_button)).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mAuthDialog.dismiss();
					Preferences.this.finish();
				}
			});
			mAuthDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					mAuthDialog.dismiss();
					Preferences.this.finish();
				}
			});
			mAuthDialog.show();
		}
	}
	
	@Override
	public void onBackPressed() {
		ContactsSync app = ContactsSync.getInstance();
		
		if (!app.getDisableAds()) {
			AppBrain.getAds().maybeShowInterstitial(this);
		}
		
		finish();
	}
	
	public static class AllFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle icicle) {
			super.onCreate(icicle);
			
			addPreferencesFromResource(R.xml.preferences_sync);
			addPreferencesFromResource(R.xml.preferences_troubleshooting);
			addPreferencesFromResource(R.xml.preferences_other);
			addPreferencesFromResource(R.xml.preferences_about);
		}
		
		@Override
		public void onResume() {
			super.onResume();
			
			findPreference("disable_ads").setOnPreferenceChangeListener(disableAdsChange);
		}
		
		Preference.OnPreferenceChangeListener disableAdsChange = new Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				try {
					ContactsSync app = ContactsSync.getInstance();
					app.setDisableAds((Boolean) newValue);
					return true;
				} catch (Exception e) {
					Log.d("contactsync-preferences", "error: " + e.getMessage());
					return false;
				}
			}
		};
	}
	
	public static class OtherFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle icicle) {
			super.onCreate(icicle);
			
			addPreferencesFromResource(R.xml.preferences_other);
		}
		
		@Override
		public void onResume() {
			super.onResume();
			
			findPreference("disable_ads").setOnPreferenceChangeListener(disableAdsChange);
		}
		
		Preference.OnPreferenceChangeListener disableAdsChange = new Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				try {
					ContactsSync app = ContactsSync.getInstance();
					app.setDisableAds((Boolean) newValue);
					return true;
				} catch (Exception e) {
					Log.d("contactsync-preferences", "error: " + e.getMessage());
					return false;
				}
			}
		};
	}
	
	public static class AboutFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle icicle) {
			super.onCreate(icicle);
			
			addPreferencesFromResource(R.xml.preferences_about);
		}
		
		@Override
		public void onResume() {
			super.onResume();
			
			View v = getView();
			LayoutParams params = v.getLayoutParams();
			params.height = 300;//LayoutParams.WRAP_CONTENT;
			v.setLayoutParams(params);
			
			String donateUrl = "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=7SUETTCRKTMKY";
			Intent donate_intent = new Intent(Intent.ACTION_VIEW, Uri.parse(donateUrl));
			findPreference("donate").setIntent(donate_intent);
			
			String version = "";
			try {
				version = getActivity().getPackageManager()
					.getPackageInfo(getActivity().getPackageName(), 0).versionName;
			} catch (NameNotFoundException e1) { }
			findPreference("version").setSummary(version);
			
			boolean market_installed = true;
			try {
				getActivity().getPackageManager().getPackageInfo("com.android.vending", 0);
			} catch (PackageManager.NameNotFoundException e) {
				market_installed = false;
			}
			if (market_installed) {
				Intent rate_intent = new Intent(Intent.ACTION_VIEW);
				rate_intent.setData(Uri.parse("market://details?id=ro.weednet.contactssync"));
				findPreference("rate_app").setIntent(rate_intent);
			} else {
				findPreference("rate_app").setEnabled(false);
			}
		}
	}
}
