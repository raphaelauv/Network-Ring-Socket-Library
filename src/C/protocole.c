#include "protocole.h"


boolean verbose=TRUE;
extern parametres entite ;
extern adresses addrs;
extern  hashmap_map *messages;

int  traitement_mudp(char * buff){
  char mess[512];
  if(strlen(buff)<13){
    printf(" le message %s ne respecte pas le format \n",buff);
    return -1;
  }
  if(!deja_recu(buff)){
   ajout_idm(buff);
  if((strncmp(buff,"WHOS",4)==0)){
       
   
  }else if((strncmp(buff,"MEMB",4)==0)){
          send_udp(buff);

  }else if((strncmp(buff,"APPL",4)==0)){
        send_udp(buff);
  }else if((strncmp(buff,"TEST",4)==0)){
     
  }if((strncmp(buff,"GBYE",4)==0)){
         formate_mudp(mess,2,"EBYE",entite.ip);
           send_udp(mess);

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
  }
  return 0;
}
int deja_recu(char*mess){
  char idm [9];
  char *res=malloc(9*sizeof(char));
   strncpy(idm,mess+5,8);
  if(hashmap_get(messages,idm,(void**)&res)==-3){
    printf("message déja reçu : %s \n ",mess);
    return 0;
   }
 return 1;
}
int ajout_idm(char*mess){
  char idm [9];
  strncpy(idm,mess+5,8);
  hashmap_put(messages,idm,(void**)&idm);
    return 0;
}
char* make_idm(){
  char *res=(char*)(malloc(sizeof(char)*8));
  int n=entite.idm+entite.ind_m;
  sprintf(res,"%d",n);
  entite.ind_m=(entite.ind_m+1) % MAX_NBM;
  printf("idm= %s \n",res);
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
int init_parametres(int _port_tcp,int _port_udp,char*ip_multi, int _portmulti,char*ip){
  entite.casse=0;
  entite.port_udp=_port_udp ;
  entite.port_tcp=_port_tcp;
  entite.udp_succ1=_port_udp;
  strcpy(entite.ip,ip);
  strcpy(entite.ip_succ,ip);
  strcpy(entite.id, idmachine(entite.ip,_port_udp));
  entite.idm=atoi(entite.id)*1000;
  entite.ind_m=0;
  entite.is_connected=FALSE;
  entite.is_dupl=FALSE;
  strcpy(entite.ip_multi,ip_multi);
  struct addrinfo *first_info;
  struct addrinfo hints;
  memset(&hints, 0, sizeof(struct addrinfo));
  hints.ai_family = AF_INET;
  hints.ai_socktype=SOCK_DGRAM;
   char multi[4] ;
   sprintf(multi,"%d",_portmulti);
  int r=getaddrinfo(ip_multi,multi,NULL,&first_info);
  if(r==0){
    if(first_info!=NULL){
      addrs.addrmulti=first_info->ai_addr;
    }
  }
  entite.port_multi=_portmulti;
  addrs.sock=socket(PF_INET,SOCK_DGRAM,0);
  return 0;
}
int connectringo(char* ip ,char* port,int flag){
   printf("l ip est : %s le port est : %s le flag est : %d \n",ip,port,flag);
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

char* norm_addr(char *ip){
  int i=0;
  int j=0;
  int k=0 ;
  int n=strlen(ip);
  char * res=(char*)malloc(15*sizeof(char));
  for(i=1;i<=n;i++){
        if(ip[n-i]=='.'){
          while((k%3)!=0){
           res[14-j]='0';
	   j++;k++;
         }
         k=-1;
        }
       res[14-j]=ip[n-i];
       j++;k++;
   }
  for(i=j;i<15;i++)res[14-i]='0';
return res;
}

char* idmachine(char* ip,int port){
    int i=0;
  int j=4;
   int k=0 ;
  int n=strlen(ip);
  char *id=(char*)(malloc(5*sizeof(char)));
  char * res=(char*)malloc((n-2)*sizeof(char));
   res[n-3]='\0';
  for(i=1;i<=n;i++){
        if(ip[n-i]!='.'){
          res[n-j]=ip[n-i];
           j++;
        }  
   }
  char *test;
    int p=port*9;
   if(port<10)p=port*9000;
   else if(port<100)p=port*900;
   else if(port<1000)p=port*90;
   k= strtol(res+9, &test,10);
   k+=p;
   char *res1=(char*)(malloc(5*sizeof(char)));
    sprintf(res1,"%d",k);
   return res1; 
}

