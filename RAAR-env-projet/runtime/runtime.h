/*
	=======================================================================
	Copyright (C) 2007-2008 C_MPI Generator
	This program is free software; you can redistribute it and/or modify
	it under the terms of the GNU General Public License (GPL) as published
	of the License, or (at your option) any later version.
	
	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.
	
	To read the license please visit http://www.gnu.org/copyleft/gpl.html
	=======================================================================
	
	File			: runtime.h
	Authors			: Leonardo BAUTISTA GOMEZ <leobago@gmail.com>
					: Florent WEBER <florent.weber@gmail.com>
					: Julien CLEMENT <julien.jclement@gmail.com>
					: Felix MACH <felix.mach@gmail.com>
					: Henry LAY <lay.henri83@gmail.com>
					: Remi VILLE <raymooz@gmail.com>
					: Shi-Hon CHAN <syone7@gmail.com>
*/

/** Communication places manager's queue functions */
void appendToQueue (CP_QUEUE_NODE ** pqueue, int rank, int * tokenMask, int arraySize) ;
void freeQueue (CP_QUEUE_NODE ** pqueue) ;
int tab_cmp(int* tab1, int* tab2, int size) ;
CP_QUEUE_NODE* findProcessToServe (int* tab_ref, CP_QUEUE_NODE** pqueue, int size) ;
void deleteNodeByRank (CP_QUEUE_NODE ** pqueue, int rank) ;

/** Synchronisation transition manager's functions */
SYNC_PROC_T* newSyncProc (int size) ;
void treatACK(SYNC_PROC_T* syncElement, int source, int syncID, int my_rank);
void treatCAN(SYNC_PROC_T* syncElement, int source, int syncID, int my_rank);

/** Random functions */
int randomIndex (int rank, int range) ;
void initRandomSeed (void) ;
void randomSleep (void) ;

/** Time functions */
long getMilliTime(void) ;

/** Logging functions */
void startLogging(void) ;
void stopLogging(void) ;

void procLogEvt (int rank, const char * curState, int curType, const char * targetState, int targetType) ;
void procLogEvtBack (int rank, const char * curState, int curType, const char * targetState, int targetType) ;
void procLogEvtAsk (int rank, const char * curState, int curType, const char * targetState, int targetType) ;
void procLogEvtHad (int rank, const char * curState, int curType, const char * targetState, int targetType) ;
void procLogEvtPut (int rank, const char * curState, int curType, const char * targetState) ;

void procLogStart (int rank) ;
void procLogEnd (int rank) ;
void procLogMsg (int rank, const char * msg) ;

