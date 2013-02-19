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
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener, TextWatcher,
		OnEditorActionListener,

		OnMenuItemClickListener,
		OnItemClickListener,
		OnItemLongClickListener {

    private final static int MENU_ITEM_LIBRARY_FILES = 1;
    private final static int MENU_ITEM_SETTINGS = 2;
    private final static int MENU_ITEM_SAVE_STATE = 4;
    private final static int MENU_ITEM_RESTORE_STATE = 5;
    private final static int MENU_ITEM_PICTURES = 6;
    private final static int MENU_ITEM_WORDS = 7;
    private final static int MENU_ITEM_PLAY_SCRIPT = 8;
    private final static int MENU_ITEM_LIBRARY = 9;
    private final static int MENU_ITEM_HISTORY = 10;

	private final static int LIBRARYACTIVITY_RESULT = 1;
	private final static int RESTOREGAMEACTIVITY_RESULT = 2;
    
	SharedPreferences sp;
	Typeface tf;
	Typeface tfDefault=null;
	float fontSizeDefault=0;
	
	View activityRootView;
	
	ImageButton ibCmd;
	ImageButton ibMenu;
	EditText etCmd;
	
	Button bSpace;
	Button bEnter;

	ListView lvMain;
	ListView lvHistory;
   
	ImageView ivScreen;
	int MaxPictureHeightInPercent = 30; 
    
	String command;
    
	static Threads mt;
    
	boolean killThreadsOnDestroyActivity=true;
	
	boolean pictureZoomHeight = false; //TODO: вынести в настройки
	
	int prevAppHeight = 0;
	int prevAppWidth = 0;
	int prevLogHeight = 0;
	int prevLogWidth = 0;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        
        ivScreen=(ImageView) findViewById(R.id.ivPicture);
        ivScreen.setOnClickListener(this);
                       
        ibCmd = (ImageButton) findViewById(R.id.ibCmd);
        ibCmd.setOnClickListener(this);
    	ibMenu = (ImageButton) findViewById(R.id.ibMenu);
    	ibMenu.setOnClickListener(this);
        
        
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
        //lvMain.setSelection(lvMain.getAdapter().getCount()-1);
        
        lvHistory.setAdapter(mt.lvHistoryAdapter);
        //lvHistory.setSelection(lvHistory.getAdapter().getCount()-1);
        lvHistory.setOnItemClickListener(this);
        lvHistory.setOnItemLongClickListener(this);
        
        activityRootView = findViewById(R.id.rlMain);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            public void onGlobalLayout() {
            	int viewHeight = activityRootView.getHeight();
            	int viewWidth = activityRootView.getWidth();
            	if (((viewHeight!=prevAppHeight) || (viewWidth != prevAppWidth) ) && ivScreen!=null) {
                	prevAppHeight = viewHeight;
                	prevAppWidth = viewWidth;
                	updatePictureSize();
            	};
            	
             }});
	    
    }
    
    public void updatePictureSize() {
    	if ((mt.bm!=null) && (activityRootView!=null)) {
    		int maxWidth = activityRootView.getWidth();
    		if (maxWidth<1) return;
    		int bmHeight=mt.bm.getHeight();
    		int bmWidth=mt.bm.getWidth();
    		if (bmWidth==160 && bmHeight ==128) bmWidth*=2;
    		if (bmWidth>maxWidth) bmWidth=maxWidth;
    		int maxHeight = bmHeight * activityRootView.getWidth() / bmWidth ;
    		int h = activityRootView.getHeight() * MaxPictureHeightInPercent / 100;
    		if (h>maxHeight) h=maxHeight;
    		int w = bmWidth * h / bmHeight;
    		if (pictureZoomHeight) w=maxWidth;
    		ivScreen.getLayoutParams().height=h;
    		ivScreen.getLayoutParams().width=w;
    		ivScreen.requestLayout();
    	};
    };
    
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
    	//etCmd.setTypeface(tf);
    	//etCmd.setTextSize(fontSize);
    	
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
        menu.setGroupVisible(2, mt.menuHashEnabled && Threads.gfx_ready && (!mt.menuPicturesEnabled));
        menu.setGroupVisible(3, mt.menuHashEnabled && Threads.gfx_ready &&  mt.menuPicturesEnabled);
        
        return super.onPrepareOptionsMenu(menu);
    }
    
    //@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuItem mi;
    	mi = menu.add(0, MENU_ITEM_LIBRARY,0,"Library");
    	mi.setOnMenuItemClickListener(this);
        mi = menu.add(1, MENU_ITEM_SAVE_STATE, 0,"Save State");
    	mi.setOnMenuItemClickListener(this);
    	mi = menu.add(1, MENU_ITEM_RESTORE_STATE, 0,"Restore State");
    	mi.setOnMenuItemClickListener(this);
    	mi = menu.add(2, MENU_ITEM_PICTURES, 0,"Pictures");
    	mi.setOnMenuItemClickListener(this);
    	mi = menu.add(3, MENU_ITEM_WORDS, 0,"Words");
    	mi.setOnMenuItemClickListener(this);
        mi = menu.add(0, MENU_ITEM_SETTINGS, 0, "Settings");
        mi.setIntent(new Intent(this, PrefActivity.class));
    	mi = menu.add(1, MENU_ITEM_PLAY_SCRIPT, 0,"Play Script");
    	mi.setOnMenuItemClickListener(this);
    	mi = menu.add(0, MENU_ITEM_LIBRARY_FILES, 2, "Library Files");
    	mi.setOnMenuItemClickListener(this);
    	mi = menu.add(0, MENU_ITEM_HISTORY, 3, "Commands History");
    	mi.setOnMenuItemClickListener(this);
    	
        return super.onCreateOptionsMenu(menu);
    }

	//@Override
	public boolean onMenuItemClick(MenuItem arg0) {
		Intent intent;
		switch (arg0.getItemId()) {
		case MENU_ITEM_LIBRARY_FILES: 
			intent=new Intent(this, LibraryActivity.class);
			startActivityForResult(intent, LIBRARYACTIVITY_RESULT);
			break;
		case MENU_ITEM_LIBRARY: 
			intent=new Intent(this, LibraryGamesActivity.class);
			startActivityForResult(intent, LIBRARYACTIVITY_RESULT);
			break;
		case MENU_ITEM_SAVE_STATE:
			postHashCommand("#save");
			break;
		case MENU_ITEM_RESTORE_STATE:
			postHashCommand("#restore");
			break;
		case MENU_ITEM_PICTURES:
			postHashCommand("pictures");
			break;
		case MENU_ITEM_WORDS:
			postHashCommand("words");
			break;
		case MENU_ITEM_PLAY_SCRIPT:
			postHashCommand("#play");
			break;
		case MENU_ITEM_HISTORY:
			toggleCommandsHistory();
			break;
		};

		return false;
	}
	
	public void selectFileToRestore() {
		Intent intent=new Intent(this, RestoreGameActivity.class);
		intent.putExtra("gamepath", mt.lib.getGamePath());
		startActivityForResult(intent, RESTOREGAMEACTIVITY_RESULT);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// если пришло ОК
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case LIBRARYACTIVITY_RESULT: 
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
			case RESTOREGAMEACTIVITY_RESULT: //restore game activity
				String restoreGame=data.getStringExtra("restoregame");
				mt.choosed_restore_filename=restoreGame;
				mt.choosing_restore_filename=false;
				break;
			}
			// если вернулось не ОК
		} else {
			switch (requestCode) {
			case RESTOREGAMEACTIVITY_RESULT:
				mt.choosed_restore_filename=null;
				mt.choosing_restore_filename=false;
				break;
			}
		};
	}
    
    // some text
	//@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ibCmd: // кнопка ввода команды
			if (etCmd.length()>0) {
				postCommand();
			} else {
				toggleCommandsHistory();
			}
			break;
		case R.id.ibMenu:
			openOptionsMenu();
			break;
		case R.id.bSpace:
			mt.keyPressed=' ';
			break;
		case R.id.bEnter:
			mt.keyPressed='\r';
			break;
		case R.id.ivPicture:
			if (mt!=null && mt.l9!=null) 
				mt.l9.waitPictureToDraw(); 
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
			//lvMain.setSelection(lvMain.getAdapter().getCount()-1);
		};
	}
	
	void postCommand() {
		if (etCmd.length()>0 && mt.l9.L9State==L9.L9StateWaitForCommand) {
			outUserInputToLog(etCmd.getText().toString());
    
			mt.history.add(etCmd.getText().toString());
			mt.lvHistoryAdapter.notifyDataSetChanged();
			//lvHistory.setSelection(lvHistory.getAdapter().getCount()-1);
			
			command=etCmd.getText().toString();
			etCmd.setText("");
		};

	};
	
	void postHashCommand(String cmd) {
		etCmd.setText(cmd);
		postCommand();
	}

	public void afterTextChanged(Editable arg0) {
		ibCmdSet();
	}

	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		//Auto-generated method stub
		
	}

	public void onTextChanged(CharSequence s, int start, int before, int count) {
		//Auto-generated method stub
	}
	
	void toggleCommandsHistory() {
		setVisibilityCommandsHistory(!getVisibilityCommandsHistory());
	};
	
	void setVisibilityCommandsHistory(boolean show) {
		if (show) {
			lvHistory.setVisibility(View.VISIBLE);
			ibCmdSet();
		} else {
			lvHistory.setVisibility(View.GONE);
			ibCmdSet();
		};
	}
	
	boolean getVisibilityCommandsHistory() {
		return lvHistory.getVisibility()==View.VISIBLE;
	};
	
	void ibCmdSet() {
		if ((etCmd==null) || (lvHistory==null)) return;
		if (etCmd.getText().length()>0) ibCmd.setImageResource(R.drawable.ic_do); //bCmd.setText(label);
		else {
			if (lvHistory.getVisibility()==View.VISIBLE) ibCmd.setImageResource(R.drawable.ic_history_hide); //bCmd.setText(">");
			else ibCmd.setImageResource(R.drawable.ic_history_show); //bCmd.setText("<");
			
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

