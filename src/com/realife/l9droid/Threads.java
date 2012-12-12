package com.realife.l9droid;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class Threads {
	
	public final static int MACT_L9WORKING = 0;
	public final static int MACT_L9WAITFORCOMMAND = 1;
	public final static int MACT_PRINTCHAR = 2;
	public final static int MACT_SAVEGAMESTATE = 3;
	public final static int MACT_LOADGAMESTATE=4;
	public final static int MACT_GFXON=5;
	public final static int MACT_GFXOFF=6;
	public final static int MACT_GFXUPDATE=7;
	public final static int MACT_L9WAITBEFORESCRIPT=8;
	public final static int MACT_TOAST=9;
	public final static int MACT_L9WAITFORCHAR=10;
	public final static int MACT_FLUSH=11;
	
	MainActivity activity;
	Library lib;
    Handler h;
    Thread t,g;
    
    boolean needToQuit=false;
    boolean activityPaused=false;
    //boolean menuPicturesFound=false;
    boolean menuPicturesEnabled=false;
    boolean menuHashEnabled=false;
    
    Bitmap bm=null;
    L9implement l9;
    byte gamedata[];
    
    boolean saveload_flag=false;
    static boolean gfx_ready=false;
    
	ArrayAdapter<SpannableStringBuilder> lvAdapter;
	SpannableStringBuilder logStringCapacitor=null;
	int logStrId=-1;

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
        // присваиваем адаптер списку

		
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
		    		menuHashEnabled=true;
		    		activity.bCmd.setText("Do");
		    		activity.bCmd.setEnabled(true);
		    		break;
		    	case MACT_L9WAITFORCHAR:
		    		//menuHashEnabled=true;
		    		activity.bCmd.setText("*");
		    		activity.bCmd.setEnabled(true);
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
	    		case MACT_SAVEGAMESTATE:
    				l9.saveok=lib.fileSave(l9.saveloadBuff);
    				l9.saveloaddone=true;
	    			break;
	    		case MACT_LOADGAMESTATE:
	    			l9.saveloadBuff=lib.fileLoad();
	    			l9.saveloaddone=true;
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
	
	void startGame(String gamepath, boolean loadAutoSave) {
		
		destroy(false);

		l9=new L9implement(lib,h);
        lib.setPath(gamepath);
        String picturefilename=l9.findPictureFile(gamepath);
        if (l9.LoadGame(gamepath, picturefilename)!=true) {
        	l9=null;
        	return;
        }
        if (loadAutoSave) {
        	l9.restore_autosave(lib.getAbsolutePath("Saves/auto.sav"));
        	ArrayList<String> log=l9.restore_autosave_log(lib.getAbsolutePath("Saves/auto.log"));
        	lvAdapter.clear();
        	if (log!=null) 
        		for (int i=0; i<log.size();i++)
        			lvAdapter.add(unwrapSpans(log.get(i)));
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
		needToQuit=true;
		//TODO: почистить очередь?
		if (l9!=null && needAutoSave) {
			String name=lib.getAbsolutePath("Saves/auto.sav");
			if (l9.L9State!=l9.L9StateStopped) {
				//SpannableStringBuilder()
	        	ArrayList<String> log=new ArrayList<String>();
	        	if (lvAdapter!=null) 
	        		for (int i=0; i<lvAdapter.getCount();i++) {
	        			log.add((wrapSpans(lvAdapter.getItem(i))));
	        		};
				
				l9.autosave(name,log/*activity.etLog.getText().toString()*/);
			};
			l9.StopGame();
		}
		if (g!=null) while (g.isAlive());
		if (t!=null) while (t.isAlive());
		t=null;
		g=null;
		l9=null;
		needToQuit=false;
	};
	
	//завернуть spans в тэги {}
	private String wrapSpans(SpannableStringBuilder spannedString) {
		//ForegroundColorSpan a[]=spannedString.getSpans(0, 1, ForegroundColorSpan.class);
		String result=new String();
		int size=spannedString.length();
		int i=spannedString.getSpans(0, size, ForegroundColorSpan.class).length;
		if (i>0) {
			//TODO: пока только одна подсвеченная команда в строке будет заключена в фигурные скобки 
			//TODO: раскомментировать .replace("{", "{/").replace("}", "}}") для маскировки этих символов
			int begin=0;
			int beginSpan=-1;
			int endSpan=0;
			beginSpan=spannedString.nextSpanTransition(begin, size-1, ForegroundColorSpan.class);
			if (beginSpan>=0) {
				endSpan=spannedString.nextSpanTransition(beginSpan, size-1, ForegroundColorSpan.class);
				result+=spannedString.subSequence(begin, beginSpan).toString()/*.replace("{", "{{").replace("}", "}}")*/
						+"{"
						+spannedString.subSequence(beginSpan, endSpan+1).toString()/*.replace("{", "{{").replace("}", "}}")*/
						+"}";
				begin=endSpan+1;
			};
			if (begin<size) result+=spannedString.subSequence(begin, size-1).toString();
		} else result=spannedString.toString()/*.replace("{", "{{").replace("}", "}}")*/;
		return result;//spannedString.toString();
	}
	
	//развернуть spans из тэгов {}
	private SpannableStringBuilder unwrapSpans(String wrappedString) {
		
		int size=wrappedString.length();
		int i=wrappedString.indexOf('{', 0);
		int j=wrappedString.indexOf('}', 0);
		if (i>=0 && j>i) {
			SpannableStringBuilder text = new SpannableStringBuilder(wrappedString.subSequence(0, i).toString()
					+wrappedString.subSequence(i+1, j).toString()
					+wrappedString.subSequence(j+1, size).toString());
	        ForegroundColorSpan style = new ForegroundColorSpan(Color.rgb(0, 0, 255)); 
	        text.setSpan(style, i, j-1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
	        return text; 
		} else return new SpannableStringBuilder(wrappedString);
	}
	
}

