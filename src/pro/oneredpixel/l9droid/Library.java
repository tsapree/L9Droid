package pro.oneredpixel.l9droid;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.format.Time;
import android.text.style.ForegroundColorSpan;
import android.widget.ArrayAdapter;

public class Library {

	private static Library lib_instance;
	
	final static String LIBDIR_SD = "L9Droid";
	final static String FILE_NOMEDIA=".nomedia";
	final static String FILE_MARK=".mark";
	final static String DIR_CACHE="_cache";
	final static String DIR_SAVES="Saves";
	
	public static final String ATTR_NAME = "name";
	public static final String ATTR_DATE = "date";
	public static final String ATTR_SIZE = "size";
	public static final String ATTR_PATH = "path";
	public static final String ATTR_TYPE = "type";
	public static final String ATTR_IMAGE = "image";
	public static final String ATTR_MODIFIED = "modified";
	
	public static final int TYPE_PARENT_FOLDER = 0;
	public static final int TYPE_FOLDER = 1;
	public static final int TYPE_FILE = 2;
	
	final String MARK_LABEL_RATE_DOWN = "BAD";
	final String MARK_LABEL_RATE_UP = "GOOD";
	final String MARK_LABEL_COMPLETED = "DONE";

	public static final int MARK_NOT_INSTALLED = 0;
	public static final int MARK_INSTALLED = 1;
	public static final int MARK_RATE_DOWN = 2;
	public static final int MARK_RATE_UP = 3;
	public static final int MARK_COMPLETED = 4;
	public static final int MARK_INFO = 5;
	
	public static final int MARK_PICTURES_RESID[] = {
		R.drawable.ic_empty, 	//MARK_NOT_INSTALLED = 0;
		R.drawable.ic_installed,//MARK_INSTALLED = 1;
		R.drawable.ic_rate_down,//MARK_RATE_DOWN = 2;
		R.drawable.ic_rate_up,	//MARK_RATE_UP = 3;
		R.drawable.ic_done,		//MARK_COMPLETED = 4;
		R.drawable.ic_tip
	};
	
	Handler h;
	private String GameFullPathName;
	private ArrayList<String> paths;

	String tags[][]={
			{"1983","1983"},
			{"1984","1984"},
			{"1985","1985"},
			{"1986","1986"},
			{"1987","1987"},
			{"1988","1988"},
			{"1989","1989"},
			{"Amiga","Amiga"},
			{"Atari","Atari"},
			{"BBC","BBC"},
			{"CPC","CPC"},
			{"C64","Commodore 64"},
			{"Mac","Mac"},
			{"S48","Speccy 48k"},
			{"S128","Speccy 128k"},
			{"ST","ST"},
			{"PC","PC"},
			{"V1","A-Code V1"},
			{"V2","A-Code V2"},
			{"V3","A-Code V3"},
			{"V4","A-Code V4"},
	};

	private Library() {
		h=null;
		paths=null;
		GameFullPathName="";
	};
	

	public static Library getInstance() {
		return lib_instance;
	}
	
	public static void initInstance() {
		if (lib_instance == null) {
			lib_instance = new Library();
		}
	}
	
	public boolean checkIfSDCardPresent() {
		String sdState = android.os.Environment.getExternalStorageState();
		return (sdState.equals(android.os.Environment.MEDIA_MOUNTED));
	}
	
