/*
##=======================================================================
##Copyright (C) 2007-2008 C_MPI Generator
##This program is free software; you can redistribute it and/or modify
##it under the terms of the GNU General Public License (GPL) as published
##of the License, or (at your option) any later version.
##
##This program is distributed in the hope that it will be useful,
##but WITHOUT ANY WARRANTY; without even the implied warranty of
##MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
##GNU General Public License for more details.
##
##To read the license please visit http://www.gnu.org/copyleft/gpl.html
##=======================================================================
##
## File          : hello.c
## Authors       : Leonardo BAUTISTA GOMEZ <leobago@gmail.com>
##               : Florent WEBER <florent.weber@gmail.com>
##               : Julien CLEMENT <julien.jclement@gmail.com>
##               : Felix MACH <felix.mach@gmail.com>
##               : Henry LAY <lay.henri83@gmail.com>
##               : Remi VILLE <raymooz@gmail.com>
##               : Shi-Hon CHAN <syone7@gmail.com>
##
##
*/

#include <math.h>
#include <stdio.h>
#include <unistd.h>
#include <string.h>
#include <time.h>
#include <mpi.h>

#include "globals.h"
#include "runtime.h"

void appendToQueue (CP_QUEUE_NODE ** pqueue, int rank, int * tokenMask, int arraySize)
{
	int i ;
	if (!(*pqueue))
	{
		*pqueue = (CP_QUEUE_NODE *) malloc(sizeof(CP_QUEUE_NODE)) ;
		(*pqueue)->next = NULL ;
		(*pqueue)->rank = rank ;
		for (i = 0; i<arraySize; i++)(*pqueue)->tokenMask[i] = tokenMask[i] ;
		printf("Added a process to queue, rank=%d\n", rank) ;
		return ;
	}

	CP_QUEUE_NODE * tmp = *pqueue ;

	while (tmp->next) tmp = tmp->next ;

	tmp->next = (CP_QUEUE_NODE *) malloc(sizeof(CP_QUEUE_NODE)) ;
	tmp->next->next = NULL ;
	tmp->next->rank = rank ;
	for (i = 0; i<arraySize; i++)(*pqueue)->tokenMask[i] = tokenMask[i] ;
	printf("Added a process to queue, rank=%d\n", rank) ;
}

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

int tab_cmp(int* tab1, int* tab2, int size)
{
	int i;
	int top=0;
	int point=0;

	for(i=0;i<size;i++)
	{
		if (*(tab1+i) > top)
			top = *(tab1+i);
	}

	top = top*size ;

	for (i=0;i<size;i++)
	{
		if (*(tab1+i) != 0)
		{
			if (*(tab2+i) > 0)
				point+=top+(*(tab1+i)<=*(tab2+i)?*(tab1+i):*(tab2+i));
		}
	}
	return point;
}

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
		
			free(tmp);
			return ;
		}
	
		tmp2 = tmp ;
		tmp = tmp->next;
	}
}

//This function returns a struct SYNC_PROC_T
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

int randomIndex (int rank, int range)
{
	int index = (int) (range * (rand() / (RAND_MAX + 1.0))) ;
	index = (index + rank) % range;

	printf ("Random index : %d, Range : 0-%d\n", index, range) ;

	return index ;
}

void initRandomSeed (void)
{
	time_t t1;
	(void) time(&t1);
	srand(t1); /* use time in seconds to set seed */
}

void randomSleep (void)
{
	int sleep = (1 + (int) (9.0 * (rand() / (RAND_MAX + 1.0)))) * 100000 ;

// printf ("Sleeping : %d microseconds\n", sleep) ;
	usleep(sleep) ;
}

