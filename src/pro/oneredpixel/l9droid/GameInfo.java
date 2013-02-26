package pro.oneredpixel.l9droid;

import java.util.ArrayList;

public class GameInfo {
	private String category;
	private String id;
	private String title;
	private String about;
	private String author;
	
	private ArrayList<String> paths;
	private ArrayList<String> tags;
	private ArrayList<String> files;
	
	private int highest_mark=0;
	
	String getCategory()  { return (category!=null)?category:""; }
	String getId()  { return (id!=null)?id:""; }
	String getTitle() { return (title!=null)?title:""; }
	String getAbout() { return (about!=null)?about:""; }
	String getAuthors() { return (author!=null)?author:""; }
	
	int getNumberOfPaths() { return (paths!=null)?paths.size():0; };
	String getPath(int n) {return (String) ((paths!=null)?paths.get(n):0); };
	String getTags(int n) {return (String) ((tags!=null)?tags.get(n):0); };
	String getFiles(int n) {return (String) ((files!=null)?files.get(n):0); };
	
	int getHighestMark() {return highest_mark; };
	
	void setCategory(String s) { category = s; }
	void setId(String s) { id = s; }
	void setTitle(String s) { title = s; }
	void setAbout(String s) { about = s; }
	void setAuthors(String s) { author = s; }
	void setHighestMark(int m) { highest_mark = m; };

	void addPath(String str) {
		if (paths==null) paths=new ArrayList<String>();
		paths.add(str);
	}
	void addFiles(String str) {
		if (files==null) files=new ArrayList<String>();
		files.add(str);
	}
	void addTags(String str) {
		if (tags==null) tags=new ArrayList<String>();
		tags.add(str);
	}
}