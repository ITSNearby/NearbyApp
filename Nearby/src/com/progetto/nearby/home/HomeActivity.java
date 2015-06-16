package com.progetto.nearby.home;



import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.progetto.nearby.GPSProvider;
import com.progetto.nearby.R;
import com.progetto.nearby.Tools;
import com.progetto.nearby.Filtri.FiltriActivity;
import com.progetto.nearby.navigationdrawer.NavigationDrawerFragment;
import com.progetto.nearby.offerte.OfferteFragment;

public class HomeActivity extends AppCompatActivity implements
		NavigationDrawerFragment.NavigationDrawerCallbacks {

	private NavigationDrawerFragment mNavigationDrawerFragment;

	private CharSequence mTitle;
	private HomeFragment homefragment;
	private OfferteFragment offertefragment;
	private FragmentManager fragmentmanager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		Tools.gpsProvider = new GPSProvider(this);
		
		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
				.findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();
		
		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));
		}

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		fragmentmanager = getFragmentManager();
		switch(position)
		{
			case 0:
			{
				if(fragmentmanager.findFragmentByTag(HomeFragment.TAG) == null)
				{
					Bundle bundle = null;
					homefragment = HomeFragment.newInstance(bundle);
				}
				else
					homefragment = (HomeFragment) fragmentmanager.findFragmentByTag(HomeFragment.TAG);
					
				fragmentmanager
					.beginTransaction()
					.replace(R.id.container, homefragment, HomeFragment.TAG).commit();
			}
				break;
			case 1:
			{
				if(fragmentmanager.findFragmentByTag(OfferteFragment.TAG) == null)
				{
					Bundle bundle = null;
					offertefragment = OfferteFragment.newInstance(bundle);
				}
				else
					offertefragment = (OfferteFragment) fragmentmanager.findFragmentByTag(OfferteFragment.TAG);
					fragmentmanager
						.beginTransaction()
						.replace(R.id.container,  offertefragment, OfferteFragment.TAG)
						.addToBackStack(null)
						.commit();
			}
				break;
			default:
				fragmentmanager
				.beginTransaction()
				.replace(R.id.container,
						PlaceholderFragment.newInstance(position + 1))
						.addToBackStack(null)
						.commit();
				break;
		}
		
	}

	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			((HomeActivity) activity).onSectionAttached(getArguments().getInt(
					ARG_SECTION_NUMBER));
		}
	}
	public void onSectionAttached(int number) {
		switch (number) {
		case 1:
			mTitle = getString(R.string.title_section1);
			break;
		case 2:
			mTitle = getString(R.string.title_section2);
			break;
		case 3:
			mTitle = getString(R.string.title_section3);
			break;
		}
	}

	public void restoreActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			// Only show items in the action bar relevant to this screen
			// if the drawer is not showing. Otherwise, let the drawer
			// decide what to show in the action bar.
			getMenuInflater().inflate(R.menu.main, menu);
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent filtriIntent = new Intent(HomeActivity.this, FiltriActivity.class);
			startActivity(filtriIntent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onDestroy() {
		Tools.gpsProvider.stopUsingGPS();
		Tools.gpsProvider = null;
		super.onDestroy();
	}
}
