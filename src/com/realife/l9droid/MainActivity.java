package com.realife.l9droid;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener,OnEditorActionListener, OnMenuItemClickListener {
	
	SharedPreferences sp;
	Typeface tf;
	Typeface tfDefault=null;
	float fontSizeDefault=0;
	
	Button bCmd;
	EditText etLog;
    EditText etCmd;
    
    ImageView ivScreen;
    
    String command;
    
    static Threads mt;
    
    boolean killThreadsOnDestroyActivity=true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        
        ivScreen=(ImageView) findViewById(R.id.imageView1);
                       
        bCmd = (Button) findViewById(R.id.bCmd);
        bCmd.setOnClickListener(this);
        
        etLog = (EditText) findViewById(R.id.etLog);
        etCmd = (EditText) findViewById(R.id.etCmd);
        etCmd.setOnEditorActionListener(this);

        etCmd.setText("");
        //etLog.setText("Welcome to Level9 emulator v0.001\n(c)2012 Paul Stakhov\n");
                
        command=null;
        
        mt = (Threads) getLastNonConfigurationInstance();
	    if (mt == null) {
	    	mt=new Threads();
	    	mt.link(this);
	        mt.create();
	        SharedPreferences sPref=getPreferences(MODE_PRIVATE);
	        String lastGame = sPref.getString("lastgame", "Worm In Paradise/Speccy/worm.sna");
	        mt.startGame(lastGame);
	        if (mt.l9!=null) Toast.makeText(this, "Started: "+lastGame, Toast.LENGTH_SHORT).show();
	        else Toast.makeText(this, "Fault start of: "+lastGame, Toast.LENGTH_SHORT).show();
	        
	    } else mt.link(this);
	    ivScreen.setScaleType(ScaleType.FIT_XY);
    }
    
    public Object onRetainNonConfigurationInstance() {
    	killThreadsOnDestroyActivity=false;
	  	mt.unlink();
	    return mt;
	};
	
    protected void onResume() {
    	float fontSize=14;
    	if (tfDefault==null) tfDefault=etCmd.getTypeface();
    	if (fontSizeDefault==0) fontSizeDefault=etCmd.getTextSize();
    	//String listValue = sp.getString("list", "не выбрано");
    	//tvInfo.setText("Значение списка - " + listValue);
    	//tf=Typeface.create(Typeface.DEFAULT, (sp.getBoolean("fontbold", false)?(Typeface.BOLD):(Typeface.NORMAL)));
    	
    	if (sp.getBoolean("fontcustom", false)) {
    		tf=Typeface.create(sp.getString("fontface", "DEFAULT"), (sp.getBoolean("fontbold", false)?(Typeface.BOLD):(Typeface.NORMAL)));
    		//TODO: УПРОСТИТЬ! до невозможности тяжелое решение! 
    		int s;
    		String fsa[]=getResources().getStringArray(R.array.pref_font_size_entries);
    		String fss=sp.getString("fontsize", fsa[3]);
    		for (s=0;s<fsa.length;s++)
    			if (fss.equals(fsa[s])) {
    				fontSize=12+s*2;
    				break;
    			};
    		
    	} else {
    		tf=tfDefault;
    		fontSize=fontSizeDefault;
    	};
    	etLog.setTypeface(tf);
    	etCmd.setTypeface(tf);
    	etLog.setTextSize(fontSize); //TODO: как вернуть настройки шрифта к системным?
    	etCmd.setTextSize(fontSize);
    	super.onResume();
    }
    
    protected void onDestroy() {
        super.onDestroy();
        //Log.d("l9droid", "need to stop application");
        if (killThreadsOnDestroyActivity) mt.destroy();
        //mt=null;
    }

    //@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.main, menu);
        //return true;
    	MenuItem mi;
    	mi = menu.add(0,1,0,"Library");
    	mi.setOnMenuItemClickListener(this);
        mi = menu.add(0, 2, 0, "Settings");
        mi.setIntent(new Intent(this, PrefActivity.class));
        mi = menu.add(0, 3, 0, "About");
        mi.setIntent(new Intent(this, AboutActivity.class));
        
        return super.onCreateOptionsMenu(menu);
    }

	//@Override
	public boolean onMenuItemClick(MenuItem arg0) {
		switch (arg0.getItemId()) {
		case 1: //library TODO: переделать в id, возможно перенести меню в ресурсы
			Intent intent=new Intent(this, LibraryActivity.class);
			startActivityForResult(intent, 1); //TODO: "1"-change it or kill ))
	        //mi.setIntent(intent);
			break;
		};
		return false;
	}
	
	@Override
	  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    // запишем в лог значения requestCode и resultCode
	    Log.d("myLogs", "requestCode = " + requestCode + ", resultCode = " + resultCode);
	    // если пришло ОК
	    if (resultCode == RESULT_OK) {
	      switch (requestCode) {
	      case 1:
	    	  //Toast.makeText(this, data.getStringExtra("opengame"), Toast.LENGTH_SHORT).show();
	    	  String newGame=data.getStringExtra("opengame");
		      mt.startGame(newGame);
	    	  etLog.setText(""); //TODO: поумнее очищать лог, есть вероятность потерять начало предложения.
		      if (mt.l9!=null) {
		    	  Toast.makeText(this, "Started: "+newGame, Toast.LENGTH_SHORT).show();
		    	  SharedPreferences sPref=getPreferences(MODE_PRIVATE);
		    	  Editor ed = sPref.edit();
		    	  ed.putString("lastgame", newGame);
		    	  ed.commit();
		      } else Toast.makeText(this, "Fault start of: "+newGame, Toast.LENGTH_SHORT).show();
	        break;
	      }
	    // если вернулось не ОК
	    } else {
	      Toast.makeText(this, "Wrong result", Toast.LENGTH_SHORT).show();
	    }
	  }
    
    // some text
	//@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bCmd: // кнопка ввода команды
			postCommand();
			break;
		}
	}

	public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
		postCommand();
		return true;
	}

	void postCommand() {
		//TODO: как вариант - глотать команды, добавляя в command - но только в 3и4 версиях
		if (etCmd.length()>0 && mt.l9.L9State==mt.l9.L9StateWaitForCommand) {
			etLog.append(etCmd.getText());
			command=etCmd.getText().toString();
			etCmd.setText("");
		};

	};

	}

class DebugStorage {
    private char[] debug;
    private int debugptr;
    private static final int debugsize=500;
    DebugStorage() {
		debug=new char[debugsize];
		debugptr=0;
    }
    boolean putchar(char c) {
    	debug[debugptr++]=c;
    	return (debugptr>=debugsize);
    }
    String getstr() {
    	String str=String.valueOf(debug, 0, debugptr);
    	debugptr=0;
    	return str;
    }
}
