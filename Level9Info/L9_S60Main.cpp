//

#include "L9_S60Application.h"
#include "L9_S60AppUi.h"
#include "L9_S60AppView.h"
#include "L9_S60Main.h"

#include "level9.h"

#include <string.h>

#include <e32base.h>
#include <e32std.h>
#include <e32cons.h>			// Console

//0 - default
//1 - waiting for command from user
//2 - command entered, need to put it into parser
//TODO: поменять на enum
int inputSelected;

#define L9MaxPath MAX_PATH
//буфер для хранения директории игры.
char L9Dir[L9MaxPath];
//буфер для хранения имени игры.
char L9FileName[L9MaxPath];

//буфер для хранения имени файла с картинкой.
char L9PicFile[L9MaxPath];

//буфер для имени файла отгрузки
char savFileName[L9MaxPath];

BitmapType L9BitmapType;

//длина буфера вывода текста.
#define OUTPUT_BUFFER_LENGHT 32
//буфер для вывода текста.
char outputBuff[OUTPUT_BUFFER_LENGHT+1];
//количество символов в буфере.
int charsInBuffer;

//таймер ожидания ввода символа, в миллисекундах/20 (т.к. частота моего таймера 50hz)
int charTimer;

//gfx vars:
int PicWidth;
int PicHeight;
unsigned char* PicBuff;
//int PicPalette[4];
#define L9PictureUpdateTempo 10


