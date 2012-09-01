package com.realife.l9droid;

//started: 01.09.2012

//byte		L9BYTE		unsigned 8 bit quantity
//short 	L9UINT16	unsigned 16 bit quantity
//int		L9UINT32	unsigned 32 bit quantity
//boolean	L9BOOL		quantity capable of holding the values TRUE (1) and FALSE (0)

public class L9 {
	
	int showtitle=1;
	
	//GameState workspace;
	GameState workspace;

	//L9UINT16 randomseed;
	boolean Running;
	//L9BOOL Running;
	short randomseed;
	
	
	/*-------------------------------------------
	L9BOOL LoadGame(char *filename,char *picname)
	{
		L9BOOL ret=LoadGame2(filename,picname);
		showtitle=1;
		clearworkspace();
		workspace.stackptr=0;
		// need to clear listarea as well
		memset((L9BYTE*) workspace.listarea,0,LISTAREASIZE);
		return ret;
	}
	*/
	public boolean LoadGame(String fileName, String picName) {
		
		boolean ret=LoadGame2(fileName, picName);
		showtitle=1;
		clearworkspace();
		workspace.stackptr=0;
		/* need to clear listarea as well */
		//TODO: возможно, поискать более красивое решение - метод memset
		//TODO: вообще перенести очистку в класс GameState
		//memset((L9BYTE*) workspace.listarea,0,LISTAREASIZE);
		for (int i=0;i<workspace.listarea.length;i++) workspace.listarea[i]=0;
		return ret;

	}
	
	public boolean RunGame() {
		return true;
	}
	
	public void StopGame () {
		
	}
	
	public void RestoreGame(String inFile) {
		
	}
	
	public void FreeMemory() {
		
	}
	
////////////////////////////////////////////////////////////////////////
	
	/*-----------------------
	void clearworkspace(void)
	{
		memset(workspace.vartable,0,sizeof(workspace.vartable));
	}
	*/
	private void clearworkspace() 	{
		//TODO: возможно, поискать более красивое решение - метод memset
		//TODO: вообще перенести очистку в класс GameState
		//memset(workspace.vartable,0,sizeof(workspace.vartable));
		for (int i=0;i<workspace.vartable.length;i++) workspace.vartable[i]=0; 
	}
	
	/*
	L9BOOL LoadGame2(char *filename,char *picname)
	{
	#ifdef CODEFOLLOW
		f=fopen(CODEFOLLOWFILE,"w");
		fprintf(f,"Code follow file...\n");
		fclose(f);
	#endif

		// may be already running a game, maybe in input routine 
		Running=FALSE;
		ibuffptr=NULL;

	//  intstart 
		if (!intinitialise(filename,picname)) return FALSE;
	// 	if (!checksumgamedata()) return FALSE; 

		codeptr=acodeptr;
		randomseed=(L9UINT16)time(NULL);
		strcpy(LastGame,filename);
		return Running=TRUE;
	}
	*/
	boolean LoadGame2(String filename, String picname) {
		Running=false;
		//ibuffptr=NULL;
		//if (!intinitialise(filename,picname)) return false;
		//codeptr=acodeptr;
		//randomseed=(L9UINT16)time(NULL);
		//strcpy(LastGame,filename);
		Running=true;
		return Running;
	}

}

/*-------------
 typedef struct
{
	L9UINT32 Id;
	L9UINT16 codeptr,stackptr,listsize,stacksize,filenamesize,checksum;
	L9UINT16 vartable[256];
	L9BYTE listarea[LISTAREASIZE];
	L9UINT16 stack[STACKSIZE];
	char filename[MAX_PATH];
} GameState;

*/
class GameState {

	//TODO: перенести LISTAREASIZE и STACKSIZE в глобальные константы 
	private static final int LISTAREASIZE = 0x800;
	private static final int STACKSIZE = 1024;
	
	int Id;
	short codeptr,stackptr,listsize,stacksize,filenamesize,checksum;
	short vartable[];
	byte listarea[];
	short stack[];
	String filename;
	
	GameState() {
		vartable=new short[256];
		listarea=new byte[LISTAREASIZE];
		stack=new short[STACKSIZE];
	}
	
}



//L9BYTE		unsigned 8 bit quantity
//L9UINT16	unsigned 16 bit quantity
//L9UINT32	unsigned 32 bit quantity
//L9BOOL		quantity capable of holding the values TRUE (1) and FALSE (0)
//#define MAX_PATH 256

//void os_printchar(char c)
//L9BOOL os_input(char* ibuff, int size)
//char os_readchar(L9UINT32 millis)
//L9BOOL os_stoplist(void)
//void os_flush(void)
//L9BOOL os_save_file(L9BYTE* Ptr, int Bytes)
//L9BOOL os_load_file(L9BYTE* Ptr, int* Bytes, int Max)
//L9BOOL os_get_game_file(char* NewName, int Size)
//void os_set_filenumber(char* NewName, int Size, int n)
//void os_graphics(int mode)
//void os_cleargraphics(void)
//void os_setcolour(int colour, int index)
//void os_drawline(int x1, int y1, int x2, int y2, int colour1, int colour2)
//void os_fill(int x, int y, int colour1, int colour2)
//void os_show_bitmap(int pic, int x, int y)
//L9BOOL LoadGame(char* filename, char* picname)
//L9BOOL RunGame(void)
//void StopGame(void)
//void RestoreGame(char *inFile)
//void FreeMemory(void)
//void GetPictureSize(int* width, int* height)
//L9BOOL RunGraphics(void)
//BitmapType DetectBitmaps(char* dir)
//Bitmap* DecodeBitmap(char* dir, BitmapType type, int num, int x, int y)


	