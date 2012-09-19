package com.realife.l9droid;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class MainActivity extends Activity implements OnClickListener,OnEditorActionListener {
	
	Button bCmd;
	EditText etLog;
    EditText etCmd;
    
    L9implement l9;
    byte gamedata[];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
               
        bCmd = (Button) findViewById(R.id.bCmd);
        bCmd.setOnClickListener(this);
        
        etLog = (EditText) findViewById(R.id.etLog);
        etCmd = (EditText) findViewById(R.id.etCmd);
        etCmd.setOnEditorActionListener(this);

        etCmd.setText("");
        etLog.setText("Welcome to Level9 emulator v0.001\n(c)2012 Paul Stakhov\n");
        
        gamedata=new byte[49179];
        
        try {
            //InputStream is=getResources().openRawResource(R.raw.timev2);
        	InputStream is=getResources().openRawResource(R.raw.wormv3);
            is.read(gamedata);            
          } catch (IOException e) {
            e.printStackTrace();
          }
        
        l9=new L9implement(etLog,gamedata,this);
        
        l9.LoadGame("test", "");
        l9.step();
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // some text
	//@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bCmd: // кнопка ввода команды
			//int i=400;
			//while (l9.RunGame() && (i-->0) && l9.codeptr!=20631);
			if (etCmd.length()>0) {
				etLog.append(etCmd.getText()+"\n");
				l9.InputCommand(etCmd.getText().toString());
				etCmd.setText("");
				l9.step();
			};
			break;
		}
		
	}

	public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
		// TODO Auto-generated method stub
		if (etCmd.length()>0) {
			etLog.append(etCmd.getText()+"\n");
			l9.InputCommand(etCmd.getText().toString());
			etCmd.setText("");
			l9.step();
		};

		return true;
	}
	
	public boolean fileSave(short buff[]) {
		boolean rez=false;
		try {
		      // отрываем поток для записи
		      BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
		          openFileOutput("1.sav", MODE_PRIVATE)));
		      // пишем данные
		      for (int i=0;i<buff.length;i++)
		    	  bw.write(buff[i]);
		      // закрываем поток
		      bw.close();
		      rez=true;
		    } catch (FileNotFoundException e) {
		      e.printStackTrace();
		    } catch (IOException e) {
		      e.printStackTrace();
		    }
		return rez;
	}
	
	//todo: дописать :)
	public short[] fileLoad() {
		try {
			// открываем поток для чтения
		    BufferedReader br = new BufferedReader(new InputStreamReader(
		        openFileInput("1.sav")));
		    //String str = "";
		    // читаем содержимое
		    //while ((str = br.readLine()) != null) {
		      //Log.d(LOG_TAG, str);
		    //}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}

class L9implement extends L9 {
	
    //0 - default
    //1 - waiting for command from user
    //2 - command entered, need to put it into parser
    //TODO: заменить на enum?
	
    String cmdStr;
    DebugStorage ds;
    String vStr;
    MainActivity mAct;
	
	EditText et;
	byte gamedata[];
	L9implement(EditText et1, byte dat[], MainActivity m) {
		et=et1;
		gamedata=dat;
		cmdStr=null;
		ds=new DebugStorage();
		mAct=m;
	};
	
	void os_printchar(char c) {
		if (c==0x0d) log_debug(ds.getstr());
		else if (ds.putchar(c)) log_debug(ds.getstr());
		if (c==0x0d) et.append("\n");
		et.append(String.valueOf(c));
	};
	
	byte[] os_load(String filename) {
		return gamedata;
	};
	
	void os_debug(String str) {
		log_debug(ds.getstr());
		log_debug(str);
	};
	
	void os_verbose(String str) {
		log_verbose(str);
	};
	
	void log_debug(String str) {
		final String LOG_TAG = "l9droid";
		if (str.length()>0) 
			Log.d(LOG_TAG, str);
	};
	
	void log_verbose(String str) {
		final String LOG_TAG = "l9droid";
		if (str.length()>0) 
			Log.v(LOG_TAG, str);
	};
	
	void os_flush() {
		os_debug(ds.getstr());
	}
	
	void step() {
		while (L9State==L9StateRunning || L9State==L9StateCommandReady) RunGame();
	};
	
	boolean os_save_file(short[] buff) {
		return mAct.fileSave(buff);
	};
	
	short[] os_load_file() {
		return mAct.fileLoad();
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