TUint32 PaletteAmiga[8]={
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

TUint32 PaletteSpectrum[8]={
		0xff000000,		//(black)  0
		0xffC00000,		//(red)    2
		0xff00C000,		//(green)  4
		0xffC0C000,		//(yellow) 6
		0xff0000C0,		//(blue)   1
		0xffC000C0,		//(brown)  3
		0xff00C0C0,		//(cyan)   5
		0xffC0C0C0		//(white)  7
};

int L9PaletteIndexes[4];

CL9_S60AppView* iApV;

//размер буфера заливки, в словах.
#define L9Fill_StackSize 512
int L9_FillStack[L9Fill_StackSize];
//текущее заполнение буфера заливки
int L9_FillCount;
//текущие цвета для заливки.
int L9_FillColour1;
int L9_FillColour2;

void L9MainInit(CL9_S60AppView* iAV) {
	//by default, set only text mode available
	iApV=iAV;
	inputSelected=NoInputAllowed;
	charsInBuffer=0;
	
	PicWidth=0;
	PicHeight=0;
	PicBuff=NULL;
	
	for (int i=0; i<4; i++) {
		iApV->SelectedPalette[i]=0x000000;
		L9PaletteIndexes[i]=i;
	};
	
	L9Dir[0]=0; //директория игры пока пуста.

};

void L9MainShut(void) {
	inputSelected=NoInputAllowed;
	delete[] PicBuff; PicBuff = NULL;
	FreeMemory();
};

void L9DoPeriodTask(void) {

	if (charTimer>0) {
		charTimer--;
		if (charTimer==0) {
			inputSelected = CharEditorTick;
			iApV->DoEnterCommand();
		};
	};

	if (iApV->PicMode==0 || iApV->PicMode==2 || iApV->iPicturesEnabled==EFalse) return;
	
	//Красивая прорисовка Fill. 
		int j=0;
		for (int i=0; i<iApV->iPicturesSpeed; i++)
			if (L9Fill_Step()>0) j++;
			else if (RunGraphics()) j++;	//если встретился fill - нельзя выполнять другие операции
		if (j!=0) {
			iApV->DrawDeferred();
			return;
		};
	
};

bool L9LoopUntilCommand(int nStatus) {
	inputSelected = nStatus;
	//цикл повторяется пока парсер не дойдет до ввода текста... Пока - до статуса CommandEditorEnabled
	while ((inputSelected==NoInputAllowed) || (inputSelected==CommandAvailable) || (inputSelected==CharAvailable) || (inputSelected==CharUnAvailable)) {
		if (RunGame()==false) return false;
	};
	return true;	
};

int L9GetState(void) {
	return inputSelected;
};

void L9SetFileExt(char* filename, char* newext) {
	int len=strlen(filename);
	while (len>0) {
		len--;
		if (filename[len]=='.') {
			filename[len]=0;
			break;
		};
		if (filename[len]=='/' || filename[len]=='\\') break;
	};
	len=strlen(filename);
	if (len+5>L9MaxPath) return;
	strcat(filename,newext);
};

bool L9FileExist(const char* filename) {
	if (strlen(filename)<1) return FALSE;
	FILE *f=fopen(filename,"rb");
	if (f)
	{
		fclose(f);
		return TRUE;
	}
	return FALSE;
};

void L9PicFilename(const char* filename) {
//проверить имена: имя игры.pic, имя игры.cga, имя игры.hrc,
	strcpy(L9PicFile,filename);
	L9SetFileExt(L9PicFile,".pic");
	if (L9FileExist(L9PicFile)) return;
	L9SetFileExt(L9PicFile,".cga");
	if (L9FileExist(L9PicFile)) return;
	L9SetFileExt(L9PicFile,".hrc");
	if (L9FileExist(L9PicFile)) return;
	strcpy (L9PicFile,L9Dir);
	strcat(L9PicFile,"picture.dat");	
};

bool L9LoadGame(const char* filename) {
	//todo: попытка исправить проблему с остающимися картинками в играх, где нет команды на смену графического режима
	os_graphics(0); //iApV->PicMode=0;

	//обрезание полного пути к файлу игры до пути.
	//todo: небезопасно.
	strcpy(L9Dir,filename);
	int len=strlen(L9Dir);
	while (len>0) {
		len--;
		if (L9Dir[len]=='/' || L9Dir[len]=='\\') {
			strcpy(L9FileName,L9Dir+len+1);
			L9Dir[len+1]=0;
			break;
		};
	};
	
	L9PicFilename(filename);	
	if (LoadGame((char*)filename,L9PicFile)) {
	
		//скопировать путь к игре - в путь к отгрузкам.
		strcpy(savFileName,L9Dir);
		//todo: опасно! возможно переполнение!!!
		if ((strlen(savFileName)+6)<L9MaxPath) strcat(savFileName,"Saves\\");
		strcat(savFileName,L9FileName);
		L9SetFileExt(savFileName,".sav");
		
		//todo: теоретически возможен выход из игры на первом цикле... учесть.
		L9LoopUntilCommand(NoInputAllowed);
	} else {
		//неудачная загрузка
		FreeMemory();
		return false;
	};
	return true;
};

unsigned char* L9GetPicBuff(void) {
	return PicBuff;
};

//вернуть путь к игре.
char* L9GetGameDir(void) {
	return L9Dir;
};

/*
 * void os_printchar(char c)

	os_printchar() prints a character to the output. The interface
	can either buffer this character or print it immediately, but
	if buffering is used then the characters must all be sent to the
	output when the interpreter calls os_flush(). A paragraph of
	text is output as one long stream of characters, without line
	breaks, so the interface must provide its own word wrapping and
	any other features that are desired, such as justification or a
	[More] prompt. The carriage return character is always '\r',
	rather than '\n'.
 * 
 */
void os_printchar(char c) {
	//if (c == '\n') iApV->printChar('\r');
	//else iApV->printChar(c);
	if ( charsInBuffer ==OUTPUT_BUFFER_LENGHT) os_flush();
	if (c == 13) c='\f';
	//if (c == 10) c='\f';
	outputBuff[charsInBuffer++]=c;
};

/*
 * L9BOOL os_input(char* ibuff, int size)

	os_input() reads a line of text from the user, usually to accept
	the next command to be sent to the game. The text input must be
	stored in ibuff with a terminating zero, and be no longer than
	size characters. Normally os_input() should return TRUE, but may
	return FALSE to cause the entire input so far to be discarded.
	The reason for doing so is discussed in the section at the end
	on allowing the interpreter to load a new game without exiting.
 * 
 */
L9BOOL os_input(char* ibuff, int size) {
	if (inputSelected==CommandAvailable) {
		inputSelected=NoInputAllowed;
		iApV->GetCommand(ibuff,size);
		if (ibuff[0]==0) return false;
		return true;
	};
	inputSelected=CommandEditorEnabled;
	return false;
	};


/*
 * char os_readchar(L9UINT32 millis)

	os_readchar() looks to see if a key has been pressed if one has,
	returns the character to the interpreter immediately. If no key
	has been pressed the interpreter should wait for a key for at
	least the number of milliseconds given in the argument. If after
	this period no key has been pressed, 0 should be returned. This
	is most commonly used when a game is exited, causing it to print
	"Press SPACE to play again" and then call os_readchar().
 * 
 */

char os_readchar(int millis) {
//	static TInt vv=0;
//	vv+=1;
//	if (vv==2) {
//	vv=0; return 0;
//	} else return '1';
//#define CharEditorEnabled		3
//#define CharAvailable			4

	if (inputSelected==CharAvailable) {
		char c=iApV->GetCharCommand();
		charTimer=0;
		inputSelected=NoInputAllowed;
		return c;
	};
	
	if (inputSelected==CharUnAvailable) {
		charTimer=0;
		inputSelected=NoInputAllowed;
		return 0;
	};
	
	charTimer = millis/20;
	if (charTimer<1) charTimer=1;
	inputSelected=CharEditorEnabled;
	return 0;
}

/*
 * L9BOOL os_stoplist(void)

	Called during dictionary listing. If true is returned (typically
	because the user has pressed a key) then the listing is stopped.
	This routine should return immediately, without waiting. If this
	is not possible then FALSE should be returned.
 * 
 */
L9BOOL os_stoplist(void)
	{return false;};


/*
 * void os_flush(void)

	If the calls to os_printchar() are being buffered by the
	interface then the buffered text must be printed when os_flush()
	is called.
 * 
 */
//todo: передаю длину строки двумя путями. смысл??? от одного надо избавиться
void os_flush(void) {
	if (charsInBuffer>0) {
		outputBuff[charsInBuffer]=0;
		iApV->printString(outputBuff);
	}
	charsInBuffer=0;	
};

/*
 * L9BOOL os_save_file(L9BYTE* Ptr, int Bytes)

	os_save_file() should prompt the user in some way (with either
	text or a file requester) for a filename to save the area of
	memory of size Bytes pointed to by Ptr. TRUE or FALSE should be
	returned depending on whether or not the operation was successful.
 * 
 */
L9BOOL os_save_file(L9BYTE* Ptr, int Bytes) {
	if (iApV->ChooseFileToSaveGame(savFileName, L9MaxPath)!=true) {
		return false;
	};
	
	//TODO: Переделать работу с файлами под symbian-методы.
	FILE *f=fopen(savFileName,"wb");
	if (!f) return FALSE;
	//int FileSize=filelength(f);
	//if (FileSize>Max) FileSize=Max;
	//*Bytes = FileSize;
	if (fwrite(Ptr,1,Bytes,f)!=Bytes)
	{
		fclose(f);
		return FALSE;
	};
		fclose(f);
	return TRUE;
};


/*
 * L9BOOL os_load_file(L9BYTE* Ptr, int* Bytes, int Max)

	os_load_file() should prompt the user for the name of a file to
	load. At most Max bytes should be loaded into the memory pointed
	to by Ptr, and the number of bytes read should be placed into the
	variable pointed to by Bytes.
 * 
 */
L9BOOL os_load_file(L9BYTE* Ptr, int* Bytes, int Max) {
	if (iApV->ChooseFileToRestoreGame(savFileName, L9MaxPath)!=true) {
		return false;
	};
	
	//TODO: Переделать работу с файлами под symbian-методы.
	FILE *f=fopen(savFileName,"rb");
	if (!f) return FALSE;
	int FileSize=filelength(f);
	if (FileSize>Max) FileSize=Max;
	*Bytes = FileSize;
	if (fread(Ptr,1,FileSize,f)!=FileSize)
	{
		fclose(f);
		return FALSE;
	};
 	fclose(f);
	return TRUE;
};

/*
 * L9BOOL os_get_game_file(char* NewName, int Size)

	os_get_game_file() should prompt the user for a new game file, to
	be stored in NewName, which can take a maximum name of Size
	characters. When this function is called the NewName array
	contains the name of the currently loaded game, which can be used
	to derive a name to prompt the user with.

	This is used by at least the Adrian Mole games, which load in the
	next part of the game after the part currently being played has
	been completed. These games were originally written for tape-based
	systems where the call was simply "load the next game from the
	tape".
 * 
 */
L9BOOL os_get_game_file(char* NewName, int Size)
	{return false; };

/*
 * void os_set_filenumber(char* NewName, int Size, int n)

	os_set_filename() is for multi-part games originally written for
	disk-based systems, which used game filenames such as

		gamedat1.dat
		gamedat2.dat

	etc. The routine should take the full filename in NewName (of
	maximum size Size) and modify it to reflect the number n, e.g.
	os_set_filename("gamedat1.dat",2) should leave "gamedat2.dat"
	in NewName.
 * 
 */
//todo: ограничение на алгоритм - меняет последнюю цифру в пути... даже если она в расширении файла.
void os_set_filenumber(char* NewName, int Size, int n) {
	int len=strlen(NewName);
	if (len>Size) return;
	while (len>0) {
		len--;
		if (NewName[len]>='0' && NewName[len]<='9') {
			NewName[len]='0'+n;
			return;
		};
	};
};

void L9UpdateGfxSize() {
	if (iApV->PicSize.iHeight!=PicHeight || iApV->PicSize.iWidth!=PicWidth || iApV->iOffScreenBitmap==NULL || PicBuff == NULL) {
		iApV->PicSize.iHeight=PicHeight;
		iApV->PicSize.iWidth=PicWidth;
		iApV->CreateOffScreenBitmap();
		// обновление буфера
		delete[] PicBuff; PicBuff = NULL;
		if (PicWidth*PicHeight>0) PicBuff = new unsigned char[PicHeight*PicWidth];
	};
	iApV->SizeChanged(); //заменить на что-то более правильное, этот метод должен быть скрыт от меня. наверное.
};

/*  void os_graphics(int mode)

	Called when graphics are turned on or off, either by the game or
	by the user entering "graphics" or "text" as a command. If mode
	is 0 graphics should be turned off. If mode is 1 then line drawn
	graphics will follow, so graphics should be turned on. If mode is
	2 then bitmap graphics will follow, so graphics should be turned
	on, provided that appropriate bitmap graphics files are available
	(This can be determined by calling DetectBitmaps(), which is
	discussed below.). After an os_graphics(0) call all the other
	graphics functions should do nothing.

	Typically, if mode is not 0 the code will allocate some suitable
	bitmap for drawing graphics into. For line drawn graphics, to
	determine the size of the bitmap the code should call
	GetPictureSize(). The graphics routines should draw in a bitmap of
	the size returned by this function, and then scale the bitmap
	appropriately for display. If instead the graphics code tries to
	scale the co-ordinates passed to os_drawline() and os_fill() then
	problems occur with fill colours "leaking" into other areas of the
	picture. The values returned by GetPictureSize() will not change
	unless a new game is loaded.

	The graphics bitmap for line drawn graphics is always 4 colour.
	The 4 colours are chosen from a possible 8 by calls to
	os_setcolour (see below). Note that a call to os_setcolour() must
	affect the colour of pixels already drawn on the bitmap. For
	example, suppose a pixel in the bitmap is set to the first colour
	in the palette during drawing, which at that moment is red. If
	later the first colour in the palette is set to blue, at the end
	the pixel should be shown blue.

	In order to actually draw graphics, the input routines os_input()
	and os_readchar() should call RunGraphics(). This is discussed
	further below.
 */
void os_graphics(int mode) {
	if (mode==2) {
		L9BitmapType = DetectBitmaps(L9Dir);
		if (L9BitmapType==NO_BITMAPS) mode=0;
	};
	//todo: в случае, если режим меняется - сначала освободить память.
	iApV->PicMode = mode;
	GetPictureSize(&PicWidth,&PicHeight);
	L9UpdateGfxSize();
};


/*
 *  void os_cleargraphics(void)
	Clear the current graphics bitmap by filling the entire bitmap
	with colour 0.
*/
void os_cleargraphics(void) {
	if (iApV->PicMode==0 || iApV->PicMode==2 || PicBuff==NULL || iApV->iPicturesEnabled==EFalse) return;
	memclr(PicBuff,PicHeight*PicWidth);
	L9_FillCount=0; //отменить закраску, если она выполнялась.
	
	iApV->DrawDeferred();
	
};

void L9UpdatePalette(void) {
	for (int i=0;i<4;i++) {
		if (iApV->iPicturesPalette==0)
			 iApV->SelectedPalette[i]=PaletteSpectrum[L9PaletteIndexes[i]];
		else iApV->SelectedPalette[i]=PaletteAmiga[L9PaletteIndexes[i]];
	};
};



/* void os_setcolour(int colour, int index)

	Set the given colour in the graphics bitmap's palette to the
	colour at the given index in the interpreter's table of colours.

	The actual table of colours in the interpreters provided by
	Level 9 vary across different machines. An acceptable palette
	that matches reasonably closely to the Amiga releases is as
	follows (all colours are 8 bit R,G,B):

		0x00,0x00,0x00  (black)
		0xFF,0x00,0x00  (red)
		0x30,0xE8,0x30  (green)
		0xFF,0xFF,0x00  (yellow)
		0x00,0x00,0xFF  (blue)
		0xA0,0x68,0x00  (brown)
		0x00,0xFF,0xFF  (cyan)
		0xFF,0xFF,0xFF  (white)
 */
void os_setcolour(int colour, int index) {
	if (iApV->PicMode==0 || iApV->PicMode==2) return;
	if ((index>7) || (index<0) || (colour >3) || (colour<0)) return;
	//PicPalette[index]=colour;
	L9PaletteIndexes[colour]=index;
	L9UpdatePalette();
	//iApV->SelectedPalette[colour]=PaletteAmiga[index];
	iApV->DrawDeferred();
};

/*
 * void os_drawline(int x1, int y1, int x2, int y2, int colour1, int colour2)

	Draw a line on the graphics bitmap between (x1,y1) and (x2,y2).
	Note that either point may lie outside of the bitmap, and that it
	is the responsibility of the routine to clip to the appropriate
	co-ordinates.

	For each point on the line, if the colour at that point is equal
	to colour2 the pixel's colour should be changed to colour1, else
	it should not be modified.
 * 
 */
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

void L9Plot(int x, int y, int colour1, int colour2) {
	if (x<0 || x>=PicWidth || y<0 || y>=PicHeight) return; 
	if (PicBuff[y*PicWidth+x]==colour2) PicBuff[y*PicWidth+x]=colour1; 
};

//returns 255 if not applicable
unsigned char L9Point(int x, int y) {
	if (x<0 || x>=PicWidth || y<0 || y>=PicHeight) return 255;
	return PicBuff[y*PicWidth+x];
};

//
//y=k*x+b
//k=(y2-y1)/(x2-x1)
//b=y-k*x
//

void os_drawline(int x1, int y1, int x2, int y2, int colour1, int colour2)
{
	if (iApV->PicMode==0 || iApV->PicMode==2) return;

	int x0;
	int y0;
	int sx = Abs(x2-x1);
	int sy = Abs(y2-y1);
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


/*
 * void os_fill(int x, int y, int colour1, int colour2)

	If the pixel's colour at (x,y) is equal to colour2, fill the
	region containing (x,y) with colour1. The boundaries of the
	region are defined as those areas of the bitmap with a colour
	other than colour2.
 *	
 */

void os_fill(int x, int y, int colour1, int colour2) {
	L9Fill_Start (x, y, colour1, colour2);
};


void L9Fill_Start (int x, int y, int colour1, int colour2) {
	L9_FillCount=0;
	L9_FillColour1=colour1;
	L9_FillColour2=colour2;
	if (iApV->PicMode==0 || iApV->PicMode==2) return;
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
		TBool FillingUp   = false; //признак того, что предыдущую точку уже заполняю
		TBool FillingDown = false;
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

/*
 * void os_show_bitmap(int pic, int x, int y)

	Show the bitmap given by the number pic at the co-ordinates
	(x,y).

	Note that the game can request the same picture several times
	in a row: it is a good idea for ports to record the last picture
	number and check it against any new requests.

	The interpreter source code provides a decoder that understands
	most Level 9 bitmap formats. The decoder is accessed by calling
	DecodeBitmap(), which is discussed below.
 * 
 */
void os_show_bitmap(int pic, int x, int y) {
	static int lastpic=-1;
	if (iApV->PicMode==0 || iApV->PicMode==1 || iApV->iPicturesEnabled==EFalse) {
		lastpic=-1;
		return;
	};
	static Bitmap* pic_bitmap=0;
	if (pic!=lastpic || PicWidth==0 || PicHeight==0) {
		lastpic=pic;
		pic_bitmap=DecodeBitmap(L9Dir, L9BitmapType, pic, x, y);
		PicWidth=pic_bitmap->width;
		PicHeight=pic_bitmap->height;
		L9UpdateGfxSize();
	};
	
	if (pic_bitmap!=0) {
		int max_x=pic_bitmap->width;    //if (max_x>PicWidth)  max_x=PicWidth;
		int max_y=pic_bitmap->height;   //if (max_y>PicHeight) max_y=PicHeight;
		int max_c=pic_bitmap->npalette; if (max_c>32)        max_c=32;
		for (int c=0; c<max_c; c++)
			iApV->SelectedPalette[c] = (pic_bitmap->palette[c].red<<16)|(pic_bitmap->palette[c].green<<8)|(pic_bitmap->palette[c].blue);
		for (int j=0; j<max_y; j++)
			for (int i=0; i<max_x; i++)
				PicBuff[j*PicWidth+i]=pic_bitmap->bitmap[j*pic_bitmap->width+i];
	};
	iApV->DrawDeferred();
};


/*
 * 
The interpreter provides several functions to be called by the interface
code. These are:

L9BOOL LoadGame(char* filename, char* picname)

	LoadGame() attempts to load filename and then searches it for
	a valid Level 9 game. If it is successful TRUE is returned, else
	FALSE. The previous game in memory will be overwritten if the
	file filename can be loaded, even if it does not contain a Level 9
	game, so even if LoadGame() returns FALSE it must be assumed that
	the game memory has changed.

	The second argument is the name of the file containing picture
	data, and may be NULL. Ports should usually ask the user for just
	the filename and derive picname from it in some way. The
	recommended approach is to first try the filename with an extension
	of ".pic" and then try replacing the filename with "picture.dat".


L9BOOL RunGame(void)

	If LoadGame() has been successful, RunGame() can be called to run
	the Level 9 game. Each call to RunGame() executes a single opcode
	of the game. In pre-emptive multitasking systems or systems without
	any multitasking it is enough to sit in a loop calling RunGame(),
	e.g.
		while (RunGame());

	RunGame() returns TRUE if an opcode code was executed and FALSE if
	the game is stopped, either by an error or by a call to StopGame().


void StopGame(void)

	StopGame() stops the current game from playing.


void RestoreGame(char *inFile)

	RestoreGame() attempts to restore the currently running game to 
	the position stored in the inFile saved game file. This gives 
	interface code a means to restore a game position.


void FreeMemory(void)

	FreeMemory() frees memory used to store the game. This routine
	should be called when exiting the interpreter.


void GetPictureSize(int* width, int* height)

	Returns the width and height of the bitmap that graphics should
	be drawn into. This is constant for any particular game.


L9BOOL RunGraphics(void)

	Runs an opcode of the graphics routines. If a graphics opcode was
	run TRUE is returned, otherwise FALSE.

	The simplest way to get graphics to display is to add a loop to
	repeatedly call RunGraphics() to os_input() and os_readchar():

		while (RunGraphics());
		// Now draw graphics bitmap on display...

	Optionally, the code can provide a more "atmospheric" recreation
	of the games by drawing the graphics slowly, as was the case on
	the old 8-bit computers. This is achieved by calling RunGraphics()
	several times then waiting for a while before calling it again.
	Note that when waiting the code should still respond to user
	input.


BitmapType DetectBitmaps(char* dir)

	Given a directory, returns the type of bitmap picture files in it,
	or NO_BITMAPS if there are no bitmaps.

	This function is only available if the preprocessor symbol
	BITMAP_DECODER is defined.


Bitmap* DecodeBitmap(char* dir, BitmapType type, int num, int x, int y)

	This function loads and decodes the specified bitmap, returning
	a Bitmap structure. The structure contains the width and height
	of the bitmap, a palette of up to 32 colours used in the bitmap,
	and the actual data as an array of indexes into the palette.

	This function is only available if the preprocessor symbol
	BITMAP_DECODER is defined.


One more complex feature of the interpreter is that a new Level 9 game can
be loaded without exiting and restarting the interpreter. This is of use
in a windowing environment. In this case, both main() and the code that
catches a "New Game" menu item should call a routine such as the example
new_game() below. This ensures that each new game does not use up more and
more of the interpreter's stack.

int newgame(char* game_name)
{
  static int playing = FALSE;

  if (LoadGame(game,NULL))
  {
    if (playing)
      return -1;

    playing = TRUE;
    while (RunGame());
    playing = FALSE;
  }
  else
    warn("Unable to load game");

  return -1;
}

 * 
 * 
 */

