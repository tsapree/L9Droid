package com.realife.l9droid;

import java.util.concurrent.TimeUnit;

import android.graphics.Bitmap;
import android.os.Handler;

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
	public final static int MACT_LOADFILE=9;
	
	MainActivity activity;
	Library lib;
    Handler h;
    Thread t,g;
    
    boolean needToQuit=false;
    
    Bitmap bm=null;
    L9implement l9;
    byte gamedata[];
    
    boolean saveload_flag=false;
    static boolean gfx_ready=false;

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
		    		activity.bCmd.setText("...");
		    		activity.bCmd.setEnabled(false);
		    		break;
		    	case MACT_L9WAITFORCOMMAND:
		    		activity.bCmd.setText("Do");
		    		activity.bCmd.setEnabled(true);
		    		break;
		    	case MACT_L9WAITBEFORESCRIPT:
		    		activity.bCmd.setText("<!>");
		    		activity.bCmd.setEnabled(false);
		    		break;
	    		case MACT_PRINTCHAR:
	    			char c=(char)msg.arg1;
	    			if (c==0x0d) activity.etLog.append("\n");
	    			else activity.etLog.append(String.valueOf(c));
	    			break;
	    		case MACT_SAVEGAMESTATE:
    				l9.saveok=lib.fileSave(l9.saveloadBuff);
    				l9.saveloaddone=true;
	    			break;
	    		case MACT_LOADGAMESTATE:
	    			l9.saveloadBuff=lib.fileLoad();
	    			l9.saveloaddone=true;
	    			break;
	    		case MACT_LOADFILE:
	    			l9.saveloadBuff=lib.fileLoadGame(l9.saveloadFileName);
	    			l9.saveloaddone=true;
	    			break;
	    		case MACT_GFXOFF:
	    			bm=null;
	    			activity.ivScreen.setImageBitmap(bm);
	    			break;
	    		case MACT_GFXON:
	    			activity.ivScreen.setImageBitmap(bm);
	    			break;
	    		case MACT_GFXUPDATE:
	    			if (bm!=l9.bm) {
	    				bm=l9.bm;
	    				activity.ivScreen.setImageBitmap(bm);
	    			}
	    			activity.ivScreen.invalidate();
	    				
	    			break;
		    	}
		    };
		};
		h.sendEmptyMessage(MACT_L9WORKING);
		
//        gamedata=new byte[49179];	        
//		try {
//			//InputStream is=getResources().openRawResource(R.raw.timev2);
//			InputStream is=activity.getResources().openRawResource(R.raw.wormv3);
//			is.read(gamedata);            
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

	};
	
	void startGame(String path) {

		destroy();

		//gamedata=lib.fileLoadGame(path);
        l9=new L9implement(lib,h);
        String picturefilename=l9.findPictureFile(path);
        if (l9.LoadGame(path, picturefilename)!=true) {
        	l9=null;
        	return;
        }
        
		gfx_ready=false;
		g = new Thread(new Runnable() {
			public void run() {
				h.sendEmptyMessage(MACT_GFXOFF);
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
		        while (l9.L9State!=l9.L9StateStopped && needToQuit!=true) {
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
		        		//TODO: лучше так не менять значение переменных, заменить
		        		// на l9.InputCommand("") либо создать отдельный метод.
		        		//l9.L9State=l9.L9StateCommandReady;
		        		//h.sendEmptyMessage(MACT_L9WORKING);
		        		l9.InputCommand("");
		        	} else l9.step();
		        };
			}
		});
		t.start();
	}
	
	void destroy() {
		needToQuit=true;
		//TODO: почистить очередь?
		if (l9!=null) l9.StopGame();
		if (g!=null) while (g.isAlive());
		if (t!=null) while (t.isAlive());
		t=null;
		g=null;
		l9=null;
		needToQuit=false;
	};
	
}

