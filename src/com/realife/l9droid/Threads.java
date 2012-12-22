package com.realife.l9droid;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import android.graphics.Bitmap;
import android.os.Handler;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class Threads {
	
	public final static int MACT_L9WORKING = 0;
	public final static int MACT_L9WAITFORCOMMAND = 1;
	public final static int MACT_PRINTCHAR = 2;
	public final static int MACT_GFXON=5;
	public final static int MACT_GFXOFF=6;
	public final static int MACT_GFXUPDATE=7;
	public final static int MACT_L9WAITBEFORESCRIPT=8;
	public final static int MACT_TOAST=9;
	public final static int MACT_L9WAITFORCHAR=10;
	public final static int MACT_FLUSH=11;
	public final static int MACT_REPLACE_LOG=12;
	
	MainActivity activity;
	Library lib;
    Handler h;
    Thread t,g;
    
    boolean needToQuit=false;
    boolean activityPaused=false;
    boolean menuPicturesEnabled=false;
    boolean menuHashEnabled=false;
    
    Bitmap bm=null;
    L9implement l9;
    byte gamedata[];
    
    static boolean gfx_ready=false;
    
	ArrayAdapter<SpannableStringBuilder> lvAdapter;
	SpannableStringBuilder logStringCapacitor=null;
	int logStrId=-1;
	
	History history;
	ArrayAdapter<String> lvHistoryAdapter;
	
	char keyPressed=0;

    void link(MainActivity m) {
    	activity=m;
    	activity.ivScreen.setImageBitmap(bm);
    }
    
    void unlink() {
    	activity=null;
    }
    
	void create() {
		lib=new Library();
	    lib.prepareLibrary(activity);
	    
		lvAdapter = new ArrayAdapter<SpannableStringBuilder>(activity, R.layout.log_list_item, new ArrayList<SpannableStringBuilder>());
		history=new History();
		lvHistoryAdapter = new ArrayAdapter<String>(activity, R.layout.history_list_item, history.getHistory());

		//lvHistoryAdapter.add("unfas para");
		
		needToQuit=false;
		h = new Handler() {
		    public void handleMessage(android.os.Message msg) {
		    	try {
		    		//сведение вероятности падения при повороте экрана к минимуму
					while (activity==null) 
						TimeUnit.MILLISECONDS.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				};
		    	switch (msg.what) {
		    	case MACT_L9WORKING:
		    		menuHashEnabled=false;
		    		activity.bCmd.setText("...");
		    		activity.bCmd.setEnabled(false);
		    		break;
		    	case MACT_L9WAITFORCOMMAND:
		    		activity.outLogFlush(false);
		    		activity.etCmd.setFocusable(true);
		    		activity.etCmd.requestFocus();
		    		menuHashEnabled=true;
		    		activity.bCmd.setText("Do");
		    		activity.bCmd.setEnabled(true);
		    		activity.bSpace.setVisibility(View.INVISIBLE);
		    		activity.bEnter.setVisibility(View.INVISIBLE);
		    		activity.bCmd.setVisibility(View.VISIBLE);
		    		activity.etCmd.setVisibility(View.VISIBLE);
		    		activity.etCmd.requestFocus();
		    		break;
		    	case MACT_L9WAITFORCHAR:
		    		activity.bSpace.setVisibility(View.VISIBLE);
		    		activity.bEnter.setVisibility(View.VISIBLE);
		    		activity.bCmd.setVisibility(View.INVISIBLE);
		    		activity.etCmd.setVisibility(View.INVISIBLE);
		    		if (!activity.bEnter.isFocused())
		    			activity.bSpace.requestFocusFromTouch();
		    		keyPressed=0;
		    		break;	
		    	case MACT_L9WAITBEFORESCRIPT:
		    		activity.bCmd.setText("<!>");
		    		activity.bCmd.setEnabled(false);
		    		break;
	    		case MACT_PRINTCHAR:
	    			char c=(char)msg.arg1;
	    			if (c==0x0d) activity.outCharToLog('\n');
	    			else activity.outCharToLog(c);
	    			break;
	    		case MACT_FLUSH:
	    			activity.outLogFlush(false);
	    			break;
	    		case MACT_REPLACE_LOG:
	    			logStringCapacitor=null;
	    			lvAdapter.clear();
	    			if (l9.tempLog!=null) {
		    			for (int i=0;i<l9.tempLog.size();i++) lvAdapter.add(l9.tempLog.get(i));
	    			};
	    			break;
	    		case MACT_GFXOFF:
		    		menuPicturesEnabled=false;
	    			bm=null;
	    			activity.ivScreen.setImageBitmap(bm);
	    			break;
	    		case MACT_GFXON:
	    			menuPicturesEnabled=true;
	    			activity.ivScreen.setImageBitmap(bm);
	    			break;
	    		case MACT_GFXUPDATE:
	    			
	    			if ((l9!=null) && (bm!=l9.bm)) {
	    				bm=l9.bm;
	    				activity.ivScreen.setImageBitmap(bm);
	    			}
	    			activity.ivScreen.invalidate();
	    				
	    			break;
		    	case MACT_TOAST:
		    		Toast.makeText(activity, (String)msg.obj, Toast.LENGTH_LONG).show();
		    		break;
		    	}
		    };
		};
		h.sendEmptyMessage(MACT_L9WORKING);
		lib.h=h;
	};
	
	void stopGame() {
		destroy(false);
	};
	
	void startGame(String gamepath, boolean loadAutoSave) {
		
		destroy(false);

		l9=new L9implement(lib,h,this);
        lib.setPath(gamepath);
        String picturefilename=l9.findPictureFile(gamepath);
        if (l9.LoadGame(gamepath, picturefilename)!=true) {
        	l9=null;
        	return;
        }
        if (loadAutoSave) {
        	l9.restore_autosave(lib.getAbsolutePath("Saves/auto.sav"));
        } else
        	h.sendEmptyMessage(MACT_GFXOFF); //убираю картинку от прошлой игры
        
		gfx_ready=false;
		
		g = new Thread(new Runnable() {
			public void run() {
				while(needToQuit!=true) {
					//Log.d("l9droid", "thread g still working");
					try {
						if (gfx_ready) {
							if ((l9!=null) && (l9.L9DoPeriodGfxTask())) {
								h.removeMessages(MACT_GFXUPDATE);
								h.sendEmptyMessage(MACT_GFXUPDATE);
								TimeUnit.MILLISECONDS.sleep(50);
							}
							else TimeUnit.MILLISECONDS.sleep(500);
						} else TimeUnit.MILLISECONDS.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					};
				}
			}
		});
		g.start();

		t = new Thread(new Runnable() {
			public void run() {
		        while ((l9.L9State!=l9.L9StateStopped) && (needToQuit!=true)) {
		        	if (l9.L9State==l9.L9StateWaitForCommand) {
		        		h.sendEmptyMessage(MACT_L9WAITFORCOMMAND);
		        		//TODO: проверить try-catch на грамотность, не нужно ли все заключить в них, что произойдет, если наступит exception?
						try {
							while ((activity==null || activity.command==null) && needToQuit!=true ) {
					        	//Log.d("l9droid", "thread t still working");
								TimeUnit.MILLISECONDS.sleep(200);
							};
							h.sendEmptyMessage(MACT_L9WORKING);
							//TODO: t.wait - возможно, более правильное решение.
							//TODO: возможна потеря activity при повороте экрана
							l9.InputCommand(activity.command);
							activity.command=null;
						} catch (InterruptedException e) {
							e.printStackTrace();
						};
		        	} else if (l9.L9State==l9.L9StateWaitBeforeScriptCommand) {
		        		h.sendEmptyMessage(MACT_L9WAITBEFORESCRIPT);
		        		try {
							TimeUnit.MILLISECONDS.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		        		l9.InputCommand("");
		        	} else if (!activityPaused) l9.step();
		        };
			}
		});
		t.start();
	}
	
	void destroy(boolean needAutoSave) {
		//TODO: почистить очередь?
		if (l9!=null && needAutoSave) {
			if (l9.L9State!=l9.L9StateStopped) {
				String name=lib.getAbsolutePath("Saves/auto.sav");
				l9.autosave(name);
			};
			l9.StopGame();
		};
		needToQuit=true;
		if (g!=null) while (g.isAlive());
		if (t!=null) while (t.isAlive());
		t=null;
		g=null;
		l9=null;
		needToQuit=false;
	};
	
}

