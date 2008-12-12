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
	
	File			: runtime.c
	Authors			: Leonardo BAUTISTA GOMEZ <leobago@gmail.com>
					: Florent WEBER <florent.weber@gmail.com>
					: Julien CLEMENT <julien.jclement@gmail.com>
					: Felix MACH <felix.mach@gmail.com>
					: Henry LAY <lay.henri83@gmail.com>
					: Remi VILLE <raymooz@gmail.com>
					: Shi-Hon CHAN <syone7@gmail.com>
*/

#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include <unistd.h>
#include <string.h>
#include <sys/timeb.h>
#include <mpi.h>

#include "globals.h"
#include "runtime.h"

/** Append the given instance with its requirements vector to the waiting queue */
void appendToQueue (CP_QUEUE_NODE ** pqueue, int rank, int * tokenMask, int arraySize)
{
	int i ;
	char msgStr[64] ;
	msgStr[63] = 0 ;
	
	if (!(*pqueue))
	{
		*pqueue = (CP_QUEUE_NODE *) malloc(sizeof(CP_QUEUE_NODE)) ;
		(*pqueue)->next = NULL ;
		(*pqueue)->rank = rank ;
		for (i = 0; i<arraySize; i++)
			(*pqueue)->tokenMask[i] = tokenMask[i] ;
		
		snprintf(msgStr, 63, "Added a process to queue, rank=%d", rank) ;
		procLogMsg(2, msgStr) ;
		return ;
	}

	CP_QUEUE_NODE * tmp = *pqueue ;

	/* Go to the end of queue */
	while (tmp->next)
		tmp = tmp->next ;

	tmp->next = (CP_QUEUE_NODE *) malloc(sizeof(CP_QUEUE_NODE)) ;
	tmp->next->next = NULL ;
	tmp->next->rank = rank ;
	for (i = 0; i<arraySize; i++)
		tmp->next->tokenMask[i] = tokenMask[i] ;
	
	snprintf(msgStr, 63, "Added a process to queue, rank=%d", rank) ;
	procLogMsg(2, msgStr) ;
}

/** Free the waiting queue */
void freeQueue (CP_QUEUE_NODE ** pqueue)
{
	CP_QUEUE_NODE * tmp2, * tmp = *pqueue ;

	while (tmp)
	{
		tmp2 = tmp ;
		tmp = tmp->next ;
		free(tmp2) ;
	}
}

/** Communication places vectors comparison function */
int tab_cmp(int* tab1, int* tab2, int size)
{
	int i ;
	int top = 0 ;
	int point = 0 ;

	for(i=0; i<size; i++)
	{
		if (tab1[i] > top)
			top = tab1[i] ;
	}

	top *= size ;

	for (i=0; i<size; i++)
	{
		if (tab1[i] != 0)
		{
			if (tab2[i] > 0)
				point += top+(tab1[i]<=tab2[i] ? tab1[i] : tab2[i]) ;
		}
	}
	
	return point ;
}

/** Communication places scheduling algorithm */
CP_QUEUE_NODE* findProcessToServe (int* tab_ref, CP_QUEUE_NODE** pqueue, int size)
{
	if (!(*pqueue)) return NULL ;

	CP_QUEUE_NODE * tmp2=NULL, * tmp = *pqueue ;
	int max = 0, tmpMax ;
	CP_QUEUE_NODE * maxProc = NULL, *maxProcBefore = NULL ;

	while (tmp)
	{
		tmpMax = tab_cmp(tab_ref, tmp->tokenMask, size) ;

		if (tmpMax > max)
		{
			max = tmpMax ;
			maxProc = tmp ;
			maxProcBefore = tmp2;
		}

		tmp2 = tmp ;
		tmp = tmp->next;
	}

	if (maxProc)
	{
		if (!maxProcBefore) *pqueue = maxProc->next ;
		else maxProcBefore->next = maxProc->next ;

		return maxProc ;
	}

	return NULL ;
}

/** Look for a queued instance with the given MPI Rank and remove it */
void deleteNodeByRank (CP_QUEUE_NODE ** pqueue, int rank)
{
	if (!(*pqueue)) return ;

	CP_QUEUE_NODE * tmp2=NULL, * tmp = *pqueue ;

	while (tmp)
	{
		if (tmp->rank == rank)
		{
			if (tmp2) tmp2->next = tmp->next;
			else *pqueue = tmp->next ;

			free(tmp) ;
			return ;
		}

		tmp2 = tmp ;
		tmp = tmp->next ;
	}
}

