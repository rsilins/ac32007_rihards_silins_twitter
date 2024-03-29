package com.test1.servlets;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.datastax.driver.core.Cluster;

import com.test1.lib.*;
import com.test1.models.*;
//import com.test1.stores.*;

/**
 * Servlet implementation class Install
 * 
 * Meant for setting up cassandra tables.
 */
@WebServlet({ "/Install", "/Install/*" })
public class Install extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private Cluster cluster;
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Install() {
        super();
    }
    
    public void init(ServletConfig config) throws ServletException {
		cluster = CassandraHosts.getCluster();
	}
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 * 
	 * Attempts to create tables.
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		InstallModel im = new InstallModel();
		im.setCluster(cluster);
	
		if(im.createKeyspace()) {
			response.getWriter().write("Keyspace '"+im.getKeyspace()+"' created!\n");
		} else {
			response.getWriter().write("Failed to create keyspace '"+im.getKeyspace()+"'!\n");
		}
		
		if(im.createUserTable()) {
			response.getWriter().write("User table created!\n");
		} else {
			response.getWriter().write("Failed to create User table (or already exists)!\n");
		}
		
		if(im.createTweetTable()) {
			response.getWriter().write("Tweet table created!\n");
		} else {
			response.getWriter().write("Failed to create Tweet table (or already exists)!\n");
		}
	}
}