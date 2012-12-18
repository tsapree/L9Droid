package com.realife.l9droid;

public class GameInfo {
	private String category;
	private String id;
	private String title;
	private String about;
	private String author;
	
	String getCategory()  { return (category!=null)?category:""; }
	String getId()  { return (id!=null)?id:""; }
	String getTitle() { return (title!=null)?title:""; }
	String getAbout() { return (about!=null)?about:""; }
	String getAuthors() { return (author!=null)?author:""; }
	
	void setCategory(String s) { category = s; }
	void setId(String s) { id = s; }
	void setTitle(String s) { title = s; }
	void setAbout(String s) { about = s; }
	void setAuthors(String s) { author = s; }
	
}