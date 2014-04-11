package com.dieend.uvahunt;

import java.util.Locale;

import org.json.JSONException;

import android.app.SearchManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.dieend.uvahunt.callback.ProblemViewer;
import com.dieend.uvahunt.model.User;
import com.dieend.uvahunt.service.UhuntService;
import com.dieend.uvahunt.service.base.ServiceManager;


public class UvaHuntActivity extends ActionBarActivity implements ProblemViewer{
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mMenuTitles;
    private String uid;
    
    public static final String TAG = "com.dieend.uvahunt";
    public static final String PREFERENCES_FILE= TAG + ".PREFERENCES";

    private ServiceManager uhuntService;
    private User user;
    private boolean ready;
    private boolean problemReady = false;
	private boolean profileReady = false;
    private static class MessageHandler extends Handler {
    	UvaHuntActivity callee;
    	private String userdata;
    	public MessageHandler(UvaHuntActivity main) {
    		callee = main;
    	}
    	
		@Override
		public void handleMessage(Message msg) {
			try {
				switch (msg.what) {
				case UhuntService.MSG_PROFILE_READY:
					callee.profileReady = true;
					userdata = (String)msg.obj;
					populateProblem();
					break;
				case UhuntService.MSG_DETAIL_PROBLEM_READY:
					callee.problemReady = true;
					populateProblem();
					break;
				case UhuntService.MSG_READY:
					callee.uhuntService.send(Message.obtain(null, UhuntService.MSG_REQUEST_PROFILE, callee.uid)); 
					break;
				}
			} catch (RemoteException ex) {
				ex.printStackTrace();
			}
			super.handleMessage(msg);
		}
    	private void populateProblem() throws RemoteException{
    		if (callee.problemReady && callee.profileReady) {
    			callee.ready(userdata);
			}
    	}
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
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
        uhuntService = new ServiceManager(this, UhuntService.class, new MessageHandler(this));
        uhuntService.start();
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

    private void selectItem(int position) {
    	if (!ready) return;
        // update the main content by replacing fragments
    	Fragment fragment = null;
    	switch (position) {
    	case 0:
    		fragment = new ProfileFragment();
	        Bundle args = new Bundle();
	        args.putSerializable("user", user);
	        fragment.setArguments(args);
    	}

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mMenuTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }
    @Override
    public void showProblem(int problemNumber, String problemTitles) {
    	Fragment fragment = ProblemViewFragment.newInstance(problemNumber);
    	FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        mDrawerList.setItemChecked(6, true);
        setTitle(problemTitles);
    }
    private void ready(String json) {
    	ready = true;
		try {
			Log.d(TAG, json);
			user = new User(json);
		} catch (JSONException e) {
			e.printStackTrace();
		}
    	findViewById(android.R.id.progress).setVisibility(View.GONE);
    	selectItem(0);
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
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Fragment that appears in the "content_frame", shows a planet
     */
    public static class PlanetFragment extends Fragment {
        public static final String ARG_PLANET_NUMBER = "planet_number";

        public PlanetFragment() {
            // Empty constructor required for fragment subclasses
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_planet, container, false);
            int i = getArguments().getInt(ARG_PLANET_NUMBER);
            String planet = getResources().getStringArray(R.array.drawer_items)[i];

            int imageId = getResources().getIdentifier(planet.toLowerCase(Locale.getDefault()),
                            "drawable", getActivity().getPackageName());
            ((ImageView) rootView.findViewById(R.id.image)).setImageResource(imageId);
            getActivity().setTitle(planet);
            return rootView;
        }
    }
}