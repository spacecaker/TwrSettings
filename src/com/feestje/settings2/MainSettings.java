package com.feestje.settings2;

import android.os.Bundle;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.Window;
import android.widget.HorizontalScrollView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.TabSpec;

public class MainSettings extends TabActivity {

	private static final String PREF_IS_TABBED = "pref_is_tabbed";
	public static final String SHARED_PREFERENCES_BASENAME = "com.android.settings";
	public static final String ACTION_UPDATE_PREFERENCES = "com.android.settings.UPDATE";
	private SharedPreferences mPreferences;
    private boolean mIsTabbed = true;
    
    private Intent intent;
    private static HorizontalScrollView mHorizontalScrollView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); 
        super.onCreate(savedInstanceState);
        
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (getResources().getBoolean(R.bool.config_allow_toggle_tabbed)) {
            mIsTabbed = mPreferences.getBoolean(PREF_IS_TABBED,
                    getResources().getBoolean(R.bool.config_use_tabbed));
        } else {
            mIsTabbed = getResources().getBoolean(R.bool.config_use_tabbed);
        }
        setUpUi();
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,    R.layout.screen_title);        
    }

    
    private void setUpUi() {
        if (!mIsTabbed) {
            setContentView(R.xml.mainsettings);
            
            mHorizontalScrollView = (HorizontalScrollView) findViewById(R.id.horizontalScrollView);

    		intent = new Intent().setClass(MainSettings.this, TabSettings.class);
    		setupTab(new TextView(this), getString(R.string.tab1), intent);

    		intent = new Intent().setClass(MainSettings.this, ProgramSettings.class);
    		setupTab(new TextView(this), getString(R.string.tab2), intent);

    		intent = new Intent().setClass(MainSettings.this, AboutSettings.class);
    		setupTab(new TextView(this), getString(R.string.tab3), intent);
    		
    		intent = new Intent().setClass(MainSettings.this, SpaceSettings.class);
    		setupTab(new TextView(this), getString(R.string.tab4), intent);		
        } else {
            setContentView(R.xml.mainsettingshidden);
            
            mHorizontalScrollView = (HorizontalScrollView) findViewById(R.id.horizontalScrollView);

    		intent = new Intent().setClass(MainSettings.this, ListSettings.class);
    		setupTab(new TextView(this), getString(R.string.tab1), intent);	
        }
    }
    
    public static class FlingableTabHost extends TabHost implements TabHost.OnTabChangeListener {
        private GestureDetector mGestureDetector;
        private static final int MAJOR_MOVE = 60;
        private Animation mRightInAnimation;
        private Animation mRightOutAnimation;
        private Animation mLeftInAnimation;
        private Animation mLeftOutAnimation;

        public FlingableTabHost(Context context, AttributeSet attrs) {
            super(context, attrs);

            mRightInAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_right_in);
            mRightOutAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_right_out);
            mLeftInAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_left_in);
            mLeftOutAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_left_out);

            setOnTabChangedListener(this);

            mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                        float velocityY) {
                    int tabCount = getTabWidget().getTabCount();
                    int currentTab = getCurrentTab();
                    int dx = (int) (e2.getX() - e1.getX());

                    // don't accept the fling if it's too short
                    // as it may conflict with tracking move
                    if (Math.abs(dx) > MAJOR_MOVE && Math.abs(velocityX) > Math.abs(velocityY)) {

                        final boolean right = velocityX < 0;
                        final int newTab = MathUtils.constrain(currentTab + (right ? 1 : -1),
                                0, tabCount - 1);
                        if (newTab != currentTab) {
                            // Somewhat hacky, depends on current implementation of TabHost:
                            // http://android.git.kernel.org/?p=platform/frameworks/base.git;a=blob;
                            // f=core/java/android/widget/TabHost.java
                            View currentView = getCurrentView();
                            setCurrentTab(newTab);
                            View newView = getCurrentView();

                            newView.startAnimation(right ? mRightInAnimation : mLeftInAnimation);
                            currentView.startAnimation(
                                    right ? mRightOutAnimation : mLeftOutAnimation);
                        }
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        }

        @Override
        public void onTabChanged(String tabId) {
            View tabView = getCurrentTabView();
            final int width = mHorizontalScrollView.getWidth();
            final int scrollPos = tabView.getLeft() - (width - tabView.getWidth()) / 2; 
            mHorizontalScrollView.scrollTo(scrollPos, 0);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            mGestureDetector.onTouchEvent(event);
            return true;
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent event) {
            return mGestureDetector.onTouchEvent(event);
        }
    }

	private void setupTab(final View view, final String tag, final Intent myIntent) {

                final TabHost mTabHost = getTabHost();

		View tabview = createTabView(mTabHost.getContext(), tag);
		TabSpec setContent =  mTabHost.newTabSpec(tag).setIndicator(tabview).setContent(myIntent);
		mTabHost.addTab(setContent);
	}

	private static View createTabView(final Context context, final String text) {

		View view = LayoutInflater.from(context).inflate(R.layout.space_settings_tabs, null);
		TextView tv = (TextView) view.findViewById(R.id.tabsText);
		tv.setText(text);
		return view;
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
	    
	    getMenuInflater().inflate(mIsTabbed ? R.menu.settings_tab : R.menu.settings, menu);
        if (!getResources().getBoolean(R.bool.config_allow_toggle_tabbed)) {
            menu.removeItem(R.id.tab_settings);
        }	    
		return super.onCreateOptionsMenu(menu);
    }
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int choice = item.getItemId();
        switch (choice) {
            case R.id.tab_settings:
                mIsTabbed = !mIsTabbed;
                mPreferences.edit().putBoolean(PREF_IS_TABBED, mIsTabbed).commit();
                restartActivity(this);
                return true;

            default:
                return false;
        }
    }
    
    public static void restartActivity(final Activity activity) {
        if (activity == null)
            return;
        final int enter_anim = android.R.anim.fade_in;
        final int exit_anim = android.R.anim.fade_out;
        activity.overridePendingTransition(enter_anim, exit_anim);
        activity.finish();
        activity.overridePendingTransition(enter_anim, exit_anim);
        activity.startActivity(activity.getIntent());
    }
    
	public void MenuClickButton(View v) {
	    openOptionsMenu();
	}   	
}