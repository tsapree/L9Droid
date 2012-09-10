package com.realife.l9droid;

//started: 01.09.2012

//char		16 bit
//byte		signed 8 bit	->	L9BYTE		unsigned 8 bit quantity
//short 	signed 16 bit	->	L9UINT16	unsigned 16 bit quantity
//int		signed 32 bit	->	L9UINT32	unsigned 32 bit quantity
//boolean					->	L9BOOL		quantity capable of holding the values TRUE (1) and FALSE (0)
//
//0=false
//1=true 
//if (var) -> if(var!=0)
//*getvar()->workspace.vartable[getvar()]

public class L9 {
	
	//TODO: перенести LISTAREASIZE и STACKSIZE в глобальные константы 
	//TODO: может, перенести их в какой-либо класс, а не таскать по всем.
	private static final int LISTAREASIZE = 0x800;
	private static final int STACKSIZE = 1024;
	
	//#define IBUFFSIZE 500
	private static final int IBUFFSIZE = 500; 

	int showtitle=1;
	
	//GameState workspace;
	GameState workspace;
	//L9UINT16 randomseed;
	short randomseed;
	//L9BOOL Running;
	boolean Running;
	
	//char LastGame[MAX_PATH];
	String LastGame;
	

//// "L901"
//#define L9_ID 0x4c393031
//

//#define RAMSAVESLOTS 10
//#define GFXSTACKSIZE 100
//
//
//// Typedefs
//typedef struct
//{
//	L9UINT16 vartable[256];
//	L9BYTE listarea[LISTAREASIZE];
//} SaveStruct;
//
//typedef struct
//{
//	L9BYTE *a5;
//	int scale;
//} GfxState;
//
//
// Enumerations 
//enum L9GameTypes {L9_V1,L9_V2,L9_V3,L9_V4};
	private static final int L9_V1=1;
	private static final int L9_V2=2;
	private static final int L9_V3=3;
	private static final int L9_V4=4;
//enum V2MsgTypes {V2M_NORMAL,V2M_ERIK};
int V2M_NORMAL=1;
int V2M_ERIK=2;
//

// Global Variables
//L9BYTE* startfile=NULL,*pictureaddress=NULL,*picturedata=NULL;
	byte l9memory[];
	int startfile;
	int filesize;
	int startdata;
	int datasize;
	int listarea;
//L9UINT32 picturesize;
//
	int L9Pointers[];
	int absdatablock;
//L9BYTE *list2ptr
//L9BYTE *list3ptr
	int list2ptr;
	int list3ptr;
//L9BYTE *list9startptr
	int acodeptr;
//L9BYTE *startmd
	int startmd;
//L9BYTE *endmd
	int endmd;
//L9BYTE *endwdp5
	int endwdp5;
//L9BYTE *wordtable
	int wordtable;
//L9BYTE *dictdata
	int dictdata;
//L9BYTE *defdict;
	int defdict;
//L9UINT16 dictdatalen;
	int dictdatalen;
//L9BYTE *startmdV2;
	int startmdV2;
//
	int wordcase;
	int unpackcount;
	byte unpackbuf[];
//L9BYTE* dictptr;
	int dictptr;
	byte threechars[];
	int L9GameType;
	int V2MsgType;
//
//SaveStruct ramsavearea[RAMSAVESLOTS];
//
//char ibuff[IBUFFSIZE];
//L9BYTE* ibuffptr;
	char obuff[];
	int wordcount;
	char ibuff[];
	String ibuffstr;
	
	
//
	boolean Cheating=false;
//int CheatWord;
//GameState CheatWorkspace;
//
//int reflectflag,scale,gintcolour,option;
	int l9textmode=0;
//int drawx=0,drawy=0,screencalled=0;
//L9BYTE *gfxa5=NULL;
//L9BOOL scalegfx=TRUE;
//Bitmap* bitmap=NULL;
	
	
	//
//GfxState GfxStack[GFXSTACKSIZE];
//int GfxStackPos=0;
//
	char lastchar='.';
	char lastactualchar=0;
	int d5;
//
int codeptr;	// instruction codes - pointer 
int code;		// instruction codes - code
//
//L9BYTE* list9ptr;
//
//int unpackd3;
//
	short exitreversaltable[]={0x00,0x04,0x06,0x07,0x01,0x08,0x02,0x03,0x05,0x0a,0x09,0x0c,0x0b,0xff,0xff,0x0f};
//
//L9UINT16 gnostack[128];
	//L9BYTE gnoscratch[32];
	int gnostack[];
	short gnoscratch[];
	short searchdepth;
	short inithisearchpos;
	short gnosp;
	short object;
	short numobjectfound;

//vars added by tsap
	int amessageV2_depth=0;
	int amessageV25_depth=0;
	int displaywordref_mdtmode=0;

	
	L9() {
		workspace=new GameState();
		unpackbuf=new byte[8];
		L9Pointers=new int[12];
		threechars=new byte[34];
		obuff=new char[34];
		gnoscratch=new short[32];
		gnostack=new int [128];
	};
	
	/*--was--	L9BOOL LoadGame(char *filename,char *picname)
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
		//TODO: возможно, поискать более красивое решение - метод memset (как и clearworkspace)
		//TODO: вообще перенести очистку в класс GameState
		for (int i=0;i<LISTAREASIZE;i++) l9memory[listarea+i]=0;
		return ret;

	}
	
	/*--was-- L9BOOL RunGame(void)
	{
		code=*codeptr++;
	//	printf("%d",code); 
		executeinstruction();
		return Running;
	}*/
	public boolean RunGame() {
		code=l9memory[codeptr++];
		executeinstruction();
		return Running;
	}
	
	/* can be called from input to cause fall through for exit */
	/*--was-- void StopGame(void)
	{
		Running=FALSE;
	}*/
	public void StopGame () {
		Running=false;
	}
	
	public void RestoreGame(String inFile) {
		
	}
	
	public void FreeMemory() {
		
	}
	
	//TODO: void GetPictureSize(int* width, int* height)
	//TODO: L9BOOL RunGraphics(void)
	//TODO: BitmapType DetectBitmaps(char* dir)
	//TODO: Bitmap* DecodeBitmap(char* dir, BitmapType type, int num, int x, int y)
	
////////////////////////////////////////////////////////////////////////

	void os_printchar(char c) {};
	//L9BOOL os_input(char* ibuff, int size)
	String os_input(int size) {return null;}; 
	//char os_readchar(L9UINT32 millis)
	//L9BOOL os_stoplist(void)
	void os_flush() {};
	//L9BOOL os_save_file(L9BYTE* Ptr, int Bytes)
	//L9BOOL os_load_file(L9BYTE* Ptr, int* Bytes, int Max)
	//L9BOOL os_get_game_file(char* NewName, int Size)
	//void os_set_filenumber(char* NewName, int Size, int n)
	void os_graphics(int mode) {};
	void os_cleargraphics() {};
	//void os_setcolour(int colour, int index)
	//void os_drawline(int x1, int y1, int x2, int y2, int colour1, int colour2)
	//void os_fill(int x, int y, int colour1, int colour2)
	void os_show_bitmap(int pic, int x, int y) {};
	
	byte[] os_load(String filename) { return null; };
	
////////////////////////////////////////////////////////////////////////
	
	/*--was--	void clearworkspace(void)
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
	
	/*--was-- L9BOOL LoadGame2(char *filename,char *picname)
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
		// may be already running a game, maybe in input routine
		Running=false;
		//TODO: ibuffptr=NULL;
		if (!intinitialise(filename,picname)) return false;
		codeptr=acodeptr;
		randomseed = (short)(Math.random()*32767);
		LastGame=filename;
		Running=true;
		return Running;
	}
	
	/*--was-- L9BOOL intinitialise(char*filename,char*picname)
	{
	// init 
	// driverclg 

		int i;
		int hdoffset;
		long Offset;
		FILE *f;

		if (pictureaddress)
		{
			free(pictureaddress);
			pictureaddress=NULL;
		}
		picturedata=NULL;
		picturesize=0;
		gfxa5=NULL;

		if (!load(filename))
		{
			error("\rUnable to load: %s\r",filename);
			return FALSE;
		}

		// try to load graphics
		if (picname)
		{
			f=fopen(picname,"rb");
			if (f)
			{
				picturesize=filelength(f);
				L9Allocate(&pictureaddress,picturesize);
				if (fread(pictureaddress,1,picturesize,f)!=picturesize)
				{
					free(pictureaddress);
					pictureaddress=NULL;
					picturesize=0;
				}
				picturedata=pictureaddress;
				fclose(f);
			}
		}
		screencalled=0;
		l9textmode=0;

	#ifdef FULLSCAN
		FullScan(startfile,FileSize);
	#endif

		Offset=Scan(startfile,FileSize);
		if (Offset<0)
		{
			Offset=ScanV2(startfile,FileSize);
			L9GameType=L9_V2;
			if (Offset<0)
			{
				Offset=ScanV1(startfile,FileSize);
				L9GameType=L9_V1;
				if (Offset<0)
				{
					error("\rUnable to locate valid header in file: %s\r",filename);
				 	return FALSE;
				}
			}
		}

		startdata=startfile+Offset;
		FileSize-=Offset;

	// setup pointers 
		if (L9GameType!=L9_V1)
		{
			// V2,V3,V4 

			hdoffset=L9GameType==L9_V2 ? 4 : 0x12;

			for (i=0;i<12;i++)
			{
				L9UINT16 d0=L9WORD(startdata+hdoffset+i*2);
				L9Pointers[i]= (i!=11 && d0>=0x8000 && d0<=0x9000) ? workspace.listarea+d0-0x8000 : startdata+d0;
			}
			absdatablock=L9Pointers[0];
			list2ptr=L9Pointers[3];
			list3ptr=L9Pointers[4];
			//list9startptr 

			// if ((((L9UINT32) L9Pointers[10])&1)==0) L9Pointers[10]++; amiga word access hack

			list9startptr=L9Pointers[10];
			acodeptr=L9Pointers[11];
		}

		switch (L9GameType)
		{
			case L9_V1:
				break;
			case L9_V2:
			{
				double a2,a25;
				startmd=startdata + L9WORD(startdata+0x0);
				startmdV2=startdata + L9WORD(startdata+0x2);

				// determine message type 
				if (analyseV2(&a2) && a2>2 && a2<10)
				{
					V2MsgType=V2M_NORMAL;
					#ifdef L9DEBUG
					printf("V2 msg table: normal, wordlen=%.2lf",a2);
					#endif
				}
				else if (analyseV25(&a25) && a25>2 && a25<10)
				{
					V2MsgType=V2M_ERIK;
					#ifdef L9DEBUG
					printf("V2 msg table: Erik, wordlen=%.2lf",a25);
					#endif
				}
				else
				{
					error("\rUnable to identify V2 message table in file: %s\r",filename);
					return FALSE;
				}
				break;
			}
			case L9_V3:
			case L9_V4:
				startmd=startdata + L9WORD(startdata+0x2);
				endmd=startmd + L9WORD(startdata+0x4);
				defdict=startdata+L9WORD(startdata+6);
				endwdp5=defdict + 5 + L9WORD(startdata+0x8);
				dictdata=startdata+L9WORD(startdata+0x0a);
				dictdatalen=L9WORD(startdata+0x0c);
				wordtable=startdata + L9WORD(startdata+0xe);
				break;
		}

	#ifndef NO_SCAN_GRAPHICS
		// If there was no graphics file, look in the game data 
		if (picturedata==NULL)
		{
			int sz=FileSize-(acodeptr-startdata);
			int i=0;
			while ((i<sz-0x1000)&&(picturedata==NULL))
			{
				picturedata=acodeptr+i;
				picturesize=sz-i;
				if (!checksubs())
				{
					picturedata=NULL;
					picturesize=0;
				}
				i++;
			}
		}
	#endif
		return TRUE;
	}
	*/
	boolean intinitialise(String filename, String picname)
	{
	// init 
	// driverclg 
		
		int i;
		int hdoffset;
		int Offset;
		//TODO: FILE *f;

		//TODO: if (pictureaddress)
		//TODO: {
		//TODO: 	free(pictureaddress);
		//TODO: 	pictureaddress=NULL;
		//TODO: }
		//TODO: picturedata=NULL;
		//TODO: picturesize=0;
		//TODO: gfxa5=NULL;
		
		if (!load(filename))
		{
			error("\rUnable to load: %s\r",filename);
			return false;
		}
		
		L9DEBUG("Loaded ok, size=%d\r",filesize);
		
		/*TODO:
		// try to load graphics
		if (picname)
		{
			f=fopen(picname,"rb");
			if (f)
			{
				picturesize=filelength(f);
				L9Allocate(&pictureaddress,picturesize);
				if (fread(pictureaddress,1,picturesize,f)!=picturesize)
				{
					free(pictureaddress);
					pictureaddress=NULL;
					picturesize=0;
				}
				picturedata=pictureaddress;
				fclose(f);
			}
		}
		screencalled=0;
		l9textmode=0;

	#ifdef FULLSCAN
		FullScan(startfile,FileSize);
	#endif

		*/
		Offset=Scan();
		if (Offset<0)
		{
			Offset=ScanV2();
			L9GameType=L9_V2;
			if (Offset<0)
			{
				Offset=ScanV1();
				L9GameType=L9_V1;
				if (Offset<0)
				{
					error("\rUnable to locate valid header in file: %s\r",filename);
				 	return false;
				}
			}
		}

		startdata=startfile+Offset;
		datasize-=filesize-Offset;

	// setup pointers 
		if (L9GameType!=L9_V1)
		{
			// V2,V3,V4 

			hdoffset=L9GameType==L9_V2 ? 4 : 0x12;

			for (i=0;i<12;i++)
			{
				int d0=L9WORD(startdata+hdoffset+i*2);
				L9Pointers[i]= (i!=11 && d0>=0x8000 && d0<=0x9000) ? listarea+d0-0x8000 : startdata+d0;
			}
			absdatablock=L9Pointers[0];
			list2ptr=L9Pointers[3];
			list3ptr=L9Pointers[4];
			//TODO: list9startptr=L9Pointers[10];
			acodeptr=L9Pointers[11];
		}

		switch (L9GameType)
		{
			case L9_V1:
				break;
			case L9_V2:
			{
				double a2,a25;
				startmd=startdata + L9WORD(startdata+0x0);
				startmdV2=startdata + L9WORD(startdata+0x2);

				// determine message type
				a2=analyseV2();
				if (a2>0.0 && a2>2 && a2<10)
				{
					V2MsgType=V2M_NORMAL;
					L9DEBUG("V2 msg table: normal, wordlen=%d/10\r",(int)a2*10);
				}
				else {
					a25=analyseV25();
					if (a25>0 && a25>2 && a25<10)
					{
						V2MsgType=V2M_ERIK;
						L9DEBUG("V2 msg table: Erik, wordlen=%d/10\r",(int)a25*10);
					}
					else
					{
						error("\rUnable to identify V2 message table in file: %s\r",filename);
						return false;
					}
				};
				break;
			}
			case L9_V3:
			case L9_V4:
				startmd=startdata + L9WORD(startdata+0x2);
				endmd=startmd + L9WORD(startdata+0x4);
				defdict=startdata+L9WORD(startdata+6);
				endwdp5=defdict + 5 + L9WORD(startdata+0x8);
				dictdata=startdata+L9WORD(startdata+0x0a);
				dictdatalen=L9WORD(startdata+0x0c);
				wordtable=startdata + L9WORD(startdata+0xe);
				break;
		};

//TODO:	#ifndef NO_SCAN_GRAPHICS
//TODO:		// If there was no graphics file, look in the game data 
//TODO:		if (picturedata==NULL)
//TODO:		{
//TODO:			int sz=FileSize-(acodeptr-startdata);
//TODO:			int i=0;
//TODO:			while ((i<sz-0x1000)&&(picturedata==NULL))
//TODO:			{
//TODO:				picturedata=acodeptr+i;
//TODO:				picturesize=sz-i;
//TODO:				if (!checksubs())
//TODO:				{
//TODO:					picturedata=NULL;
//TODO:					picturesize=0;
//TODO:				}
//TODO:				i++;
//TODO:			}
//TODO:		}
//TODO:	#endif
	
		return true;
	}
	