	//TODO: вызываю извне этот метод, лучше убрать и вызывать из конструктора
	boolean prepareLibrary(Activity act) {
		//getting sdcard path
		String sdState = android.os.Environment.getExternalStorageState();
		if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
			File sdPath = android.os.Environment.getExternalStorageDirectory();
			sdPath = new File(sdPath.getAbsolutePath() + "/"+LIBDIR_SD);
			if (!sdPath.isDirectory()) {
				//Toast.makeText(act, "Creating library", Toast.LENGTH_LONG).show();
				sdPath.mkdirs();
				File sdFile = new File(sdPath, FILE_NOMEDIA);
			    try {	        
			    	OutputStream out = new FileOutputStream(sdFile);
	                out.write(' ');
	                out.close();
			    } catch (IOException e) {
			      e.printStackTrace();
			      return false;
			    }
			};
			requestPaths();
			
		} else return false;
		return true;
	}
	
	public void requestPaths() {
		paths = new ArrayList<String>();
		File sdPath = android.os.Environment.getExternalStorageDirectory();
		sdPath = new File(sdPath.getAbsolutePath() + "/"+LIBDIR_SD+"/");
		File[] pathdirs=sdPath.listFiles();
		if (pathdirs!=null) {
			for (int i=0; i<pathdirs.length; i++) {
				File[] files=pathdirs[i].listFiles(new GameFilter() );
				if (files!=null) 
					for (int j=0;j<files.length; j++)
						if (files[j].isFile()) {
							paths.add(files[j].getAbsolutePath());
						}
			};
		};
	};
	
    class GameFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
        	String lowCaseName=name.toLowerCase();
        	long size=(new File(dir.getPath()+"/"+name)).length();
        	if (size<13491) return false;
        	return (lowCaseName.endsWith(".sna")
        			|| (  lowCaseName.endsWith(".dat") && !(lowCaseName.endsWith("gamedat2.dat") || lowCaseName.endsWith("gamedat3.dat")))
        			|| lowCaseName.indexOf('.')<0);
        }
    }

	
    ArrayList<String> getPaths() {
		return paths;
	};
	
	byte[] fileLoadGame(String path) {
		byte resbuff[]=fileLoadToArray(path);
		//if (resbuff!=null) GameFullPathName=path;
		return resbuff;
	}
	
	void setGamePath(String path) {
		GameFullPathName=path;
	}
	
	String getGamePath() {return GameFullPathName;};
	
	byte[] fileLoadRelativeToArray(String relativePath) {
		return fileLoadToArray(getAbsolutePath(relativePath));
	}
	
	byte[] fileLoadToArray(String absolutePath) {
		if (absolutePath==null) return null;
		byte buff[]=null;
		String sdState = android.os.Environment.getExternalStorageState();
		if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
			File sdFile = new File(absolutePath);
		    try {
		    	int size=(int)sdFile.length();
		    	if (size>0) {
		    		buff = new byte[size];
			    	InputStream in = new FileInputStream(sdFile);
	                int len=in.read(buff);
	                in.close();
		    	}
		    } catch (IOException e) {
		      e.printStackTrace();
		    }

		    //if (buff!=null) sendUserMessage("Loaded: "+absolutePath);
		    //else sendUserMessage("ERROR load: "+absolutePath);
		    
		};
		return buff;
	};
	
	boolean fileSaveFromArray(String path,byte buff[]) {
		String sdState = android.os.Environment.getExternalStorageState();
		if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
			try {
				File sdFile = new File(path);
				//folder exists?
				File sdPath = new File(sdFile.getParent());
				if (!sdPath.isDirectory()) {
					//create folder
					sdPath.mkdirs();
				};
				OutputStream out = new FileOutputStream(sdFile);
				out.write(buff);
				out.close();
				//sendUserMessage("Saved: "+path);
				return true;
			} catch (FileNotFoundException e) {
				//TODO: e.printStackTrace();
			} catch (IOException e) {
				//TODO: e.printStackTrace();
			}
			//sendUserMessage("ERROR save: "+path);
		};
		return false;
	}
	
	ArrayList<String> fileLoadToStringArray(String absolutePath) {
		if (absolutePath==null) return null;
		ArrayList<String> buff=new ArrayList<String>();

		try {
			File sdFile = new File(absolutePath);
			InputStream in = new FileInputStream(sdFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String str = "";
			while ((str = br.readLine()) != null) {
				buff.add(str);
			};
			br.close();
			
		} catch (FileNotFoundException e) {
			//TODO: e.printStackTrace();
		} catch (IOException e) {
			//TODO: e.printStackTrace();
		}
	    //if (buff!=null) sendUserMessage("Loaded strings: "+absolutePath);
	    //else sendUserMessage("ERROR load strings: "+absolutePath);
	    return buff;
	};
	
	boolean fileSaveFromStringArray(String absolutePath, ArrayList<String> buff) {
		String sdState = android.os.Environment.getExternalStorageState();
		if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
			try {
				
				File sdFile = new File(absolutePath);
				//folder exists?
				File sdPath = new File(sdFile.getParent());
				if (!sdPath.isDirectory()) sdPath.mkdirs();
				OutputStream out = new FileOutputStream(sdFile);
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
				
				for (int i=0;i<buff.size();i++)
					bw.write(buff.get(i)+"\n");
				bw.close();
				
				//sendUserMessage("Saved strings: "+absolutePath);
				return true;
			} catch (FileNotFoundException e) {
				//TODO: e.printStackTrace();
			} catch (IOException e) {
				//TODO: e.printStackTrace();
			}
			//sendUserMessage("ERROR save strings: "+absolutePath);
		};
		return false;		
	}
	
	boolean pictureSaveFromBitmap(String path, Bitmap bm) {
		if (bm==null) return false;
		String sdState = android.os.Environment.getExternalStorageState();
		if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
			try {
				File sdFile = new File(path);
				//folder exists?
				File sdPath = new File(sdFile.getParent());
				if (!sdPath.isDirectory()) {
					//create folder
					sdPath.mkdirs();
				};
				OutputStream out = new FileOutputStream(sdFile);
				bm.compress(Bitmap.CompressFormat.PNG, 100, out);
				out.close();
				//sendUserMessage("Saved: "+path);
				return true;
			} catch (FileNotFoundException e) {
				//TODO: e.printStackTrace();
			} catch (IOException e) {
				//TODO: e.printStackTrace();
			}
			//sendUserMessage("ERROR save: "+path);
		};
		return false;
	}
	
	Bitmap pictureLoadToBitmap(String path) {
		Bitmap b=BitmapFactory.decodeFile(path);
		if (b!=null) b=b.copy(Bitmap.Config.ARGB_8888,true); //i need mutable bitmap for draw on it
		//if (b!=null) sendUserMessage("Loaded: "+path);
		//else sendUserMessage("ERROR load: "+path);
		return b;
	}
	
	boolean deleteFile(String path) {
		//TODO: проверить, что файл в моей библиотеке, иначе не совать нос и не удалять!
		String sdState = android.os.Environment.getExternalStorageState();
		if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
			File sdFile = new File(path);
			if (sdFile.delete()) {
				//sendUserMessage("Deleted: "+path);
				return true;
			}
		};
		//sendUserMessage("ERROR delete: "+path);
		return false;
	}
	
	boolean deleteFolder(String path) {
		File f = new File (path);
		if (!f.isDirectory()) return false;
		for (File f1 : f.listFiles()) {
			if (f1.isDirectory()) deleteFolder(f1.getAbsolutePath());
			else deleteFile(f1.getAbsolutePath());
		};
		return f.delete();
	}
	
	String getFolder(String path) {
		File f=new File(path);
		if (f.isDirectory()) return path;
		else return f.getParent();
	}
	
	String getFileNameWithoutPath (String path) {
		File f=new File(path);
		return f.getName();	
	}

	//returns:
	//	if (relativePath='/****') absolute path = relativePath
	//	else if (relativePath='.****') absolute path = gamePath-gameName
	//	else absolute path = gamePath-gameName+relativePath
	String getAbsolutePath(String relativePath) { 
		String absolutePath=null;
		//TODO: на момент, пока игра не стартанула, в пути GameFullPathName ерунда
		File sdFile = new File(GameFullPathName);
		absolutePath=sdFile.getParent()+'/';
		if (relativePath!=null) {
			//if relativePath starts with '/', when assume it is absolute path already, don't change it
			if (relativePath.length()>0) {
				if (relativePath.charAt(0)=='/') {
					absolutePath=relativePath;
				} else if (relativePath.charAt(0)=='.') {
					//do nothing
				} else {
					absolutePath+=relativePath;
				};
			}
		};
		return absolutePath;
	}
	
	boolean FileExist(String path) {
		//TODO: на момент, пока игра не стартанула, в пути GameFullPathName ерунда
		String checkPath=getAbsolutePath(path);
		File sdFile = new File(checkPath);
		return sdFile.exists();
	};
	
	String changeFileExtension(String path, String newExtension) {
		if ((path==null) || (newExtension==null)) return null;
		int i=path.length();
		if (i==0) return null;
		int j=i;
		while (i>0) {
			if (path.charAt(--i)=='.') {
				j=i+1;
				break;
			}
		};
		return path.substring(0, j-1)+'.'+newExtension;
	};
	
	private void sendUserMessage(String txt) {
		if (h==null) return;
		Message msg=h.obtainMessage(Threads.MACT_TOAST, txt);
		h.sendMessage(msg);
	}
	
	public boolean importFile(String fileName, String folderName) {
		String sdState = android.os.Environment.getExternalStorageState();
		if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
			File sdPath = android.os.Environment.getExternalStorageDirectory();
			File newFolder = new File (	unifyFolder(sdPath.getAbsolutePath() + "/"+LIBDIR_SD+"/"+folderName));

			File source=new File(fileName);
			if (source.isDirectory()) return copy(fileName,newFolder.toString());
			else if (source.isFile()) {
				newFolder.mkdirs();
				copy(fileName,newFolder.toString()+"/"+source.getName());
			};
		};
		return false; 
	};

	public static boolean copy(String from, String to) {
		try {      
			File fFrom = new File(from);
			if (fFrom.isDirectory()) { // Если директория, копируем все ее содержимое
				File f1 = new File(to); //Создаем файловую переменную
                if (!f1.exists()) f1.mkdirs();
				String[] FilesList = fFrom.list();
				for (int i = 0; i < FilesList.length; i++)
					if (!copy(from + "/" + FilesList[i], to + "/" + FilesList[i]))
						return false; // Если при копировании произошла ошибка
			} else if (fFrom.isFile()) { // Если файл просто копируем его
			    File fTo = new File(to);
			    InputStream in = new FileInputStream(fFrom);
			    OutputStream out = new FileOutputStream(fTo);
			    byte[] buf = new byte[1024];
			    int len;
			    while ((len = in.read(buf)) > 0) {
			        out.write(buf, 0, len);
			    }
			    in.close();
			    out.close();
			}
		} catch (FileNotFoundException ex) { // Обработка ошибок
		} catch (IOException e) { // Обработка ошибок
		}
		return true;
	}
	
	//завернуть spans в тэги {}
	//TODO: посмотреть на решение по поиску спэнов в refreshLogCommandsColor(), там получилось лучше
	private String wrapSpans(SpannableStringBuilder spannedStr) {
		String result=new String();
		SpannableStringBuilder spannedString=new SpannableStringBuilder(" ");
		spannedString.append(spannedStr);//исправление особенности nextSpanTransition, который не видит 
		int size=spannedString.length();
		int i=spannedString.getSpans(0, size, ForegroundColorSpan.class).length;
		if (i>0) {
			
			//TODO: пока только одна подсвеченная команда в строке будет заключена в фигурные скобки 
			//TODO: раскомментировать .replace("{", "{/").replace("}", "}}") для маскировки этих символов
			int begin=0;
			int beginSpan=-1;
			int endSpan=0;
			beginSpan=spannedString.nextSpanTransition(begin, size-1, ForegroundColorSpan.class);
			begin=1; //Пропустить вставленный мною пробел
			if (beginSpan>=0) {
				endSpan=spannedString.nextSpanTransition(beginSpan, size-1, ForegroundColorSpan.class);
				result+=spannedString.subSequence(begin, beginSpan).toString()/*.replace("{", "{/").replace("}", "}/")*/
						+"{"
						+spannedString.subSequence(beginSpan, endSpan+1).toString()/*.replace("{", "{/").replace("}", "}/")*/
						+"}";
				begin=endSpan+1;
			};
			if (begin<size) result+=spannedString.subSequence(begin, size-1).toString();
			
		} else result=spannedStr.toString()/*.replace("{", "{/").replace("}", "}/")*/;
		return result;
	}
	
	public String getSpannedString(SpannableStringBuilder ssb) {
		String wrapped=wrapSpans(ssb);
		int begin=wrapped.indexOf('{');
		int end=wrapped.lastIndexOf('}');
		if (begin+1<end && begin>0) return wrapped.substring(begin+1, end);
		else return null;
	}
	
	//развернуть spans из тэгов {}
	private SpannableStringBuilder unwrapSpans(String wrappedString, int color) {
		
		int size=wrappedString.length();
		int i=wrappedString.indexOf('{', 0);
		int j=wrappedString.indexOf('}', 0);
		if (i>=0 && j>i) {
			SpannableStringBuilder text = new SpannableStringBuilder(wrappedString.subSequence(0, i).toString()
					+wrappedString.subSequence(i+1, j).toString()
					+wrappedString.subSequence(j+1, size).toString());
	        ForegroundColorSpan style = new ForegroundColorSpan(color); 
	        text.setSpan(style, i, j-1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
	        return text; 
		} else return new SpannableStringBuilder(wrappedString);
	}
	
	public boolean SaveLogFromSpannableArrayAdapter(String path, ArrayAdapter<SpannableStringBuilder> adapter, int strid) {
		ArrayList<String> log=new ArrayList<String>();
    	if (adapter!=null) 
    		for (int i=0; i<adapter.getCount();i++) {
    			log.add((wrapSpans(adapter.getItem(i))));
    		};
    	if (strid==-1) log.add("");
		return fileSaveFromStringArray(path, log);
	}
	
	public ArrayList<SpannableStringBuilder> LoadLogToSpannableArrayList(String path, int color) {
		ArrayList<String> log=fileLoadToStringArray(path);
		ArrayList<SpannableStringBuilder> array=new ArrayList<SpannableStringBuilder>();
		if (log!=null) 
			for (int i=0; i<log.size();i++)
				array.add(unwrapSpans(log.get(i),color));
		return array;
	}
	
	public void refreshLogCommandsColor(ArrayAdapter<SpannableStringBuilder> adapter, int newColor) {
		SpannableStringBuilder s;
		ForegroundColorSpan style=new ForegroundColorSpan(newColor);
		for (int i=0;i<adapter.getCount();i++) {
			s=adapter.getItem(i);
			ForegroundColorSpan f[]=s.getSpans(0, s.length(), ForegroundColorSpan.class);
			for (ForegroundColorSpan fcs:f) {
				int start = s.getSpanStart(fcs);
				int end = s.getSpanEnd(fcs);
				s.clearSpans();
				s.setSpan(style, start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			}
		};
	}
	
	public void invalidateInstalledVersions() {
		paths=null;
	}
	
	//вернуть список путей с именами запускаемых файлов, соответствующих игре gameName
	public ArrayList<String> getInstalledVersions(String gameName) {
		if (paths==null) requestPaths();
		ArrayList<String> p=new ArrayList<String>();
		if (paths!=null) {
			for (int i=0;i<paths.size();i++) {
				if (paths.get(i).toLowerCase().contains(gameName.toLowerCase())) {
					p.add(paths.get(i));
				};
			};
		};
		return p;
	}
	
	public String getTags(String pathFilename) {
		File f = new File(pathFilename);
		String s=f.getParentFile().getName();
		return decodeTags(s);
	}
	
	public String decodeTags(String s) {
		String r="";
		for (int i=0;i<tags.length;i++) {
			if (s.contains(tags[i][0])) {
				if (r.length()>0) r+="/";
				r+=tags[i][1];
			};
		};
		return r;
		
	}
	
	public int getMark(String path) {
		int mark=MARK_NOT_INSTALLED;
		File f = new File(path);
		if (f.isFile()) f=f.getParentFile();
		if (f.isDirectory()) {
			mark=MARK_INSTALLED;
			ArrayList<String> m = fileLoadToStringArray(f.getAbsolutePath()+"/" + FILE_MARK);
			if (m!=null && m.size()>0) {
				String first=m.get(0);
				if (first.equalsIgnoreCase(MARK_LABEL_RATE_DOWN)) mark=MARK_RATE_DOWN;
				if (first.equalsIgnoreCase(MARK_LABEL_RATE_UP)) mark=MARK_RATE_UP;
				if (first.equalsIgnoreCase(MARK_LABEL_COMPLETED)) mark=MARK_COMPLETED;
			};
		};
		return mark;
	}
	
	void setMark(String path, int mark) {
		File f = new File(path);
		if (f.isFile()) f=f.getParentFile();
		if (f.isDirectory()) {
			String filename = f.getAbsolutePath()+"/"+FILE_MARK;
			ArrayList<String> m = new ArrayList<String>();
			switch (mark) {
			case MARK_RATE_DOWN:
				m.add(MARK_LABEL_RATE_DOWN);
				break;
			case MARK_RATE_UP:
				m.add(MARK_LABEL_RATE_UP);
				break;
			case MARK_COMPLETED:
				m.add(MARK_LABEL_COMPLETED);
				break;
			};
			if (m.size()<1) {
				deleteFile(filename);
			} else {
				fileSaveFromStringArray(filename, m);
			}
		};
	}
	
	//получить список игр, с указанием короткого имени, названия игры и категории
	public ArrayList<GameInfo> getGameList(Activity act) {
		ArrayList<GameInfo> rez = new ArrayList<GameInfo>();
		String currentCategory="";

		try {
			XmlPullParser parser = act.getResources().getXml(R.xml.games);

			while (parser.next() != XmlPullParser.END_DOCUMENT) {
				if (parser.getEventType() != XmlPullParser.START_TAG) {
					continue;
			    }
				String name = parser.getName();
			    // Starts by looking for the entry tag
			    if (name.equals("game")) {
					GameInfo gi=new GameInfo();

					for (int i=0;i<parser.getAttributeCount();i++) {
						if (parser.getAttributeName(i).equals("name")) gi.setId(parser.getAttributeValue(i));
					};
					while (parser.next() != XmlPullParser.END_TAG) {
						if (parser.getEventType() != XmlPullParser.START_TAG) {
							continue;
						}
						String n = parser.getName();
						if (n.equals("title"))	gi.setTitle(readTag(parser,"title"));
						else {
							skip(parser);
						}
					}
					gi.setCategory(currentCategory);
					
					ArrayList<String> versions = getInstalledVersions(gi.getId());
					for (String v: versions) {
						int m=getMark(v);
						if (gi.getHighestMark()<m) gi.setHighestMark(m);
					};
					
					rez.add(gi);
					//break;

			    } else if (name.equals("category")) {
			    	for (int i=0;i<parser.getAttributeCount();i++) {
			    		if (parser.getAttributeName(i).equals("name")) currentCategory=parser.getAttributeValue(i);
			    	};
			    }
			};

		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return rez;
	}
	
	public GameInfo getGameInfo(Activity act, String gameName) {
		GameInfo gi=new GameInfo();
		
		String currentCategory="";
		String currentGame="";
		
		/*
		//TODO: убрать, когда корректно наполню библиотеку
		gi.setCategory("!category!");
		gi.setId("!id!");
		gi.setTitle("!title!");
		gi.setAbout("!About!");
		gi.setAuthors("!authors!");
		*/
		
	    try {
	        XmlPullParser parser = act.getResources().getXml(R.xml.games);

	        while (parser.next() != XmlPullParser.END_DOCUMENT) {
	        	if (parser.getEventType() != XmlPullParser.START_TAG) {
	                continue;
	            }
	        	String name = parser.getName();
	            // Starts by looking for the entry tag
	            if (name.equals("game")) {
	            	for (int i=0;i<parser.getAttributeCount();i++) {
	            		if (parser.getAttributeName(i).equals("name")) currentGame=parser.getAttributeValue(i);
	            	};
	            	//if (!currentGame.equalsIgnoreCase(gameName)) continue;
	            	if (!gameName.toLowerCase().startsWith(currentGame.toLowerCase())) continue;
	            	while (parser.next() != XmlPullParser.END_TAG) {
	                    if (parser.getEventType() != XmlPullParser.START_TAG) {
	                        continue;
	                    }
	                    String n = parser.getName();
	                    if (n.equals("title"))			gi.setTitle(readTag(parser,"title"));
	                    else if (n.equals("about"))		gi.setAbout(readTag(parser,"about"));
	                    else if (n.equals("authors"))	gi.setAuthors(readTag(parser,"authors"));
	                    else if (n.equals("path"))		{
	                    	for (int i=0;i<parser.getAttributeCount();i++) {
	                    		String atr=parser.getAttributeName(i);
	    	            		if (atr.equals("tags")) gi.addTags(parser.getAttributeValue(i));
	    	            		else if (atr.equals("files")) gi.addFiles(parser.getAttributeValue(i));
	    	            	};
	                    	gi.addPath(readTag(parser,"path"));
	                    }
	                    else {
	                        skip(parser);
	                    }
	                }
	            	gi.setCategory(currentCategory);
	            	gi.setId(currentGame);
	            	break;
	
	            } else if (name.equals("category")) {
	            	for (int i=0;i<parser.getAttributeCount();i++) {
	            		if (parser.getAttributeName(i).equals("name")) currentCategory=parser.getAttributeValue(i);
	            	};
	            }
	            
	        };

	      } catch (XmlPullParserException e) {
	        e.printStackTrace();
	      } catch (IOException e) {
	        e.printStackTrace();
	      }
	    
		return gi;
	}
	
	//from http://developer.android.com/intl/ru/training/basics/network-ops/xml.html#read
	private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
	    if (parser.getEventType() != XmlPullParser.START_TAG) {
	        throw new IllegalStateException();
	    }
	    int depth = 1;
	    while (depth != 0) {
	        switch (parser.next()) {
	        case XmlPullParser.END_TAG:
	            depth--;
	            break;
	        case XmlPullParser.START_TAG:
	            depth++;
	            break;
	        }
	    }
	}
	
	//from http://developer.android.com/intl/ru/training/basics/network-ops/xml.html#read
	// Processes title tags in the feed.
	private String readTag(XmlPullParser parser, String tag) throws IOException, XmlPullParserException {
	    parser.require(XmlPullParser.START_TAG, null, tag);
	    String title = readText(parser);
	    parser.require(XmlPullParser.END_TAG, null, tag);
	    return title;
	}
	
	//from http://developer.android.com/intl/ru/training/basics/network-ops/xml.html#read
	// For the tags title and summary, extracts their text values.
	private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
	    String result = "";
	    if (parser.next() == XmlPullParser.TEXT) {
	        result = parser.getText();
	        parser.nextTag();
	    }
	    return result;
	}
	
	
	String unifyFolder(String folder) {
		String uF;
		File f;
		int i=0;
		do {
			uF=(i==0)?folder:String.format("%s %d", folder, i);
			i++;
			f = new File(uF);
		} while (f.isDirectory());
		return uF;
	};
	
	String unifyFile(String file) {
		String uF;
		int i=0;
		File f;
		
		String filename;
		String ext;
		int filenameAt = file.lastIndexOf('/');
		if (filenameAt<0) filenameAt=0; else filenameAt++;
		int extAt = file.lastIndexOf('.');
		if (extAt<filenameAt) {
			filename=file;
			ext="";
		} else {
			filename=file.substring(0, extAt);
			ext=file.substring(extAt);
		};
		if (extAt==filenameAt) i++;

		do {
			uF=(i==0&&(filename.length()>0))?file:String.format("%s%d%s", filename, i, ext);
			i++;
			f = new File(uF);
		} while (f.isFile());
		return uF;
	};
	
	public String checkFileInCache(String src) {
		String dst=null;
		String filename=getFileName(src);
		String folder=src.replace('/', '_').replace(":", "_");
		
		String sdState = android.os.Environment.getExternalStorageState();
		if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
			File sdPath = android.os.Environment.getExternalStorageDirectory();
			dst=sdPath.getAbsolutePath() +"/" + LIBDIR_SD + "/"+DIR_CACHE+"/" + folder + "/" + filename;
			File fdst=new File(dst);
			if (fdst.exists()) return dst; //если уже файл скачан ранее, вернуть путь
		};
		return null;
	};
	
	public boolean checkPathInLibrary(String folderTo) {
		String sdState = android.os.Environment.getExternalStorageState();
		if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
			File sdPath = android.os.Environment.getExternalStorageDirectory();
			File path=new File(sdPath.getAbsolutePath() + "/"+LIBDIR_SD+"/" + folderTo);
			if (path.isDirectory()) return true;
		};
		return false;
	};
	
	public String downloadFileToCache(String src, DownloadInstallFileTask d) {
		String dst=null;
		File fdst=null;
		File dir=null;
		boolean cancelled=false;
		
		try {
			String filename=getFileName(src);
			String folder=src.replace('/', '_').replace(":", "_");
			
			String sdState = android.os.Environment.getExternalStorageState();
			if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
				File sdPath = android.os.Environment.getExternalStorageDirectory();
				dst=sdPath.getAbsolutePath() + "/" + LIBDIR_SD + "/"+DIR_CACHE+"/" + folder + "/" + filename;
				
				fdst=new File(dst);
				dir=fdst.getParentFile();
				if (!dir.isDirectory()) dir.mkdirs();
				
				if (fdst.exists()) return dst; //если уже файл скачан ранее, просто вернуть путь

				URL url = new URL(src);
				URLConnection connection;
				
				//TODO: методы получения адреса и порта прокси deprecated, поэтому
				//      надо перевести на определение стандартным java-способом
				//      либо вынести настройки прокси в настройки и не париться.
				String proxyServer = android.net.Proxy.getDefaultHost();
				int proxyPort = android.net.Proxy.getDefaultPort();
				if (proxyServer!=null && proxyPort>0) {
					Proxy proxy=new Proxy(java.net.Proxy.Type.HTTP,new InetSocketAddress(proxyServer,proxyPort));
					connection = url.openConnection(proxy);
				} else {
					connection = url.openConnection();
				};
				connection.setConnectTimeout(3000);
				int filelenght=connection.getContentLength();
				InputStream in = connection.getInputStream();
				
				OutputStream out = new FileOutputStream(fdst);
			    byte[] buf = new byte[8192];
			    int len;
			    int downloaded=0;
			    while ((len = in.read(buf)) > 0 && !cancelled) {
			        out.write(buf, 0, len);
			        downloaded+=len;
			        if (d!=null) cancelled=d.doProgressUpdate(downloaded, filelenght);
			    }
			    in.close();
			    out.close();
			    
			};

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			d.errorDescription = e.toString();//.getMessage();
			e.printStackTrace();
			dst=null;
			//return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			d.errorDescription = e.toString();//.getMessage();
			e.printStackTrace();
			dst=null;
			//return null;
		}
		if (cancelled) d.errorDescription = "Canceled";
		if (cancelled || (dst==null)) { //если произошла отмена или ошибка
			if ((fdst!=null) && (fdst.exists())) fdst.delete();
			if ((dir!=null) && (dir.isDirectory())) dir.delete();
			dst=null;
		};
		return dst;
		
	}
	
	String getFileName(String src) {
		return src.substring(src.lastIndexOf('/')+1);
	};
	
	//метод разархивирует все файлы, начинающиеся с fileToExtract, маленький недочет в том, что все файлы,
	//начинающиеся с этого пути будут помещены в одну папку, меня это пока устраивает
	public boolean unzipFile(String zipPath, String fileToExtract, String folderTo, DownloadInstallFileTask d) {
		ZipFile z;
		ZipEntry ze;
		String absFolderTo;
		int filesUnzipped=0;
		
		try {
			
			String sdState = android.os.Environment.getExternalStorageState();
			if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
				File sdPath = android.os.Environment.getExternalStorageDirectory();
				absFolderTo=unifyFolder(sdPath.getAbsolutePath() + "/"+LIBDIR_SD+"/" + folderTo);
				z=new ZipFile(zipPath);
				File path=new File(absFolderTo);
				if (!path.isDirectory()) path.mkdirs();
				
				Enumeration<? extends ZipEntry> e=z.entries();
				while (e.hasMoreElements())
				{
					ze=e.nextElement();
					String zipPathName=ze.getName().toLowerCase();
					if (zipPathName.startsWith(fileToExtract.toLowerCase())) {

						if (!ze.isDirectory()) {
							File fdst=new File(absFolderTo+"/"+getFileName(zipPathName));
							OutputStream out = new FileOutputStream(fdst);
							InputStream in = z.getInputStream(ze);
							byte[] buf = new byte[1024];
						    int len;
						    int filelenght=(int)ze.getSize();
						    int unzipped=0;
						    while ((len = in.read(buf)) > 0) {
						        out.write(buf, 0, len);
						        if (d!=null) 
						        	d.doProgressUpdate(unzipped, filelenght);
						    }
						    in.close();
						    out.close();
						    filesUnzipped++;
						};
					};
				};
			    z.close();
				
				/*
				ze = z.getEntry(fileToExtract);
				if (ze==null) {
					z.close();
					return false;
				};
				*/
				
			} else return false;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			d.errorDescription = e.getMessage();
			e.printStackTrace();
			return false;
		}
		if (filesUnzipped>0) return true;
		else {
			d.errorDescription = "No necessary files found in archive";
			return false;
		}
	};
	
	//получить информацию о сохраненных играх по папке или пути до файла
	ArrayList<Map<String, Object>> getSaved(String path) {

		ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        Map<String, Object> m;

		Time t=new Time();
		String sdState = android.os.Environment.getExternalStorageState();
		if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
			File sdpath=new File(path);
			if (sdpath.isFile()) sdpath=sdpath.getParentFile();
			sdpath = new File(sdpath.getAbsolutePath()+"/"+DIR_SAVES);
			if (sdpath.isDirectory()) {
				File[] f = sdpath.listFiles(new SavedGamesFilter());
				for (int i=0; i<f.length;i++) {
					if (f[i].isFile()) {
						m = new HashMap<String, Object>();
						Long modified = Long.valueOf(f[i].lastModified());
						m.put(ATTR_MODIFIED, modified);
						t.set(modified);
						  
						m.put(ATTR_DATE, t.format("%H:%M %d.%m.%Y"));
						m.put(ATTR_NAME, f[i].getName());
						m.put(ATTR_PATH, f[i].getAbsolutePath());
						m.put(ATTR_IMAGE, R.drawable.ic_empty);
						
						//sort
						int x=0;
						while (x<data.size() && ((Long)(data.get(x).get(ATTR_MODIFIED))>modified)) x++;
						data.add(x,m);
					};
	    
				};
			};
		};
		
        return data;
	}

	class SavedGamesFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
        	return (name.toLowerCase().endsWith(".sav"));
        }
    }
	
	//получить информацию о каталоге для activity select file
	ArrayList<Map<String, Object>> getFilesInFolder(String path) {

		ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        Map<String, Object> m;

		Time t=new Time();
		String sdState = android.os.Environment.getExternalStorageState();
		if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
			File sdpath=new File(path);
			if (sdpath.isDirectory()) {
				File[] f = sdpath.listFiles();
				if (f!=null) {
					for (int i=0; i<f.length;i++) {
						if (!f[i].isHidden()) {
							m = new HashMap<String, Object>();
							Long modified = Long.valueOf(f[i].lastModified());
							m.put(ATTR_MODIFIED, modified);
							t.set(modified);
							
							m.put(ATTR_DATE, t.format("%H:%M %d.%m.%Y"));
							String name=f[i].getName();
							m.put(ATTR_NAME, name);
							m.put(ATTR_PATH, f[i].getAbsolutePath());
								
	
							int type=f[i].isFile()?TYPE_FILE:TYPE_FOLDER;
							m.put(ATTR_TYPE, type);
							switch (type) {
							case TYPE_FOLDER:
								m.put(ATTR_IMAGE, R.drawable.ic_folder);
								m.put(ATTR_SIZE, null);
								break;
							case TYPE_FILE:
								m.put(ATTR_IMAGE, R.drawable.ic_file);
								long size=f[i].length();
								if (size<1024) m.put(ATTR_SIZE, String.format("%d b", size));
								else if (size<1000000) m.put(ATTR_SIZE, String.format("%.1f kb", (float)size/1024));
								else if (size<1000000000) m.put(ATTR_SIZE, String.format("%.1f Mb", (float)size/(1024*1024)));
								break;
							};
							
							//sort
							int x=0;
							while (x<data.size()) {
								int mtype = ((Integer)(data.get(x).get(ATTR_TYPE)));
								if (mtype>type) break; //file>folder
								if (mtype==type)  //file==file  folder==folder
									if (((String)(data.get(x).get(ATTR_NAME))).compareToIgnoreCase(name)>0) break;
								x++;
							};
							data.add(x,m);
						};
					};
				};
				
				File p=sdpath.getParentFile();
				if (p!=null) {
					m = new HashMap<String, Object>();
					m.put(ATTR_MODIFIED, 0);
					m.put(ATTR_DATE, null);
					m.put(ATTR_NAME, "..");
					m.put(ATTR_PATH, p.getAbsolutePath());
					m.put(ATTR_TYPE, TYPE_PARENT_FOLDER);
					m.put(ATTR_IMAGE, R.drawable.ic_parent_directory);
					m.put(ATTR_SIZE, null);
					data.add(0,m);
				};
			};
		};
		
        return data;
	}
	
}
