#include "protocole.h"


boolean verbose=TRUE;
extern parametres entite ;
extern adresses addrs;
extern  hashmap_map *messages;

int  traitement_mudp(char * buff){
  char mess[512];
  if((strncmp(buff,"WHOS",4)==0)){
       

  }else if((strncmp(buff,"MEMB",4)==0)){
          send_udp(buff);


  }else if((strncmp(buff,"APPL",4)==0)){
        send_udp(buff);
  }else if((strncmp(buff,"TEST",4)==0)){
     


  }if((strncmp(buff,"GBYE",4)==0)){



  }else if((strncmp(buff,"EBYE",4)==0)){



  }else{
    if(verbose){
      char tmp[5];
      strncpy(tmp,buff,4);
      tmp[4]='\0';
      fprintf(stderr,"le type de message est  inconnu ");
    }
    return -1;
  }
  send_udp(buff);
  return 0;
}
char* make_idm(char*id){
  char *res=(char*)(malloc(sizeof(char)*8));
  strncpy(res,id,6);
  sprintf(res+6,"%u",(unsigned int) entite.ind_m);
  entite.ind_m=(entite.ind_m+1) % MAX_NBM;
  return res;
}
int formate_mudp(char*res,int nbarg,char*type,char* arg1,...){
  int i=2;
  strcpy(res,type);
  strcat(res," ");
  strcat(res,make_idm(entite.id));
  strcat(res," ");
  strcat(res,arg1);
  va_list ap;
  va_start(ap,arg1);
  char*tmp;
  for( i=3 ;i<nbarg;i++){
    strcat(res," ");
    tmp=(char*)(va_arg(ap,char*));
    strcat(res,(char*)(tmp));
  }
  va_end(ap);
  return 0;
}
int formate_mtcp(char*res,int nbarg,char*type,char* arg1,...){
  int i=2;
  strcpy(res,type);
  strcat(res," ");
  strcat(res,arg1);
  va_list ap;
  va_start(ap,arg1);
  char*tmp;
  for( i=3 ;i<nbarg;i++){
    strcat(res," ");
    tmp=(char*)(va_arg(ap,char*));
    strcat(res,(char*)(tmp));
  }
  va_end(ap);
  return 0;
}
int init_parametres(int _port_tcp,int _port_udp){
  entite.casse=0;
  entite.port_udp=_port_udp ;
  entite.port_tcp=_port_tcp;
  entite.udp_succ1=_port_udp;
  strcpy(entite.ip,"127.000.000.001");
  strcpy(entite.ip_succ,"127.000.000.001");
  strcpy(entite.id,entite.ip+9);
  entite.ind_m=0;
  entite.is_connected=FALSE;
  entite.is_dupl=FALSE;
  strcpy(entite.ip_multi,"225.001.002.004");
  struct addrinfo *first_info;
  struct addrinfo hints;
  memset(&hints, 0, sizeof(struct addrinfo));
  hints.ai_family = AF_INET;
  hints.ai_socktype=SOCK_DGRAM;
  int r=getaddrinfo("225.001.002.004","9999",NULL,&first_info);
  if(r==0){
    if(first_info!=NULL){
      addrs.addrmulti=first_info->ai_addr;
    }
  }
  entite.port_multi=9999;
  addrs.sock=socket(PF_INET,SOCK_DGRAM,0);
  return 0;
}
int connectringo(char* ip ,char* port,int flag){
  if(flag==1){
    entite.udp_succ1=atoi(port);
    struct addrinfo *first_info;
    struct addrinfo hints;
    memset(&hints, 0, sizeof(struct addrinfo));
    hints.ai_family = AF_INET;
    hints.ai_socktype=SOCK_DGRAM;
    int r=getaddrinfo(ip,port,&hints,&first_info);
    if(r==0){
      if(first_info!=NULL){
	addrs.addrudp1=first_info->ai_addr;
        entite.is_connected=TRUE;
        strcpy(entite.ip_succ,ip);
	return 0;
      }
    }
    fprintf(stderr," l' adresse : %s n existe pas",ip);
  }else{
    entite.udp_succ2=atoi(port);
    struct addrinfo *first_info;
    struct addrinfo hints;
    memset(&hints, 0, sizeof(struct addrinfo));
    hints.ai_family = AF_INET;
    hints.ai_socktype=SOCK_DGRAM;
    int r=getaddrinfo(ip,port,&hints,&first_info);
    if(r==0){
      if(first_info!=NULL){
	addrs.addrudp2=first_info->ai_addr;
        entite.is_dupl=TRUE;
	return 0;
      }
    }
    fprintf(stderr," l  adresse : %s n existe pas",ip);
  }
  return -1 ;
}




