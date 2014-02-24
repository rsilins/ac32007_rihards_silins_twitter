package com.test1.stores;

public class UserStore {
	
	String Email;
	String User;
	String Password;
	String Subscribed;
	String Image;
	int Followers;
	int Following;
	
	public UserStore() {
		Email = "";
		User = "";
		Password = "";
		Subscribed = "";
		Image = "";
		Followers = 0;
		Following = 0;
	}
	
	public String getEmail(){
		return Email;
	}
	public String getUser(){
		return User;
	}
	public String getPassword(){
		return Password;
	}
	public String getSubscribed(){
		return Subscribed;
	}
	public String getImage(){
		return Image;
	}
	public int getFollowers(){
		return Followers;
	}
	public int getFollowing(){
		return Following;
	}
	/*
	public void setSubscribed(String[] subscribed){
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < subscribed.length; i++) {
		    sb.append(subscribed[i]);
		    if (i != subscribed.length - 1) {
		        sb.append(",");
		    }
		}
		
		this.Subscribed = sb.toString();
	}*/
	public void setSubscribed(String Subscribed) {
		this.Subscribed=Subscribed;
	}
	public void setImage(String Image) {
		this.Image=Image;
	}
	public void setFollowers(int Followers) {
		this.Followers=Followers;
	}
	public void setFollowing(int Following) {
		this.Following=Following;
	}
	public void setEmail(String Email){
		this.Email=Email;
	}
	public void setUser(String User){
		this.User=User;
	}
	public void setPassword(String Password){
		this.Password=Password;
	}
}