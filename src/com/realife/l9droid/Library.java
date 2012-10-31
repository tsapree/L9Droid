package com.realife.l9droid;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;

public class Library {

	final String DIR_SD = "L9Droid/Worm In Paradise/Speccy";
	final String FILE_SD="worm.sna";
	
	boolean prepareLibrary(Activity act) {
		//getting sdcard path
		String sdState = android.os.Environment.getExternalStorageState();
		if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
			File sdPath = android.os.Environment.getExternalStorageDirectory();
			sdPath = new File(sdPath.getAbsolutePath() + "/" + DIR_SD);
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
		    }
		} else return false;
		return true;
	}
	
	byte[] fileLoadGame() {
		byte buff[]=null;
		String sdState = android.os.Environment.getExternalStorageState();
		if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
			File sdPath = android.os.Environment.getExternalStorageDirectory();
			sdPath = new File(sdPath.getAbsolutePath() + "/" + DIR_SD);
			File sdFile = new File(sdPath, FILE_SD);
		    try {
		    	InputStream in = new FileInputStream(sdFile);
                byte[] tempbuff = new byte[80000];
                int len=in.read(tempbuff);
                in.close();
                
                buff=new byte[len];
                for (int i=0;i<len;i++) buff[i]=tempbuff[i];
		    } catch (IOException e) {
		      e.printStackTrace();
		    }
		};
		return buff;
	};
	
	public boolean fileSave(byte buff[]) {
		try {
			OutputStream out = null; //TODO: openFileOutput ("1.sav", MODE_PRIVATE);
			out.write(buff);
			out.close();
			return true;
		} catch (FileNotFoundException e) {
			//TODO: e.printStackTrace();
		} catch (IOException e) {
			//TODO: e.printStackTrace();
		}
		return false;
	}
	
	public byte[] fileLoad() {
		try {
			InputStream in=null; //TODO: openFileInput("1.sav");
			byte tempbuff[]=new byte[0x2000];
			int len=in.read(tempbuff);
			byte buff[]=new byte[len];
			for (int i=0;i<len;i++) buff[i]=tempbuff[i];
			in.close();
			return buff;
		} catch (FileNotFoundException e) {
			//TODO: e.printStackTrace();
		} catch (IOException e) {
			//TODO: e.printStackTrace();
		}
		return null;
	}
}
