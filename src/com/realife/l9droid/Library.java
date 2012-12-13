package com.realife.l9droid;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class Library {

	final String LIBDIR_SD = "/L9Droid/";
	final String DIR_SD = "Worm In Paradise/Speccy";
	final String FILE_SD="worm.sna";
	
	Handler h;
	String GameFullPathName;
	String paths[];
	int paths_num;
	
	Library() {
		h=null;
		paths=null;
		paths_num=0;
		GameFullPathName="";
	};
	
	boolean prepareLibrary(Activity act) {
		paths=null;
		paths_num=0;
		//getting sdcard path
		String sdState = android.os.Environment.getExternalStorageState();
		if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
			File sdPath = android.os.Environment.getExternalStorageDirectory();
			sdPath = new File(sdPath.getAbsolutePath() + LIBDIR_SD+DIR_SD);
			if (!sdPath.isDirectory()) {
				Toast.makeText(act, "Creating library", Toast.LENGTH_LONG).show();
				//
				sdPath.mkdirs();
				File sdFile = new File(sdPath, FILE_SD);
			    try {
			    	
			        byte buff[]=new byte[49179];	        
					InputStream is=act.getResources().openRawResource(R.raw.wormv3);
					is.read(buff);            
			    	OutputStream out = new FileOutputStream(sdFile);
	                out.write(buff, 0, buff.length);
	                out.close();
			    } catch (IOException e) {
			      e.printStackTrace();
			      return false; //ошибка - заканчиваю с подготовкой библиотеки?
			    }
			};
			
			String[] temppaths=new String[100];
			sdPath = android.os.Environment.getExternalStorageDirectory();
			sdPath = new File(sdPath.getAbsolutePath() + LIBDIR_SD);
			File[] pathdirs=sdPath.listFiles();
			if (pathdirs!=null) {
				for (int i=0; i<pathdirs.length; i++) {
					File[] files=pathdirs[i].listFiles();
					if (files!=null) 
						for (int j=0;j<files.length; j++)
							if (files[j].isFile()) temppaths[paths_num++]=files[j].getAbsolutePath();
				};
			};
			//TODO: temppaths-lame! kill it!
			if (paths_num>0) {
				paths=new String[paths_num];
				for (int i=0; i<paths_num; i++) paths[i]=temppaths[i];
			};
			
		} else return false;
		return true;
	}
	
	String[] getPaths() {
		return paths;
	};
	
	byte[] fileLoadGame(String path) {
		byte resbuff[]=fileLoadToArray(path);
		//if (resbuff!=null) GameFullPathName=path;
		return resbuff;
	}
	
	void setPath(String path) {
		GameFullPathName=path;
	}
	
	byte[] fileLoadRelativeToArray(String relativePath) {
		return fileLoadToArray(getAbsolutePath(relativePath));
	}
	
	byte[] fileLoadToArray(String absolutePath) {
		if (absolutePath==null) return null;
		byte buff[]=null;
		String sdState = android.os.Environment.getExternalStorageState();
		if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
			//File sdPath = android.os.Environment.getExternalStorageDirectory();
			//File sdFile = new File(sdPath.getAbsolutePath() + LIBDIR_SD + path);
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

		    if (buff!=null) sendUserMessage("Loaded: "+absolutePath);
		    else sendUserMessage("ERROR load: "+absolutePath);
		    
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
				sendUserMessage("Saved: "+path);
				return true;
			} catch (FileNotFoundException e) {
				//TODO: e.printStackTrace();
			} catch (IOException e) {
				//TODO: e.printStackTrace();
			}
			sendUserMessage("ERROR save: "+path);
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
	    if (buff!=null) sendUserMessage("Loaded strings: "+absolutePath);
	    else sendUserMessage("ERROR load strings: "+absolutePath);
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
				
				sendUserMessage("Saved strings: "+absolutePath);
				return true;
			} catch (FileNotFoundException e) {
				//TODO: e.printStackTrace();
			} catch (IOException e) {
				//TODO: e.printStackTrace();
			}
			sendUserMessage("ERROR save strings: "+absolutePath);
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
				sendUserMessage("Saved: "+path);
				return true;
			} catch (FileNotFoundException e) {
				//TODO: e.printStackTrace();
			} catch (IOException e) {
				//TODO: e.printStackTrace();
			}
			sendUserMessage("ERROR save: "+path);
		};
		return false;
	}
	
	Bitmap pictureLoadToBitmap(String path) {
		Bitmap b=BitmapFactory.decodeFile(path);
		if (b!=null) b=b.copy(Bitmap.Config.ARGB_8888,true); //i need mutable bitmap for draw on it
		if (b!=null) sendUserMessage("Loaded: "+path);
		else sendUserMessage("ERROR load: "+path);
		return b;
	}
	
	boolean deleteFile(String path) {
		//TODO: проверить, что файл в моей библиотеке, иначе не совать нос и не удалять!
		String sdState = android.os.Environment.getExternalStorageState();
		if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
			File sdFile = new File(path);
			if (sdFile.delete()) {
				sendUserMessage("Deleted: "+path);
				return true;
			}
		};
		sendUserMessage("ERROR delete: "+path);
		return false;
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
			File newFolder;
			int index=0;
			do {
				newFolder = new File(sdPath.getAbsolutePath() + LIBDIR_SD+folderName);
				//TODO: сделать изменение папки, если она уже существует
				index++;
			} while (newFolder.exists());
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
	private String wrapSpans(SpannableStringBuilder spannedString) {
		//ForegroundColorSpan a[]=spannedString.getSpans(0, 1, ForegroundColorSpan.class);
		String result=new String();
		int size=spannedString.length();
		int i=spannedString.getSpans(0, size, ForegroundColorSpan.class).length;
		if (i>0) {
			//TODO: пока только одна подсвеченная команда в строке будет заключена в фигурные скобки 
			//TODO: раскомментировать .replace("{", "{/").replace("}", "}}") для маскировки этих символов
			int begin=0;
			int beginSpan=-1;
			int endSpan=0;
			beginSpan=spannedString.nextSpanTransition(begin, size-1, ForegroundColorSpan.class);
			if (beginSpan>=0) {
				endSpan=spannedString.nextSpanTransition(beginSpan, size-1, ForegroundColorSpan.class);
				result+=spannedString.subSequence(begin, beginSpan).toString()/*.replace("{", "{/").replace("}", "}/")*/
						+"{"
						+spannedString.subSequence(beginSpan, endSpan+1).toString()/*.replace("{", "{/").replace("}", "}/")*/
						+"}";
				begin=endSpan+1;
			};
			if (begin<size) result+=spannedString.subSequence(begin, size-1).toString();
		} else result=spannedString.toString()/*.replace("{", "{/").replace("}", "}/")*/;
		return result;
	}
	
	//развернуть spans из тэгов {}
	private SpannableStringBuilder unwrapSpans(String wrappedString) {
		
		int size=wrappedString.length();
		int i=wrappedString.indexOf('{', 0);
		int j=wrappedString.indexOf('}', 0);
		if (i>=0 && j>i) {
			SpannableStringBuilder text = new SpannableStringBuilder(wrappedString.subSequence(0, i).toString()
					+wrappedString.subSequence(i+1, j).toString()
					+wrappedString.subSequence(j+1, size).toString());
	        ForegroundColorSpan style = new ForegroundColorSpan(Color.rgb(0, 0, 255)); 
	        text.setSpan(style, i, j-1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
	        return text; 
		} else return new SpannableStringBuilder(wrappedString);
	}
	
	public boolean SaveLogFromSpannableArrayAdapter(String path, ArrayAdapter<SpannableStringBuilder> adapter) {
		ArrayList<String> log=new ArrayList<String>();
    	if (adapter!=null) 
    		for (int i=0; i<adapter.getCount();i++) {
    			log.add((wrapSpans(adapter.getItem(i))));
    		};
		return fileSaveFromStringArray(path, log);
	}
	
	public ArrayList<SpannableStringBuilder> LoadLogToSpannableArrayList(String path) {
		ArrayList<String> log=fileLoadToStringArray(path);
		ArrayList<SpannableStringBuilder> array=new ArrayList<SpannableStringBuilder>();
		if (log!=null) 
			for (int i=0; i<log.size();i++)
				array.add(unwrapSpans(log.get(i)));
		return array;
	}
	
}
