package com.realife.l9droid;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity implements OnClickListener {
	
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

        etCmd.setText("GO WEST");
        etLog.setText("Welcome to Level9 emulator v0.001\n(c)2012 Paul Stakhov\n");
        
        gamedata=new byte[49179];
        
        try {
            InputStream is=getResources().openRawResource(R.raw.timev2);
        	//InputStream is=getResources().openRawResource(R.raw.wormv3);
            is.read(gamedata);            
          } catch (IOException e) {
            e.printStackTrace();
          }
        
        l9=new L9implement(etLog,gamedata);
        
        l9.LoadGame("test", "");
        
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
				etLog.append(">"+etCmd.getText()+"\n");
				etCmd.setText("");
				l9.InputCommand(etCmd.getText().toString());
				l9.step();

			};
			break;
		}
		
	}
}

class L9implement extends L9 {
	
    //0 - default
    //1 - waiting for command from user
    //2 - command entered, need to put it into parser
    //TODO: заменить на enum?
	
    String cmdStr;
	
	EditText et;
	byte gamedata[];
	L9implement(EditText et1, byte dat[]) {
		et=et1;
		gamedata=dat;
		cmdStr=null;
	};
	
	void os_printchar(char c) {
		if (c==0x0d) et.append("\n");
		et.append(String.valueOf(c));
	};
	
	byte[] os_load(String filename) {
		return gamedata;
	};
	
	void os_debug(String str) {
		final String LOG_TAG = "l9droid";
		Log.d(LOG_TAG, str);
	};
	
	void step() {
		while (L9State==L9StateRunning) RunGame();
	};


}
