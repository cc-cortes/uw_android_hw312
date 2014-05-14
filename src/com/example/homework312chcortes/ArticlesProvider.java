package com.example.homework312chcortes;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;


public class ArticlesProvider extends ContentProvider {

    //Defines a handle to the database helper object.
    private ArticlesDbHelper dbHelper;
    
    // authority is the symbolic name of your provider
    private static final String AUTHORITY = "com.example.homework312chcortes";
    
    // create content URIs from the authority by appending path to database table
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/articles");
    
    //URI codes
    private static final int SINGLE_ARTICLE = 1;
    private static final int ALL_ARTICLES = 2;
    
    // a content URI pattern matches content URIs using wildcard characters:
    // #: Matches a string of numeric characters of any length.
    private static final UriMatcher uriMatcher;
    static {
     uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
     uriMatcher.addURI(AUTHORITY, "articles", ALL_ARTICLES);
     uriMatcher.addURI(AUTHORITY, "articles/#", SINGLE_ARTICLE);
    }

	public ArticlesProvider() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		switch (uriMatcher.match(uri)) {
		  case ALL_ARTICLES:
		   //do nothing 
		   break;
		  case SINGLE_ARTICLE:
		   String id = uri.getPathSegments().get(1);
		   selection = ArticlesDbContract.Articles.COLUMN_KEY_ROWID + "=" + id
		   + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
		   break;
		  default:
		   throw new IllegalArgumentException("Unsupported URI: " + uri);
		  }

		int deletedRows = db.delete(ArticlesDbContract.Articles.TABLE_NAME, selection, selectionArgs);
		 
		getContext().getContentResolver().notifyChange(uri, null);

		return deletedRows;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		  case ALL_ARTICLES: 
		   return "vnd.android.cursor.dir/vnd.com.example.homework311chcortes.articles";
		  case SINGLE_ARTICLE: 
		   return "vnd.android.cursor.item/vnd.com.example.homework311chcortes.articles";
		  default: 
		   throw new IllegalArgumentException("Unsupported URI: " + uri);
		  }
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		
		//Check for malformed uri
		switch (uriMatcher.match(uri)) {
		  case ALL_ARTICLES:
		   //do nothing, expected uri for insert
		   break;
		  default:
		   throw new IllegalArgumentException("Unsupported URI: " + uri);
		 }


		 SQLiteDatabase db = dbHelper.getWritableDatabase();
		  
		 long id = db.insert(ArticlesDbContract.Articles.TABLE_NAME, null, values);
		  
		 getContext().getContentResolver().notifyChange(uri, null);
		  
		 return Uri.parse(CONTENT_URI + "/" + id);
	}

	@Override
	public boolean onCreate() {
		dbHelper = new ArticlesDbHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		
        SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
        
        // Set the table to query
        qBuilder.setTables(ArticlesDbContract.Articles.TABLE_NAME);
        
        switch (uriMatcher.match(uri)) {
        case ALL_ARTICLES:
         //do nothing 
         break;
        case SINGLE_ARTICLE:
         String id = uri.getPathSegments().get(1);
         qBuilder.appendWhere(ArticlesDbContract.Articles.COLUMN_KEY_ROWID + "=" + id);
         break;
        default:
         throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        // Make the query
        Cursor c = qBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        
        c.setNotificationUri(getContext().getContentResolver(), uri);
        
        return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		switch (uriMatcher.match(uri)) {
		  case ALL_ARTICLES:
		   //do nothing 
		   break;
		  case SINGLE_ARTICLE:
		   String id = uri.getPathSegments().get(1);
		   selection = ArticlesDbContract.Articles.COLUMN_KEY_ROWID + "=" + id
		   + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
		   break;
		  default:
		   throw new IllegalArgumentException("Unsupported URI: " + uri);
		  }

		int updateCount = db.update(ArticlesDbContract.Articles.TABLE_NAME, values, selection, selectionArgs);
		
		getContext().getContentResolver().notifyChange(uri, null);
		
		return updateCount;
	}

}
