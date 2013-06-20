package ro.weednet.contactssync.ads;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;

public class AdPreference extends Preference {
	
	public AdPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public AdPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public AdPreference(Context context) {
		super(context);
	}
	/*
	@Override
	protected View onCreateView(ViewGroup parent) {
		View view = super.onCreateView(parent);
		
		return view;
	}
	*/
}