SYNC_PROC_T* newSyncProc (int size)
{
	SYNC_PROC_T* nsp = malloc(sizeof(SYNC_PROC_T));
	nsp->tab = malloc(NB_TYPE_PROC * INSTANCE_COUNT * sizeof(int));
	nsp->cmp = malloc(INSTANCE_COUNT * sizeof(int));
	nsp->current = 0;
	nsp->last = 1;
	nsp->size = size;

	int i;
	for(i = 0; i < NB_TYPE_PROC * INSTANCE_COUNT; i++)
		nsp->tab[i] = -1;

	for(i = 0; i < INSTANCE_COUNT; i++)
		nsp->cmp[i] = size;

	return nsp;
}

/** Return a random index in the range 0-range
	and uses the process rank to improve entropy */
int randomIndex (int rank, int range)
{
	char msgStr[64] ;
	msgStr[63] = 0 ;
	
	int index = (int) (range * (random() / (RAND_MAX + 1.0))) ;
	
	index = (index + rank) % range ;
	
	snprintf(msgStr, 63, "Random index : %d, Range : 0-%d", index, range) ;
	procLogMsg(rank, msgStr) ;
	
	return index ;
}

/** Initialise the random seed
	using current time in milliseconds */
void initRandomSeed (void)
{
	long t = getMilliTime() ;
	srandom(t) ; /* use time in milliseconds to set seed */
}

/** Sleep for a random duration
	between 0.1 and 0.9 seconds */
void randomSleep (void)
{
	int sleep = (1 + (int) (9.0 * (random() / (RAND_MAX + 1.0)))) * 100000 ;

	usleep(sleep) ;
}

/** Process a ACK message for the synchronized transitions manager */
void treatACK(SYNC_PROC_T* syncElement, int source, int syncID, int my_rank)
{
	char msgStr[64] ;
	msgStr[63] = 0 ;
	
	if (syncElement->tab[(syncElement->current * syncElement->size) + instanceArray[source-3]->PNProcess] == -1)
	{
		syncElement->cmp[syncElement->current]--;
		syncElement->tab[(syncElement->current * syncElement->size) + instanceArray[source-3]->PNProcess] = source;
		
		if (syncElement->cmp[syncElement->current] == 0)
		{
			int i;
			for(i = syncElement->current * syncElement->size; i < (syncElement->current * syncElement->size) + NB_TYPE_PROC; i++)
			{
				if (syncElement->tab[i] != -1)
				{
					MPI_Send(&syncID,1,MPI_INT,syncElement->tab[i],TAG_SYNCT_ACK,MPI_COMM_WORLD);
					
					snprintf(msgStr, 63, "Sent a TAG_SYNCT_ACK to process %d", syncElement->tab[i]) ;
					procLogMsg(my_rank, msgStr) ;
					
					syncElement->tab[i] = -1;
				}
			}
			syncElement->cmp[syncElement->current] = syncElement->size;

			if ((syncElement->current + 1) % INSTANCE_COUNT != syncElement->last )
				syncElement->current = (syncElement->current + 1) % INSTANCE_COUNT;
		}
	}
	else
	{
		int found = 0;
		int level = syncElement->current + 1;
		while(level != syncElement->last)
		{
			if(syncElement->tab[(level * syncElement->size) + instanceArray[source-3]->PNProcess] == -1)
			{
				syncElement->tab[(level * syncElement->size) + instanceArray[source-3]->PNProcess] = source;
				syncElement->cmp[level]--;
				found = 1;
				break;
			}
			else
				level = (level + 1) % INSTANCE_COUNT;
		}

		if (!found)
		{
			syncElement->tab[(level * syncElement->size) + instanceArray[source-3]->PNProcess] = source;
			syncElement->cmp[level]--;
			syncElement->last = (syncElement->last + 1) % INSTANCE_COUNT;
		}
	}
}