	/*--was-- L9BOOL analyseV2(double *wl)
	{
		long words=0,chars=0;
		int i;
		for (i=1;i<256;i++)
		{
			long w=0,c=0;
			if (amessageV2(startmd,i,&w,&c))
			{
				words+=w;
				chars+=c;
			}
			else return FALSE;
		}
		*wl=words ? (double) chars/words : 0.0;
		return TRUE;
	}*/
	double analyseV2()
	{
		int words=0,chars=0;
		int i;
		for (i=1;i<256;i++)
		{
			//long w=0,c=0;
			int w[]={0};
			int c[]={0};
			if (amessageV2(startmd,i,w,c))
			{
				words+=w[0];
				chars+=c[0];
			}
			else return -1.0;
		}
		return words!=0 ? (double) chars/words : 0.0;
	}

	/*--was-- L9BOOL analyseV25(double *wl)
	{
		long words=0,chars=0;
		int i;
		for (i=0;i<256;i++)
		{
			long w=0,c=0;
			if (amessageV25(startmd,i,&w,&c))
			{
				words+=w;
				chars+=c;
			}
			else return FALSE;
		}

		*wl=words ? (double) chars/words : 0.0;
		return TRUE;
	}*/
	double analyseV25()
	{
		int words=0,chars=0;
		int i;
		for (i=0;i<256;i++)
		{
			//long w=0,c=0;
			int w[]={0};
			int c[]={0};
			if (amessageV25(startmd,i,w,c))
			{
				words+=w[0];
				chars+=c[0];
			}
			else return -1.0;
		}

		return words!=0 ? (double) chars/words : 0.0;
	}
	
	/*--was-- L9BOOL amessageV2(L9BYTE *ptr,int msg,long *w,long *c)
	{
		int n;
		L9BYTE a;
		static int depth=0;
		if (msg==0) return FALSE;
		while (--msg)
		{
			ptr+=msglenV2(&ptr);
		}
		if (ptr >= startdata+FileSize) return FALSE;
		n=msglenV2(&ptr);

		while (--n>0)
		{
			a=*++ptr;
			if (a<3) return TRUE;

			if (a>=0x5e)
			{
				if (++depth>10 || !amessageV2(startmdV2-1,a-0x5d,w,c))
				{
					depth--;
					return FALSE;
				}
				depth--;
			}
			else
			{
				char ch=a+0x1d;
				if (ch==0x5f || ch==' ') (*w)++;
				else (*c)++;
			}
		}
		return TRUE;
	}*/
	boolean amessageV2(int ptr,int msg,int w[],int c[])
	{
		int n;
		int a;
		if (msg==0) return false;
		while (--msg!=0)
		{
			ptr+=msglenV2(ptr);
		}
		if (ptr >= startdata+datasize) return false;
		n=msglenV2(ptr);

		while (--n>0) {
			a=l9memory[++ptr]&0xff;
			if (a<3) return true;
			if (a>=0x5e)
			{
				if (++amessageV2_depth>10 || !amessageV2(startmdV2-1,a-0x5d,w,c))
				{
					amessageV2_depth--;
					return false;
				}
				amessageV2_depth--;
			}
			else
			{
				char ch=(char)(a+0x1d);
				if (ch==0x5f || ch==' ') w[0]++;
				else c[0]++;
			}
		}
		return true;
	}

	/*--was-- L9BOOL amessageV25(L9BYTE *ptr,int msg,long *w,long *c)
	{
		int n;
		L9BYTE a;
		static int depth=0;

		while (msg--)
		{
			ptr+=msglenV25(&ptr);
		}
		if (ptr >= startdata+FileSize) return FALSE;
		n=msglenV25(&ptr);

		while (--n>0)
		{
			a=*ptr++;
			if (a<3) return TRUE;

			if (a>=0x5e)
			{
				if (++depth>10 || !amessageV25(startmdV2,a-0x5e,w,c))
				{
					depth--;
					return FALSE;
				}
				depth--;
			}
			else
			{
				char ch=a+0x1d;
				if (ch==0x5f || ch==' ') (*w)++;
				else (*c)++;
			}
		}
		return TRUE;
	}*/
	boolean amessageV25(int ptr,int msg,int w[],int c[])
	{
		int n;
		int a;
		
		while (msg--!=0)
		{
			ptr+=msglenV25(ptr);
		}
		if (ptr >= startdata+datasize) return false;
		n=msglenV25(ptr);

		while (--n>0)
		{
			a=l9memory[ptr++]&0xff;
			if (a<3) return true;

			if (a>=0x5e)
			{
				if (++amessageV25_depth>10 || !amessageV25(startmdV2,a-0x5e,w,c))
				{
					amessageV25_depth--;
					return false;
				}
				amessageV25_depth--;
			}
			else
			{
				char ch=(char)(a+0x1d);
				if (ch==0x5f || ch==' ') w[0]++;
				else c[0]++;
			}
		}
		return true;
	}
	
	/* v2 message stuff */
	/*--was-- int msglenV2(L9BYTE **ptr)
	{
		int i=0;
		L9BYTE a;

		// catch berzerking code 
		if (*ptr >= startdata+FileSize) return 0;

		while ((a=**ptr)==0)
		{
		 (*ptr)++;
		 
		 if (*ptr >= startdata+FileSize) return 0;

		 i+=255;
		}
		i+=a;
		return i;
	}*/
	int msglenV2(int ptr)
	{
		int i=0;
		int a;

		/* catch berzerking code */
		if (ptr >= startdata+datasize) return 0;

		while ((a=l9memory[ptr])==0) {
			ptr++;
			if (ptr >= startdata+datasize) return 0;
			i+=255;
		}
		i+=a;
		return i;
	}
	
	/*--was-- int msglenV25(L9BYTE **ptr)
	{
		L9BYTE *ptr2=*ptr;
		while (ptr2<startdata+FileSize && *ptr2++!=1) ;
		return ptr2-*ptr;
	}*/
	int msglenV25(int ptr)
	{
		int ptr2=ptr;
		while (ptr2<startdata+datasize && l9memory[ptr2++]!=1) ;
		return ptr2-ptr;
	}

	
	/*--was-- long Scan(L9BYTE* StartFile,L9UINT32 FileSize)
		{
			L9BYTE *Chk=malloc(FileSize+1);
			L9BYTE *Image=calloc(FileSize,1);
			L9UINT32 i,num,Size,MaxSize=0;
			int j;
			L9UINT16 d0=0,l9,md,ml,dd,dl;
			L9UINT32 Min,Max;
			long Offset=-1;
			L9BOOL JumpKill, DriverV4;
		
			if ((Chk==NULL)||(Image==NULL))
			{
				fprintf(stderr,"Unable to allocate memory for game scan! Exiting...\n");
				exit(0);
			}
		
			Chk[0]=0;
			for (i=1;i<=FileSize;i++)
				Chk[i]=Chk[i-1]+StartFile[i-1];
		
			for (i=0;i<FileSize-33;i++)
			{
				num=L9WORD(StartFile+i)+1;
		
				//Chk[i] = 0 +...+ i-1
				//Chk[i+n] = 0 +...+ i+n-1
				//Chk[i+n] - Chk[i] = i + ... + i+n
		
				if (num>0x2000 && i+num<=FileSize && Chk[i+num]==Chk[i])
				{
					md=L9WORD(StartFile+i+0x2);
					ml=L9WORD(StartFile+i+0x4);
					dd=L9WORD(StartFile+i+0xa);
					dl=L9WORD(StartFile+i+0xc);
		
					if (ml>0 && md>0 && i+md+ml<=FileSize && dd>0 && dl>0 && i+dd+dl*4<=FileSize)
					{
						// v4 files may have acodeptr in 8000-9000, need to fix
						for (j=0;j<12;j++)
						{
							d0=L9WORD (StartFile+i+0x12 + j*2);
							if (j!=11 && d0>=0x8000 && d0<0x9000)
							{
								if (d0>=0x8000+LISTAREASIZE) break;
							}
							else if (i+d0>FileSize) break;
						}
						// list9 ptr must be in listarea, acode ptr in data
						//if (j<12 || (d0>=0x8000 && d0<0x9000)) continue;
						if (j<12) continue;
								
						l9=L9WORD(StartFile+i+0x12 + 10*2);
						if (l9<0x8000 || l9>=0x8000+LISTAREASIZE) continue;
		
						Size=0;
						Min=Max=i+d0;
						DriverV4=0;
						if (ValidateSequence(StartFile,Image,i+d0,i+d0,&Size,FileSize,&Min,&Max,FALSE,&JumpKill,&DriverV4))
						{
		#ifdef L9DEBUG
							printf("Found valid header at %ld, code size %ld",i,Size);
		#endif
							if (Size>MaxSize)
							{
								Offset=i;
								MaxSize=Size;
								L9GameType=DriverV4?L9_V4:L9_V3;
							}
						}
					}
				}
			}
			free(Chk);
			free(Image);
			return Offset;
		}

	 */
	int Scan()
	{
		
		//L9BYTE *Chk=malloc(FileSize+1);
		byte Chk[] = new byte[filesize+1];
		//L9BYTE *Image=calloc(FileSize,1);
		byte Image[] = new byte[filesize];
		int i,num,MaxSize=0;
		int j;
		int d0=0,l9,md,ml,dd,dl;
		int Offset=-1;
		
		ScanData scandata=new ScanData();
		
		//TODO: Есть ли шанс, что массивы Chk и Image не создадутся?
		//if ((Chk==NULL)||(Image==NULL))
		//{
		//	fprintf(stderr,"Unable to allocate memory for game scan! Exiting...\n");
		//	exit(0);
		//}

		Chk[0]=0;
		for (i=1;i<=filesize;i++)
			//Chk[i]=Chk[i-1]+StartFile[i-1];
			Chk[i]=(byte)(((Chk[i-1]&0xff)+(l9memory[startfile+i-1]&0xff))&0xff);

		for (i=0;i<filesize-33-1;i++)
		{
			num=L9WORD(i)+1;
	
			//Chk[i] = 0 +...+ i-1
			//Chk[i+n] = 0 +...+ i+n-1
			//Chk[i+n] - Chk[i] = i + ... + i+n

			if (num>0x2000 && i+num<=filesize && Chk[i+num]==Chk[i])
			{
				md=L9WORD(i+0x2);
				ml=L9WORD(i+0x4);
				dd=L9WORD(i+0xa);
				dl=L9WORD(i+0xc);

				if (ml>0 && md>0 && i+md+ml<=filesize && dd>0 && dl>0 && i+dd+dl*4<=filesize)
				{
					// v4 files may have acodeptr in 8000-9000, need to fix 
					for (j=0;j<12;j++)
					{
						d0=L9WORD (i+0x12 + j*2);
						if (j!=11 && d0>=0x8000 && d0<0x9000)
						{
							if (d0>=0x8000+LISTAREASIZE) break;
						}
						else if (i+d0>filesize) break;
					}
					// list9 ptr must be in listarea, acode ptr in data 
					//if (j<12 || (d0>=0x8000 && d0<0x9000)) continue;
					//if (j<12) continue;

					l9=L9WORD(i+0x12 + 10*2);
					if (l9<0x8000 || l9>=0x8000+LISTAREASIZE) continue;

					scandata.Size=0;
					scandata.Min=scandata.Max=i+d0;
					scandata.DriverV4=false;
					if (ValidateSequence(Image,i+d0,i+d0,scandata,false,true))
					{
						L9DEBUG("Found valid header at %d, code size %d\r",i,scandata.Size);
						if (scandata.Size>MaxSize)
						{
							Offset=i;
							MaxSize=scandata.Size;
							L9GameType=scandata.DriverV4?L9_V4:L9_V3;
						}
					}
				}
			}
		}
		return Offset;
	}
	
