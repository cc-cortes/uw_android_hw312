package com.example.homework312chcortes;

import java.util.Date;

import com.example.homework312chcortes.R;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.AlertDialog;
import android.content.res.AssetManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity; 
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;

public class MainActivity extends FragmentActivity {
	

	//*********Members************************//
	
	//Reference to the Database
	ArticlesDbHelper dbHelper;
	
	//State of the UI, determines which fragment, list or details, is visible
	int viewState = 0;
	
	//Modes for switching between a list and the details page
	public static final int LIST_MODE = 1;
	public static final int DETAILS_MODE = 2;
	
	public static final String LIST_TAG = "list fragment";
	public static final String DETAILS_TAG = "details fragment";
	
	//Shaking event manager
	private ShakeEventManager mShaker;
	
	//Handler for message sent to UI thread when list data is collected from online
	// Handler for updating the UI
    private Handler rssLoadCompleteHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            
            String update = msg.getData().getString("RssLoadComplete");
            if (update != null){
            	updateListFragmentOnLoadComplete();
            }
        }                
    };

	
	//*********Methods************************//
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//Set reference to a new database helper
	    dbHelper = new ArticlesDbHelper(getBaseContext());
	    
	    //Get Articles from Google News and Yahoo and put them into the db
		readRssFeedsIntoDb();
		
		//Add the list and details fragments into the UI, if they haven't already been added
		addFragmentsToUI();
		
		//Set the Shake Event Manager
		setShakeEventManager();
		
		//Set which fragments are visible
		setFragmentVisibleState();
	}
	
	/** loads all Rss Feeds into the Database Asynchronously **/
	private void readRssFeedsIntoDb() {
		//Clear current DB entries
		//dbHelper.clearArticlesInDatabase();
		
		this.setRefreshBarVisible();
		
		//Add google news feed data into db
		GoogleNewsRssLoader gRssLoader = new GoogleNewsRssLoader(this, rssLoadCompleteHandler);
		gRssLoader.loadFeedIntoDb();
		
		//Add Yahoo news feed data into Db
		YahooNewsRssLoader yRssLoader = new YahooNewsRssLoader(this, rssLoadCompleteHandler);
		yRssLoader.loadFeedIntoDb();
	}

	private void setShakeEventManager() {
		 mShaker = new ShakeEventManager(this);
		    mShaker.setOnShakeListener(new ShakeEventManager.OnShakeListener () {
		      public void onShake()
		      {
		    	showOnShakeMessage();
		        readRssFeedsIntoDb();
		      }
		    });
	}
	
	/*
	@Override
	  public void onResume()
	  {
	    mShaker.resume();
	    super.onResume();
	  }
	  @Override
	  public void onPause()
	  {
	    mShaker.pause();
	    super.onPause();
	  }
	  */
	
	public void showOnShakeMessage(){
		this.setRefreshBarVisible();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    	case R.id.action_settings:
	    		clearArticles();
	    		return true;
	    	default:
	    		return false;
	    }
	}
	
	private void clearArticles(){
		dbHelper.clearArticlesInDatabase();
		
		Toast t = Toast.makeText(this, "Articles Deleted", Toast.LENGTH_SHORT);
		t.show();
		
		updateList();
	}
	
	// Save the Instance state of the UI
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	  super.onSaveInstanceState(savedInstanceState);
	  
	  //Save the view mode state
	  savedInstanceState.putInt("view_state", this.viewState);
	}
			
	// Restore UI state. This bundle has also been passed to onCreate.
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);	  
		
		//Restore the view mode state
		this.viewState = savedInstanceState.getInt("view_state");
		
		//Set the visibility of the fragments
		this.setFragmentVisibleState();
	}
	

	private void addFragmentsToUI() {
		addListFragmentToUI();
		addDetailsFragmentToUI();
	}
	
	//Adds the Article List Fragment to the UI, if it hasn't already been added
	private void addListFragmentToUI(){
		
		ArticleListFragment alf = new ArticleListFragment();
		
		FragmentManager fm = this.getSupportFragmentManager();
		
		Fragment f = fm.findFragmentByTag(LIST_TAG);
		
		if(f == null){
			//There is no fragment with that tag, so add it
			FragmentTransaction ft = fm.beginTransaction();
			ft.add(R.id.listfragmentholder, alf, LIST_TAG);
			ft.commit();
		}
	}
	
	//Adds the Article Details Fragment to the UI, if it hasn't already been added
	private void addDetailsFragmentToUI(){

		ArticleDetailsFragment adf = new ArticleDetailsFragment();
			
		FragmentManager fm = this.getSupportFragmentManager();
			
		Fragment f = fm.findFragmentByTag(DETAILS_TAG);
			
		if(f == null){
			//There is no fragment with that tag, so add it
			FragmentTransaction ft = fm.beginTransaction();
			ft.add(R.id.detailsfragmentholder, adf, DETAILS_TAG);
			ft.commit();
		}
	}
	
	//Set which fragments are visible
	private void setFragmentVisibleState(){
		if(this.viewState == 0){
			showListFragment();
			hideDetailsFragment();
		}
		else if(this.viewState == MainActivity.LIST_MODE){
			showListFragment();
			hideDetailsFragment();
		}
		else if(this.viewState == MainActivity.DETAILS_MODE){
			hideListFragment();
			showDetailsFragment();
		}
	}

	private void showDetailsFragment() {
		FragmentManager fm = this.getSupportFragmentManager();
		
		Fragment f = fm.findFragmentByTag(DETAILS_TAG);
		
		FragmentTransaction ft = fm.beginTransaction();
		
		if(f != null){
			ft.show(f);
			ft.commit();
			this.viewState = MainActivity.DETAILS_MODE;
		}
	}

	private void hideListFragment() {
		FragmentManager fm = this.getSupportFragmentManager();
		
		Fragment f = fm.findFragmentByTag(LIST_TAG);
		
		FragmentTransaction ft = fm.beginTransaction();
		
		if(f != null){
			ft.hide(f);
			ft.commit();
		}
	}

	private void hideDetailsFragment() {
		FragmentManager fm = this.getSupportFragmentManager();
		
		Fragment f = fm.findFragmentByTag(DETAILS_TAG);
		
		FragmentTransaction ft = fm.beginTransaction();
		
		if(f != null){
			ft.hide(f);
			ft.commit();
		}
	}

	private void showListFragment() {
		
		FragmentManager fm = this.getSupportFragmentManager();
		
		Fragment f = fm.findFragmentByTag(LIST_TAG);
		
		FragmentTransaction ft = fm.beginTransaction();
		
		if(f != null){
			ft.show(f);
			ft.commit();
			this.viewState = MainActivity.LIST_MODE;
		}
	}
	
	//If the details fragment is visible, make the list fragment visible
	@Override
	public void  onBackPressed (){
		FragmentManager fm = this.getSupportFragmentManager();
		
		Fragment f = fm.findFragmentByTag(DETAILS_TAG);
		
		if(f != null && !f.isHidden()){
			showListFragment();
			hideDetailsFragment();
		}
		else{
			super.onBackPressed();
		}
	}

	/** Updates the list fragment to show data. Called when RSS Loader has completed */
	private void updateListFragmentOnLoadComplete(){
		FragmentManager fm = this.getSupportFragmentManager();
		
		Fragment f = fm.findFragmentByTag(LIST_TAG);
		
		if(f == null){
			return;
		}
		
		//Set the "Refreshing" header to not visible
		TextView header = (TextView) findViewById(R.id.refreshing_textview);
		header.setVisibility(View.GONE);
		
		//Cast to Article List Fragment to get access to adapter
		ArticleListFragment alf = (ArticleListFragment) f;
		
		//Set the load complete state
		alf.setLoadCompleteState();
	}
	
	/** force the list of articles to update */
	private void updateList(){
		FragmentManager fm = this.getSupportFragmentManager();
		
		Fragment f = fm.findFragmentByTag(LIST_TAG);
		
		if(f == null){
			return;
		}
		
		//Cast to Article List Fragment to get access to adapter
		ArticleListFragment alf = (ArticleListFragment) f;
		
		//Set the load complete state
		alf.setLoadCompleteState();
	}
	
	private void setRefreshBarVisible(){
		TextView header = (TextView) findViewById(R.id.refreshing_textview);
		header.setVisibility(View.VISIBLE);
	}
	
}