/** Process a cancellation message for the synchronized transitions manager */
void treatCAN(SYNC_PROC_T* syncElement, int source, int syncID, int my_rank)
{
	int lev = syncElement->current;
	char msgStr[64] ;
	msgStr[63] = 0 ;
	
	while(lev != syncElement->last)
	{
		if (syncElement->tab[(lev * syncElement->size) + instanceArray[source-3]->PNProcess] == source)
		{
			int sublev = lev + 1;
			while(syncElement->tab[(sublev * syncElement->size) + instanceArray[source-3]->PNProcess] != -1)
			{
				syncElement->tab[(lev * syncElement->size) + instanceArray[source-3]->PNProcess] = syncElement->tab[(sublev * syncElement->size) + instanceArray[source-3]->PNProcess];
				lev = (lev + 1) % INSTANCE_COUNT;
				sublev = (sublev + 1) % INSTANCE_COUNT;
			}
			syncElement->tab[(lev * syncElement->size) + instanceArray[source-3]->PNProcess] = -1;
			syncElement->cmp[lev]--;

			MPI_Send(&syncID,1,MPI_INT,source,TAG_SYNCT_CCK,MPI_COMM_WORLD);
			
			snprintf(msgStr, 63, "Sent a TAG_SYNCT_CCK to process %d", source) ;
			procLogMsg(my_rank, msgStr) ;
			
			break;
		}
		else
			lev = (lev + 1) % INSTANCE_COUNT;
	}
}

/** Return the current time in milliseconds */
long getMilliTime(void)
{
	struct timeb t ;
	
	ftime(&t) ;
	return (1000 * t.time) + t.millitm ;
}

/** Standardized logging functions */

void startLogging(void)
{
#ifdef LOG_TO_DOT
	fprintf (LOG_STD_DEST, "digraph G {\n") ;
#endif
}

void stopLogging(void)
{
#ifdef LOG_TO_DOT
	fprintf (LOG_STD_DEST, "}\n") ;
#endif
}

void procLogEvt (int rank, const char * curState, int curType, const char * targetState, int targetType)
{
#ifdef LOG_TO_DOT

	if (!strncmp(curState, "INIT", 4))
	{
		fprintf (LOG_STD_DEST,
				 "\t%s_%d [shape=invtriangle,label=\"%s\",color=\"firebrick1\",style=filled] ;\n",
				curState,
				rank,
				curState) ;
		
		fprintf (LOG_STD_DEST,
				"\t%s_%d -> %s_%d [style=dotted] ;\n",
				curState,
				rank,
				targetState,
				rank) ;
	}
	else if (curType == LOG_PLACE_TYPE)
	{
		if (targetType == LOG_ST_TYPE)
		{
			fprintf (LOG_STD_DEST,
					 "\t%s_%d [label=\"%s\"] ;\n",
					 curState,
					 rank,
					 curState) ;
			
			fprintf (LOG_STD_DEST,
					 "\t%s [style=filled,shape=box,color=\"green\"] ;\n",
					 targetState) ;
			
			fprintf (LOG_STD_DEST,
					 "\t%s_%d -> %s ;\n",
					 curState,
					 rank,
					 targetState) ;
		}
		else
		{
			fprintf (LOG_STD_DEST,
					 "\t%s_%d [label=\"%s\"] ;\n",
					 curState,
					 rank,
					 curState) ;
			
			fprintf (LOG_STD_DEST,
					 "\t%s_%d [shape=box,label=\"%s\"] ;\n",
					 targetState,
					 rank,
					 targetState) ;
			
			fprintf (LOG_STD_DEST,
					"\t%s_%d -> %s_%d ;\n",
					curState,
					rank,
					targetState,
					rank) ;
		}
	}
	else if (curType == LOG_ST_TYPE)
	{
		fprintf (LOG_STD_DEST,
				 "\t%s [style=filled,shape=box,color=\"green\"] ;\n",
				 curState) ;
		
		fprintf (LOG_STD_DEST,
				 "\t%s -> %s_%d [color=\"forestgreen\"] ;\n",
				 curState,
				 targetState,
				 rank) ;
	}
	else if (curType == LOG_CP_TYPE)
	{
		fprintf (LOG_STD_DEST,
				 "\t%s [style=filled,color=\"dodgerblue\"] ;\n",
				 curState) ;
	
		fprintf (LOG_STD_DEST,
				 "\t%s -> %s_%d ;\n",
				 curState,
				 targetState,
				 rank) ;
	}
	else
	{
		if (targetType == LOG_CP_TYPE)
		{
			fprintf (LOG_STD_DEST,
					 "\t%s [style=filled,color=\"dodgerblue\"] ;\n",
					 targetState) ;
			
			fprintf (LOG_STD_DEST,
					 "\t%s_%d -> %s ;\n",
					 curState,
					 rank,
					 targetState) ;
		}
		else
		{
			fprintf (LOG_STD_DEST,
					"\t%s_%d [label=\"%s\"] ;\n",
					curState,
					rank,
					curState) ;
			
			fprintf (LOG_STD_DEST,
					"\t%s_%d -> %s_%d ;\n",
					curState,
					rank,
					targetState,
					rank) ;
		}
	}
	
#else
	if (curType == LOG_CP_TYPE)
	{
		fprintf (LOG_STD_DEST,
				 "[%s\t%d]\tHad token(s) from CP %s to %s\n",
				 instanceArray[rank-3]->PNProcStr,
				 rank,
				 curState,
				 targetState) ;
	}
	else if (targetType == LOG_CP_TYPE)
	{
		fprintf (LOG_STD_DEST,
				 "[%s\t%d]\tPut token(s) from %s in CP %s\n",
				 instanceArray[rank-3]->PNProcStr,
				 rank,
				 curState,
				 targetState) ;
	}
	else
	{
		fprintf (LOG_STD_DEST,
				 "[%s\t%d]\tFrom %s going to %s\n",
				 instanceArray[rank-3]->PNProcStr,
				 rank,
				 curState,
				 targetState) ;
	}
#endif

	fflush(LOG_STD_DEST) ;
}