	/*--was-- L9BOOL ValidateSequence(L9BYTE* Base,L9BYTE* Image,L9UINT32 iPos,L9UINT32 acode,L9UINT32 *Size,L9UINT32 FileSize,L9UINT32 *Min,L9UINT32 *Max,L9BOOL Rts,L9BOOL *JumpKill, L9BOOL *DriverV4)
	{
		L9UINT32 Pos;
		L9BOOL Finished=FALSE,Valid;
		L9UINT32 Strange=0;
		int ScanCodeMask;
		int Code;
		*JumpKill=FALSE;
	
		if (iPos>=FileSize)
			return FALSE;
		Pos=iPos;
		if (Pos<*Min) *Min=Pos;
	
		if (Image[Pos]) return TRUE; // hit valid code 
	
		do
		{
			Code=Base[Pos];
			Valid=TRUE;
			if (Image[Pos]) break; // converged to found code 
			Image[Pos++]=2;
			if (Pos>*Max) *Max=Pos;
	
			ScanCodeMask=0x9f;
			if (Code&0x80)
			{
				ScanCodeMask=0xff;
				if ((Code&0x1f)>0xa)
					Valid=FALSE;
				Pos+=2;
			}
			else switch (Code & 0x1f)
			{
				case 0: // goto 
				{
					L9UINT32 Val=scangetaddr(Code,Base,&Pos,acode,&ScanCodeMask);
					Valid=ValidateSequence(Base,Image,Val,acode,Size,FileSize,Min,Max,TRUE,JumpKill,DriverV4);
					Finished=TRUE;
					break;
				}
				case 1: // intgosub 
				{
					L9UINT32 Val=scangetaddr(Code,Base,&Pos,acode,&ScanCodeMask);
					Valid=ValidateSequence(Base,Image,Val,acode,Size,FileSize,Min,Max,TRUE,JumpKill,DriverV4);
					break;
				}
				case 2: // intreturn 
					Valid=Rts;
					Finished=TRUE;
					break;
				case 3: // printnumber
					Pos++;
					break;
				case 4: // messagev 
					Pos++;
					break;
				case 5: // messagec 
					scangetcon(Code,&Pos,&ScanCodeMask);
					break;
				case 6: // function 
					switch (Base[Pos++])
					{
						case 2:// random 
							Pos++;
							break;
						case 1:// calldriver 
							if (DriverV4)
							{
								if (CheckCallDriverV4(Base,Pos-2))
									*DriverV4 = TRUE;
							}
							break;
						case 3:// save 
						case 4:// restore 
						case 5:// clearworkspace 
						case 6:// clear stack 
							break;
						case 250: // printstr 
							while (Base[Pos++]);
							break;
	
						default:
	//#ifdef L9DEBUG
	//						// printf("scan: illegal function call: %d",Base[Pos-1]); 
	//#endif
							Valid=FALSE;
							break;
					}
					break;
				case 7: // input 
					Pos+=4;
					break;
				case 8: // varcon 
					scangetcon(Code,&Pos,&ScanCodeMask);
					Pos++;
					break;
				case 9: // varvar
					Pos+=2;
					break;
				case 10: // _add 
					Pos+=2;
					break;
				case 11: // _sub 
					Pos+=2;
					break;
				case 14: // jump 
	//#ifdef L9DEBUG
	//				// printf("jmp at codestart: %ld",acode); 
	//#endif
					*JumpKill=TRUE;
					Finished=TRUE;
					break;
				case 15: // exit 
					Pos+=4;
					break;
				case 16: // ifeqvt 
				case 17: // ifnevt 
				case 18: // ifltvt 
				case 19: // ifgtvt 
				{
					L9UINT32 Val;
					Pos+=2;
					Val=scangetaddr(Code,Base,&Pos,acode,&ScanCodeMask);
					Valid=ValidateSequence(Base,Image,Val,acode,Size,FileSize,Min,Max,Rts,JumpKill,DriverV4);
					break;
				}
				case 20: // screen 
					if (Base[Pos++]) Pos++;
					break;
				case 21: // cleartg 
					Pos++;
					break;
				case 22: // picture 
					Pos++;
					break;
				case 23: // getnextobject 
					Pos+=6;
					break;
				case 24: // ifeqct 
				case 25: // ifnect 
				case 26: // ifltct 
				case 27: // ifgtct 
				{
					L9UINT32 Val;
					Pos++;
					scangetcon(Code,&Pos,&ScanCodeMask);
					Val=scangetaddr(Code,Base,&Pos,acode,&ScanCodeMask);
					Valid=ValidateSequence(Base,Image,Val,acode,Size,FileSize,Min,Max,Rts,JumpKill,DriverV4);
					break;
				}
				case 28: // printinput 
					break;
				case 12: // ilins 
				case 13: // ilins 
				case 29: // ilins 
				case 30: // ilins 
				case 31: // ilins 
	//#ifdef L9DEBUG 
	//				// printf("scan: illegal instruction"); 
	//#endif
					Valid=FALSE;
					break;
			}
		if (Valid && (Code & ~ScanCodeMask))
			Strange++;
		} while (Valid && !Finished && Pos<FileSize); // && Strange==0); 
		(*Size)+=Pos-iPos;
		return Valid; // && Strange==0; 
	}
	*/
	boolean ValidateSequence(byte[] Image,int iPos,int acode,ScanData sdat,boolean Rts, boolean checkDriverV4)
	{

		//byte Base[]=l9memory;
		boolean Finished=false,Valid;
		int Strange=0;
		int Code;
		sdat.JumpKill=false;
		
		PosScanCodeMask pscm=new PosScanCodeMask();
	
		if (iPos>=filesize)
			return false;
		pscm.Pos=iPos;
		if (pscm.Pos<sdat.Min) sdat.Min=pscm.Pos;
	
		if (Image[pscm.Pos]!=0) return true; // hit valid code 
	
		do
		{
			Code=l9memory[pscm.Pos]&0xff;
			Valid=true;
			if (Image[pscm.Pos]!=0) break; // converged to found code 
			Image[pscm.Pos++]=2;
			if (pscm.Pos>sdat.Max) sdat.Max=pscm.Pos;
	
			pscm.ScanCodeMask=0x9f;
			if ((Code&0x80)!=0)
			{
				pscm.ScanCodeMask=0xff;
				if ((Code&0x1f)>0xa)
					Valid=false;
				pscm.Pos+=2;
			}
			else switch (Code & 0x1f)
			{
				case 0: // goto 
				{
					int Val=scangetaddr(Code,pscm,acode);
					Valid=ValidateSequence(Image,Val,acode,sdat,true,checkDriverV4);
					Finished=true;
					break;
				}
				case 1: // intgosub 
				{
					int Val=scangetaddr(Code,pscm,acode);
					Valid=ValidateSequence(Image,Val,acode,sdat,true,checkDriverV4);
					break;
				}
				case 2: // intreturn 
					Valid=Rts;
					Finished=true;
					break;
				case 3: // printnumber
					pscm.Pos++;
					break;
				case 4: // messagev 
					pscm.Pos++;
					break;
				case 5: // messagec 
					scangetcon(Code,pscm);
					break;
				case 6: // function 
					switch ((int)(l9memory[pscm.Pos++]&0xff))
					{
						case 2:// random 
							pscm.Pos++;
							break;
						case 1:// calldriver
							if (checkDriverV4) {
								if (CheckCallDriverV4(pscm.Pos-2))
									sdat.DriverV4 = true;
							}
							break;
						case 3:// save 
						case 4:// restore 
						case 5:// clearworkspace 
						case 6:// clear stack 
							break;
						case 250: // printstr 
							while (l9memory[pscm.Pos++]!=0);
							break;
	
						default:
							L9DEBUG("scan: illegal function call: %d\r",l9memory[pscm.Pos-1]);
							Valid=false;
							break;
					}
					break;
				case 7: // input 
					pscm.Pos+=4;
					break;
				case 8: // varcon 
					scangetcon(Code,pscm);
					pscm.Pos++;
					break;
				case 9: // varvar
					pscm.Pos+=2;
					break;
				case 10: // _add 
					pscm.Pos+=2;
					break;
				case 11: // _sub 
					pscm.Pos+=2;
					break;
				case 14: // jump 
					L9DEBUG("jmp at codestart: %ld",acode);
					sdat.JumpKill=true;
					Finished=true;
					break;
				case 15: // exit 
					pscm.Pos+=4;
					break;
				case 16: // ifeqvt 
				case 17: // ifnevt 
				case 18: // ifltvt 
				case 19: // ifgtvt 
				{
					int Val;
					pscm.Pos+=2;
					Val=scangetaddr(Code,pscm,acode);
					Valid=ValidateSequence(Image,Val,acode,sdat,Rts,checkDriverV4);
					break;
				}
				case 20: // screen 
					if (l9memory[pscm.Pos++]!=0) pscm.Pos++;
					break;
				case 21: // cleartg 
					pscm.Pos++;
					break;
				case 22: // picture 
					pscm.Pos++;
					break;
				case 23: // getnextobject 
					pscm.Pos+=6;
					break;
				case 24: // ifeqct 
				case 25: // ifnect 
				case 26: // ifltct 
				case 27: // ifgtct 
				{
					int Val;
					pscm.Pos++;
					scangetcon(Code,pscm);
					Val=scangetaddr(Code,pscm,acode);
					Valid=ValidateSequence(Image,Val,acode,sdat,Rts,checkDriverV4);
					break;
				}
				case 28: // printinput 
					break;
				case 12: // ilins 
				case 13: // ilins 
				case 29: // ilins 
				case 30: // ilins 
				case 31: // ilins 
					L9DEBUG("scan: illegal instruction\r");
					Valid=false;
					break;
			}
		if (Valid && ((Code & ~pscm.ScanCodeMask)!=0))
			Strange++;
		} while (Valid && !Finished && pscm.Pos<filesize); // && Strange==0); 
		(sdat.Size)+=pscm.Pos-iPos;
		return Valid; // && Strange==0; 
	}
	
	/*--was-- L9UINT32 scangetaddr(int Code,L9BYTE *Base,L9UINT32 *Pos,L9UINT32 acode,int *Mask)
		{
			(*Mask)|=0x20;
			if (Code&0x20)
			{
				// getaddrshort 
				signed char diff=Base[*Pos];
				(*Pos)++;
				return (*Pos)+diff-1;
			}
			else
			{
				return acode+scanmovewa5d0(Base,Pos);
			}
		}
		*/
	int scangetaddr(int Code,PosScanCodeMask dat,int acode)
	{
		(dat.ScanCodeMask)|=0x20;
		if ((Code&0x20)!=0)
		{
			// getaddrshort 
			byte diff=l9memory[dat.Pos];
			(dat.Pos)++;
			return (dat.Pos)+diff-1;
		}
		else
		{
			return acode+scanmovewa5d0(dat);
		}
	}
	
	/*--was-- L9UINT16 scanmovewa5d0(L9BYTE* Base,L9UINT32 *Pos)
	{
		L9UINT16 ret=L9WORD(Base+*Pos);
		(*Pos)+=2;
		return ret;
	}*/
	int scanmovewa5d0(PosScanCodeMask dat)
	{
		int ret=L9WORD(dat.Pos);
		(dat.Pos)+=2;
		return ret;
	}
	
