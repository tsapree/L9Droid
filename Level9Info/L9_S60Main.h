/*
 * L9_S60Main.h
 *
 *  Created on: 07.07.2010
 *      Author: capry
 */

#ifndef L9_S60MAIN_H_
#define L9_S60MAIN_H_

#define NoInputAllowed			0
#define CommandEditorEnabled	1
#define CommandAvailable		2
#define CharEditorEnabled		3
#define CharAvailable			4
#define CharEditorTick			5
#define CharUnAvailable			6

#include "L9_S60AppView.h"

void L9MainInit(CL9_S60AppView* iAV);
void L9MainShut(void);
bool L9LoadGame(const char* filename);
void L9DoPeriodTask(void);
int L9GetState(void);
bool L9LoopUntilCommand(int nStatus);
unsigned char* L9GetPicBuff(void);
char* L9GetGameDir(void);
void L9UpdatePalette(void);

void L9Fill_Start (int x, int y, int colour1, int colour2);
int L9Fill_Step();

#endif /* L9_S60MAIN_H_ */
