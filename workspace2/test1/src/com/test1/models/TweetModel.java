package com.test1.models;

import java.util.LinkedList;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

//import com.test1.lib.*;
import com.test1.stores.TweetStore;

/*
CREATE TABLE ac32007_rihardssilins.tweets (
	user varchar,
	tweet varchar,
	timeuuid timeuuid,
	PRIMARY KEY (user,timeuuid)
) WITH CLUSTERING ORDER BY (timeuuid DESC);
 */

/**
 * Class TweetModel
 * Tweet model
 * @author Rihards Silins
 */
public class TweetModel {
	Cluster cluster;
	String keyspace;
	public TweetModel(){
		keyspace = "ac32007_rihardssilins";
	}

	public void setCluster(Cluster cluster){
		this.cluster=cluster;
	}
	
	/**
	 * TweetModel::getTweet()
	 * Returns tweet that has the corresponding uuid
	 * @param String uuid
	 * @return TweetStore ts / null
	 */
	public TweetStore getTweet(String uuid) {
		TweetStore ts = new TweetStore();
		Session session = cluster.connect(keyspace);
		String query = "SELECT * from tweets WHERE timeuuid = "+uuid+" ALLOW FILTERING";
		PreparedStatement statement = session.prepare(query);
		BoundStatement boundStatement = new BoundStatement(statement);
		ResultSet rs = session.execute(boundStatement);
		if (rs.isExhausted()) {
			return null;
		} else {
			for (Row row : rs) {
				ts.setTweet(row.getString("tweet"));
				ts.setUser(row.getString("user"));
				ts.setTimeuuid(row.getUUID("timeuuid"));
			}
		}
		session.close();
		return ts;
	}
	
	/**
	 * TweetModel::deleteTweet()
	 * Deletes tweet
	 * @param TweetStore ts
	 * @return boolean success
	 */
	public boolean deleteTweet(TweetStore ts) {
		Session session = cluster.connect(keyspace);
		String query = "DELETE from tweets WHERE timeuuid = "+ts.getTimeuuid().toString()+" AND user = '"+ts.getUser()+"'";
		PreparedStatement statement = session.prepare(query);
		BoundStatement boundStatement = new BoundStatement(statement);
		session.execute(boundStatement);
		session.close();
		return true;
	}
	
	/**
	 * TweetModel::getTweets()
	 * Returns all tweets from users mentioned in the passed (subscribe) string
	 * @param String subscribed
	 * @return LinkedList<TweetStore> 
	 */
	public LinkedList<TweetStore> getTweets(String subscribed) {
		LinkedList<TweetStore> tweetList = new LinkedList<TweetStore>();
		Session session = cluster.connect(keyspace);

		String query = "";
		if ( subscribed.isEmpty() ) {
			query = "SELECT * from tweets";
		} else {
			query = "SELECT * from tweets WHERE user IN ("+subscribed.replaceAll("\"", "'")+") ALLOW FILTERING";
		}
		//System.out.print(query);
		PreparedStatement statement = session.prepare(query);
		BoundStatement boundStatement = new BoundStatement(statement);
		ResultSet rs = session.execute(boundStatement);
		if (rs.isExhausted()) {
			System.out.println("No Tweets returned");
		} else {
			for (Row row : rs) {
				TweetStore ts = new TweetStore();
				ts.setTweet(row.getString("tweet"));
				ts.setUser(row.getString("user"));
				ts.setTimeuuid(row.getUUID("timeuuid"));
				tweetList.add(ts);
			}
		}
		session.close();
		return tweetList;
	}
	
	/**
	 * TweetModel::getTweets()
	 * Returns all tweets from users 
	 * 		mentioned in the passed (subscribe) string
	 * 		and after time passed in String from
	 * @param String from, String subscribed
	 * @return LinkedList<TweetStore> 
	 */
	public LinkedList<TweetStore> getTweets(String from, String subscribed) {
		LinkedList<TweetStore> tweetList = new LinkedList<TweetStore>();
		Session session = cluster.connect(keyspace);

		String query = "";
		if ( subscribed.isEmpty() ) {
			query = "SELECT * from tweets WHERE timeuuid > "+from.toString()+" ALLOW FILTERING";
		} else {
			query = "SELECT * from tweets WHERE timeuuid > "+from.toString()+" AND user IN ("+subscribed.replaceAll("\"", "'")+") ALLOW FILTERING";
		}
		//System.out.print(query);
		PreparedStatement statement = session.prepare(query);
		BoundStatement boundStatement = new BoundStatement(statement);
		ResultSet rs = session.execute(boundStatement);
		if (rs.isExhausted()) {
			System.out.println("No Tweets returned");
		} else {
			for (Row row : rs) {
				TweetStore ts = new TweetStore();
				ts.setTweet(row.getString("tweet"));
				ts.setUser(row.getString("user"));
				ts.setTimeuuid(row.getUUID("timeuuid"));
				tweetList.add(ts);
			}
		}
		session.close();
		return tweetList;
	}
	
	/**
	 * TweetModel::saveTweet()
	 * Save tweet to database
	 * @param TweetStore ts
	 * @return boolean success
	 */
	public boolean saveTweet(TweetStore ts){
		Session session = cluster.connect(keyspace);

		String query = ""
		+ "INSERT INTO tweets (user,tweet,timeuuid) "
		+ "VALUES ('"+ts.getUser()+"','"+ts.getTweet().replaceAll("'", "").replaceAll("\"", "")+"',NOW()) ";
		PreparedStatement statement = session.prepare(query);		
		BoundStatement boundStatement = new BoundStatement(statement);
		
		try {
			session.execute(boundStatement);
			session.close();
			//System.out.println("Success!");
			return true;	
		} catch ( Exception e ) {
			//System.out.println("Failure!!");
			e.printStackTrace();
		}
		session.close();
		return false;
	}
}