	/*--was-- void scangetcon(int Code,L9UINT32 *Pos,int *Mask)
	{
		(*Pos)++;
		if (!(Code & 64)) (*Pos)++;
		(*Mask)|=0x40;
	}
	*/
	void scangetcon(int Code,PosScanCodeMask dat)
	{
		(dat.Pos)++;
		if (!((Code & 64)!=0)) (dat.Pos)++;
		(dat.ScanCodeMask)|=0x40;
	}
		
	/*--was-- L9BOOL CheckCallDriverV4(L9BYTE* Base,L9UINT32 Pos)
		{
			int i,j;
		
			// Look back for an assignment from a variable
			// to list9[0], which is used to specify the
			// driver call.
			//
			for (i = 0; i < 2; i++)
			{
				int x = Pos - ((i+1)*3);
				if ((Base[x] == 0x89) && (Base[x+1] == 0x00))
				{
					// Get the variable being copied to list9[0] 
					int var = Base[x+2];
		
					// Look back for an assignment to the variable. 
					for (j = 0; j < 2; j++)
					{
						int y = x - ((j+1)*3);
						if ((Base[y] == 0x48) && (Base[y+2] == var))
						{
							// If this a V4 driver call? 
							switch (Base[y+1])
							{
							case 0x0E:
							case 0x20:
							case 0x22:
								return TRUE;
							}
							return FALSE;
						}
					}
				}
			}
			return FALSE;
		}
	 */
	boolean CheckCallDriverV4(int Pos)
	{
		int i,j;
	
		// Look back for an assignment from a variable
		// to list9[0], which is used to specify the
		// driver call.
		//
		for (i = 0; i < 2; i++)
		{
			int x = Pos - ((i+1)*3);
			if ((l9memory[x] == 0x89) && (l9memory[x+1] == 0x00))
			{
				// Get the variable being copied to list9[0] 
				int var = l9memory[x+2];
	
				// Look back for an assignment to the variable. 
				for (j = 0; j < 2; j++)
				{
					int y = x - ((j+1)*3);
					if ((l9memory[y] == 0x48) && (l9memory[y+2] == var))
					{
						// If this a V4 driver call? 
						switch (l9memory[y+1]&0xff)
						{
						case 0x0E:
						case 0x20:
						case 0x22:
							return true;
						}
						return false;
					}
				}
			}
		}
		return false;
	}

	/*--was-- long ScanV2(L9BYTE* StartFile,L9UINT32 FileSize)
	{
		L9BYTE *Chk=malloc(FileSize+1);
		L9BYTE *Image=calloc(FileSize,1);
		L9UINT32 i,Size,MaxSize=0,num;
		int j;
		L9UINT16 d0=0,l9;
		L9UINT32 Min,Max;
		long Offset=-1;
		L9BOOL JumpKill;
	
		if ((Chk==NULL)||(Image==NULL))
		{
			fprintf(stderr,"Unable to allocate memory for game scan! Exiting...\n");
			exit(0);
		}
	
		Chk[0]=0;
		for (i=1;i<=FileSize;i++)
			Chk[i]=Chk[i-1]+StartFile[i-1];
	
		for (i=0;i<FileSize-28;i++)
		{
			num=L9WORD(StartFile+i+28)+1;
			if (i+num<=FileSize && ((Chk[i+num]-Chk[i+32])&0xff)==StartFile[i+0x1e])
			{
				for (j=0;j<14;j++)
				{
					 d0=L9WORD (StartFile+i+ j*2);
					 if (j!=13 && d0>=0x8000 && d0<0x9000)
					 {
						if (d0>=0x8000+LISTAREASIZE) break;
					 }
					 else if (i+d0>FileSize) break;
				}
				// list9 ptr must be in listarea, acode ptr in data 
				//if (j<14 || (d0>=0x8000 && d0<0x9000)) continue;
				if (j<14) continue;
	
				l9=L9WORD(StartFile+i+6 + 9*2);
				if (l9<0x8000 || l9>=0x8000+LISTAREASIZE) continue;
	
				Size=0;
				Min=Max=i+d0;
				if (ValidateSequence(StartFile,Image,i+d0,i+d0,&Size,FileSize,&Min,&Max,FALSE,&JumpKill,NULL))
				{
	#ifdef L9DEBUG 
					printf("Found valid V2 header at %ld, code size %ld",i,Size);
	#endif
					if (Size>MaxSize)
					{
						Offset=i;
						MaxSize=Size;
					}
				}
			}
		}
		free(Chk);
		free(Image);
		return Offset;
	}
*/
	int ScanV2()
	{
		//L9BYTE *Chk=malloc(FileSize+1);
		byte Chk[] = new byte[filesize+1];
		//L9BYTE *Image=calloc(FileSize,1);
		byte Image[] = new byte[filesize];
		
		int i,MaxSize=0,num;
		int j;
		int d0=0,l9;
		//int Min,Max,Size;
		//boolean JumpKill;
		int Offset=-1;
	
		ScanData scandata=new ScanData();
		
		/* TODO: Есть ли шанс, что массивы Chk и Image не создадутся?
		if ((Chk==NULL)||(Image==NULL))
		{
			fprintf(stderr,"Unable to allocate memory for game scan! Exiting...\n");
			exit(0);
		}*/
	
		Chk[0]=0;
		for (i=1;i<=filesize;i++)
			//Chk[i]=Chk[i-1]+StartFile[i-1];
			Chk[i]=(byte)(((Chk[i-1]&0xff)+(l9memory[i-1]&0xff))&0xff);
	
		//BUGFIXbyTSAP, possible out of array on L9WORD - Filesize-28+28=Filesize
		for (i=0;i<filesize-28-1;i++)
		{
			num=L9WORD(i+28)+1;
			if (i+num<=filesize && (((Chk[i+num]&0xff)-(Chk[i+32]&0xff))&0xff)==(l9memory[i+0x1e]&0xff))
			{
				for (j=0;j<14;j++)
				{
					 d0=L9WORD (i+ j*2);
					 if (j!=13 && d0>=0x8000 && d0<0x9000)
					 {
						if (d0>=0x8000+LISTAREASIZE) break;
					 }
					 else if (i+d0>filesize) break;
				}
				// list9 ptr must be in listarea, acode ptr in data 
				//if (j<14 || (d0>=0x8000 && d0<0x9000)) continue;
				if (j<14) continue;
	
				l9=L9WORD(i+6 + 9*2);
				if (l9<0x8000 || l9>=0x8000+LISTAREASIZE) continue;
	
				scandata.Size=0;
				scandata.Min=scandata.Max=i+d0;
				if (ValidateSequence(Image,i+d0,i+d0,scandata,false,false))
				{
					L9DEBUG("Found valid V2 header at %d, code size %d\r",i,scandata.Size);
					if (scandata.Size>MaxSize)
					{
						Offset=i;
						MaxSize=scandata.Size;
					}
				}
			}
		}
		return Offset;
	}
	
	/*--was-- long ScanV1(L9BYTE* StartFile,L9UINT32 FileSize)
	{
		return -1;
	
//		L9BYTE *Image=calloc(FileSize,1);
//		L9UINT32 i,Size;
//		int Replace;
//		L9BYTE* ImagePtr;
//		long MaxPos=-1;
//		L9UINT32 MaxCount=0;
//		L9UINT32 Min,Max,MaxMin,MaxMax;
//		L9BOOL JumpKill,MaxJK;
//	
//		L9BYTE c;
//		int maxdict,maxdictlen;
//		L9BYTE *ptr,*start;
//	
//		for (i=0;i<FileSize;i++)
//		{
//			Size=0;
//			Min=Max=i;
//			Replace=0;
//			if (ValidateSequence(StartFile,Image,i,i,&Size,FileSize,&Min,&Max,FALSE,&JumpKill,NULL))
//			{
//				if (Size>MaxCount)
//				{
//					MaxCount=Size;
//					MaxMin=Min;
//					MaxMax=Max;
//	
//					MaxPos=i;
//					MaxJK=JumpKill;
//				}
//				Replace=0;
//			}
//			for (ImagePtr=Image+Min;ImagePtr<=Image+Max;ImagePtr++) if (*ImagePtr==2) *ImagePtr=Replace;
//		}
//	#ifdef L9DEBUG
//		printf("V1scan found code at %ld size %ld",MaxPos,MaxCount);
//	#endif
//	
//		ptr=StartFile;
//		maxdictlen=0;
//		do
//		{
//			start=ptr;
//			do
//			{
//				do
//				{
//					c=*ptr++;
//				} while (((c>='A' && c<='Z') || c=='-') && ptr<StartFile+FileSize);
//				if (c<0x7f || (((c&0x7f)<'A' || (c&0x7f)>'Z') && (c&0x7f)!='/')) break;
//				ptr++;
//			} while (TRUE);
//			if (ptr-start-1>maxdictlen)
//			{
//				maxdict=start-StartFile;
//				maxdictlen=ptr-start-1;
//			}
//		} while (ptr<StartFile+FileSize);
//	#ifdef L9DEBUG
//		if (maxdictlen>0) printf("V1scan found dictionary at %ld size %ld",maxdict,maxdictlen);
//	#endif
//	
//		MaxPos=-1;
//	
//		free(Image);
//		return MaxPos;
//	
	}*/
	//TODO: в оригинале - большой закоментированный блок. вернуть?
	int ScanV1() {
		return -1;
	}
	
	/*--was--	L9BOOL load(char *filename)
	{
		FILE *f=fopen(filename,"rb");
		if (!f) return FALSE;
		FileSize=filelength(f);
		L9Allocate(&startfile,FileSize);

		if (fread(startfile,1,FileSize,f)!=FileSize)
		{
			fclose(f);
			return FALSE;
		}
	 	fclose(f);
		return TRUE;
	}
	*/
	boolean load(String filename)
	{
		byte filedata[]=os_load(filename);
		if (filedata==null) return false;
		filesize=filedata.length;
		l9memory=new byte[filesize+LISTAREASIZE];
		listarea=filesize;
		startfile=0;
		for (int i=0;i<filesize;i++) l9memory[startfile+i]=filedata[i];
		return true;
	}
	
	/*--was--	void error(char *fmt,...)
	{
		char buf[256];
		int i;
		va_list ap;
		va_start(ap,fmt);
		vsprintf(buf,fmt,ap);
		va_end(ap);
		for (i=0;i< (int) strlen(buf);i++) os_printchar(buf[i]);
	}
	*/
	//TODO: error*3 - Изврат
	//TODO: может error выводить инфу во всплывающем окне?
	void error(String txt) {
		for (int i=0;i<txt.length();i++) os_printchar(txt.charAt(i));
	}
	
	void error(String txt1, String txt2) {
		String str=String.format(txt1, txt2);
		for (int i=0;i<str.length();i++) os_printchar(str.charAt(i));
	}

	void error(String txt, int val) {
		String str=String.format(txt, val);
		for (int i=0;i<str.length();i++) os_printchar(str.charAt(i));
	}
	
	void error(String txt, int val1, int val2) {
		String str=String.format(txt, val1, val2);
		for (int i=0;i<str.length();i++) os_printchar(str.charAt(i));
	}

	/*--was--	void printchar(char c)
	{
		if (Cheating) return;

		if (c&128) lastchar=(c&=0x7f);
		else if (c!=0x20 && c!=0x0d && (c<'\"' || c>='.'))
		{
			if (lastchar=='!' || lastchar=='?' || lastchar=='.') c=toupper(c);
			lastchar=c;
		}
		// eat multiple CRs
		if (c!=0x0d || lastactualchar!=0x0d) os_printchar(c);
		lastactualchar=c;
	}
	*/
	void printchar(char c)
	{
		if (Cheating) return;

		//if <128, Upper case after ".", "!", "?"
		if ((c&128)!=0) lastchar=(c&=0x7f);
		else if (c!=0x20 && c!=0x0d && (c<'\"' || c>='.'))
		{
			if (lastchar=='!' || lastchar=='?' || lastchar=='.') c=toupper(c);
			lastchar=c;
		}
		// eat multiple CRs
		if (c!=0x0d || lastactualchar!=0x0d) os_printchar(c);
		lastactualchar=c;
	}
 
	/*--was--	void printstring(char*buf)
	{
		int i;
		for (i=0;i< (int) strlen(buf);i++) printchar(buf[i]);
	}
	*/
	void printstring(String str)
	{
		for (int i=0;i<str.length();i++) printchar(str.charAt(i));
	}
	
	void printstringb(int ptr) {
		char c;
		while((c=(char)l9memory[ptr++])!=0) {
			printchar(c);
		}
	}
	
	/*--was--	void printdecimald0(int d0)
	{
		char temp[12];
		sprintf(temp,"%d",d0);
		printstring(temp);
	}
	*/
	void printdecimald0(int d0)
	{
		printstring(String.valueOf(d0));
	}

	/*--was--	void printautocase(int d0)
	{
		if (d0 & 128) printchar((char) d0);
		else
		{
			if (wordcase) printchar((char) toupper(d0));
			else if (d5<6) printchar((char) d0);
			else
			{
				wordcase=0;
				printchar((char) toupper(d0));
			}
		}
	}
	*/
	void printautocase(int d0)
	{
		if ((d0 & 128)!=0) printchar((char) d0);
		else
		{
			//TODO: check wordcase!=0
			if (wordcase!=0) printchar(toupper((char)d0));
			else if (d5<6) printchar((char) d0);
			else
			{
				wordcase=0;
				printchar(toupper((char)d0));
			}
		}
	}
	
