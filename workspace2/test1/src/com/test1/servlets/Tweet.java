package com.test1.servlets;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;

import com.datastax.driver.core.Cluster;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import com.test1.lib.*;
import com.test1.models.*;
import com.test1.stores.*;

/**
 * Servlet implementation class Tweet
 * 
 * Meant for handling tweet requests
 */
@WebServlet({ "/Tweet", "/Tweet/*" })
public class Tweet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private Cluster cluster;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Tweet() {
        super();
    }
    
    public void init(ServletConfig config) throws ServletException {
		cluster = CassandraHosts.getCluster();
	}
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 * 
	 * TODO: Should i have put this into several servlets?
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// IF DELETE FORM METHOD IS NOT SUPPORTED - IT SHALL BECOME A GET
		// SO THIS IS THE FALLBACK FOR doDelete
		if ( request.getPathInfo() != null && request.getPathInfo().equals("/delete") ) {
			
			// Delete tweet
			
			// Check if logged in
			UserStore logged_in_user = new UserStore();
			Authentication auth = new Authentication();
			if ( !auth.isLoggedIn(request) ) {
				response.setStatus(500); 
				return;
			}
			// Get logged in user
			UserModel um = new UserModel();
			um.setCluster(cluster);
			logged_in_user = um.getUserFromUsername(auth.getLoggedInUser(request));
	
			// Get params
			String tweet_uuid = request.getParameter("tweet_uuid");
			String ajax = request.getParameter("ajax");
			// Get tweet
			TweetModel tm = new TweetModel();
			tm.setCluster(cluster);
			TweetStore ts = tm.getTweet(tweet_uuid);
			// Check permissions
			if ( logged_in_user.getUser().equals( ts.getUser() ) ) {
				tm.deleteTweet(ts);
				// if its an ajax request - just set status
				if ( ajax != null ) {
					response.setStatus(200); 
				} else {
					// else - redirect to feed
					response.sendRedirect("/test1/Tweet"); // REDIRECT TO HIS FEED
				} 
				return;
			}
			// if its an ajax request - just set status
			if ( ajax != null ) {
				response.setStatus(500); 
			} else {
				// else - redirect to feed
				response.sendRedirect("/test1/Tweet"); // REDIRECT TO HIS FEED
			}
			return;
		}
		
		// Check what kind of view are view doing
		
		boolean viewing_user_tweets = false;
		boolean viewing_specific_tweet = false;
		//boolean viewing_my_feed = true;
		String viewing_user_tweets_username = "";
		String viewing_specific_tweet_uuid = "";
		
		if ( request.getPathInfo() != null ) {
			if ( request.getPathInfo().matches("/[A-Za-z0-9]+") ) {
				viewing_user_tweets_username = request.getPathInfo().replaceAll("/","");
				viewing_user_tweets = true;
				//viewing_my_feed = false;
				//System.out.print("viewing_user_tweets");
			} else if ( request.getPathInfo().matches("/[A-Za-z0-9-]+") ) {
				viewing_specific_tweet_uuid = request.getPathInfo().replaceAll("/","");
				viewing_specific_tweet = true;
				//viewing_my_feed = false;
				//System.out.print("viewing_specific_tweet");
			} else {
				//System.out.print("viewing_my_feed");
			}
		}
		// pass state
		request.setAttribute("viewing_user_tweets_username", viewing_user_tweets_username);
		request.setAttribute("viewing_specific_tweet_uuid", viewing_specific_tweet_uuid);
		
		// get auth info
		UserStore logged_in_user = new UserStore();
		Authentication auth = new Authentication();
		if ( auth.isLoggedIn(request) ) {
			UserModel um = new UserModel();
			um.setCluster(cluster);
			logged_in_user = um.getUserFromUsername(auth.getLoggedInUser(request));
			request.setAttribute("logged_in", true);
			request.setAttribute("logged_in_user", logged_in_user); 
		} else {
			request.setAttribute("logged_in", false); 
		}
		
		TweetModel tm= new TweetModel();
		tm.setCluster(cluster);
		
		// init tweet list
		LinkedList<TweetStore> tweetList = new LinkedList<TweetStore>();
		
		// depending on the status retrieve tweets in a different way
		if ( viewing_specific_tweet ) {
			TweetStore ts = tm.getTweet(viewing_specific_tweet_uuid);
			tweetList.add(ts);
		} else if ( viewing_user_tweets && request.getParameter("from")!= null) {
			tweetList = tm.getTweets(request.getParameter("from"), "'"+request.getPathInfo().replaceAll("/","")+"'");
		} else if ( viewing_user_tweets ) { 
			tweetList = tm.getTweets("'"+request.getPathInfo().replaceAll("/","")+"'");
		} else if ( request.getParameter("from")!= null ) {
			tweetList = tm.getTweets(request.getParameter("from"), logged_in_user.getSubscribed());
		} else {
			tweetList = tm.getTweets(logged_in_user.getSubscribed());
		}
		
		// pass tweets
		request.setAttribute("Tweets", tweetList); //Set a bean with the list in it
		
		// if its an API request - just display the json
		if ( request.getParameter("api")!= null && request.getParameter("api").equals("true") ) {
			RequestDispatcher rd = request.getRequestDispatcher("/views/Tweet/api_get.jsp"); 
			rd.forward(request, response);
		} else {
			RequestDispatcher rd = request.getRequestDispatcher("/views/Tweet/view.jsp"); 
			rd.forward(request, response);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 * Save tweet
	 * I would be using put but put is not supported by traditional submitting
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Check login
		Authentication auth = new Authentication();
		if ( !auth.isLoggedIn(request) ) {
			// if not logged in - exit
			response.setStatus(500);
			return;
		}
		
		// Get passed spout param
		String spout = request.getParameter("spout");
		if ( spout == null || spout.isEmpty() == false || spout.length() == 0 || spout.length() > 140 ) {
			// if invalid spout(tweet) - return false
			response.setStatus(500); 
			response.getWriter().write("false");
		}
		
		// Setup tweet
		TweetStore ts = new TweetStore();
		ts.setTweet(spout);
		ts.setUser(auth.getLoggedInUser(request));
		
		// Save
		TweetModel tm= new TweetModel();
		tm.setCluster(cluster);
		tm.saveTweet(ts);
		
		// Return true
		response.setStatus(200); 
		response.getWriter().write("true");
		
		this.doGet(request, response);
	}
	
	/**
	 * @see HttpServlet#doPut(HttpServletRequest request, HttpServletResponse response)
	 * Handles tweet (saving) put requests
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Check login
		Authentication auth = new Authentication();
		if ( !auth.isLoggedIn(request) ) {
			// if not logged in - exit
			response.setStatus(500);
			return;
		}
		
		// for some reason you can't get put attributes from getParameter()
		// probably because PUT is deprecated
		BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
		String data = br.readLine();
	    String[] params = data.split("&");  
	    Map<String, String> map = new HashMap<String, String>();  
	    for (String param : params) {  
	        String name = param.split("=")[0];  
	        String value = param.split("=")[1];  
	        map.put(name, value);  
	    }  
	    
		//String spout = request.getParameter("spout");
		String spout = map.get("spout");
		spout = URLDecoder.decode(spout, "UTF-8");
		
		if ( spout == null || spout.isEmpty() == false || spout.length() == 0 || spout.length() > 140 ) {
			// if invalid spout(tweet) - return false
			response.setStatus(500); 
			response.getWriter().write("false");
		}
			
		// Setup tweet
		TweetStore ts = new TweetStore();
		ts.setTweet(spout);
		ts.setUser(auth.getLoggedInUser(request));
		
		// Save
		TweetModel tm= new TweetModel();
		tm.setCluster(cluster);
		tm.saveTweet(ts);
		
		response.setStatus(200); 
		response.getWriter().write("true");
		
		return;
	}
	/**
	 * @see HttpServlet#doDelete(HttpServletRequest request, HttpServletResponse response)
	 * Delete tweet request
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Delete tweet
		// Check if logged in
		UserStore logged_in_user = new UserStore();
		Authentication auth = new Authentication();
		if ( !auth.isLoggedIn(request) ) {
			response.setStatus(500); 
			return;
		}
		// Get logged in user
		UserModel um = new UserModel();
		um.setCluster(cluster);
		logged_in_user = um.getUserFromUsername(auth.getLoggedInUser(request));

		// Get params
		// for some reason you can't get delete attributes from getParameter()
		// probably because DELETE is deprecated
		BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
		String data = br.readLine();
	    String[] params = data.split("&");  
	    Map<String, String> map = new HashMap<String, String>();  
	    for (String param : params) {  
	        String name = param.split("=")[0];  
	        String value = param.split("=")[1];  
	        map.put(name, value);  
	    }  
	    String tweet_uuid = map.get("tweet_uuid");
		String ajax = map.get("ajax");
		//String tweet_uuid = request.getParameter("tweet_uuid");
	    //String ajax = request.getParameter("ajax");

		// Get tweet
		TweetModel tm = new TweetModel();
		tm.setCluster(cluster);
		TweetStore ts = tm.getTweet(tweet_uuid);
		// Check permissions
		if ( logged_in_user.getUser().equals( ts.getUser() ) ) {
			tm.deleteTweet(ts);
			// if its an ajax request - just set status
			if ( ajax != null ) {
				response.setStatus(200); 
			} else {
				// else - redirect to feed
				response.sendRedirect("/test1/Tweet"); // REDIRECT TO HIS FEED
			} 
			return;
		}
		// if its an ajax request - just set status
		if ( ajax != null ) {
			response.setStatus(500); 
		} else {
			// else - redirect to feed
			response.sendRedirect("/test1/Tweet"); // REDIRECT TO HIS FEED
		}
		return;
	}
}
