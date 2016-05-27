#ifndef  __PROTOCOLE_H__
#define __PROTOCOLE_H__
#define FALSE 0 
#define TRUE 1 
#define MAX_LENGTH_MESS 512 
#define LENGTH_IDM 8
#define MAX_NBM 65000
#define SIZE_IP 15
#define SIZE_TYPE 4

#include <stdio.h>
#include <stdarg.h>
#include <string.h>
#include <stdlib.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <string.h>
#include <sys/socket.h>
#include <unistd.h>
#include <sys/types.h>
#include <stdint.h>
#include "Thread.h"
#include "hashmap.h"

typedef short boolean;

typedef struct {
char id[6] ;
int port_udp ;
int port_tcp ;
char ip[16];
int udp_succ1;
char ip_succ[16];
int udp_succ2;
int port_multi;
char ip_multi[16];
boolean is_connected ;
boolean is_dupl;
boolean casse;
boolean dec;
int ind_m;//indice du idm 
int idm;
}parametres;
typedef struct {
  int sock;
  struct sockaddr *addrudp1;
  struct sockaddr *addrudp2;
  struct sockaddr *addrmulti;  
}adresses;

int  traitement_mudp(char * );
int formate_mudp(char*,int ,char*,char* ,...);
int formate_mtcp(char*,int ,char*,char* ,...);
int init_parametres();
int connectringo(char*,char*,int);
char* norm_addr(char *);
int ajout_idm(char*);
char* idmachine(char*,int);
#endif 
