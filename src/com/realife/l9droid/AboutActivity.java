package com.realife.l9droid;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class AboutActivity extends Activity {
	@Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.about);
	    
	    //TODO: абсолютный лэйм, у евга всего 4-5 строк...
	    char[] c;
	    try {
	    	byte buff[]=new byte[2048]; 
			InputStream is=getResources().openRawResource(R.raw.about);
			int len=is.read(buff);
			if (len>0) c=new char[len];
			else return;
			for (int i=0;i<len;i++) c[i]=(char)buff[i];
	    } catch (IOException e) {
	      e.printStackTrace();
	      return; //ошибка - заканчиваю с подготовкой библиотеки?
	    }
	    WebView wv=(WebView)findViewById(R.id.webView1);
	    wv.loadData(String.valueOf(c), "text/html", null);
	  }
}