/** Event : Trying to get token(s) from a communication place */
void procLogEvtAsk (int rank, const char * curState, int curType, const char * targetState, int targetType)
{
#ifdef LOG_TO_DOT
	if (targetType == LOG_CP_TYPE)
	{
		fprintf (LOG_STD_DEST,
				 "\t%s [style=filled,color=\"dodgerblue\"] ;\n",
				targetState) ;
		
		fprintf (LOG_STD_DEST,
				"\t%s_%d -> %s [color=\"dodgerblue\"] ;\n",
				curState,
				rank,
				targetState) ;
	}
	else if (targetType == LOG_ST_TYPE)
	{
		fprintf (LOG_STD_DEST,
				 "\t%s [style=filled,shape=box,color=\"green\"] ;\n",
				 targetState) ;
		
		fprintf (LOG_STD_DEST,
				 "\t%s -> %s_%d [color=\"dodgerblue\"] ;\n",
				 targetState,
				 curState,
				 rank) ;
	}
#else
	if (targetType == LOG_CP_TYPE)
	{
		fprintf (LOG_STD_DEST,
				"[%s\t%d]\tAsk for token(s) from CP %s to %s\n",
				instanceArray[rank-3]->PNProcStr,
				rank,
				targetState,
				curState) ;
	}
	else if (targetType == LOG_ST_TYPE)
	{
		fprintf (LOG_STD_DEST,
				 "[%s\t%d]\tTry to fire ST %s from %s\n",
				 instanceArray[rank-3]->PNProcStr,
				 rank,
				 targetState,
				 curState) ;
	}
#endif

	fflush(LOG_STD_DEST) ;
}

/** Event : Succeeded to get token(s) from a communication place */
void procLogEvtHad (int rank, const char * curState, int curType, const char * targetState, int targetType)
{
#ifdef LOG_TO_DOT

	if (curType == LOG_CP_TYPE)
	{
		fprintf (LOG_STD_DEST,
				 "\t%s [style=filled,color=\"dodgerblue\"] ;\n",
				curState) ;
		
		fprintf (LOG_STD_DEST,
				"\t%s -> %s_%d [color=\"forestgreen\"] ;\n",
				curState,
				targetState,
				rank) ;
	}
	else if (curType == LOG_ST_TYPE)
	{
		fprintf (LOG_STD_DEST,
				 "\t%s [style=filled,shape=box,color=\"green\"] ;\n",
				 curState) ;
		
		fprintf (LOG_STD_DEST,
				 "\t%s_%d -> %s [color=\"forestgreen\"] ;\n",
				 targetState,
				 rank,
				 curState) ;
	}
#else
	if (curType == LOG_CP_TYPE)
	{
		fprintf (LOG_STD_DEST,
				"[%s\t%d]\tHad token(s) from CP %s to %s\n",
				instanceArray[rank-3]->PNProcStr,
				rank,
				curState,
				targetState) ;
	}
	else if (curType == LOG_ST_TYPE)
	{
		fprintf (LOG_STD_DEST,
				 "[%s\t%d]\tGranted to fire ST %s from %s\n",
				 instanceArray[rank-3]->PNProcStr,
				 rank,
				 curState,
				 targetState) ;
	}
#endif

	fflush(LOG_STD_DEST) ;
}

