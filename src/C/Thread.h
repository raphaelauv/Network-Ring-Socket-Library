#ifndef  __THREAD_H__
#define  __THREAD_H__
#include <stdlib.h>
#include <stdio.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <string.h>
#include <sys/socket.h>
#include <unistd.h>
#include <pthread.h>
#include "protocole.h"
typedef struct {
  pthread_t th_tcp;
  pthread_t th_multi;
  pthread_t th_udp;
}_threads;
int tokenize_string(char* , char** );
void*  serveur_tcp(void *);
int insertion(char*,int );
void* serveur_multi(void*);
void* serveur_udp(void*);
void send_udp(char*);
void send_multi(char*);
#endif
