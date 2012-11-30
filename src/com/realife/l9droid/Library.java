package com.realife.l9droid;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
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
	//				try {
	//					//InputStream is=getResources().openRawResource(R.raw.timev2);
	//					InputStream is=activity.getResources().openRawResource(R.raw.wormv3);
	//					is.read(gamedata);            
	//				} catch (IOException e) {
	//					e.printStackTrace();
	//				}
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
	
	public boolean fileSave(byte buff[]) {
		String path="Saves/1.sav";
		path=getAbsolutePath(path);
		return fileSaveFromArray(path,buff);
	}
	
	public byte[] fileLoad() {
		String path="Saves/1.sav";
		path=getAbsolutePath(path);
		return fileLoadToArray(path);
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
}