	//#define L9WORD(x) (*(x) + ((*(x+1))<<8))
	//!! возвращаю int, чтобы не заморачиваться с +/-
	int L9WORD(int x) {
		return (l9memory[x]&255)+((l9memory[x+1]&255)<<8); //&255 для конверсии в int без учета знака.
	}
	
	//#define L9SETWORD(x,val) *(x)=(L9BYTE) val; *(x+1)=(L9BYTE)(val>>8);
	void L9SETWORD(int x, int val) {
		l9memory[x]=(byte)(val & 0xff);
		l9memory[x+1]=(byte)((val & 0xff00)>>8);
	}
	
	//#define L9SETDWORD(x,val) *(x)=(L9BYTE)val; *(x+1)=(L9BYTE)(val>>8); *(x+2)=(L9BYTE)(val>>16); *(x+3)=(L9BYTE)(val>>24);
	void L9SETDWORD(int x, int val) {
		l9memory[x]=(byte)(val & 0xff);
		l9memory[x+1]=(byte)((val & 0xff00)>>8);
		l9memory[x+2]=(byte)((val & 0xff0000)>>16);
		l9memory[x+3]=(byte)((val & 0xff000000)>>24);
	}
	
/*--was--	void executeinstruction(void)
	{
	#ifdef CODEFOLLOW
		f=fopen(CODEFOLLOWFILE,"a");
		fprintf(f,"%ld (s:%d) %x",(L9UINT32) (codeptr-acodeptr)-1,workspace.stackptr,code);
		if (!(code&0x80))
			fprintf(f," = %s",codes[code&0x1f]);
	#endif

		if (code & 0x80) listhandler();
		else switch (code & 0x1f)
		{
			case 0:		Goto();break;
			case 1: 	intgosub();break;
			case 2:		intreturn();break;
			case 3:		printnumber();break;
			case 4:		messagev();break;
			case 5:		messagec();break;
			case 6:		function();break;
			case 7:		input();break;
			case 8:		varcon();break;
			case 9:		varvar();break;
			case 10:	_add();break;
			case 11:	_sub();break;
			case 12:	ilins(code & 0x1f);break;
			case 13:	ilins(code & 0x1f);break;
			case 14:	jump();break;
			case 15:	Exit();break;
			case 16:	ifeqvt();break;
			case 17:	ifnevt();break;
			case 18:	ifltvt();break;
			case 19:	ifgtvt();break;
			case 20:	_screen();break;
			case 21:	cleartg();break;
			case 22:	picture();break;
			case 23:	getnextobject();break;
			case 24:	ifeqct();break;
			case 25:	ifnect();break;
			case 26:	ifltct();break;
			case 27:	ifgtct();break;
			case 28:	printinput();break;
			case 29:	ilins(code & 0x1f);break;
			case 30:	ilins(code & 0x1f);break;
			case 31:	ilins(code & 0x1f);break;
		}
	#ifdef CODEFOLLOW
		fprintf(f,"\n");
		fclose(f);
	#endif
	}
*/
	void executeinstruction()
	{
//	#ifdef CODEFOLLOW
//		f=fopen(CODEFOLLOWFILE,"a");
//		fprintf(f,"%ld (s:%d) %x",(L9UINT32) (codeptr-acodeptr)-1,workspace.stackptr,code);
//		if (!(code&0x80))
//			fprintf(f," = %s",codes[code&0x1f]);
//	#endif

		if ((code & 0x80)!=0) listhandler();
		else switch (code & 0x1f)
		{
			case 0:		Goto();break;
			case 1: 	intgosub();break;
			case 2:		intreturn();break;
			case 3:		printnumber();break;
			case 4:		messagev();break;
			case 5:		messagec();break;
			case 6:		function();break;
			case 7:		input();break;
			case 8:		varcon();break;
			case 9:		varvar();break;
			case 10:	_add();break;
			case 11:	_sub();break;
			case 12:	ilins(code & 0x1f);break;
			case 13:	ilins(code & 0x1f);break;
			case 14:	jump();break;
			case 15:	Exit();break;
			case 16:	ifeqvt();break;
			case 17:	ifnevt();break;
			case 18:	ifltvt();break;
			case 19:	ifgtvt();break;
			case 20:	_screen();break;
			case 21:	cleartg();break;
			case 22:	picture();break;
			case 23:	getnextobject();break;
			case 24:	ifeqct();break;
			case 25:	ifnect();break;
			case 26:	ifltct();break;
			case 27:	ifgtct();break;
			case 28:	printinput();break;
			case 29:	ilins(code & 0x1f);break;
			case 30:	ilins(code & 0x1f);break;
			case 31:	ilins(code & 0x1f);break;
		}
	}

	/*--was--	void listhandler(void)
	{
		L9BYTE *a4,*MinAccess,*MaxAccess;
		L9UINT16 val;
		L9UINT16 *var;
	#ifdef CODEFOLLOW
		int offset; 
	#endif

		if ((code&0x1f)>0xa)
		{
			error("\rillegal list access %d\r",code&0x1f);
			Running=FALSE;
			return;
		}
		a4=L9Pointers[1+code&0x1f];

		if (a4>=workspace.listarea && a4<workspace.listarea+LISTAREASIZE)
		{
			MinAccess=workspace.listarea;
			MaxAccess=workspace.listarea+LISTAREASIZE;
		}
		else
		{
			MinAccess=startdata;
			MaxAccess=startdata+FileSize;
		}

		if (code>=0xe0)
		{
			// listvv 
	#ifndef CODEFOLLOW
			a4+=*getvar();
			val=*getvar();
	#else
			offset=*getvar();
			a4+=offset;
			var=getvar();
			val=*var;
			fprintf(f," list %d [%d]=Var[%d] (=%d)",code&0x1f,offset,var-workspace.vartable,val);
	#endif

			if (a4>=MinAccess && a4<MaxAccess) *a4=(L9BYTE) val;
			#ifdef L9DEBUG
			else printf("Out of range list access");
			#endif
		}
		else if (code>=0xc0)
		{
			// listv1c 
	#ifndef CODEFOLLOW
			a4+=*codeptr++;
			var=getvar();
	#else
			offset=*codeptr++;
			a4+=offset;
			var=getvar();
			fprintf(f," Var[%d]= list %d [%d])",var-workspace.vartable,code&0x1f,offset);
			if (a4>=MinAccess && a4<MaxAccess) fprintf(f," (=%d)",*a4);
	#endif

			if (a4>=MinAccess && a4<MaxAccess) *var=*a4;
			else
			{
				*var=0;
				#ifdef L9DEBUG
				printf("Out of range list access");
				#endif
			}
		}
		else if (code>=0xa0)
		{
			// listv1v 
	#ifndef CODEFOLLOW
			a4+=*getvar();
			var=getvar();
	#else
			offset=*getvar();
			a4+=offset;
			var=getvar();

			fprintf(f," Var[%d] =list %d [%d]",var-workspace.vartable,code&0x1f,offset);
			if (a4>=MinAccess && a4<MaxAccess) fprintf(f," (=%d)",*a4);
	#endif

			if (a4>=MinAccess && a4<MaxAccess) *var=*a4;
			else
			{
				*var=0;
				#ifdef L9DEBUG
				printf("Out of range list access");
				#endif
			}
		}
		else
		{
	#ifndef CODEFOLLOW
			a4+=*codeptr++;
			val=*getvar();
	#else
			offset=*codeptr++;
			a4+=offset;
			var=getvar();
			val=*var;
			fprintf(f," list %d [%d]=Var[%d] (=%d)",code&0x1f,offset,var-workspace.vartable,val);
	#endif

			if (a4>=MinAccess && a4<MaxAccess) *a4=(L9BYTE) val;
			#ifdef L9DEBUG
			else printf("Out of range list access");
			#endif
		}
	}*/
	void listhandler()
	{
		int a4, MinAccess, MaxAccess;
		short val;
		int var;
//	#ifdef CODEFOLLOW
//		int offset; 
//	#endif

		if ((code&0x1f)>0xa) {
			error("\rillegal list access %d\r",code&0x1f);
			Running=false;
			return;
		}
		a4=L9Pointers[1+code&0x1f];

		if (a4>=listarea && a4<listarea+LISTAREASIZE) {
			MinAccess=listarea;
			MaxAccess=listarea+LISTAREASIZE;
		}
		else {
			MinAccess=startdata;
			MaxAccess=startdata+datasize;
		}

		if (code>=0xe0)			// listvv 
		{

//	#ifndef CODEFOLLOW
			a4+=workspace.vartable[getvar()];
			val=workspace.vartable[getvar()];
//	#else
//			offset=*getvar();
//			a4+=offset;
//			var=getvar();
//			val=*var;
//			fprintf(f," list %d [%d]=Var[%d] (=%d)",code&0x1f,offset,var-workspace.vartable,val);
//	#endif

			if (a4>=MinAccess && a4<MaxAccess) l9memory[a4]=(byte)(val&0xff);
			L9DEBUG("Out of range list access");
		}
		else if (code>=0xc0) 	// listv1c 
		{

//	#ifndef CODEFOLLOW
			a4+=l9memory[codeptr++];
			var=getvar();
//	#else
//			offset=*codeptr++;
//			a4+=offset;
//			var=getvar();
//			fprintf(f," Var[%d]= list %d [%d])",var-workspace.vartable,code&0x1f,offset);
//			if (a4>=MinAccess && a4<MaxAccess) fprintf(f," (=%d)",*a4);
//	#endif

			if (a4>=MinAccess && a4<MaxAccess) workspace.vartable[var]=l9memory[a4];
			else
			{
				workspace.vartable[var]=0;
				L9DEBUG("Out of range list access");

			}
		}
		else if (code>=0xa0)	// listv1v 
		{
//	#ifndef CODEFOLLOW
			a4+=workspace.vartable[getvar()];
			var=getvar();
//	#else
//			offset=*getvar();
//			a4+=offset;
//			var=getvar();
//
//			fprintf(f," Var[%d] =list %d [%d]",var-workspace.vartable,code&0x1f,offset);
//			if (a4>=MinAccess && a4<MaxAccess) fprintf(f," (=%d)",*a4);
//	#endif

			if (a4>=MinAccess && a4<MaxAccess) workspace.vartable[var]=l9memory[a4];
			else
			{
				workspace.vartable[var]=0;
				L9DEBUG("Out of range list access");
			}
		}
		else
		{
//	#ifndef CODEFOLLOW
			a4+=l9memory[codeptr++];
			val=workspace.vartable[getvar()];
//	#else
//			offset=*codeptr++;
//			a4+=offset;
//			var=getvar();
//			val=*var;
//			fprintf(f," list %d [%d]=Var[%d] (=%d)",code&0x1f,offset,var-workspace.vartable,val);
//	#endif

			if (a4>=MinAccess && a4<MaxAccess) l9memory[a4]=(byte) (val&0xff);
			L9DEBUG("Out of range list access");
		}
	}

	/*--was-- void ilins(int d0)
		{
			error("\rIllegal instruction: %d\r",d0);
			Running=FALSE;
		}*/
	void ilins(int d0)
	{
		error("\rIllegal instruction: %d\r",d0);
		Running=false;
	}

	/*--was-- L9UINT16 *getvar(void)
	{
	#ifndef CODEFOLLOW
		return workspace.vartable + *codeptr++;
	#else
		cfvar2=cfvar;
		return cfvar=workspace.vartable + *codeptr++;
	#endif
	}*/
	int getvar() {
//	#ifndef CODEFOLLOW
		return l9memory[codeptr++];
//	#else
//		cfvar2=cfvar;
//		return cfvar=workspace.vartable + *codeptr++;
//	#endif
	}
	
	/*--was--	L9BYTE* getaddr(void)
	{
		if (code&0x20)
		{
			//getaddrshort
			signed char diff=*codeptr++;
			return codeptr+ diff-1;
		}
		else
		{
			return acodeptr+movewa5d0();
		}
	}*/
	int getaddr()
	{
		if ((code&0x20)!=0)
		{
			//diff-signed!
			byte diff=l9memory[codeptr++];
			return codeptr+ diff-1;
		}
		else
		{
			return acodeptr+movewa5d0();
		}
	}
	
	/*--was--	L9UINT16 movewa5d0(void)
	{
		L9UINT16 ret=L9WORD(codeptr);
		codeptr+=2;
		return ret;
	}*/
	int movewa5d0()
	{
		int ret=L9WORD(codeptr);
		codeptr+=2;
		return ret;
	}

	/*--was-- void Goto(void)
	{
		codeptr=getaddr();
	}*/
	void Goto() {
		codeptr=getaddr();
	}
	
	/*--was--	void intgosub(void)
	{
		L9BYTE* newcodeptr=getaddr();
		if (workspace.stackptr==STACKSIZE)
		{
			error("\rStack overflow error\r");
			Running=FALSE;
			return;
		}
		workspace.stack[workspace.stackptr++]=(L9UINT16) (codeptr-acodeptr);
		codeptr=newcodeptr;
	}*/
	void intgosub() {
		int newcodeptr=getaddr();
		if (workspace.stackptr==STACKSIZE)
		{
			error("\rStack overflow error\r");
			Running=false;
			return;
		}
		workspace.stack[workspace.stackptr++]=(short)((codeptr-acodeptr)&0xffff);
		codeptr=newcodeptr;
	}
	
