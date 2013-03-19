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
package ro.weednet.contactssync.preferences;

import ro.weednet.ContactsSync;
import ro.weednet.contactssync.Constants;
import ro.weednet.contactssync.R;
import ro.weednet.contactssync.activities.Preferences;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.ContactsContract;
import android.util.Log;

public class SyncFragment extends PreferenceFragment {
	private Account[] mAccounts;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		//TODO: use current/selected account (not the first one)
		// Log.d("pref-bundle", icicle != null ? icicle.toString() : "null");
		addPreferencesFromResource(R.xml.preferences_sync);
	//	addPreferencesFromResource(R.xml.preferences_troubleshooting);
	//	addPreferencesFromResource(R.xml.preferences_other);
	//	addPreferencesFromResource(R.xml.preferences_about);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		ContactsSync app = ContactsSync.getInstance();
		
		AccountManager am = AccountManager.get(SyncFragment.this.getActivity());
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
			
			findPreference("sync_type").setOnPreferenceChangeListener(syncTypeChange);
			findPreference("sync_freq").setOnPreferenceChangeListener(syncFreqChange);
			findPreference("pic_size").setOnPreferenceChangeListener(picSizeChange);
			findPreference("sync_all").setOnPreferenceChangeListener(syncAllChange);
			findPreference("sync_wifi_only").setOnPreferenceChangeListener(syncWifiOnlyChange);
			findPreference("sync_join_by_id").setOnPreferenceChangeListener(syncJoinByIdChange);
			findPreference("sync_birthdays").setOnPreferenceChangeListener(syncBirthdaysChange);
			findPreference("birthday_format").setOnPreferenceChangeListener(birthdayFormatChange);
			findPreference("sync_statuses").setOnPreferenceChangeListener(syncStatusesChange);
			findPreference("sync_emails").setOnPreferenceChangeListener(syncEmailsChange);
		}
	}
	
	Preference.OnPreferenceChangeListener syncTypeChange = new Preference.OnPreferenceChangeListener() {
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			try {
				ContactsSync app = ContactsSync.getInstance();
				app.setSyncType(Integer.parseInt((String) newValue));
				return true;
			} catch (Exception e) {
				Log.d("contactsync-preferences", "error: " + e.getMessage());
				return false;
			}
		}
	};
	Preference.OnPreferenceChangeListener syncFreqChange = new Preference.OnPreferenceChangeListener() {
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			try {
				ContactsSync app = ContactsSync.getInstance();
				app.setSyncFrequency(Integer.parseInt((String) newValue));
				
				int sync_freq = app.getSyncFrequency() * 3600;
				
				AccountManager am = AccountManager.get(SyncFragment.this.getActivity());
				Account[] accounts = am.getAccountsByType(Constants.ACCOUNT_TYPE);
				
				if (sync_freq > 0) {
					ContentResolver.setSyncAutomatically(accounts[0], ContactsContract.AUTHORITY, true);
					
					Bundle extras = new Bundle();
					ContentResolver.addPeriodicSync(accounts[0], ContactsContract.AUTHORITY, extras, sync_freq);
				} else {
					ContentResolver.setSyncAutomatically(accounts[0], ContactsContract.AUTHORITY, false);
				}
				
				return true;
			} catch (Exception e) {
				Log.d("contactsync-preferences", "error: " + e.getMessage());
				return false;
			}
		}
	};
	Preference.OnPreferenceChangeListener picSizeChange = new Preference.OnPreferenceChangeListener() {
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			try {
				ContactsSync app = ContactsSync.getInstance();
				app.setPictureSize(Integer.parseInt((String) newValue));
				
				return true;
			} catch (Exception e) {
				Log.d("contactsync-preferences", "error: " + e.getMessage());
				return false;
			}
		}
	};
	Preference.OnPreferenceChangeListener syncAllChange = new Preference.OnPreferenceChangeListener() {
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			try {
				final ContactsSync app = ContactsSync.getInstance();
				
				if ((Boolean) newValue == true) {
					app.setSyncAllContacts(true);
					return true;
				} else {
					new AlertDialog.Builder(SyncFragment.this.getActivity())
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle("Confirm")
					.setMessage("This action will trigger a full sync. It will use more bandwidth and remove all manual contact joins. Are you sure you want to do this?")
					.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							app.setSyncAllContacts(false);
							app.requestFullSync();
							((CheckBoxPreference) findPreference("sync_all")).setChecked(false);
						}
					})
					.setNegativeButton("No", null)
					.show();
					return false;
				}
			} catch (Exception e) {
				Log.d("contactsync-preferences", "error: " + e.getMessage());
				return false;
			}
		}
	};
	Preference.OnPreferenceChangeListener syncWifiOnlyChange = new Preference.OnPreferenceChangeListener() {
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			try {
				ContactsSync app = ContactsSync.getInstance();
				app.setSyncWifiOnly((Boolean) newValue);
				return true;
			} catch (Exception e) {
				Log.d("contactsync-preferences", "error: " + e.getMessage());
				return false;
			}
		}
	};
	Preference.OnPreferenceChangeListener syncJoinByIdChange = new Preference.OnPreferenceChangeListener() {
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			try {
				ContactsSync app = ContactsSync.getInstance();
				app.setJoinById((Boolean) newValue);
				return true;
			} catch (Exception e) {
				Log.d("contactsync-preferences", "error: " + e.getMessage());
				return false;
			}
		}
	};
	Preference.OnPreferenceChangeListener syncBirthdaysChange = new Preference.OnPreferenceChangeListener() {
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			try {
				final ContactsSync app = ContactsSync.getInstance();
				
				if ((Boolean) newValue == true) {
					new AlertDialog.Builder(SyncFragment.this.getActivity())
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle("Confirm")
					.setMessage("This feature only works on some devices.\nIt may even cause crashes or freezes on few devices.\nAre you sure you want to continue?")
					.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							app.setSyncBirthdays(true);
							((CheckBoxPreference) findPreference("sync_birthdays")).setChecked(true);
						}
					})
					.setNegativeButton("No", null)
					.show();
					return false;
				} else {
					app.setSyncBirthdays(false);
					return true;
				}
			} catch (Exception e) {
				Log.d("contactsync-preferences", "error: " + e.getMessage());
				return false;
			}
		}
	};
	Preference.OnPreferenceChangeListener birthdayFormatChange = new Preference.OnPreferenceChangeListener() {
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			try {
				ContactsSync app = ContactsSync.getInstance();
				app.setBirthdayFormat(Integer.parseInt((String) newValue));
				app.savePreferences();
				return true;
			} catch (Exception e) {
				Log.d("contactsync-preferences", "error: " + e.getMessage());
				return false;
			}
		}
	};
	Preference.OnPreferenceChangeListener syncStatusesChange = new Preference.OnPreferenceChangeListener() {
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			try {
				ContactsSync app = ContactsSync.getInstance();
				app.setSyncStatuses((Boolean) newValue);
				return true;
			} catch (Exception e) {
				Log.d("contactsync-preferences", "error: " + e.getMessage());
				return false;
			}
		}
	};
	Preference.OnPreferenceChangeListener syncEmailsChange = new Preference.OnPreferenceChangeListener() {
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			try {
				ContactsSync app = ContactsSync.getInstance();
				app.setSyncEmails((Boolean) newValue);
				return true;
			} catch (Exception e) {
				Log.d("contactsync-preferences", "error: " + e.getMessage());
				return false;
			}
		}
	};
}
