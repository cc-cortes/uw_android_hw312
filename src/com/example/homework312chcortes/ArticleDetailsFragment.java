package com.example.homework312chcortes;

import com.example.homework312chcortes.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

public class ArticleDetailsFragment extends Fragment {
	
	//Members
	String articleTitle;
	String articleContent;
	String articleDate;
	
	public ArticleDetailsFragment() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void  onCreate(Bundle savedInstanceState){
		//Hide this fragment at first creation
		this.hideDetailsFragment();
		
		super.onCreate(savedInstanceState);
	}
	

	 @Override
	 public void onSaveInstanceState(Bundle savedInstanceState) {
	    super.onSaveInstanceState(savedInstanceState);
	    
	    savedInstanceState.putString("title", getArticleTitle());
	    savedInstanceState.putString("content", getArticleContent());
	    savedInstanceState.putString("date", getArticleDate());
	    
	 }
	 
	// Restore UI state. This bundle has also been passed to onCreate of activity.
	 @Override
	 public void onActivityCreated(Bundle savedInstanceState) {
	    super.onActivityCreated(savedInstanceState);
	    
	    if(savedInstanceState == null){
	    	return;
	    }
	    
	    //Put the text back in the text views
	    articleTitle = savedInstanceState.getString("title");
	    articleContent = savedInstanceState.getString("content"); 
	    articleDate = savedInstanceState.getString("date");
	    
	    if(articleTitle != null && articleContent != null && articleDate != null){
	    	this.setTitleTextView(articleTitle);
		    this.setContentTextView(articleContent);
		    this.setDateTextView(articleDate);
	    }
	 }


	@Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	        // Inflate the layout for this fragment
		 
	        return inflater.inflate(R.layout.fragment_article_details, container, false);
	    }
	
	public void setUIByArticleId(long id){
		
		setArticleTitleById(id);
		
		setArticleContentById(id);
		
		setArticleDateById(id);
		
	}

	private void setArticleContentById(long id) {
		//Get reference to a new database helper
	    ArticlesDbHelper dbHelper = new ArticlesDbHelper(getActivity().getBaseContext());
		
	    articleContent = dbHelper.getArticleContentById(id);
	    
	    setContentTextView(articleContent);
	}

	private void setArticleTitleById(long id) {
		//Get reference to a new database helper
	    ArticlesDbHelper dbHelper = new ArticlesDbHelper(getActivity().getBaseContext());
		
	    articleTitle = dbHelper.getArticleTitleById(id);
	    
	    setTitleTextView(articleTitle);
	}
	
	private void setArticleDateById(long id) {
		//Get reference to a new database helper
	    ArticlesDbHelper dbHelper = new ArticlesDbHelper(getActivity().getBaseContext());
		
	    articleDate = dbHelper.getArticleDateById(id);
	    
	    setDateTextView(articleDate);
	}
	
	private void setContentTextView(String content){
		//Put the string of html into the textview
		WebView wv = (WebView) getView().findViewById(R.id.article_details_content);
		
		String mime = "text/html";
		String encoding = "utf-8";

		wv.getSettings().setJavaScriptEnabled(true);
		wv.loadDataWithBaseURL(null, content, mime, encoding, null);
	}
	
	private void setTitleTextView(String title){
		//put the string into the textview
		TextView tv = (TextView) getView().findViewById(R.id.article_details_title);
		tv.setText(title);
	}
	
	private void setDateTextView(String date){
		//put the string into the textview
		TextView tv = (TextView) getView().findViewById(R.id.article_details_date);
		tv.setText(date);
	}
	
	 private String getArticleContent() {
		return articleContent;
	}

	private String getArticleTitle() {
		return articleTitle;
	}
	
	private String getArticleDate() {
		return articleDate;
	}

	private void hideDetailsFragment() {
		FragmentManager fm = getActivity().getSupportFragmentManager();
		
		Fragment f = fm.findFragmentByTag(MainActivity.DETAILS_TAG);
		
		FragmentTransaction ft = fm.beginTransaction();
		
		if(f != null){
			ft.hide(f);
			ft.commit();
		}
	}


}
