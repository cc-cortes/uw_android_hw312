package com.example.homework312chcortes;

import java.util.ArrayList;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.os.Handler;

public class GoogleNewsRssLoader extends RssLoader {

	static String googleNewsUrlString = "https://news.google.com/news/section?topic=w&output=rss";
	
	ArticlesDbHelper dbHelper;
	
	//A holder class to pass information for a single article
		private class ArticleInfo{
			ArticleInfo(){
				title = "";
				content = "";
				icon = "";
				date = null;
			}
			
			public String title;
			public String content;
			public String icon;
			public Date date;
		}
	
	public GoogleNewsRssLoader() {
		// TODO Auto-generated constructor stub
	}

	public GoogleNewsRssLoader(Context appContext, Handler messageCompleteHandler) {
		super(appContext, messageCompleteHandler);
		super.setUrl(googleNewsUrlString);
		
		dbHelper = new ArticlesDbHelper(appContext);
	}

	/** Read from the dom in order to place into a database **/
	@Override
	protected void loadFromDomIntoDb(Document document) {
		//Read from the Dom and put into the Db here
		
		//Get list of <item> elements
		NodeList articles = document.getElementsByTagName("item");
		ArticleInfo ai;
		
		for(int i = 0; i < articles.getLength(); i++){
			ai = getArticleInfoFromNode(articles.item(i));
			writeArticleInfoIntoDb(ai);
		}
		
		this.callLoadCompleteHandler();
	}
	

	//Get the article info from a node and put it into an object
	private ArticleInfo getArticleInfoFromNode(Node node){
		ArticleInfo ai = new ArticleInfo();
		
		//Get title
		ai.title = getArticleTitleFromNode(node);
		
		//Get content
		ai.content = getArticleContentFromNode(node);
		
		//get icon
		ai.icon = getArticleIconFromNode(node);
		
		//get date
		ai.date = getArticleDateFromNode(node);
		
		return ai;
	}

	//get string from a sub-node of an article item node
	private String getStringFromArticleNode(Node node, String localName){
		NodeList nl = node.getChildNodes();
		ArrayList<Node> subnodes = new ArrayList<Node>();
				
		Node nodePointer;
		String localNamePointer = "";
		String xmlString = "";
		
		short nodeType;
		String nodeName;
				
		//Run through nodelist, add to nodes array if localname is "date"
		for(int i = 0; i < nl.getLength(); i++){
			nodePointer = nl.item(i);
			localNamePointer = nodePointer.getLocalName(); //Produces null for ElementImpl
			nodeType = nodePointer.getNodeType();		
			nodeName = nodePointer.getNodeName(); //Produces the local name for ElementImpl
			
			if(localName.contains(nodeName)){
				subnodes.add(nodePointer);
			}
		}
				
		//Malformed XML cases	
		if(subnodes.size() == 0){
			//If zero nodes, give empty string
			xmlString = "";
			return xmlString;
		}
		else if(subnodes.size() > 1){
			//If greater than one node, throw a flag but still give the first one
		}
		
		//If one node as expected, get the string
		nodePointer = subnodes.get(0);
		
		xmlString = nodePointer.getTextContent();
						
		//get the string child of the one node
		return xmlString;
	}

	//get the string in the date element from the inputted node and convert it to a date object
	private Date getArticleDateFromNode(Node node) {
		String localName = "pubDate";
		String dateString = this.getStringFromArticleNode(node, localName);
		Date date;
		
		if(dateString == ""){
			date = new Date(Date.UTC(0, 0, 0, 0, 0, 0));
		}
		else{
			date = new Date(Date.parse(dateString));
		}
		
		return date;
	}

	//get the string in the icon element from the inputted node
	private String getArticleIconFromNode(Node node) {
		String localName = "icon";
		String iconString = this.getStringFromArticleNode(node, localName);
		
		//Should not exist for google news
		
		return iconString;
	}

	//get the string in the content element from the inputted node
	private String getArticleContentFromNode(Node node) {
		String localName = "description";
		String contentString = this.getStringFromArticleNode(node, localName);
		
		return contentString;
	}

	//get the string in the title element from the inputted node
	private String getArticleTitleFromNode(Node node) {
		String localName = "title";
		String titleString = this.getStringFromArticleNode(node, localName);
		
		return titleString;
	}

	//Read the information from an ArticleInfo object and write it into the Db
	private void writeArticleInfoIntoDb(ArticleInfo ai){
		//wrapping this as there may be some required conversion/cleaning for nulls
		
		dbHelper.addArticleToDbIfNew(ai.title, ai.content, ai.icon, ai.date);
	}

}