	/*--was--	void intreturn(void)
	{
		if (workspace.stackptr==0)
		{
			error("\rStack underflow error\r");
			Running=FALSE;
			return;
		}
		codeptr=acodeptr+workspace.stack[--workspace.stackptr];
	}*/
	void intreturn()
	{
		if (workspace.stackptr==0)
		{
			error("\rStack underflow error\r");
			Running=false;
			return;
		}
		codeptr=acodeptr+workspace.stack[--workspace.stackptr];
	}
	
	/*--was--	void printnumber(void)
	{
		printdecimald0(*getvar());
	}*/
	void printnumber()
	{
		printdecimald0(workspace.vartable[getvar()]);
	}
	
	/*--was--	L9UINT16 getcon(void)
	{
		if (code & 64)
		{
			// getconsmall
			return *codeptr++;
		}
		else return movewa5d0();
	}*/
	int getcon()
	{
		if ((code & 64)!=0)
		{
			// getconsmall 
			return codeptr++;
		}
		else return movewa5d0();
	}

	/*--was--	void messagec(void)
	{
		if (L9GameType==L9_V2)
			printmessageV2(getcon());
		else
			printmessage(getcon());
	}*/
	void messagec()
	{
		if (L9GameType==L9_V2)
			printmessageV2(getcon());
		else
			printmessage(getcon());
	}

	/*--was--	void messagev(void)
	{
		if (L9GameType==L9_V2)
			printmessageV2(*getvar());
		else
			printmessage(*getvar());
	}*/
	void messagev()
	{
		if (L9GameType==L9_V2)
			printmessageV2(workspace.vartable[getvar()]);
		else
			printmessage(workspace.vartable[getvar()]);
	}
	
	
	/*--was--	void displaywordref(L9UINT16 Off)
	{
		static int mdtmode=0;

		wordcase=0;
		d5=(Off>>12)&7;
		Off&=0xfff;
		if (Off<0xf80)
		{
		// dwr01 
			L9BYTE *a0,*oPtr,*a3;
			int d0,d2,i;

			if (mdtmode==1) printchar(0x20);
			mdtmode=1;

			// setindex 
			a0=dictdata;
			d2=dictdatalen;

		// dwr02 
			oPtr=a0;
			while (d2 && Off >= L9WORD(a0+2))
			{
				a0+=4;
				d2--;
			}
		// dwr04 
			if (a0==oPtr)
			{
				a0=defdict;
			}
			else
			{
				a0-=4;
				Off-=L9WORD(a0+2);
				a0=startdata+L9WORD(a0);
			}
		// dwr04b
			Off++;
			initdict(a0);
			a3=(L9BYTE*) threechars; // a3 not set in original, prevent possible spam 

			// dwr05 
			while (TRUE)
			{
				d0=getdictionarycode();
				if (d0<0x1c)
				{
					// dwr06 
					if (d0>=0x1a) d0=getlongcode();
					else d0+=0x61;
					*a3++=d0;
				}
				else
				{
					d0&=3;
					a3=(L9BYTE*) threechars+d0;
					if (--Off==0) break;
				}
			}
			for (i=0;i<d0;i++) printautocase(threechars[i]);

			// dwr10 
			while (TRUE)
			{
				d0=getdictionarycode();
				if (d0>=0x1b) return;
				printautocase(getdictionary(d0));
			}
		}

		else
		{
			if (d5&2) printchar(0x20); // prespace 
			mdtmode=2;
			Off&=0x7f;
			if (Off!=0x7e) printchar((char)Off);
			if (d5&1) printchar(0x20); // postspace
		}
	}*/
	void displaywordref(int Off)
	{
		//static int mdtmode=0;
		//	int displaywordref_mdtmode=0;

		wordcase=0;
		d5=(Off>>12)&7;
		Off&=0xfff;
		if (Off<0xf80)
		{
		// dwr01 
			int a0,oPtr,a3;
			int d0,d2,i;

			if (displaywordref_mdtmode==1) printchar(' ');
			displaywordref_mdtmode=1;

			// setindex 
			a0=dictdata;
			d2=dictdatalen;

		// dwr02 
			oPtr=a0;
			while (d2!=0 && Off >= L9WORD(a0+2))
			{
				a0+=4;
				d2--;
			}
		// dwr04 
			if (a0==oPtr)
			{
				a0=defdict;
			}
			else
			{
				a0-=4;
				Off-=L9WORD(a0+2);
				a0=startdata+L9WORD(a0);
			}
		// dwr04b
			Off++;
			initdict(a0);
			a3=0; // a3 not set in original, prevent possible spam 

			// dwr05 
			while (true)
			{
				d0=getdictionarycode();
				if (d0<0x1c)
				{
					// dwr06 
					if (d0>=0x1a) d0=getlongcode();
					else d0+=0x61;
					threechars[a3++]=(byte)(d0&0xff);
				}
				else
				{
					d0&=3;
					a3=d0;
					if (--Off==0) break;
				}
			}
			for (i=0;i<d0;i++) printautocase(threechars[i]);

			// dwr10 
			while (true)
			{
				d0=getdictionarycode();
				if (d0>=0x1b) return;
				printautocase(getdictionary(d0));
			}
		}

		else
		{
			if ((d5&2)!=0) printchar(' '); // prespace 
			displaywordref_mdtmode=2;
			Off&=0x7f;
			if (Off!=0x7e) printchar((char)Off);
			if ((d5&1)!=0) printchar(' '); // postspace
		}
	}
	
	/*--was--	void initdict(L9BYTE *ptr)
	{
		dictptr=ptr;
		unpackcount=8;
	}*/
	void initdict(int ptr)
	{
		dictptr=ptr;
		unpackcount=8;
	};
	
	/*--was--	char getdictionarycode(void)
	{
		if (unpackcount!=8) return unpackbuf[unpackcount++];
		else
		{
			// unpackbytes 
			L9BYTE d1=*dictptr++,d2;
			unpackbuf[0]=d1>>3;
			d2=*dictptr++;
			unpackbuf[1]=((d2>>6) + (d1<<2)) & 0x1f;
			d1=*dictptr++;
			unpackbuf[2]=(d2>>1) & 0x1f;
			unpackbuf[3]=((d1>>4) + (d2<<4)) & 0x1f;
			d2=*dictptr++;
			unpackbuf[4]=((d1<<1) + (d2>>7)) & 0x1f;
			d1=*dictptr++;
			unpackbuf[5]=(d2>>2) & 0x1f;
			unpackbuf[6]=((d2<<3) + (d1>>5)) & 0x1f;
			unpackbuf[7]=d1 & 0x1f;
			unpackcount=1;
			return unpackbuf[0];
		}
	}*/
	byte getdictionarycode()
	{
		if (unpackcount!=8) return unpackbuf[unpackcount++];
		else
		{
			// unpackbytes 
			byte d1=l9memory[dictptr++],d2;
			unpackbuf[0]=(byte)(d1>>3);
			d2=l9memory[dictptr++];
			unpackbuf[1]=(byte)(((d2>>6) + (d1<<2)) & 0x1f);
			d1=l9memory[dictptr++];
			unpackbuf[2]=(byte)((d2>>1) & 0x1f);
			unpackbuf[3]=(byte)(((d1>>4) + (d2<<4)) & 0x1f);
			d2=l9memory[dictptr++];
			unpackbuf[4]=(byte)(((d1<<1) + (d2>>7)) & 0x1f);
			d1=l9memory[dictptr++];
			unpackbuf[5]=(byte)((d2>>2) & 0x1f);
			unpackbuf[6]=(byte)(((d2<<3) + (d1>>5)) & 0x1f);
			unpackbuf[7]=(byte)(d1 & 0x1f);
			unpackcount=1;
			return unpackbuf[0];
		}
	}

	/*--was--	int getdictionary(int d0)
	{
		if (d0>=0x1a) return getlongcode();
		else return d0+0x61;
	}*/
	int getdictionary(int d0)
	{
		if (d0>=0x1a) return getlongcode();
		else return d0+0x61;
	}

	/*--was--	int getlongcode(void)
	{
		int d0,d1;
		d0=getdictionarycode();
		if (d0==0x10)
		{
			wordcase=1;
			d0=getdictionarycode();
			return getdictionary(d0); // reentrant?
		}
		d1=getdictionarycode();
		return 0x80 | ((d0<<5) & 0xe0) | (d1 & 0x1f);
	}*/
	int getlongcode()
	{
		int d0,d1;
		d0=getdictionarycode();
		if (d0==0x10)
		{
			wordcase=1;
			d0=getdictionarycode();
			return getdictionary(d0); // reentrant?
		}
		d1=getdictionarycode();
		return 0x80 | ((d0<<5) & 0xe0) | (d1 & 0x1f);
	}

	/*--was--	int getmdlength(L9BYTE **Ptr)
	{
		int tot=0,len;
		do
		{
			len=(*(*Ptr)++ -1) & 0x3f;
			tot+=len;
		} while (len==0x3f);
		return tot;
	}*/
	int getmdlength(int Ptr[])
	{
		int tot=0,len;
		do
		{
			//len=(*(*Ptr)++ -1) & 0x3f;
			len=(l9memory[Ptr[0]++]&0xff -1) & 0x3f;
			tot+=len;
		} while (len==0x3f);
		return tot;
	}

	/*--was--void printmessage(int Msg)
	{
		L9BYTE* Msgptr=startmd;
		L9BYTE Data;

		int len,msgtmp;
		L9UINT16 Off;

		while (Msg>0 && Msgptr-endmd<=0)
		{
			Data=*Msgptr;
			if (Data&128)
			{
				Msgptr++;
				Msg-=Data&0x7f;
			}
			else {
				msgtmp=getmdlength(&Msgptr);
				Msgptr+=msgtmp;
			}
			Msg--;
		}
		if (Msg<0 || *Msgptr & 128) return;

		len=getmdlength(&Msgptr);
		if (len==0) return;

		while (len)
		{
			Data=*Msgptr++;
			len--;
			if (Data&128)
			{
			// long form (reverse word)
				Off=(Data<<8) + *Msgptr++;
				len--;
			}
			else
			{
				Off=(wordtable[Data*2]<<8) + wordtable[Data*2+1];
			}
			if (Off==0x8f80) break;
			displaywordref(Off);
		}
	}*/
	void printmessage(int Msg)
	{
		int Msgptr[]={startmd};
		byte Data;

		int len,msgtmp;
		int Off;

		while (Msg>0 && Msgptr[0]-endmd<=0)
		{
			Data=l9memory[Msgptr[0]];
			if ((Data&128)!=0)
			{
				Msgptr[0]++;
				Msg-=Data&0x7f;
			}
			else {
				msgtmp=getmdlength(Msgptr);
				Msgptr[0]+=msgtmp;
			}
			Msg--;
		}
		if (Msg<0 || ((l9memory[Msgptr[0]]&128)!=0)) return;

		len=getmdlength(Msgptr);
		if (len==0) return;

		while (len!=0)
		{
			Data=l9memory[Msgptr[0]++];
			len--;
			if ((Data&128)!=0)
			{
			// long form (reverse word)
				Off=(Data<<8) + l9memory[Msgptr[0]++];
				len--;
			}
			else
			{
				Off=(l9memory[wordtable+Data*2]<<8) + l9memory[wordtable+Data*2+1]&0xff;
			}
			if (Off==0x8f80) break;
			displaywordref(Off);
		}
	}
	
	/*--was--	void printcharV2(char c)
	{
		if (c==0x25) c=0xd;
		else if (c==0x5f) c=0x20;
		printautocase(c);
	}*/
	void printcharV2(int c)
	{
		if (c==0x25) c=0xd;
		else if (c==0x5f) c=0x20;
		printautocase(c);
	}

	/*--was--	void displaywordV2(L9BYTE *ptr,int msg)
	{
		int n;
		L9BYTE a;
		if (msg==0) return;
		while (--msg)
		{
			ptr+=msglenV2(&ptr);
		}
		n=msglenV2(&ptr);

		while (--n>0)
		{
			a=*++ptr;
			if (a<3) return;

			if (a>=0x5e) displaywordV2(startmdV2-1,a-0x5d);
			else printcharV2((char)(a+0x1d));
		}
	}*/
	void displaywordV2(int ptr,int msg)
	{
		int n;
		int a;
		if (msg==0) return;
		while (--msg!=0)
		{
			ptr+=msglenV2(ptr);
		}
		n=msglenV2(ptr);

		while (--n>0)
		{
			a=l9memory[++ptr]&0xff;
			if (a<3) return;

			if (a>=0x5e) displaywordV2(startmdV2-1,a-0x5d);
			else printcharV2(a+0x1d);
		}
	}

