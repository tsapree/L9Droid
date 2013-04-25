package pro.oneredpixel.l9droid;

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

public class GameActivity extends Activity implements OnClickListener, TextWatcher,
		OnEditorActionListener,

		OnMenuItemClickListener,
		OnItemClickListener,
		OnItemLongClickListener {

    private final static int MENU_ITEM_SETTINGS = 1;
    private final static int MENU_ITEM_SAVE_STATE = 2;
    private final static int MENU_ITEM_RESTORE_STATE = 3;
    private final static int MENU_ITEM_PICTURES = 4;
    private final static int MENU_ITEM_WORDS = 5;
    private final static int MENU_ITEM_PLAY_SCRIPT = 6;
    private final static int MENU_ITEM_LIBRARY = 7;
    private final static int MENU_ITEM_HISTORY = 8;
    private final static int MENU_ITEM_CLOSEAPP = 9;
    private final static int MENU_ITEM_HOWTOPLAY = 10;    

	private final static int LIBRARYACTIVITY_RESULT = 1;
	private final static int RESTOREGAMEACTIVITY_RESULT = 2;
    
	SharedPreferences sp;
	
	int		pref_logtextcolor=0xFF000000;
	int		pref_logcommandcolor=0xFF0000FF;
	int		pref_logbackgroundcolor=0xFFFFFFFF;
	int		pref_logtextsize = 13;
	int		pref_logtexttypeface;
	boolean pref_logtextbold = false;
	boolean pref_logtextitalic = false;
	int		pref_loglimit = 0;

	int		pref_histtextsize = 13;
	int		pref_histwidth = 25; //in percent
	
	int		pref_picspeed = 10;
	int		pref_picmaxheight = 30;
	boolean	pref_picstretch = false;
	boolean pref_picpaletteamiga = true;

	
	String	pref_syssaveprefix = "state";
	int		pref_sysscriptdelay = 2;
	
	View activityRootView;
	
	ImageButton ibCmd;
	ImageButton ibMenu;
	EditText etCmd;
	Button bSpace;
	Button bEnter;
	ListView lvMain;
	ListView lvHistory;
	ImageView ivScreen;
	
	String command;
    
	static Threads mt;
    
	boolean killThreadsOnDestroyActivity=true;
	
	boolean needToExitApp = false;
	
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
        etCmd.setHint("Enter command");
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
	        if (mt.lib.getGamePath()==null) {
	        	Intent intent=new Intent(this, LibraryGamesActivity.class);
				startActivityForResult(intent, LIBRARYACTIVITY_RESULT);
	        } else {
	        	GameInfo gi=mt.lib.getGameInfo(this,mt.lib.getFileNameWithoutPath(mt.lib.getFolder(mt.lib.getGamePath())));
	        	Toast.makeText(this, gi.getTitle(), Toast.LENGTH_SHORT).show();
	        }
	        
	    } else mt.link(this);
	    
        lvMain.setAdapter(mt.lvAdapter);
        
        lvHistory.setAdapter(mt.lvHistoryAdapter);
        lvHistory.setOnItemClickListener(this);
        lvHistory.setOnItemLongClickListener(this);
        
        activityRootView = findViewById(R.id.rlMain);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            public void onGlobalLayout() {
            	int viewHeight = activityRootView.getHeight();
            	int viewWidth = activityRootView.getWidth();
            	if ((viewHeight!=prevAppHeight) || (viewWidth != prevAppWidth)) {
            		lvHistory.getLayoutParams().width = viewWidth*pref_histwidth/100;
            		lvHistory.requestLayout();
            	};
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
    		int h = activityRootView.getHeight() * pref_picmaxheight / 100;
    		if (h>maxHeight) h=maxHeight;
    		int w = bmWidth * h / bmHeight;
    		if (pref_picstretch) w=maxWidth;
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

		//preferences: log
		pref_logtextcolor = color(sp.getString("logtextcolor", "#00000000"),Color.BLACK);
		pref_logcommandcolor = color(sp.getString("logcommandcolor", "#000000FF"),Color.BLUE);
		pref_logbackgroundcolor = color(sp.getString("logbackgroundcolor", "#00FFFFFF"),Color.WHITE);
		pref_logtextsize = check_bounds(val(sp.getString("logtextsize", "13"),13),5,30,13);
		pref_logtextbold = sp.getBoolean("logtextbold", false);
		pref_logtextitalic = sp.getBoolean("logtextitalic", false);
		pref_loglimit = check_bounds(val(sp.getString("loglimit", "0"),0),0,2048,0);
		
		String ft[]=getResources().getStringArray(R.array.pref_font_typeface_entries);
		String fss=sp.getString("logtexttypeface", ft[0]);
		pref_logtexttypeface=0;
		for (String t: ft) {
			if (t.equalsIgnoreCase(fss)) break;
			pref_logtexttypeface++;
		};
		switch (pref_logtexttypeface) {
		case 1:  mt.lvAdapter.texttypeface = Typeface.MONOSPACE;	break;
		case 2:  mt.lvAdapter.texttypeface = Typeface.SERIF;		break;
		case 3:  mt.lvAdapter.texttypeface = Typeface.SANS_SERIF;	break;
		default: mt.lvAdapter.texttypeface = Typeface.DEFAULT;		break;
		}
		mt.lvAdapter.textcolor = pref_logtextcolor;
		mt.lvAdapter.backgroundcolor = pref_logbackgroundcolor;
		activityRootView.setBackgroundColor(pref_logbackgroundcolor);
		lvMain.setCacheColorHint(pref_logbackgroundcolor);

		mt.lvAdapter.textsize = pref_logtextsize;
		mt.lvAdapter.textstyle = (pref_logtextitalic?Typeface.ITALIC:0)|(pref_logtextbold?Typeface.BOLD:0);
		
		mt.lib.refreshLogCommandsColor(mt.lvAdapter, pref_logcommandcolor);
    	mt.lvAdapter.notifyDataSetChanged();

		//preferences: commands history
		pref_histtextsize = check_bounds(val(sp.getString("histtextsize", "13"),13),5,30,13);;
		pref_histwidth = check_bounds(val(sp.getString("histwidth", "25"),25), 10, 50, 25);
		
		mt.lvHistoryAdapter.textcolor = pref_logcommandcolor;
		mt.lvHistoryAdapter.backgroundcolor = pref_logbackgroundcolor;
		lvHistory.setCacheColorHint(pref_logbackgroundcolor);
		mt.lvHistoryAdapter.textsize = pref_histtextsize;
		mt.lvHistoryAdapter.notifyDataSetChanged();
		
   		lvHistory.getLayoutParams().width = activityRootView.getWidth()*pref_histwidth/100;
   		lvHistory.requestLayout();
		
		//preferences: picture
    	pref_picspeed = check_bounds(val(sp.getString("picspeed", "10"),10),1,255,10);
    	pref_picmaxheight = check_bounds(val(sp.getString("picmaxheight", "30"),30),5,70,30);
    	pref_picstretch = sp.getBoolean("picstretch", false);
    	
    	pref_picpaletteamiga = sp.getString("picpalette","Amiga").equalsIgnoreCase("Amiga"); 
    	if (mt!=null && mt.l9!=null) {
    		mt.l9.L9UpdatePalette();
    		mt.l9.repaintPicture();
    	}
    	updatePictureSize();
    	
    	//preferences: system
    	pref_syssaveprefix = sp.getString("syssaveprefix","state");
    	pref_sysscriptdelay = check_bounds(val(sp.getString("sysscriptdelay", "2"),2), 0, 30, 2);

    	
    	mt.activityPaused=false;
    	super.onResume();
    	
    	if (needToExitApp) finish();
    }
    
    int color(String s, int default_color) {
    	int c;
    	try {
    		c=Color.parseColor(s);
        	} catch (IllegalArgumentException e) {
    			c=default_color;
    		};
    	return c | 0xFF000000;
    }
    
    int val(String s, int default_value) {
    	int i;
    	try {
    		i=Integer.decode(s);
        	} catch (NumberFormatException e) {
    			i=default_value;
    		}
    	return i;
    };
    
    int range(int val, int min, int max) {
    	int r = val;
    	if (r>max) r=max;
    	if (r<min) r=min;
    	return r;
    }
    
    int check_bounds(int val, int min, int max, int default_value) {
    	if (val>=min && val<=max) return val;
    	else return default_value;
    }
    
    protected void onPause() {
    	super.onPause();
    	mt.activityPaused=true;
    	
		SharedPreferences sPref=getPreferences(MODE_PRIVATE);
		Editor ed = sPref.edit();
		ed.putBoolean("showhistory", getVisibilityCommandsHistory());

		if (mt.l9!=null) {
			if (mt.lib.getGamePath()!=null) {
				ed.putString("lastgame", mt.l9.LastGame);
				mt.autosaveGame();
			} else {
				ed.remove("lastgame");
			}
		}
		ed.commit();
    }
    
    protected void onDestroy() {
		super.onDestroy();
		mt.activityPaused=true;
	
		if (killThreadsOnDestroyActivity && mt.l9!=null) {
			mt.destroy();
		}

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
        /*
    	mi = menu.add(1, MENU_ITEM_PLAY_SCRIPT, 0,"Play Script");
    	mi.setOnMenuItemClickListener(this);
    	*/
        mi = menu.add(0, MENU_ITEM_HOWTOPLAY, 3, "How To Play?");
    	mi.setOnMenuItemClickListener(this);
    	mi = menu.add(0, MENU_ITEM_HISTORY, 3, "Commands History");
    	mi.setOnMenuItemClickListener(this);
    	mi = menu.add(0, MENU_ITEM_CLOSEAPP, 3, "Exit");
    	mi.setOnMenuItemClickListener(this);
    	
        return super.onCreateOptionsMenu(menu);
    }

	//@Override
	public boolean onMenuItemClick(MenuItem arg0) {
		Intent intent;
		switch (arg0.getItemId()) {
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
		case MENU_ITEM_HOWTOPLAY:
			intent=new Intent(this, LibraryGameInfoActivity.class);
			intent.putExtra("selectedgame", "info_how2play");
			startActivity(intent);
			break;
		case MENU_ITEM_CLOSEAPP:
			finish();
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
		if (mt.lib.getGamePath()==null) needToExitApp=true;
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
				mt.keyPressed=' ';
				return true;
			}
			else if (keyCode==KeyEvent.KEYCODE_ENTER) {				
				mt.keyPressed='\r';
				return true;
			}
		};
		return super.onKeyDown(keyCode, event);
	}
	
	void outCharToLog(char c) {
		if (mt.logStringCapacitor==null) mt.logStringCapacitor=new SpannableStringBuilder();
		if (c=='\n') outLogFlush(true);
		else mt.logStringCapacitor.append(c);
	};
	
	void outUserInputToLog(String str) {
		SpannableStringBuilder text = new SpannableStringBuilder(str);
        ForegroundColorSpan style = new ForegroundColorSpan(pref_logcommandcolor); 
        text.setSpan(style, 0, text.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        if (mt.logStringCapacitor==null) mt.logStringCapacitor=new SpannableStringBuilder();
        mt.logStringCapacitor.append(text);
        outLogFlush(true);
	};
	
	void outLogFlush(boolean finishThisString) {
		if (mt.logStringCapacitor!=null && mt.logStringCapacitor.length()>0) {
			boolean empty=true;
			for (int i=0; i<mt.logStringCapacitor.length();i++) {
				if (mt.logStringCapacitor.charAt(i)>32) {empty=false; break;};
			};
			if (empty && finishThisString) {
				mt.logStringCapacitor=null;
			} else {
				if ((mt.logStrId>=0) && (mt.logStrId<lvMain.getAdapter().getCount())) {
					mt.lvAdapter.getItem(mt.logStrId).append(mt.logStringCapacitor);
				} else {
					mt.lvAdapter.add(mt.logStringCapacitor);
					limitlvAdapter();
				}
				mt.logStringCapacitor=null;
				if (!finishThisString) mt.logStrId=lvMain.getAdapter().getCount()-1;
				mt.lvAdapter.notifyDataSetChanged();
			};
		};
		if (finishThisString) mt.logStrId=-1;
	}
	
	void limitlvAdapter() {
		if (pref_loglimit>0) {
			while (mt.lvAdapter.getCount()>pref_loglimit) {
				mt.lvAdapter.remove(mt.lvAdapter.getItem(0));
			};
			mt.lvAdapter.notifyDataSetChanged();
		}
	}
	
	void postCommand() {
		if (etCmd.length()>0 && mt.l9.L9State==L9.L9StateWaitForCommand) {
			outUserInputToLog(etCmd.getText().toString());
    
			mt.history.add(etCmd.getText().toString());
			mt.lvHistoryAdapter.notifyDataSetChanged();
			
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

