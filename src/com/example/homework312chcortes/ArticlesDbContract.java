package com.example.homework312chcortes;

import android.provider.BaseColumns;

//Contract Class for the Db that contains the Articles
public final class ArticlesDbContract {

	public ArticlesDbContract() {
		// To prevent someone from accidentally instantiating the contract class,
	    // give it an empty constructor.
	}
	
	/* Inner class that defines the table contents for the Articles Table*/
    public static abstract class Articles implements BaseColumns {
        public static final String TABLE_NAME = "Articles";
        //using _ID as the primary key
        public static final String COLUMN_KEY_ROWID = "_ID";
        public static final String COLUMN_NAME_CONTENT = "Content";
        public static final String COLUMN_NAME_ICON = "Icon";
        public static final String COLUMN_NAME_TITLE = "Title";
        public static final String COLUMN_NAME_DATE = "Date";
    }

}
