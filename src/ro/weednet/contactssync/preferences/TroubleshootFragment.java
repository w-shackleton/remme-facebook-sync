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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ro.weednet.ContactsSync;
import ro.weednet.contactssync.R;
import ro.weednet.contactssync.activities.TestFacebookApi;

import android.accounts.Account;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Toast;

public class TroubleshootFragment extends PreferenceFragment {
	private Account[] mAccounts;
	
	@SuppressWarnings("serial")
	private List<Map<String, String>> _questions = new ArrayList<Map<String, String>>() {
		private static final long serialVersionUID = 7572191145554574231L;
		{
			add(new HashMap<String, String> () { { put("name", "Some of my friends are missing. Why?"); }; });
			add(new HashMap<String, String> () { { put("name", "Is my connection and account secure?"); }; });
			add(new HashMap<String, String> () { { put("name", "How do I merge duplicated contacts?"); }; });
			add(new HashMap<String, String> () { { put("name", "I am getting a lot of connection errors. What should I do?"); }; });
			add(new HashMap<String, String> () { { put("name", "I am getting a lot of sign-in errors. What should I do?"); }; });
			add(new HashMap<String, String> () { { put("name", "The picture quality is crappy. Why?"); }; });
			add(new HashMap<String, String> () { { put("name", "Birthdays are incorrect or missing."); }; });
		}
	};
	@SuppressWarnings("serial")
	private List<List<Map<String, String>>> _answers = new ArrayList<List<Map<String, String>>>() {
		private static final long serialVersionUID = 5046933056181371918L;
		{
			add(new ArrayList<Map<String, String>>() {
				{
					add(new HashMap<String, String> () { { put("name", "Facebook users have the option to block one or all apps."); }; });
					add(new HashMap<String, String> () { { put("name", "If they opt for that, they will be excluded from your friends list."); }; });
					add(new HashMap<String, String> () { { put("name", "The only thing you can do is to ask them to change that setting."); }; });
				}
			});
			add(new ArrayList<Map<String, String>>() {
				{
					add(new HashMap<String, String> () { { put("name", "Yes."); }; });
					add(new HashMap<String, String> () { { put("name", "This application uses a secure connection (HTTPS) and stores nothing outside of your phone."); }; });
					add(new HashMap<String, String> () { { put("name", "Unless your phone gets hacked, your account is safe and your privacy is protected."); }; });
				}
			});
			add(new ArrayList<Map<String, String>>() {
				{
					add(new HashMap<String, String> () { { put("name", "You have two options:"); }; });
					add(new HashMap<String, String> () { { put("name", "1. Manually join them. (Edit contact -> Join)"); }; });
					add(new HashMap<String, String> () { { put("name", "2. Add their Facebook ID in the \"Notes\" field in your local/Google contact. They will be automatically joined. (You might have to run a full sync)"); }; });
				}
			});
			add(new ArrayList<Map<String, String>>() {
				{
					add(new HashMap<String, String> () { { put("name", "If you get this errors when your phone is in standby (closed screen), it's normal. Just disable the notifications from settings. Some phones have a policy to disable WiFi and/or 3G when they are in standby, in order to save battery."); }; });
					add(new HashMap<String, String> () { { put("name", "If you get it when the screen is on, it's probably an connection problem. Try again later."); }; });
				}
			});
			add(new ArrayList<Map<String, String>>() {
				{
					add(new HashMap<String, String> () { { put("name", "You should get this error only if you changed your Facebook password or if you manually removed the app permissions from Facebook."); }; });
					add(new HashMap<String, String> () { { put("name", "If this is not the case and you still get this error repeatedly, try removing the account and add it again."); }; });
				}
			});
			add(new ArrayList<Map<String, String>>() {
				{
					add(new HashMap<String, String> () { { put("name", "All the Android devices have a maximum size for contact pictures."); }; });
					add(new HashMap<String, String> () { { put("name", "If you select a bigger image size, it will be automatically scaled down."); }; });
					add(new HashMap<String, String> () { { put("name", "For example: Nexus One has 96x96, Galaxy Nexus with ICS has 256x256. In Jelly Bean this setting has been increased to 720x720."); }; });
				}
			});
			add(new ArrayList<Map<String, String>>() {
				{
					add(new HashMap<String, String> () { { put("name", "I am sorry but birthdays only work on some devices."); }; });
					add(new HashMap<String, String> () { { put("name", "In future updates this might be fixed, but until then, if it doesn't work properly just disable the option."); }; });
				}
			});
		}
	};
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		//TODO: use current/selected account (not the first one)
		// Log.d("pref-bundle", icicle != null ? icicle.toString() : "null");
		addPreferencesFromResource(R.xml.preferences_troubleshooting);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		findPreference("faq").setOnPreferenceClickListener(faqClick);
		findPreference("run_now").setOnPreferenceClickListener(syncNowClick);
		findPreference("run_now_full").setOnPreferenceClickListener(syncFullNowClick);
		
