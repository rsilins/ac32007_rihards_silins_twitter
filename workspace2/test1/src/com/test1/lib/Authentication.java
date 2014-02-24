package com.test1.lib;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Class Authentication
 * Authentication class for managing auth and login sessions
 * @author Rihards Silins
 */
public class Authentication {
	
	/**
	 * Authentication::isLoggedIn()
	 * Check if anyone is looged in
	 * @param HttpServletRequest request
	 * @return boolean result
	 */
	public boolean isLoggedIn(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if( cookies != null ) {
			for(Cookie cookie : cookies){
				if(cookie.getName().equals("user")) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Authentication::getLoggedInUser()
	 * Get logged in username 
	 * @param HttpServletRequest request
	 * @return String username
	 */
	public String getLoggedInUser(HttpServletRequest request) {
		String username = "";
		Cookie[] cookies = request.getCookies();
		if( cookies != null ) {
			for(Cookie cookie : cookies){
				if(cookie.getName().equals("user")) {
					username = cookie.getValue();
					return username;
				}
			}
		}
		return username;
	}
	
	/**
	 * Authentication::logout()
	 * destroys logout session/cookies
	 * @param HttpServletResponse response
	 * @return HttpServletResponse response
	 */
	public HttpServletResponse logout(HttpServletResponse response) {
		// DESTROY COOKIES
		Cookie cookie = new Cookie("user", "");
		cookie.setMaxAge(0); 
		cookie.setPath("/");
		response.addCookie(cookie);
		return response;
	}
	
	/**
	 * Authentication::login()
	 * adds login session/cookies
	 * @param HttpServletResponse response, String username
	 * @return HttpServletResponse response
	 */
	public HttpServletResponse login(HttpServletResponse response, String username) {
		Cookie loginCookie = new Cookie("user",username);
        loginCookie.setMaxAge(30*60); // 30 MIN
        loginCookie.setPath("/");
        response.addCookie(loginCookie);
		return response;
	}
	
	
}