	/*--was--	void displaywordV25(L9BYTE *ptr,int msg)
	{
		int n;
		L9BYTE a;
		while (msg--)
		{
			ptr+=msglenV25(&ptr);
		}
		n=msglenV25(&ptr);

		while (--n>0)
		{
			a=*ptr++;
			if (a<3) return;

			if (a>=0x5e) displaywordV25(startmdV2,a-0x5e);
			else printcharV2((char)(a+0x1d));
		}
	}*/
	void displaywordV25(int ptr,int msg)
	{
		int n;
		int a;
		while (msg--!=0)
		{
			ptr+=msglenV25(ptr);
		}
		n=msglenV25(ptr);

		while (--n>0)
		{
			a=l9memory[ptr++]&0xff;
			if (a<3) return;

			if (a>=0x5e) displaywordV25(startmdV2,a-0x5e);
			else printcharV2(a+0x1d);
		}
	}

	/*--was--	void printmessageV2(int Msg)
	{
		if (V2MsgType==V2M_NORMAL) displaywordV2(startmd,Msg);
		else displaywordV25(startmd,Msg);
	}*/
	void printmessageV2(int Msg)
	{
		if (V2MsgType==V2M_NORMAL) displaywordV2(startmd,Msg);
		else displaywordV25(startmd,Msg);
	};
	
	void picture()
	{
		//TODO: show_picture(*getvar());
	}
	
	/*--was--	void _screen(void)
	{
		int mode = 0;

		l9textmode = *codeptr++;
		if (l9textmode)
		{
			if (L9GameType==L9_V4)
				mode = 2;
			else if (picturedata)
				mode = 1;
		}
		os_graphics(mode);

		screencalled = 1;

	#ifdef L9DEBUG
		printf("screen %s",l9textmode ? "graphics" : "text");
	#endif

		if (l9textmode)
		{
			codeptr++;
	// clearg 
	// gintclearg 
			os_cleargraphics();

			// title pic
			if (showtitle==1 && mode==2)
			{
				showtitle = 0;
				os_show_bitmap(0,0,0);
			}
		}
	// screent 
	}*/
	void _screen()
	{
		int mode = 0;

		l9textmode = l9memory[codeptr++];
		if (l9textmode!=0)
		{
			if (L9GameType==L9_V4)
				mode = 2;
			//TODO: else if (picturedata)
			//TODO:	mode = 1;
		}
		os_graphics(mode);

		//TODO: screencalled = 1;

		L9DEBUG ("screen %s",l9textmode!=0 ? "graphics" : "text");


		if (l9textmode!=0)
		{
			codeptr++;
	// clearg 
	// gintclearg 
			os_cleargraphics();

			/* title pic */
			if (showtitle==1 && mode==2)
			{
				showtitle = 0;
				os_show_bitmap(0,0,0);
			}
		}
	// screent 
	}
	
	void varcon()
	{
		int d6=getcon();
		workspace.vartable[getvar()]=(short)d6;

//	#ifdef CODEFOLLOW
//		fprintf(f," Var[%d]=%d)",cfvar-workspace.vartable,*cfvar);
//	#endif
	}

	void varvar()
	{
		int d6=workspace.vartable[getvar()];
		workspace.vartable[getvar()]=(short)d6;

//	#ifdef CODEFOLLOW
//		fprintf(f," Var[%d]=Var[%d] (=%d)",cfvar-workspace.vartable,cfvar2-workspace.vartable,d6);
//	#endif
	}

	void _add()
	{
		int d0=workspace.vartable[getvar()];
		workspace.vartable[getvar()]+=d0;

//	#ifdef CODEFOLLOW
//		fprintf(f," Var[%d]+=Var[%d] (+=%d)",cfvar-workspace.vartable,cfvar2-workspace.vartable,d0);
//	#endif
	}

	void _sub()
	{
		int d0=workspace.vartable[getvar()];
		workspace.vartable[getvar()]-=d0;

//	#ifdef CODEFOLLOW
//		fprintf(f," Var[%d]-=Var[%d] (-=%d)",cfvar-workspace.vartable,cfvar2-workspace.vartable,d0);
//	#endif
	}

	void jump()
	{
		int d0=L9WORD(codeptr);
		int a0;
		codeptr+=2;

		a0=acodeptr+((d0+((workspace.vartable[getvar()])<<1))&0xffff);
		codeptr=acodeptr+L9WORD(a0);
	}

	/* bug */
	void exit1(byte d4[],byte d5[],byte d6,byte d7)
	{
		int a0=absdatablock;
		byte d1=d7,d0;
		boolean skip=false;
		if (--d1!=0)
		{
			do
			{
				d0=l9memory[a0];
				if (L9GameType==L9_V4)
				{
					if ((d0==0) && (l9memory[a0+1]==0)) {
						//TODO: проверить, что break уходит за while
						skip=true;
						break;
					}
				}
				a0+=2;
			}
			while ((d0&0x80)==0 || (--d1!=0));
		}
		if (!skip) {
			do
			{
				d4[0]=l9memory[a0++];
				if (((d4[0])&0xf)==d6)
				{
					d5[0]=l9memory[a0];
					return;
				}
				a0++;
			}
			while (((d4[0])&0x80)==0);
		}

		/* notfn4 */
	//notfn4:
		d6=(byte)(exitreversaltable[d6]&0xff);
		a0=absdatablock;
		d5[0]=1;

		do
		{
			d4[0]=l9memory[a0++];
			if (((d4[0])&0x10)==0 || ((d4[0])&0xf)!=d6) a0++;
			else if (l9memory[a0++]==d7) return;
			/* exit6noinc */
			if (((d4[0])&0x80)!=0) d5[0]++;
		} while (d4[0]!=0);
		d5[0]=0;
	}

	void Exit()
	{
		byte d4[]={0};
		byte d5[]={0};
		byte d7=(byte) (workspace.vartable[getvar()]&0xff);
		byte d6=(byte) (workspace.vartable[getvar()]&0xff);
//	#ifdef CODEFOLLOW
//		fprintf(f," d7=%d d6=%d",d7,d6);
//	#endif
		exit1(d4,d5,d6,d7);

		workspace.vartable[getvar()]=(short)((d4[0]&0x70)>>4);
		workspace.vartable[getvar()]=d5[0];
//	#ifdef CODEFOLLOW
//		fprintf(f," Var[%d]=%d(d4=%d) Var[%d]=%d",
//			cfvar2-workspace.vartable,(d4&0x70)>>4,d4,cfvar-workspace.vartable,d5);
//	#endif
	}

	void ifeqvt()
	{
		int d0=workspace.vartable[getvar()];
		int d1=workspace.vartable[getvar()];
		int a0=getaddr();
		if (d0==d1) codeptr=a0;

//	#ifdef CODEFOLLOW
//		fprintf(f," if Var[%d]=Var[%d] goto %d (%s)",cfvar2-workspace.vartable,cfvar-workspace.vartable,(L9UINT32) (a0-acodeptr),d0==d1 ? "Yes":"No");
//	#endif
	}

	void ifnevt()
	{
		int d0=workspace.vartable[getvar()];
		int d1=workspace.vartable[getvar()];
		int a0=getaddr();
		if (d0!=d1) codeptr=a0;

//	#ifdef CODEFOLLOW
//		fprintf(f," if Var[%d]!=Var[%d] goto %d (%s)",cfvar2-workspace.vartable,cfvar-workspace.vartable,(L9UINT32) (a0-acodeptr),d0!=d1 ? "Yes":"No");
//	#endif
	}

	void ifltvt()
	{
		int d0=workspace.vartable[getvar()];
		int d1=workspace.vartable[getvar()];
		int a0=getaddr();
		if (d0<d1) codeptr=a0;

//	#ifdef CODEFOLLOW
//		fprintf(f," if Var[%d]<Var[%d] goto %d (%s)",cfvar2-workspace.vartable,cfvar-workspace.vartable,(L9UINT32) (a0-acodeptr),d0<d1 ? "Yes":"No");
//	#endif
	}

	void ifgtvt()
	{
		int d0=workspace.vartable[getvar()];
		int d1=workspace.vartable[getvar()];
		int a0=getaddr();
		if (d0>d1) codeptr=a0;

//	#ifdef CODEFOLLOW
//		fprintf(f," if Var[%d]>Var[%d] goto %d (%s)",cfvar2-workspace.vartable,cfvar-workspace.vartable,(L9UINT32) (a0-acodeptr),d0>d1 ? "Yes":"No");
//	#endif
	}

	
	
	void ifeqct()
	{
		int d0=workspace.vartable[getvar()];
		int d1=getcon();
		int a0=getaddr();
		if (d0==d1) codeptr=a0;
//	#ifdef CODEFOLLOW
//		fprintf(f," if Var[%d]=%d goto %d (%s)",cfvar-workspace.vartable,d1,(L9UINT32) (a0-acodeptr),d0==d1 ? "Yes":"No");
//	#endif
	}

	void ifnect()
	{
		int d0=workspace.vartable[getvar()];
		int d1=getcon();
		int a0=getaddr();
		if (d0!=d1) codeptr=a0;
//	#ifdef CODEFOLLOW
//		fprintf(f," if Var[%d]!=%d goto %d (%s)",cfvar-workspace.vartable,d1,(L9UINT32) (a0-acodeptr),d0!=d1 ? "Yes":"No");
//	#endif
	}

	void ifltct()
	{
		int d0=workspace.vartable[getvar()];
		int d1=getcon();
		int a0=getaddr();
		if (d0<d1) codeptr=a0;
//	#ifdef CODEFOLLOW
//		fprintf(f," if Var[%d]<%d goto %d (%s)",cfvar-workspace.vartable,d1,(L9UINT32) (a0-acodeptr),d0<d1 ? "Yes":"No");
//	#endif
	}

	void ifgtct()
	{
		int d0=workspace.vartable[getvar()];
		int d1=getcon();
		int a0=getaddr();
		if (d0>d1) codeptr=a0;
//	#ifdef CODEFOLLOW
//		fprintf(f," if Var[%d]>%d goto %d (%s)",cfvar-workspace.vartable,d1,(L9UINT32) (a0-acodeptr),d0>d1 ? "Yes":"No");
//	#endif
	}
	
	void printinput()
	{
		int ptr=0;//(L9BYTE*) obuff;
		char c;
		while ((c=obuff[ptr++])!=' ') printchar(c);
		L9DEBUG ("printinput");
	}
	
	/*--was--	void initgetobj(void)
	{
		int i;
		numobjectfound=0;
		object=0;
		for (i=0;i<32;i++) gnoscratch[i]=0;
	}*/
	void initgetobj()
	{
		int i;
		numobjectfound=0;
		object=0;
		for (i=0;i<32;i++) gnoscratch[i]=0;
	}

	/*--was--	void getnextobject(void)
	{
		int d2,d3,d4;
		L9UINT16 *hisearchposvar,*searchposvar;

	#ifdef L9DEBUG
		printf("getnextobject");
	#endif

		d2=*getvar();
		hisearchposvar=getvar();
		searchposvar=getvar();
		d3=*hisearchposvar;
		d4=*searchposvar;

	// gnoabs 
		do
		{
			if ((d3 | d4)==0)
			{
				// initgetobjsp
				gnosp=128;
				searchdepth=0;
				initgetobj();
				break;
			}

			if (numobjectfound==0) inithisearchpos=d3;

		// gnonext 
			do
			{
				if (d4==list2ptr[++object])
				{
					// gnomaybefound 
					int d6=list3ptr[object]&0x1f;
					if (d6!=d3)
					{
						if (d6==0 || d3==0) continue;
						if (d3!=0x1f)
						{
							gnoscratch[d6]=d6;
							continue;
						}
						d3=d6;
					}
					// gnofound 
					numobjectfound++;
					gnostack[--gnosp]=object;
					gnostack[--gnosp]=0x1f;

					*hisearchposvar=d3;
					*searchposvar=d4;
					*getvar()=object;
					*getvar()=numobjectfound;
					*getvar()=searchdepth;
					return;
				}
			} while (object<=d2);

			if (inithisearchpos==0x1f)
			{
				gnoscratch[d3]=0;
				d3=0;

			// gnoloop 
				do
				{
					if (gnoscratch[d3])
					{
						gnostack[--gnosp]=d4;
						gnostack[--gnosp]=d3;
					}
				} while (++d3<0x1f);
			}
		// gnonewlevel 
			if (gnosp!=128)
			{
				d3=gnostack[gnosp++];
				d4=gnostack[gnosp++];
			}
			else d3=d4=0;

			numobjectfound=0;
			if (d3==0x1f) searchdepth++;

			initgetobj();
		} while (d4);

	// gnofinish 
	// gnoreturnargs 
		*hisearchposvar=0;
		*searchposvar=0;
		*getvar()=object=0;
		*getvar()=numobjectfound;
		*getvar()=searchdepth;
	}*/
	void getnextobject()
	{
		short d2,d3,d4;
		int hisearchposvar,searchposvar;

		L9DEBUG ("getnextobject");

		d2=workspace.vartable[getvar()];
		hisearchposvar=getvar();
		searchposvar=getvar();
		d3=workspace.vartable[hisearchposvar];
		d4=workspace.vartable[searchposvar];

	// gnoabs 
		do
		{
			if ((d3 | d4)==0)
			{
				// initgetobjsp
				gnosp=128;
				searchdepth=0;
				initgetobj();
				break;
			}

			if (numobjectfound==0) inithisearchpos=d3;

		// gnonext 
			do
			{
				if (d4==l9memory[list2ptr+(++object)])
				{
					// gnomaybefound 
					int d6=l9memory[list3ptr+object]&0x1f;
					if (d6!=d3)
					{
						if (d6==0 || d3==0) continue;
						if (d3!=0x1f)
						{
							gnoscratch[d6]=(short)d6;
							continue;
						}
						d3=(short)d6;
					}
					// gnofound 
					numobjectfound++;
					gnostack[--gnosp]=object;
					gnostack[--gnosp]=0x1f;

					workspace.vartable[hisearchposvar]=d3;
					workspace.vartable[searchposvar]=d4;
					workspace.vartable[getvar()]=object;
					workspace.vartable[getvar()]=numobjectfound;
					workspace.vartable[getvar()]=searchdepth;
					return;
				}
			} while (object<=d2);

			if (inithisearchpos==0x1f)
			{
				gnoscratch[d3]=0;
				d3=0;

			// gnoloop 
				do
				{
					if (gnoscratch[d3]!=0)
					{
						gnostack[--gnosp]=d4;
						gnostack[--gnosp]=d3;
					}
				} while (++d3<0x1f);
			}
		// gnonewlevel 
			if (gnosp!=128)
			{
				d3=(short)(gnostack[gnosp++]&0xffff);
				d4=(short)(gnostack[gnosp++]&0xffff);
			}
			else d3=d4=0;

			numobjectfound=0;
			if (d3==0x1f) searchdepth++;

			initgetobj();
		} while (d4!=0);

	// gnofinish 
	// gnoreturnargs 
		workspace.vartable[hisearchposvar]=0;
		workspace.vartable[searchposvar]=0;
		workspace.vartable[getvar()]=object=0;
		workspace.vartable[getvar()]=numobjectfound;
		workspace.vartable[getvar()]=searchdepth;
	}

	
	