		Intent test_intent = new Intent(getActivity(), TestFacebookApi.class);
		findPreference("test_fb_api").setIntent(test_intent);
		
		Intent email_intent = new Intent(Intent.ACTION_SENDTO);
		email_intent.setData(Uri.parse("mailto:" + getString(R.string.contact_email)
				+ "?subject=Bug%20report%20for%20application%20" + getString(R.string.app_name)));
		findPreference("contact_bug").setIntent(email_intent);
	}
	
	Preference.OnPreferenceChangeListener showNotificationsChange = new Preference.OnPreferenceChangeListener() {
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			try {
				ContactsSync app = ContactsSync.getInstance();
				app.setShowNotifications((Boolean) newValue);
				return true;
			} catch (Exception e) {
				Log.d("contactsync-preferences", "error: " + e.getMessage());
				return false;
			}
		}
	};
	Preference.OnPreferenceChangeListener connectionTimeoutChange = new Preference.OnPreferenceChangeListener() {
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			try {
				ContactsSync app = ContactsSync.getInstance();
				app.setConnectionTimeout(Integer.parseInt((String) newValue));
				return true;
			} catch (Exception e) {
				Log.d("contactsync-preferences", "error: " + e.getMessage());
				return false;
			}
		}
	};
	Preference.OnPreferenceClickListener syncNowClick = new Preference.OnPreferenceClickListener() {
		@Override
		public boolean onPreferenceClick(Preference preference) {
			if (mAccounts.length == 0) {
				return false;
			}
			
			ContactsSync app = ContactsSync.getInstance();
			
			if (app.getSyncWifiOnly() && !app.wifiConnected()) {
				Toast toast = Toast.makeText(getActivity(), "Blocked. Wifi only .. ", Toast.LENGTH_LONG);
				toast.show();
				
				return false;
			}
			
			ContentResolver.requestSync(mAccounts[0], ContactsContract.AUTHORITY, new Bundle());
			
			Toast toast = Toast.makeText(getActivity(), "Sync started .. ", Toast.LENGTH_LONG);
			toast.show();
			
			return true;
		}
	};
	Preference.OnPreferenceClickListener syncFullNowClick = new Preference.OnPreferenceClickListener() {
		@Override
		public boolean onPreferenceClick(Preference preference) {
			if (mAccounts.length == 0) {
				return false;
			}
			
			final ContactsSync app = ContactsSync.getInstance();
			
			if (app.getSyncWifiOnly() && !app.wifiConnected()) {
				Toast toast = Toast.makeText(getActivity(), "Blocked. Wifi only .. ", Toast.LENGTH_LONG);
				toast.show();
				
				return false;
			}
			
			new AlertDialog.Builder(getActivity())
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle("Confirm")
				.setMessage("Are you sure you want run a full sync? It will use more bandwidth and remove all manual contact joins.")
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						app.requestFullSync();
						ContentResolver.requestSync(mAccounts[0], ContactsContract.AUTHORITY, new Bundle());
						
						Toast toast = Toast.makeText(getActivity(), "Full Sync started .. ", Toast.LENGTH_LONG);
						toast.show();
					}
				})
				.setNegativeButton("No", null)
				.show();
			
			return true;
		}
	};
	Preference.OnPreferenceClickListener faqClick = new Preference.OnPreferenceClickListener() {
		@Override
		public boolean onPreferenceClick(Preference preference) {
			Dialog faqDialog = new Dialog(getActivity());
			faqDialog.setContentView(R.layout.faq);
			faqDialog.setTitle("FAQ:");
			
			// Set up our adapter
			final ExpandableListAdapter mAdapter = new SimpleExpandableListAdapter(
				getActivity(),
				_questions,
				R.layout.faq_row,
				new String[] { "name" },
				new int[] { R.id.question },
				_answers,
				R.layout.answer,
				new String[] { "name" },
				new int[] { R.id.category_list_name}
			);
			
			ExpandableListView categ_list = (ExpandableListView) faqDialog.findViewById(R.id.exp_list);
			categ_list.setAdapter(mAdapter);
			
			faqDialog.show();
			
			return false;
		}
	};
}
