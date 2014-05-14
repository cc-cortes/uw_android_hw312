package com.example.homework312chcortes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.regex.Matcher;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Xml;
import android.widget.ImageView;
import android.widget.Toast;

//Loads Rss Feeds into the DB
public abstract class RssLoader {

	//Members
	Handler completeHandler;
	
	private String urlString; //Store the url for google news
	private StringBuilder xmlBuilder; //String builder to put the xml together as it comes in from the web
	private static String USER_AGENT = "HelloHTTP/1.0"; //User agent for http requests
    private AndroidHttpClient httpClient; //http client for requests
    private Context context;

	//Methods
	public RssLoader() {
		// TODO Auto-generated constructor stub
	}
	
	//Methods
	public RssLoader(Context appContext, Handler messageCompleteHandler) {
		setContext(appContext);
		
		setHandler(messageCompleteHandler);
	}
	
	private void setHandler(Handler messageCompleteHandler) {
		completeHandler = messageCompleteHandler;
		
	}
	
	/** To be called when the loading of RSS data into the DB is complete. Passes message back to UI thread */
	protected void callLoadCompleteHandler(){
		
		Bundle data = new Bundle();
        data.putString("RssLoadComplete", "true");
        Message msg = new Message();
        msg.setData(data);
        completeHandler.sendMessage(msg);
	}

	//Set the app's Context
	public void setContext(Context appContext){
		context = appContext;
	}
	
	/** get the context provided*/
	protected Context getContext(){
		return context;
	}
	
	//Set the url. Returns false if the url is not of an expected WEB_URL format
	public boolean setUrl(String url){
		
		//Clean the string
		
		//Add http:// if necessary
		//Clean up the url, add http://if it needs it
		if (!url.startsWith("http://") && !url.startsWith("https://")){
			url = "http://" + url;
		}
		
		//Check if the inputted url fits the pattern
		Matcher matcher = android.util.Patterns.WEB_URL.matcher(url);
		boolean matchesUrlPattern = matcher.matches();
		
		if(!matchesUrlPattern){
			return false;
		}
		
		urlString = url;
		return true;
	}
	
	//Get the url
	public String getUrl(){
		return urlString;
	}
	
	/**Load the feed into the database Asynchronously**/
	public void loadFeedIntoDb(){
		new GetAsyncTask().execute(this.getUrl());
	}
	
	//Class called to load into the DB
	protected abstract void loadFromDomIntoDb(Document document);
	
	
	//https://github.com/uw/aad/blob/master/samples/HelloHTTP/src/aad/app/hello/http/MainActivity.java
	 /** An AsycTask used to update the retrieved HTTP header and content displays */
    private class GetAsyncTask extends AsyncTask<String, Void, Document> {
    	
        @Override
        protected Document doInBackground(String... urls) {


        	httpClient = AndroidHttpClient.newInstance(USER_AGENT);
             
             HttpResponse response = null;
             
             String urlString = urls[0];
             
             if (urlString == null) {
            	 Log.e("RssReader Async Task", "No valid URL string provided");
            	 return null;
             }
             
             // Make a GET request and execute it to return the response 
             HttpGet request = new HttpGet(urlString);
             try {
                 response = httpClient.execute(request);
             }
             catch (IOException e) {
                 e.printStackTrace();
             }
            
            if (response == null) {
                Log.e("RssReader Async Task", "Error accessing: " + urlString);
                Toast.makeText(context, "Error accessing: " + urlString, Toast.LENGTH_LONG).show(); //Pops up a toast in the UI at an error
                return null;
            }
            
            // Get the content
            BufferedReader bf;
            StringBuilder sb = new StringBuilder();
            try {
                bf = new BufferedReader(new InputStreamReader(response.getEntity().getContent()), 8192);
                sb.setLength(0); // Reuse the StringBuilder
                String line;
                while ((line = bf.readLine()) != null) {
                    sb.append(line); //Read each line from the buffer and append it to the string builder
                }
                bf.close();
            }
            catch (IllegalStateException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            
            //Put contents of String Builder into the DOM
            
            Document doc;
            
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setNamespaceAware(false);
                dbf.setValidating(false);
                DocumentBuilder db = dbf.newDocumentBuilder();
                doc = db.parse(new InputSource(new StringReader(sb.toString())));
                int i = 1;
                i++; //debug break to view the doc
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            
            httpClient.close();
			return doc;            
        }
        
        //After the pull is completed and contents in a dom, load into the db
        @Override
        protected void onPostExecute(Document doc) {


            if (doc == null) {
                Log.e("Get Async Task", "Error loading DOM");
                return;
            }
            
            
            loadFromDomIntoDb(doc);
                                    
            super.onPostExecute(doc);
        }
        
    } //End of GetAsyncTask for getting XML

    
    /** An AsycTask used to retrieve a bitmap from a URL */
    private class GetImageAsyncTask extends AsyncTask<String, Void, Bitmap> {


        private URL downloadURL;
        private ImageView mImageView;
        
        public GetImageAsyncTask(ImageView imageView) {
            mImageView = imageView;
        }
        
        @Override
        protected Bitmap doInBackground(String... urls) {
            Bitmap bitmap = null;
            try {
                // Just get the first URL
                if (urls.length > 0) {
                    downloadURL = new URL(urls[0]);
                    InputStream is = downloadURL.openStream();


                    Log.i("Get Image Async Task", "doInBackground() url: " + downloadURL);


                    boolean unsafe = true;
                    if (unsafe) {
                        bitmap = BitmapFactory.decodeStream(is);
                    }
                    else {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        bitmap = BitmapFactory.decodeStream(is, null, options);
                        options.inJustDecodeBounds = false;


                        // Need to reopen the stream
                        is = downloadURL.openStream();
                        if (options.outWidth > 1024) {
                            options.inSampleSize = 16; // 1/16
                            bitmap = BitmapFactory.decodeStream(is, null, options);
                        } else {
                            bitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.RGB_565);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("Get Image Async Task", "doInBackground() e: ", e);
            }


            return bitmap;
        }


        @Override
        protected void onPostExecute(Bitmap bitmap) {


            if (bitmap == null) {
                Log.e("Get Image Async Task", "Error accessing: " + downloadURL);
                return;
            }
            
            if (mImageView != null)
                mImageView.setImageBitmap(bitmap);
                                    
            super.onPostExecute(bitmap);
        }
        
    } //End of Get Image AsyncTask

	
	
	
	
	//https://github.com/uw/aad/blob/master/samples/HelloHTTP/src/aad/app/hello/http/MainActivity.java
}
