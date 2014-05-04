package com.dieend.uvahunt;

import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
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

import com.dieend.uvahunt.callback.LiveUpdaterHandler;
import com.dieend.uvahunt.callback.ProblemViewer;
import com.dieend.uvahunt.model.DBManager;
import com.dieend.uvahunt.model.Submission;
import com.dieend.uvahunt.model.User;
import com.dieend.uvahunt.model.UserRank;
import com.dieend.uvahunt.service.UhuntService;
import com.dieend.uvahunt.service.UhuntServiceDelegate;
import com.dieend.uvahunt.service.UhuntServiceHandler;
import com.dieend.uvahunt.service.base.ServiceManager;
import com.kskkbys.rate.RateThisApp;


public class UvaHuntActivity extends ActionBarActivity implements ProblemViewer, UhuntServiceDelegate, LiveUpdaterHandler{
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mMenuTitles;
    
    public static final String TAG = "com.dieend.uvahunt";
    public static final String PREFERENCES_FILE= TAG + ".PREFERENCES";

    private ServiceManager uhuntService;
    int uid;
    private User user;
    private boolean isLiveUpdate = false;
    // need to sync with R.array.drawer_items
    enum FRAGMENT_TYPE{
    	PROFILE_FRAGMENT,
    	SUBMISSION_STATISTICS,
    	SOLVED_PROBLEM_LEVEL,
    	LATEST_SUBMISSION,
    	COMPETITIVE_PROGRAMMING,
    	SEARCH_PROBLEMS,
    	RANK_LIST
    }
    FRAGMENT_TYPE[] fragmentTypesArray = FRAGMENT_TYPE.values();
    BaseFragment[] fragments = new BaseFragment[fragmentTypesArray.length];
    FRAGMENT_TYPE selectedItem ;
    private void restoreState(Bundle savedInstanceState) {
    	if (savedInstanceState != null) {
    		selectedItem = fragmentTypesArray[savedInstanceState.getInt("selected")];
    	} else {
    		selectedItem = fragmentTypesArray[0];
    	}
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	outState.putInt("selected", selectedItem.ordinal());
    	super.onSaveInstanceState(outState);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RateThisApp.onStart(this);
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
        mDrawerList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
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
        restoreState(savedInstanceState);
        selectItem(selectedItem.ordinal());
        uid = Integer.parseInt(intent.getStringExtra("uid"));
        uhuntService = new ServiceManager(this, UhuntService.class, new UhuntServiceHandler(this));
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
        menu.findItem(R.id.action_sign_out).setVisible(!drawerOpen);
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
        case R.id.action_sign_out:
            // create intent to perform web search for this planet
            Intent intent = new Intent(this, StartActivity.class);
            // catch event that there's no activity to handle intent
            SharedPreferences preference = getSharedPreferences(UvaHuntActivity.PREFERENCES_FILE, MODE_PRIVATE);
            preference.edit()
            	.putString("username", null)
            	.putString("uid",null)
            	.commit();
            try {
				uhuntService.send(Message.obtain(null, UhuntService.MSG_RESET_SUBMISSION));
			} catch (RemoteException e) {
				e.printStackTrace();
			}
            startActivity(intent);
            finish();
            return true;
        case R.id.action_feedback:
        	RateThisApp.showRateDialog(this);
        	return true;
        case R.id.action_about:
        	intent = new Intent(this, AboutActivity.class);
        	startActivity(intent);
        	return true;
        case R.id.action_setting:
        	// TODO setting activity
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
    
	private void selectItem(int position) {
    	selectedItem = fragmentTypesArray[position];
        // update the main content by replacing fragments
    	Fragment fragment = getFragment(selectedItem);
    	
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        
        switch (selectedItem) {
	    	case PROFILE_FRAGMENT:
	    		if (profileReady) ((ProfileFragment) fragment).updateProfile(user);
	    		if (submissionReady) ((ProfileFragment) fragment).updateSubmission();
	    		break;
			case COMPETITIVE_PROGRAMMING:
				break;
			case LATEST_SUBMISSION:
				((LatestSubmissionFragment)fragment).setToggleState(isLiveUpdate);
				if (submissionReady) ((LatestSubmissionFragment)fragment).updateSubmission(DBManager.$().getAllSubmission());
				RateThisApp.showRateDialogIfNeeded(this);
				break;
			case RANK_LIST:
				try {
					uhuntService.send(Message.obtain(null, UhuntService.MSG_REQUEST_RANK));
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				break;
			case SEARCH_PROBLEMS:
				break;
			case SUBMISSION_STATISTICS:
				if (submissionReady) ((SubmissionStatisticsFragment)fragment).updateSubmission(DBManager.$().getAllSubmission());
				break;
			case SOLVED_PROBLEM_LEVEL:
				if (submissionReady) ((SolvedProblemLevelFragment)fragment).updateProblem();
				break;
		}
        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        
        setTitle(mMenuTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }
    
    
    private synchronized BaseFragment getFragment(FRAGMENT_TYPE type) {
    	if (fragments[type.ordinal()] == null) {
	    	switch (type) {
		    	case PROFILE_FRAGMENT:
		    		fragments[type.ordinal()] = new ProfileFragment();
		    		break;
				case COMPETITIVE_PROGRAMMING:
					fragments[type.ordinal()] = CPFragment.newInstance(this);
					break;
				case LATEST_SUBMISSION:
					fragments[type.ordinal()] = new LatestSubmissionFragment();
					break;
				case RANK_LIST:
					fragments[type.ordinal()] = new RankFragment();
					break;
				case SEARCH_PROBLEMS:
					fragments[type.ordinal()] = new ProblemViewFragment();
					break;
				case SUBMISSION_STATISTICS:
					fragments[type.ordinal()] = new SubmissionStatisticsFragment();
					break;
				case SOLVED_PROBLEM_LEVEL:
					fragments[type.ordinal()] = new SolvedProblemLevelFragment();
					break;
	    	}
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
    public void profileReady(User json) {
    	profileReady = true;
    	user = json;
    	// TODO shouldn't it better to just call selectItem again?
    	if (selectedItem == FRAGMENT_TYPE.PROFILE_FRAGMENT) {
			ProfileFragment fragment = (ProfileFragment)getFragment(FRAGMENT_TYPE.PROFILE_FRAGMENT);
			fragment.updateProfile(user);
    	}
    }
    private boolean submissionReady = false;
    @Override
	public void submissionReady() {
    	submissionReady = true;
    	Fragment fragment = getFragment(selectedItem);
    	if (selectedItem == FRAGMENT_TYPE.PROFILE_FRAGMENT) {
    		((ProfileFragment)fragment).updateSubmission();
    	} else if (selectedItem == FRAGMENT_TYPE.LATEST_SUBMISSION) {
    		((LatestSubmissionFragment)fragment).updateSubmission(DBManager.$().getAllSubmission());
    	} else if (selectedItem == FRAGMENT_TYPE.SUBMISSION_STATISTICS) {
    		((SubmissionStatisticsFragment)fragment).updateSubmission(DBManager.$().getAllSubmission());
    	}
	}
    @Override
    public void submissionArrival(Map<Integer, Submission> submissions) {
    	if (selectedItem == FRAGMENT_TYPE.LATEST_SUBMISSION) {
    		LatestSubmissionFragment fragment = (LatestSubmissionFragment) getFragment(FRAGMENT_TYPE.LATEST_SUBMISSION);
        	fragment.updateSubmission(submissions);
    	}
    }
    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
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
	@Override
	public void serviceReady(boolean liveUpdateActive) {
		try {
			isLiveUpdate = liveUpdateActive;
			uhuntService.send(Message.obtain(null, UhuntService.MSG_REQUEST_PROFILE, uid, 0));
		} catch (RemoteException ex) {
			ex.printStackTrace();
		}
	}
	@Override
	public void enableLiveUpdate() {
		try {
			uhuntService.send(Message.obtain(null, UhuntService.MSG_ENABLE_LIVE_UPDATER));
		} catch (RemoteException ex) {
			ex.printStackTrace();
		}
	}
	@Override
	public void disableLiveUpdate() {
		try {
			uhuntService.send(Message.obtain(null, UhuntService.MSG_DISABLE_LIVE_UPDATER));
		} catch (RemoteException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void failed(String reason) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(reason)
			   .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			}).setTitle("Ooops!");
		Dialog d = builder.create();
		d.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				finish();
			}
		});
		d.show();
	}

	@Override
	public void rankReady(List<UserRank> ranks) {
		if (selectedItem == FRAGMENT_TYPE.RANK_LIST) {
			((RankFragment)getFragment(selectedItem)).updateRank(ranks);
		}
	}
}