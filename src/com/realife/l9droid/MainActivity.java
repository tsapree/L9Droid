package com.realife.l9droid;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
	
	public final static int MACT_L9WORKING = 0;
	public final static int MACT_L9WAITFORCOMMAND = 1;
	public final static int MACT_PRINTCHAR = 2;
	public final static int MACT_SAVEGAMESTATE = 3;
	public final static int MACT_LOADGAMESTATE=4;
	
	Button bCmd;
	EditText etLog;
    EditText etCmd;
    Handler h;
    Thread t;
    String command;
    
    L9implement l9;
    byte gamedata[];
    
    boolean saveload_flag=false;

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
        
        command=null;
        
        gamedata=new byte[49179];
        
		h = new Handler() {
		    public void handleMessage(android.os.Message msg) {
		    	switch (msg.what) {
		    	case MACT_L9WORKING:
		    		bCmd.setText("...");
		    		bCmd.setEnabled(false);
		    		break;
		    	case MACT_L9WAITFORCOMMAND:
		    		bCmd.setText("Do");
		    		bCmd.setEnabled(true);
		    		break;
	    		case MACT_PRINTCHAR:
	    			char c=(char)msg.arg1;
	    			if (c==0x0d) etLog.append("\n");
	    			else etLog.append(String.valueOf(c));
	    			break;
	    		case MACT_SAVEGAMESTATE:
    				l9.saveok=fileSave(l9.saveloadBuff);
    				l9.saveloaddone=true;
	    			break;
	    		case MACT_LOADGAMESTATE:
	    			l9.saveloadBuff=fileLoad();
	    			l9.saveloaddone=true;
	    			break;
		    	}
		    };
		};
		h.sendEmptyMessage(MACT_L9WORKING);
        
		try {
			//InputStream is=getResources().openRawResource(R.raw.timev2);
			InputStream is=getResources().openRawResource(R.raw.wormv3);
			is.read(gamedata);            
		} catch (IOException e) {
			e.printStackTrace();
		}
        
		t = new Thread(new Runnable() {
			public void run() {
		        l9=new L9implement(gamedata,h);
		        if (l9.LoadGame("test", "")==true) {
			        while (l9.L9State!=l9.L9StateStopped) {
			        	if (l9.L9State==l9.L9StateWaitForCommand) {
			        		h.sendEmptyMessage(MACT_L9WAITFORCOMMAND);
			        		//TODO: проверить try-catch на грамотность, не нужно ли все заключить в них, что произойдет, если наступит exception?
							try {
								while (command==null) 
									TimeUnit.MILLISECONDS.sleep(200);
								h.sendEmptyMessage(MACT_L9WORKING);
								//TODO: t.wait - возможно, более правильное решение.
								l9.InputCommand(command);
								command=null;
							} catch (InterruptedException e) {
								e.printStackTrace();
							};
			        	} else l9.step();
			        };
		        }
			}
		});
		t.start();
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
		if (etCmd.length()>0 && l9.L9State==l9.L9StateWaitForCommand) {
			etLog.append(etCmd.getText()+"\n");
			command=etCmd.getText().toString();
			etCmd.setText("");
		};
	};
	
	public boolean fileSave(byte buff[]) {
		try {
			OutputStream out = openFileOutput ("1.sav", MODE_PRIVATE);
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
			InputStream in=openFileInput("1.sav");
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

class L9implement extends L9 {
    String cmdStr;
    DebugStorage ds;
    String vStr;
    Handler mHandler;
    Message msg;
    byte saveloadBuff[];
    boolean saveloaddone;
    boolean saveok;
	
	EditText et;
	byte gamedata[];
	L9implement(/*EditText et1,*/ byte dat[], Handler h) {
		//et=et1;
		gamedata=dat;
		cmdStr=null;
		ds=new DebugStorage();
		mHandler=h;
	};
	
	void os_printchar(char c) {
		if (c==0x0d) log_debug(ds.getstr());
		else if (ds.putchar(c)) log_debug(ds.getstr());
		msg = mHandler.obtainMessage(MainActivity.MACT_PRINTCHAR, c, 0);
		mHandler.sendMessage(msg);
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
	
	boolean os_save_file(byte[] buff) {
		saveloadBuff=buff;
		saveloaddone=false;
		saveok=false;
		mHandler.sendEmptyMessage(MainActivity.MACT_SAVEGAMESTATE);
		while (saveloaddone==false) {
			try {
				TimeUnit.MILLISECONDS.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			};
		};
		return saveok;
	};
	
	byte[] os_load_file() {
		saveloadBuff=null;
		saveloaddone=false;
		mHandler.sendEmptyMessage(MainActivity.MACT_LOADGAMESTATE);
		while (saveloaddone==false) {
			try {
				TimeUnit.MILLISECONDS.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			};
		};
		return saveloadBuff; //TODO:mAct.fileLoad();
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
