package com.test1.stores;

import java.util.UUID;
import java.util.Date;
import java.text.SimpleDateFormat;

public class TweetStore {
	
	UUID Timeuuid;
	String Tweet;
	String User;
	
	public String getTweet(){
		return Tweet;
	}
	public String getUser(){
		return User;
	}
	public UUID getTimeuuid(){
		return Timeuuid;
	}
	
	public String getTweetTime(){
		long time = (this.Timeuuid.timestamp() - 0x01b21dd213814000L) / 10000;
		Date date = new Date(time);
		String tweettime = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(date);
		return tweettime;
	}
	
	public String getTweetTimeJSReadable(){
		long time = (this.Timeuuid.timestamp() - 0x01b21dd213814000L) / 10000;
		Date date = new Date(time);
		String tweettime = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(date);
		return tweettime;
	}
	
	public void setTweet(String Tweet){
		this.Tweet=Tweet;
	}
	public void setUser(String User){
		this.User=User;
	}
	public void setTimeuuid(UUID Timeuuid){
		this.Timeuuid=Timeuuid;
	}
	
}