	/*--was--	L9BOOL inputV2(int *wordcount)
	{
		L9BYTE a,x;
		L9BYTE *ibuffptr,*obuffptr,*ptr,*list0ptr;
		char *iptr;

		if (Cheating) NextCheat();
		else
		{
			os_flush();
			lastchar='.';
			// get input 
			if (!os_input(ibuff,IBUFFSIZE)) return FALSE; // fall through 
			if (CheckHash()) return FALSE;

			// check for invalid chars 
			for (iptr=ibuff;*iptr!=0;iptr++)
			{
				if (!isalnum(*iptr))
					*iptr=' ';
			}

			// force CR but prevent others 
			os_printchar(lastactualchar='\r');
		}
		// add space onto end 
		ibuffptr=(L9BYTE*) strchr(ibuff,0);
		*ibuffptr++=32;
		*ibuffptr=0;

		*wordcount=0;
		ibuffptr=(L9BYTE*) ibuff;
		obuffptr=(L9BYTE*) obuff;
		// ibuffptr=76,77 
		// obuffptr=84,85 
		// list0ptr=7c,7d 
		list0ptr=L9Pointers[1];

		while (*ibuffptr==32) ++ibuffptr;

		ptr=ibuffptr;
		do
		{
			while (*ptr==32) ++ptr;
			if (*ptr==0) break;
			(*wordcount)++;
			do
			{
				a=*++ptr;
			} while (a!=32 && a!=0);
		} while (*ptr>0);

		while (TRUE)
		{
			ptr=ibuffptr; // 7a,7b 
			while (*ibuffptr==32) ++ibuffptr;

			while (TRUE)
			{
				a=*ibuffptr;
				x=*list0ptr++;

				if (a==32) break;
				if (a==0)
				{
					*obuffptr++=0;
					return TRUE;
				}

				++ibuffptr;
				if (tolower(x&0x7f) != tolower(a))
				{
					while (x>0 && x<0x7f) x=*list0ptr++;
					if (x==0)
					{
						do
						{
							a=*ibuffptr++;
							if (a==0)
							{
								*obuffptr=0;
								return TRUE;
							}
						} while (a!=32);
						while (*ibuffptr==32) ++ibuffptr;
						list0ptr=L9Pointers[1];
						ptr=ibuffptr;
					}
					else
					{
						list0ptr++;
						ibuffptr=ptr;
					}
				}
				else if (x>=0x7f) break;
			}

			a=*ibuffptr;
			if (a!=32)
			{
				ibuffptr=ptr;
				list0ptr+=2;
				continue;
			}
			--list0ptr;
			while (*list0ptr++<0x7e);
			*obuffptr++=*list0ptr;
			while (*ibuffptr==32) ++ibuffptr;
			list0ptr=L9Pointers[1];
		}
	}*/
	boolean inputV2()
	{
		char a,x;
		int ibuffptr,obuffptr,ptr;
		int list0ptr;

		if (Cheating) {} //TODO: NextCheat();
		else
		{
			os_flush();
			lastchar='.';
			// get input 
			if ((ibuffstr=os_input(IBUFFSIZE))==null) return false; // fall through
			// add space and zero onto end
			ibuffstr.concat(" \0");
			ibuff=ibuffstr.toCharArray();
			//TODO:if (CheckHash()) return false;

			// check for invalid chars 
			for (int i=0;i<ibuff.length;i++) {
				if (!((ibuff[i]>='a' && ibuff[i]<='z') || (ibuff[i]>='A' && ibuff[i]<='Z') || (ibuff[i]>='0' && ibuff[i]<='9')))
					ibuff[i]=' ';
			}

			// force CR but prevent others
			os_printchar(lastactualchar='\r');
		}
		wordcount=0;
		ibuffptr=0; //(L9BYTE*) ibuff;
		obuffptr=0; //(L9BYTE*) obuff;
		// ibuffptr=76,77
		// obuffptr=84,85
		// list0ptr=7c,7d
		list0ptr=L9Pointers[1];

		while (ibuff[ibuffptr]==32) ++ibuffptr;

		ptr=ibuffptr;
		do
		{
			while (ibuff[ptr]==32) ++ptr;
			if (ibuff[ptr]==0) break;
			(wordcount)++;
			do
			{
				a=ibuff[++ptr];
			} while (a!=32 && a!=0);
		} while (ibuff[ptr]>0);

		while (true)
		{
			ptr=ibuffptr; // 7a,7b
			while (ibuff[ibuffptr]==32) ++ibuffptr;

			while (true)
			{
				a=ibuff[ibuffptr];
				x=(char)l9memory[list0ptr++];

				if (a==32) break;
				if (a==0)
				{
					obuff[obuffptr++]=0;
					return true;
				}

				++ibuffptr;
				if (tolower((char)(x&0x7f)) != tolower(a))
				{
					while (x>0 && x<0x7f) x=(char)l9memory[list0ptr++];
					if (x==0)
					{
						do
						{
							a=ibuff[ibuffptr++];
							if (a==0)
							{
								obuff[obuffptr]=0;
								return true;
							}
						} while (a!=32);
						while (ibuff[ibuffptr]==32) ++ibuffptr;
						list0ptr=L9Pointers[1];
						ptr=ibuffptr;
					}
					else
					{
						list0ptr++;
						ibuffptr=ptr;
					}
				}
				else if (x>=0x7f) break;
			}

			a=ibuff[ibuffptr];
			if (a!=32)
			{
				ibuffptr=ptr;
				list0ptr+=2;
				continue;
			}
			--list0ptr;
			while (l9memory[list0ptr]++<0x7e);
			obuff[obuffptr++]=(char)l9memory[list0ptr];
			while (ibuff[ibuffptr]==32) ++ibuffptr;
			list0ptr=L9Pointers[1];
		}
	}

	/*--was-- void input(void)
	{
		//  if corruptinginput() returns false then, input will be called again
		//   next time around instructionloop, this is used when save() and restore()
		//   are called out of line 

		codeptr--;
		if (L9GameType==L9_V2)
		{
			int wordcount;
			if (inputV2(&wordcount))
			{
				L9BYTE *obuffptr=(L9BYTE*) obuff;
				codeptr++;
				*getvar()=*obuffptr++;
				*getvar()=*obuffptr++;
				*getvar()=*obuffptr;
				*getvar()=wordcount;
			}
		}
		else
			if (corruptinginput()) codeptr+=5;
	}*/
	void input()
	{
		// if corruptinginput() returns false then, input will be called again
		// next time around instructionloop, this is used when save() and restore()
		// are called out of line 

		codeptr--;
		if (L9GameType==L9_V2)
		{
			if (inputV2())
			{
				//L9BYTE *obuffptr=(L9BYTE*) obuff;
				codeptr++;
				//todo: проверить правильность конвертации char в short
				workspace.vartable[getvar()]=(short) obuff[0];//*obuffptr++;
				workspace.vartable[getvar()]=(short) obuff[1];//*obuffptr++;
				workspace.vartable[getvar()]=(short) obuff[2];//*obuffptr;
				workspace.vartable[getvar()]=(short)wordcount;
			}
		}
		else
			{} //TODO: if (corruptinginput()) codeptr+=5;
	}


	/*void cleartg(void)
	{
		int d0 = *codeptr++;
	#ifdef L9DEBUG
		printf("cleartg %s",d0 ? "graphics" : "text");
	#endif

		if (d0)
		{
	// clearg 
			if (l9textmode)
	// gintclearg 
				os_cleargraphics();
		}
	// cleart 
	// oswrch(0x0c) 
	}*/
	void cleartg()
	{
		int d0 = l9memory[codeptr++];
		L9DEBUG ("cleartg %s",d0!=0 ? "graphics" : "text");

		if (d0!=0)
		{
	// clearg 
			if (l9textmode!=0)
	// gintclearg 
				os_cleargraphics();
		}
	// cleart 
	// oswrch(0x0c) 
	}
	
	/*--was--	void function(void)
	{
		int d0=*codeptr++;
	#ifdef CODEFOLLOW
		fprintf(f," %s",d0==250 ? "printstr" : functions[d0-1]);
	#endif

		switch (d0)
		{
			case 1: calldriver(); break;
			case 2: L9Random(); break;
			case 3: save(); break;
			case 4: NormalRestore(); break;
			case 5: clearworkspace(); break;
			case 6: workspace.stackptr=0; break;
			case 250:
				printstring((char*) codeptr);
				while (*codeptr++);
				break;

			default: ilins(d0);
		}
	}*/
	void function()
	{
		int d0=l9memory[codeptr++];
//	#ifdef CODEFOLLOW
//		fprintf(f," %s",d0==250 ? "printstr" : functions[d0-1]);
//	#endif

		switch (d0)
		{
			//TODO: case 1: calldriver(); break;
			case 2: L9Random(); break;
			//TODO: case 3: save(); break;
			//TODO: case 4: NormalRestore(); break;
			case 5: clearworkspace(); break;
			case 6: workspace.stackptr=0; break;
			case 250:
				printstringb(codeptr);
				while (l9memory[codeptr++]!=0);
				break;

			default: ilins(d0);
		}
	}

	/*--was--	void L9Random(void)
	{
	#ifdef CODEFOLLOW
		fprintf(f," %d",randomseed);
	#endif
		randomseed=(((randomseed<<8) + 0x0a - randomseed) <<2) + randomseed + 1;
		*getvar()=randomseed & 0xff;
	#ifdef CODEFOLLOW
		fprintf(f," %d",randomseed);
	#endif
	}*/
	void L9Random() {
//	#ifdef CODEFOLLOW
//		fprintf(f," %d",randomseed);
//	#endif
		//TODO: проверить генерацию randomseed
		randomseed=(short)((((randomseed<<8) + 0x0a - randomseed) <<2) + randomseed + 1);
		workspace.vartable[getvar()]=(short)(randomseed & 0xff);
//	#ifdef CODEFOLLOW
//		fprintf(f," %d",randomseed);
//	#endif
	}
	
	///////////////////// New (tsap) implementations ////////////////////
	
	char toupper(char c) {
		if (c>='a' && c<='z') c=(char)(c-32);
		return c;
	}
	
	char tolower(char c) {
		if (c>='A' && c<='Z') c=(char)(c+32);
		return c;
	}
	
	void L9DEBUG(String txt) {
		error(txt);
	}
	
	void L9DEBUG(String txt1, String txt2) {
		error(txt1,txt2);
	}
	
	void L9DEBUG(String txt, int val) {
		error(txt,val);
	}
	
	void L9DEBUG(String txt, int val1, int val2) {
		error(txt,val1,val2);
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
	//private static final int LISTAREASIZE = 0x800;
	private static final int STACKSIZE = 1024;
	
	int Id;
	short codeptr,stackptr,listsize,stacksize,filenamesize,checksum;
	short vartable[];
	//byte listarea[];
	short stack[];
	String filename;
	
	GameState() {
		vartable=new short[256];
		//listarea=new byte[LISTAREASIZE];
		stack=new short[STACKSIZE];
	}
	
}

class ScanData {
	int Size;
	int Min,Max;
	boolean JumpKill, DriverV4;
};

class PosScanCodeMask {
	int Pos;
	int ScanCodeMask;
};
