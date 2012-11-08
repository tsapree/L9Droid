package com.realife.l9droid;

import java.util.concurrent.TimeUnit;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.EditText;

public class L9implement extends L9 {
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
	
	//Gfx
	byte PicBuff[]=null;
    int PicColorBuff[]=null;
    Bitmap bm=null;
    
	int PicWidth=0;
	int PicHeight=0;
	int PicMode;
	int iPicturesSpeed=10;
	
	int iPicturesPalette=0;
	
	int PaletteAmiga[]={
			0xff000000,
			0xffff0000,
			0xff30e830,
			0xffffff00,
			0xff0000ff,
			0xffa06800,
			0xff00ffff,
			0xffffffff
	};

	//these colors taken from unreal speccy emulator, default palette
	//on speccy 8 colors plus 2 grade of bright.
	//in l9 games bright often set to 0 on pictures.

	int PaletteSpectrum[]={
			0xff000000,		//(black)  0
			0xffC00000,		//(red)    2
			0xff00C000,		//(green)  4
			0xffC0C000,		//(yellow) 6
			0xff0000C0,		//(blue)   1
			0xffC000C0,		//(brown)  3
			0xff00C0C0,		//(cyan)   5
			0xffC0C0C0		//(white)  7
	};

	int L9PaletteIndexes[]={0,0,0,0};
	int SelectedPalette[];

	
	//размер буфера заливки, в словах.
	final static int L9Fill_StackSize=512;
	int L9_FillStack[];
	//текущее заполнение буфера заливки
	int L9_FillCount;
	//текущие цвета для заливки.
	int L9_FillColour1;
	int L9_FillColour2;
	
	L9implement(/*EditText et1,*/ byte dat[], Handler h) {
		//et=et1;
		gamedata=dat;
		cmdStr=null;
		ds=new DebugStorage();
		mHandler=h;
		
		L9_FillStack=new int[L9Fill_StackSize];
		SelectedPalette=new int[32];
	};
	
	void os_printchar(char c) {
		if (c==0x0d) log_debug(ds.getstr());
		else if (ds.putchar(c)) log_debug(ds.getstr());
		msg = mHandler.obtainMessage(Threads.MACT_PRINTCHAR, c, 0);
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
		mHandler.sendEmptyMessage(Threads.MACT_SAVEGAMESTATE);
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
		mHandler.sendEmptyMessage(Threads.MACT_LOADGAMESTATE);
		while (saveloaddone==false) {
			try {
				TimeUnit.MILLISECONDS.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			};
		};
		return saveloadBuff; //TODO:mAct.fileLoad();
	};

	void os_graphics(int mode) {
		int pw[]={0};
		int ph[]={0};
		if (mode==2) {
			/*TODO:L9BitmapType = DetectBitmaps(L9Dir);
			if (L9BitmapType==NO_BITMAPS)*/ mode=0;
		};
		PicMode = mode;
		if (mode==0) mHandler.sendEmptyMessage(Threads.MACT_GFXOFF);
		else mHandler.sendEmptyMessage(Threads.MACT_GFXON);
		GetPictureSize(pw,ph);
		PicWidth=pw[0];
		PicHeight=ph[0];
		if (PicWidth<=0 || PicHeight<=0 || mode==0) return;
		PicBuff=new byte[PicWidth*PicHeight];
		PicColorBuff=new int[PicWidth*PicHeight];
		if (bm==null || bm.getHeight()!=PicHeight || bm.getWidth()!=PicWidth) {
			bm=Bitmap.createBitmap(PicWidth, PicHeight, Bitmap.Config.ARGB_8888);
			PicColorBuff=new int[PicWidth*PicHeight];
		};
		MainActivity.mt.gfx_ready=true;
	};
	
	void os_cleargraphics() {
		if (PicMode==0 || PicMode==2 || PicBuff==null /*|| iApV->iPicturesEnabled==EFalse*/) return;
		//memclr(PicBuff,PicHeight*PicWidth);
		for (int i=0;i<PicHeight*PicWidth;i++) PicBuff[i]=0;
		L9_FillCount=0; //отменить закраску, если она выполнялась.
		//TODO:iApV->DrawDeferred();
		
	};

	void L9UpdatePalette() {
		for (int i=0;i<4;i++) {
			if (iPicturesPalette==0)
				 SelectedPalette[i]=PaletteSpectrum[L9PaletteIndexes[i]];
			else SelectedPalette[i]=PaletteAmiga[L9PaletteIndexes[i]];
		};
	};
	
	void os_setcolour(int colour, int index) {
		if (PicMode==0 || PicMode==2) return;
		if ((index>7) || (index<0) || (colour >3) || (colour<0)) return;
		L9PaletteIndexes[colour]=index;
		L9UpdatePalette();
		//TODO:iApV->DrawDeferred();
	};
	
	void L9Plot(int x, int y, int colour1, int colour2) {
		if (x<0 || x>=PicWidth || y<0 || y>=PicHeight) return; 
		if (PicBuff[y*PicWidth+x]==colour2) PicBuff[y*PicWidth+x]=(byte)colour1; 
	};

