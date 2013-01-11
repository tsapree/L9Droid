package com.realife.l9droid;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener, TextWatcher,
		OnEditorActionListener,

		OnMenuItemClickListener,
		OnItemClickListener,
		OnItemLongClickListener {
	
	SharedPreferences sp;
	Typeface tf;
	Typeface tfDefault=null;
	float fontSizeDefault=0;
	
	Button bCmd;
	EditText etCmd;
	Button bSpace;
	Button bEnter;

	ListView lvMain;
	ListView lvHistory;
   
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
        ivScreen.setOnClickListener(this);
                       
        bCmd = (Button) findViewById(R.id.bCmd);
        bCmd.setOnClickListener(this);
        
        bSpace = (Button) findViewById(R.id.bSpace);
        bSpace.setOnClickListener(this);
    	bEnter = (Button) findViewById(R.id.bEnter);
    	bEnter.setOnClickListener(this);
        
        etCmd = (EditText) findViewById(R.id.etCmd);
        etCmd.setHint("Enter your command");
        etCmd.addTextChangedListener(this);
        etCmd.setOnEditorActionListener(this);
        etCmd.setText("");
        
        lvMain = (ListView) findViewById(R.id.lvLog);
        lvMain.setDividerHeight(0);
        
        lvHistory = (ListView) findViewById(R.id.lvHistory);
        //lvHistory.setVisibility(ListView.VISIBLE);
                 
        command=null;
        
        SharedPreferences sPref=getPreferences(MODE_PRIVATE);
        setVisibilityCommandsHistory(sPref.getBoolean("showhistory", false)) ;
        
        mt = (Threads) getLastNonConfigurationInstance();
	    if (mt == null) {
	    	mt=new Threads();
	    	mt.link(this);
	        mt.create();
	        String lastGame = sPref.getString("lastgame", null);
	        mt.startGame(lastGame,true);
	        if (mt.l9!=null) Toast.makeText(this, "Started: "+lastGame, Toast.LENGTH_SHORT).show();
	        else Toast.makeText(this, "Fault start of: "+lastGame, Toast.LENGTH_SHORT).show();
	        
	    } else mt.link(this);
        lvMain.setAdapter(mt.lvAdapter);
        lvMain.setSelection(lvMain.getAdapter().getCount()-1);
        
        lvHistory.setAdapter(mt.lvHistoryAdapter);
        lvHistory.setSelection(lvHistory.getAdapter().getCount()-1);
        lvHistory.setOnItemClickListener(this);
        lvHistory.setOnItemLongClickListener(this);

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
    	//etLog.setTypeface(tf);
    	etCmd.setTypeface(tf);
    	//etLog.setTextSize(fontSize); //TODO: как вернуть настройки шрифта к системным?
    	etCmd.setTextSize(fontSize);
    	
    	mt.activityPaused=false;
    	super.onResume();
    }
    
    protected void onPause() {
    	super.onPause();
    	mt.activityPaused=true;
    }
    
    protected void onDestroy() {
		super.onDestroy();
		mt.activityPaused=true;

		SharedPreferences sPref=getPreferences(MODE_PRIVATE);
		Editor ed = sPref.edit();
		ed.putBoolean("showhistory", getVisibilityCommandsHistory());
		
		//Log.d("l9droid", "need to stop application");
		if (killThreadsOnDestroyActivity && mt.l9!=null) {
			ed.putString("lastgame", mt.l9.LastGame);
			mt.destroy(true);
		}
		
		ed.commit();

    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.setGroupVisible(1, mt.menuHashEnabled);
        menu.setGroupVisible(2, mt.menuHashEnabled && mt.gfx_ready && (!mt.menuPicturesEnabled));
        menu.setGroupVisible(3, mt.menuHashEnabled && mt.gfx_ready &&  mt.menuPicturesEnabled);
        
        return super.onPrepareOptionsMenu(menu);
    }
    
    //@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.main, menu);
        //return true;
    	MenuItem mi;
    	mi = menu.add(0, 9,0,"Library");
    	mi.setOnMenuItemClickListener(this);
        mi = menu.add(1, 4, 0,"Save State");
    	mi.setOnMenuItemClickListener(this);
    	mi = menu.add(1, 5, 0,"Restore State");
    	mi.setOnMenuItemClickListener(this);
    	mi = menu.add(2, 6, 0,"Pictures");
    	mi.setOnMenuItemClickListener(this);
    	mi = menu.add(3, 7, 0,"Words");
    	mi.setOnMenuItemClickListener(this);
        mi = menu.add(0, 2, 0, "Settings");
        mi.setIntent(new Intent(this, PrefActivity.class));
    	mi = menu.add(1, 8, 0,"Play Script");
    	mi.setOnMenuItemClickListener(this);
        mi = menu.add(0, 3, 1, "About");
        mi.setIntent(new Intent(this, AboutActivity.class));
    	mi = menu.add(0, 1, 2, "Library Files");
    	mi.setOnMenuItemClickListener(this);
    	mi = menu.add(0, 10, 3, "Commands History");
    	mi.setOnMenuItemClickListener(this);
    	
        return super.onCreateOptionsMenu(menu);
    }

	//@Override
	public boolean onMenuItemClick(MenuItem arg0) {
		Intent intent;
		switch (arg0.getItemId()) {
		case 1: //library TODO: переделать в id, возможно перенести меню в ресурсы
			intent=new Intent(this, LibraryActivity.class);
			startActivityForResult(intent, 1); //TODO: "1"-change it or kill ))
	        //mi.setIntent(intent);
			break;
		case 9: //library TODO: переделать в id, возможно перенести меню в ресурсы
			intent=new Intent(this, LibraryGamesActivity.class);
			startActivityForResult(intent, 1); //TODO: "1"-change it or kill ))
	        //mi.setIntent(intent);
			break;
		case 4:
			postHashCommand("#save");
			break;
		case 5:
			postHashCommand("#restore");
			break;
		case 6:
			postHashCommand("pictures");
			break;
		case 7:
			postHashCommand("words");
			break;
		case 8:
			postHashCommand("#play");
			break;
		case 10:
			toggleCommandsHistory();
			break;
		};

		return false;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// если пришло ОК
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case 1:
				//Toast.makeText(this, data.getStringExtra("opengame"), Toast.LENGTH_SHORT).show();
				String newGame=data.getStringExtra("opengame");
				mt.stopGame();
				mt.logStringCapacitor=null;
				mt.logStrId=-1;
				mt.lvAdapter.clear();
				mt.lvHistoryAdapter.clear();
				mt.startGame(newGame,false);
				if (mt.l9!=null) {
					Toast.makeText(this, "Started: "+newGame, Toast.LENGTH_SHORT).show();
				} else Toast.makeText(this, "Fault start of: "+newGame, Toast.LENGTH_SHORT).show();
				etCmd.setText("");
				break;
			}
			// если вернулось не ОК
		};
	}
    
    // some text
	//@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bCmd: // кнопка ввода команды
			if (etCmd.length()>0) {
				postCommand();
			} else {
				toggleCommandsHistory();
			}
			break;
		case R.id.imageView1:
			if (mt!=null && mt.l9!=null) 
				mt.l9.waitPictureToDraw(); 
			break;
		case R.id.bSpace:
			mt.keyPressed=' ';
			break;
		case R.id.bEnter:
			mt.keyPressed='\r';
			break;
		}
	}

	public void onItemClick(AdapterView<?> parent, View arg1, int position, long id) {
		etCmd.setText(mt.lvHistoryAdapter.getItem(position));
		postCommand();
	};
	
	public boolean onItemLongClick(AdapterView<?> parent, View arg1, int position, long id) {
		etCmd.setText(mt.lvHistoryAdapter.getItem(position));
		return true;
	}
	
	public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
		postCommand();
		return true;
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (bSpace.getVisibility()==View.VISIBLE) {
			if (keyCode==KeyEvent.KEYCODE_SPACE) {
			//if (keyCode==KeyEvent.KEYCODE_1) {
				mt.keyPressed=' ';
				return true;
			}
			else if (keyCode==KeyEvent.KEYCODE_ENTER) {				
			//else if (keyCode==KeyEvent.KEYCODE_2) {
				mt.keyPressed='\r';
				return true;
			}
		};
		return super.onKeyDown(keyCode, event);
	}
	
	void outCharToLog(char c) {
		if (mt.logStringCapacitor==null) mt.logStringCapacitor=new SpannableStringBuilder();
		 
		//every enter starts new paragraph
		if (c=='\n') outLogFlush(true);
		else mt.logStringCapacitor.append(c);
		
		//no unnecessary line breaks
		//if (c=='\n') {
		//	if (logStringCapacitor.length()>0 && logStringCapacitor.charAt(logStringCapacitor.length()-1)!='\n') logStringCapacitor.append(c);
		//} else 
		//	logStringCapacitor.append(c);
	};
	
	void outUserInputToLog(String str) {
		SpannableStringBuilder text = new SpannableStringBuilder(str);
        ForegroundColorSpan style = new ForegroundColorSpan(Color.rgb(0, 0, 255)); 
        text.setSpan(style, 0, text.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        if (mt.logStringCapacitor==null) mt.logStringCapacitor=new SpannableStringBuilder();
        mt.logStringCapacitor.append(text);
        outLogFlush(true);
	};
	
	void outLogFlush(boolean finishThisString) {
		if (mt.logStringCapacitor!=null && mt.logStringCapacitor.length()>0) {
			if ((mt.logStrId>=0) && (mt.logStrId<lvMain.getAdapter().getCount())) {
				mt.lvAdapter.getItem(mt.logStrId).append(mt.logStringCapacitor);
			} else {
				mt.lvAdapter.add(mt.logStringCapacitor);
			}
			mt.logStringCapacitor=null;
			if (finishThisString) mt.logStrId=-1;
			else mt.logStrId=lvMain.getAdapter().getCount()-1;
			mt.lvAdapter.notifyDataSetChanged();
			lvMain.setSelection(lvMain.getAdapter().getCount()-1);
		};
	}
	
	void postCommand() {
		//TODO: как вариант - глотать команды, добавляя в command - но только в 3и4 версиях
		if (etCmd.length()>0 && mt.l9.L9State==mt.l9.L9StateWaitForCommand) {
			outUserInputToLog(etCmd.getText().toString());
    
			mt.history.add(etCmd.getText().toString());
			mt.lvHistoryAdapter.notifyDataSetChanged();
			lvHistory.setSelection(lvHistory.getAdapter().getCount()-1);
			
			command=etCmd.getText().toString();
			etCmd.setText("");
		};

	};
	
	void postHashCommand(String cmd) {
		etCmd.setText(cmd);
		postCommand();
	}

	public void afterTextChanged(Editable arg0) {
		bCmdSetText();
	}

	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub
		
	}

	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub
		
	}
	
	void toggleCommandsHistory() {
		setVisibilityCommandsHistory(!getVisibilityCommandsHistory());
	};
	
	void setVisibilityCommandsHistory(boolean show) {
		if (show) {
			lvHistory.setVisibility(View.VISIBLE);
			bCmdSetText();
		} else {
			lvHistory.setVisibility(View.GONE);
			bCmdSetText();
		};
	}
	
	boolean getVisibilityCommandsHistory() {
		return lvHistory.getVisibility()==View.VISIBLE;
	};
	
	void bCmdSetText() {
		bCmdSetText(null);
	};
		
	void bCmdSetText(String txt) {
		String label=null;
		if ((etCmd==null) || (lvHistory==null)) return;
		
		if (txt==null) {
			label=(String)(bCmd.getTag());
		} else {
			label=txt;
			bCmd.setTag(txt);
		};
		if (etCmd.getText().length()>0) bCmd.setText(label);
		else {
			if (lvHistory.getVisibility()==View.VISIBLE) bCmd.setText(">");
			else bCmd.setText("<");
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