/** Event : Put token(s) in a communication place */
void procLogEvtPut (int rank, const char * curState, int curType, const char * targetState)
{
#ifdef LOG_TO_DOT
	fprintf (LOG_STD_DEST,
			 "\t%s [style=filled,color=\"dodgerblue\"] ;\n",
			 targetState) ;
	
	fprintf (LOG_STD_DEST,
			 "\t%s_%d -> %s [color=\"darkorange1\"] ;\n",
			 curState,
			 rank,
			 targetState) ;
#else
	fprintf (LOG_STD_DEST,
			 "[%s\t%d]\tPut token(s) from %s to CP %s\n",
			 instanceArray[rank-3]->PNProcStr,
			 rank,
			 curState,
			 targetState) ;
#endif

	fflush(LOG_STD_DEST) ;
}

void procLogEvtBack (int rank, const char * curState, int curType, const char * targetState, int targetType)
{
#ifndef LOG_TO_DOT
	fprintf (LOG_STD_DEST,
			 "[%s\t%d]\tFrom %s going BACK to %s\n",
			 instanceArray[rank-3]->PNProcStr,
			 rank,
			 curState,
			 targetState) ;

	fflush(LOG_STD_DEST) ;
#endif
}

void procLogStart (int rank)
{
#ifndef LOG_TO_DOT
	char msgBuf[64] ;
	msgBuf[63] = 0 ;

	if (rank > 2)
		snprintf (msgBuf, 63, "[%s\t%d]\t", instanceArray[rank-3]->PNProcStr, rank) ;
	else if (rank == 0)
		snprintf (msgBuf, 63, "[ProtoMngr\t%d]\t", rank) ;
	else if (rank == 1)
		snprintf (msgBuf, 63, "[SyncTransMngr\t%d]\t", rank) ;
	else if (rank == 2)
		snprintf (msgBuf, 63, "[CommPlaceMngr\t%d]\t", rank) ;
	
	fprintf (LOG_STD_DEST, "%sStarting\n", msgBuf) ;
#endif

	fflush(LOG_STD_DEST) ;
}

void procLogEnd (int rank)
{
#ifndef LOG_TO_DOT
	char msgBuf[64] ;
	msgBuf[63] = 0 ;
	
	if (rank > 2)
		snprintf (msgBuf, 63, "[%s\t%d]\t", instanceArray[rank-3]->PNProcStr, rank) ;
	else if (rank == 0)
		snprintf (msgBuf, 63, "[ProtoMngr\t%d]\t", rank) ;
	else if (rank == 1)
		snprintf (msgBuf, 63, "[SyncTransMngr\t%d]\t", rank) ;
	else if (rank == 2)
		snprintf (msgBuf, 63, "[CommPlaceMngr\t%d]\t", rank) ;
	
	if (rank == 0)
		fprintf (LOG_STD_DEST, "%sSending TAG_END to all processes\n", msgBuf) ;
	else
		fprintf (LOG_STD_DEST, "%sReceived TAG_END, going down\n", msgBuf) ;
	
	fflush(LOG_STD_DEST) ;
#endif
}

void procLogMsg (int rank, const char * msg)
{
#ifndef LOG_TO_DOT
	char msgBuf[64] ;
	msgBuf[63] = 0 ;

	if (rank > 2)
		snprintf (msgBuf, 63, "[%s\t%d]\t", instanceArray[rank-3]->PNProcStr, rank) ;
	else if (rank == 0)
		snprintf (msgBuf, 63, "[ProtoMngr\t%d]\t", rank) ;
	else if (rank == 1)
		snprintf (msgBuf, 63, "[SyncTransMngr\t%d]\t", rank) ;
	else if (rank == 2)
		snprintf (msgBuf, 63, "[CommPlaceMngr\t%d]\t", rank) ;
	
	fprintf (LOG_STD_DEST, "%s%s\n", msgBuf, msg) ;

	fflush(LOG_STD_DEST) ;
#endif
}