	//returns 255 if not applicable
	byte L9Point(int x, int y) {
		if (x<0 || x>=PicWidth || y<0 || y>=PicHeight) return (byte)0xff;
		return PicBuff[y*PicWidth+x];
	};

	
	/*
	 * Алгоритм Брезенхэма для рисования линии
	 * function line(x0, x1, y0, y1)
	     int deltax := abs(x1 - x0)
	     int deltay := abs(y1 - y0)
	     int error := 0
	     int y := y0
	     for x from x0 to x1
	         plot(x,y)
	         error := error + deltay
	         if 2 * error >= deltax
	             y := y + 1
	             error := error - deltax
	 * 
	 */
	
	//
	//y=k*x+b
	//k=(y2-y1)/(x2-x1)
	//b=y-k*x
	//

	void os_drawline(int x1, int y1, int x2, int y2, int colour1, int colour2)
	{
		if (PicMode==0 || PicMode==2) return;

		int x0;
		int y0;
		int sx = Math.abs(x2-x1);
		int sy = Math.abs(y2-y1);
		int zx = ((x2>x1)?1:(-1));
		int zy = ((y2>y1)?1:(-1));
		int err=0;
		
		if (sx>=sy) {
			y0=y1;
			for (x0=x1; x0!=x2; x0+=zx) {
				L9Plot(x0, y0, colour1, colour2);
				err+=sy;
				if (2*err >= sx) {
					y0+=zy;
					err-=sx;
				};
			};
			//исправление глюка с неотображением последнего пискеля.
			L9Plot(x2, y2, colour1, colour2);
		} else if (sy>sx) {
			x0=x1;
			for (y0=y1; y0!=y2; y0+=zy) {
				L9Plot(x0, y0, colour1, colour2);
				err+=sx;
				if (2*err >= sy) {
					x0+=zx;
					err-=sy;
				};
			};
			//исправление глюка с неотображением последнего пискеля.
			L9Plot(x2, y2, colour1, colour2);
		};
	};
	
	void os_fill(int x, int y, int colour1, int colour2) {
		L9Fill_Start (x, y, colour1, colour2);
	};
	
	void L9Fill_Start (int x, int y, int colour1, int colour2) {
		L9_FillCount=0;
		L9_FillColour1=colour1;
		L9_FillColour2=colour2;
		if (PicMode==0 || PicMode==2) return;
		if (x<0 || x>PicWidth || y<0 || y> PicHeight) return; 
		if (L9_FillCount<L9Fill_StackSize-2) {
			L9_FillStack[L9_FillCount++]=x;
			L9_FillStack[L9_FillCount++]=y;
		};
	};

	int L9Fill_Step() {
		int x;
		int y;
		if (L9_FillCount>0) {
			y=L9_FillStack[--L9_FillCount];
			x=L9_FillStack[--L9_FillCount];
			
			//if (L9Point(x,y) != colour2) return;
			//нахожу первую закрашиваемую точку в этой линии
			boolean FillingUp   = false; //признак того, что предыдущую точку уже заполняю
			boolean FillingDown = false;
			while ((x>0)        && (L9Point(x-1,y)==L9_FillColour2)) x--;
			//для каждой точки линии - перекрашиваю, и оцениваю соседей сверху и снизу на закрашиваемость.
			while ((x<PicWidth) && (L9Point(x,y)==L9_FillColour2)) {
				L9Plot(x, y, L9_FillColour1, L9_FillColour2);
				if (y-1>=0) {
					if (L9Point(x,y-1)==L9_FillColour2) {
						if (!FillingUp) {
							//os_fill(x,y-1,colour1,colour2);
							if (L9_FillCount<L9Fill_StackSize-2) {
								L9_FillStack[L9_FillCount++]=x;
								L9_FillStack[L9_FillCount++]=y-1;
							};
							FillingUp = true;
						};
					} else FillingUp=false;
				};
				if (y+1<PicHeight) {
					if (L9Point(x,y+1)==L9_FillColour2) {
						if (!FillingDown) {
							//os_fill(x,y+1,colour1,colour2);
							if (L9_FillCount<L9Fill_StackSize-2) {
								L9_FillStack[L9_FillCount++]=x;
								L9_FillStack[L9_FillCount++]=y+1;
							};
							FillingDown = true;
						};
					} else FillingDown=false;
				};
				x++;
			};
		};
		return L9_FillCount;
	};
	
	boolean L9DoPeriodGfxTask() {
		if (PicMode==0 || PicMode==2 /*|| iApV->iPicturesEnabled==EFalse*/) return false;

		//Красивая прорисовка Fill. 
		int j=0;
		for (int i=0; i<iPicturesSpeed; i++)
			if (L9Fill_Step()>0) j++;
			else if (RunGraphics()) j++;	//если встретился fill - нельзя выполнять другие операции

		//draw to bitmap
		if (PicBuff!=null && PicColorBuff!=null & bm!=null) {
			int s=PicWidth*PicHeight;
			for (int i=0;i<s;i++)
				PicColorBuff[i]=SelectedPalette[PicBuff[i]];
			bm.setPixels(PicColorBuff, 0, PicWidth, 0, 0, PicWidth, PicHeight);
		};
		
		return j!=0;
	};
}
