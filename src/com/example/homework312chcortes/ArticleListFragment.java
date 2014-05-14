package com.example.homework312chcortes;

import com.example.homework312chcortes.R;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.widget.SimpleCursorAdapter;

public class ArticleListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	
	 // This is the Adapter being used to display the list's data.
    SimpleCursorAdapter mAdapter;
    
    // These are the Articles columns that we will retrieve.
    static final String[] ARTICLES_PROJECTION = new String[] {
        ArticlesDbContract.Articles.COLUMN_KEY_ROWID,
        ArticlesDbContract.Articles.COLUMN_NAME_TITLE,
        ArticlesDbContract.Articles.COLUMN_NAME_ICON,
        ArticlesDbContract.Articles.COLUMN_NAME_DATE
    };
	
	public ArticleListFragment() {
		// TODO Auto-generated constructor stub
	}
	
	@Override    
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) { 
		
		return super.onCreateView(inflater, container, savedInstanceState);    
	}    
	
	@Override 
	public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Give some text to display if there is no data.
        setEmptyText(getString(R.string.no_articles_message));

        // We have no menu items to show in action bar.
        setHasOptionsMenu(false);

        // Create an empty adapter we will use to display the loaded data.
        mAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.article_list_item, null,
                new String[] { ArticlesDbContract.Articles.COLUMN_NAME_TITLE, ArticlesDbContract.Articles.COLUMN_NAME_DATE},
                new int[] { R.id.list_item_title, R.id.list_item_date}, 0);
        setListAdapter(mAdapter);

        // Start out with a progress indicator.
        //setListShown(false);

        // Prepare the loader.  Either re-connect with an existing one or start a new one.
        getLoaderManager().initLoader(0, null, this);
    }     
	

	@Override
	public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {

		// This is called when a new Loader needs to be created. This only has one Loader, so don't care about the ID
        // First, pick the base URI for articlesContentProvider to use
        Uri baseUri = ArticlesProvider.CONTENT_URI;
        
        String sortOrder = ArticlesDbContract.Articles.COLUMN_NAME_DATE + " DESC";

        //Create and return a CursorLoader that will take care of creating a Cursor for the data being displayed.
        // Null select statement returns all rows, no selectionArgs, don't care about sortOrder
        return new CursorLoader(getActivity(), baseUri, ARTICLES_PROJECTION, null, null, sortOrder);
	}


	@Override
	public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
		// Swap the new cursor in.  (The framework will take care of closing the old cursor once we return.)
        mAdapter.swapCursor(data);

        // The list should still not be shown, need to wait for download of RSS data
        /*
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
        */
		
	}

	@Override
	public void onLoaderReset(android.support.v4.content.Loader<Cursor> arg0) {
		// This is called when the last Cursor provided to onLoadFinished() is about to be closed.
		// We need to make sure we are no longer using it.
        mAdapter.swapCursor(null);
	} 
	
	@Override
	public void  onListItemClick (ListView l, View v, int position, long id){
		//super.onListItemClick(l, v, position, id);
		
		//Open article fragment with id
		FragmentManager fm = getActivity().getSupportFragmentManager();
		
		ArticleDetailsFragment f = (ArticleDetailsFragment)fm.findFragmentByTag(MainActivity.DETAILS_TAG);
		
		if(f != null){
			f.setUIByArticleId(id);

			//Show the details fragment and hide this list fragment
			showDetailsFragment();
			hideListFragment();
		}
	}
	
	private void showDetailsFragment() {
		FragmentManager fm = getActivity().getSupportFragmentManager();
		
		Fragment f = fm.findFragmentByTag(MainActivity.DETAILS_TAG);
		
		FragmentTransaction ft = fm.beginTransaction();
		
		if(f != null){
			ft.show(f);
			ft.commit();
			
			//Hacky to make an assumption on the activity calling the fragment
			MainActivity a = (MainActivity) getActivity();
			a.viewState = MainActivity.DETAILS_MODE;
		}
	}

	private void hideListFragment() {
		FragmentManager fm = getActivity().getSupportFragmentManager();
		
		Fragment f = fm.findFragmentByTag(MainActivity.LIST_TAG);
		
		FragmentTransaction ft = fm.beginTransaction();
		
		if(f != null){
			ft.hide(f);
			ft.commit();
			
			
		}
	}
	
	/** Sets the loading state for this List, including the "refreshing" bar and animation */
	public void setLoadingState(){
		//Set the list shown as false to show loading animation
		//this.setListShown(false);
		
		//Set the "Refreshing" header to visible
		//View v = getView();
		//TextView header = (TextView) v.findViewById(R.id.list_fragment_header);
		//header.setVisibility(View.VISIBLE);
	}
	
	/** Sets the load completed state for this List, including removal of the "refreshing" bar and animation */
	public void setLoadCompleteState(){
		//Set the list shown as false to show loading animation
		this.setListShown(true);
		
		//Bug with adding a custom layout in ListFragment, need to add it to main layout
		
		//Refresh the adapter
		mAdapter.notifyDataSetChanged();
		
		//restart loader manager of the article list fragment
		LoaderManager lm = getLoaderManager();
		lm.restartLoader(0, null, this);
	}
}
