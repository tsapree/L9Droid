package com.realife.l9droid;

import android.app.Activity;
import android.os.Bundle;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
               
        bCmd = (Button) findViewById(R.id.bCmd);
        bCmd.setOnClickListener(this);
        
        etLog = (EditText) findViewById(R.id.etLog);
        etCmd = (EditText) findViewById(R.id.etCmd);

        l9=new L9implement(etLog);
        
        etCmd.setText("GO WEST");
        etLog.setText("Welcome to Level9 emulator v0.001\n(c)2012 Paul Stakhov\n");
        etLog.append("hey!\n");
        
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
			etLog.append(">"+etCmd.getText()+"\n");
			etCmd.setText("");

			break;
		}
		
	}

}

class L9implement extends L9 {
	EditText et;
	L9implement(EditText et1) {
		et=et1;
	};
	void os_printchar(char c) {
		et.append(String.valueOf(c));
	};
}
