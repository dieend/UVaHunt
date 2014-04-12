package com.dieend.uvahunt;

import org.json.JSONException;

import android.app.SearchManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.dieend.uvahunt.callback.ProblemViewer;
import com.dieend.uvahunt.model.User;
import com.dieend.uvahunt.service.UhuntService;
import com.dieend.uvahunt.service.UhuntServiceDelegate;
import com.dieend.uvahunt.service.UhuntServiceHandler;
import com.dieend.uvahunt.service.base.ServiceManager;


public class UvaHuntActivity extends ActionBarActivity implements ProblemViewer, UhuntServiceDelegate{
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mMenuTitles;
    
    public static final String TAG = "com.dieend.uvahunt";
    public static final String PREFERENCES_FILE= TAG + ".PREFERENCES";

    private ServiceManager uhuntService;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Log.d(TAG, "username = " + intent.getStringExtra("username"));
        Log.d(TAG, "uid = " + intent.getStringExtra("uid"));
    	setContentView(R.layout.activity_main);
        mMenuTitles = getResources().getStringArray(R.array.drawer_items);
        mTitle = getTitle();
        mDrawerTitle = getTitle();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mMenuTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
                ) {
            public void onDrawerClosed(View view) {
            	getSupportActionBar().setTitle(mTitle);
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        selectItem(0);
        uhuntService = new ServiceManager(this, UhuntService.class, new UhuntServiceHandler(this, intent.getStringExtra("uid")));
        uhuntService.start();
        findViewById(android.R.id.progress).setVisibility(View.GONE);
    }
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	try {
    		uhuntService.unbind();
    	} catch (Throwable t) {
    		t.printStackTrace();
    	}
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         // The action bar home/up action should open or close the drawer.
         // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch(item.getItemId()) {
        case R.id.action_websearch:
            // create intent to perform web search for this planet
            Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
            intent.putExtra(SearchManager.QUERY, getSupportActionBar().getTitle());
            // catch event that there's no activity to handle intent
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, R.string.app_not_available, Toast.LENGTH_LONG).show();
            }
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }
    enum FRAGMENT_TYPE{
    	PROFILE_FRAGMENT,
    	SUBMISSION_STATISTICS,
    	SOLVED_PROBLEM_LEVEL,
    	LATEST_SUBMISSION,
    	COMPETITIVE_PROGRAMMING,
    	SEARCH_PROBLEMS,
    	RANK_LIST,
    	LIVE_SUBMISSIONS,
    	PROBLEM_STATISTICS
    }
    FRAGMENT_TYPE[] fragmentTypesArray = FRAGMENT_TYPE.values();
    Fragment[] fragments = new Fragment[fragmentTypesArray.length];
    private void selectItem(int position) {
    	FRAGMENT_TYPE type = fragmentTypesArray[position];
        // update the main content by replacing fragments
    	Fragment fragment = getFragment(type);
    	
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        
        switch (type) {
	    	case PROFILE_FRAGMENT:
	    		if (profileReady) ((ProfileFragment) fragment).updateProfile(user);
	    		if (submissionReady) ((ProfileFragment) fragment).updateSubmission();
	    		break;
			case COMPETITIVE_PROGRAMMING:
				break;
			case LATEST_SUBMISSION:
				break;
			case LIVE_SUBMISSIONS:
				break;
			case PROBLEM_STATISTICS:
				break;
			case RANK_LIST:
				break;
			case SEARCH_PROBLEMS:
				break;
			case SUBMISSION_STATISTICS:
				break;
			case SOLVED_PROBLEM_LEVEL:
				break;
		}
        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mMenuTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }
    
    
    private synchronized Fragment getFragment(FRAGMENT_TYPE type) {
    	switch (type) {
	    	case PROFILE_FRAGMENT:
	    		if (fragments[type.ordinal()] == null) {
	    			fragments[type.ordinal()] = new ProfileFragment();
	    			Log.d(TAG, "creating new fragment");
	    		}
	    		break;
			case COMPETITIVE_PROGRAMMING:
				break;
			case LATEST_SUBMISSION:
				break;
			case LIVE_SUBMISSIONS:
				break;
			case PROBLEM_STATISTICS:
				break;
			case RANK_LIST:
				break;
			case SEARCH_PROBLEMS:
				if (fragments[type.ordinal()] == null) {
					fragments[type.ordinal()] = new ProblemViewFragment();
				}
				break;
			case SUBMISSION_STATISTICS:
				break;
			case SOLVED_PROBLEM_LEVEL:
				break;
    	}

    	return fragments[type.ordinal()];
    }
    @Override
    public void showProblem(int problemNumber, String problemTitles) {
    	ProblemViewFragment fragment = (ProblemViewFragment) getFragment(FRAGMENT_TYPE.SEARCH_PROBLEMS);
    	fragment.loadProblem(problemNumber);
    	FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        mDrawerList.setItemChecked(FRAGMENT_TYPE.SEARCH_PROBLEMS.ordinal(), true);
        setTitle(problemTitles);
    }
    
    private boolean profileReady = false;
    @Override
    public void profileReady(String json) {
    	profileReady = true;
		try {
			Log.d(TAG, json);
			user = new User(json);
			ProfileFragment fragment = (ProfileFragment)getFragment(FRAGMENT_TYPE.PROFILE_FRAGMENT);
			fragment.updateProfile(user);
		} catch (JSONException e) {
			e.printStackTrace();
		}
    }
    private boolean submissionReady = false;
    @Override
	public void submissionReady() {
    	submissionReady = true;
    	ProfileFragment fragment = (ProfileFragment)getFragment(FRAGMENT_TYPE.PROFILE_FRAGMENT);
		fragment.updateSubmission();
	}
    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
	public ServiceManager getServiceManager() {
		return uhuntService;
	}
    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggle
        mDrawerToggle.onConfigurationChanged(newConfig);
    }


}