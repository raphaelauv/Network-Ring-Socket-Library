#include "Thread.h"
extern parametres entite ;
extern adresses addrs;
extern _threads threads;
int tokenize_string(char* argl, char** argv) {
  int i;
  printf ("l argument est %s \n",argl);
  argv[0] = strtok(argl," \t\n" );
  for (i = 0; argv[i] != NULL; ++i)
    argv[i+1] = strtok(NULL," \t\n" );
  return i;
}


void*  serveur_tcp(void *_port){
  int* port=(int*)(_port);
  int sock=socket(PF_INET,SOCK_STREAM,0);
  struct sockaddr_in address_sock;
  address_sock.sin_family=AF_INET;
  address_sock.sin_port=htons(*port);
  address_sock.sin_addr.s_addr=htonl(INADDR_ANY);
  int r=bind(sock,(struct sockaddr *)&address_sock,sizeof(struct sockaddr_in));
  if(r==0){
    r=listen(sock,0);
    struct sockaddr_in caller;
    socklen_t size=sizeof(caller);
    int sock2;
    while(1){
      sock2=accept(sock,(struct sockaddr *)&caller,&size);
      if(sock2>=0){
	char mess[150]; 
	char udp[4];
        char udp1[4];
	sprintf(udp1,"%d",entite.port_multi);
	if(entite.is_connected){
        sprintf(udp,"%d",entite.udp_succ1);
        formate_mtcp(mess,6,"WELC",entite.ip_succ,udp,entite.ip_multi,udp1);
	}else{
          sprintf(udp,"%d",entite.port_udp);
	  formate_mtcp(mess,6,"WELC",entite.ip,udp,entite.ip_multi,udp1);
	}
	  strcat(mess,"\n");
	send(sock2,mess,strlen(mess)*sizeof(char)+1,0);
	char buff[100];
	int recu=recv(sock2,buff,99*sizeof(char),0);
	buff[recu]='\0';
	printf("Message recu : %s\n",buff);
	char * split [3] ;
	tokenize_string(buff,split);
	int n=(atoi(split[2]));
	printf("l'adresse est : %s le port est : %d \n ",split[1],n);
        if(!entite.is_dupl){
	  if(strcmp(split[0],"NEWC")==0){
	    connectringo(split[1],split[2],1);
            send(sock2,"ACKC\n",6,0);
	  }else if(strcmp(split[0],"DUPL")==0){
	    connectringo(split[1],split[2],2);
            sprintf(udp,"%d",entite.port_udp);
            sprintf(udp,"%d",entite.port_udp);
	    formate_mtcp(mess,2,"ACKD",udp);
            strcat(mess,"\n");
	    send(sock2,mess,strlen(mess)*sizeof(char),0);
	  }
	  
	}
        }else send(sock2,"ERRO\n",5,0);
	close(sock2);
      }
    close(sock);
  }
  return _port;
}
   
int insertion(char* _lu,int flag){
  char *split[3];
  int i=tokenize_string(_lu,split);
  if((i!=3)||((strcmp(split[0],"connecTo")!=0)&&(strcmp(split[0],"dupL")!=0))){
    fprintf(stderr,"l'instruction %s ne respect pas le format\n",_lu); 
    printf("i vaut %d \n",i);
    return -1;
  }
  char *test;
  int port = strtol(split[2], &test,10);
  if((*test) != '\0'){
    fprintf(stderr,"le 3 eme  argument doit etre un entier\n");
    return -1;
  }
  struct sockaddr_in adress_sock;
  adress_sock.sin_family = AF_INET;
  adress_sock.sin_port = htons(port);
  int descr=socket(PF_INET,SOCK_STREAM,0);
  inet_aton(split[1],&adress_sock.sin_addr);
  int r=connect(descr,(struct sockaddr *)&adress_sock,sizeof(struct sockaddr_in));
  if(r!=-1){
    char buff[100];
    int size_rec=read(descr,buff,99*sizeof(char));
    buff[size_rec]='\0';
    printf("Caracteres recus : %d\n",size_rec);
    char mess [150];
    char udp[4];
    sprintf(udp,"%d",entite.port_udp);
    if(flag==1){
      formate_mtcp(mess,4,"NEWC",entite.ip,udp);
    }else{
      char multi[4];
      sprintf(multi,"%d",entite.port_multi);
      formate_mtcp(mess,6,"DUPL",entite.ip,udp,entite.ip_multi,multi);
    }
    strcat(mess,"\n");
    send(descr,mess,strlen(mess)*sizeof(char),0);
    char *split1 [5];
    i=tokenize_string(buff,split1);
    if(flag==1){
    connectringo(split1[1],split1[2],flag);
    }
    size_rec=read(descr,buff,99*sizeof(char));
    buff[size_rec]='\0';
    printf("Message : %s\n",buff);
    if(flag!=1){
       char *split2 [3];
       i=tokenize_string(buff,split2);
       connectringo(split[1],split2[1],flag);
     }
      close(descr);
  }else {
    fprintf(stderr,"la connection a %s a echouie \n",split[1]);
    return -1 ;
  }


  return 0;
}
void* serveur_multi(void*arg){
  int sock=socket(PF_INET,SOCK_DGRAM,0);
  sock=socket(PF_INET,SOCK_DGRAM,0);
  int ok=1;
  setsockopt(sock,SOL_SOCKET,SO_REUSEPORT,&ok,sizeof(ok));
  struct sockaddr_in address_sock;
  address_sock.sin_family=AF_INET;
  address_sock.sin_port=htons(entite.port_multi);
  address_sock.sin_addr.s_addr=htonl(INADDR_ANY);
  bind(sock,(struct sockaddr *)&address_sock,sizeof(struct sockaddr_in));
  struct ip_mreq mreq;
  mreq.imr_multiaddr.s_addr=inet_addr(entite.ip_multi);
  mreq.imr_interface.s_addr=htonl(INADDR_ANY);
  setsockopt(sock,IPPROTO_IP,IP_ADD_MEMBERSHIP,&mreq,sizeof(mreq));
  char tampon[100];
  while(1){
    int rec=recv(sock,tampon,100,0);
    tampon[rec]='\0';
    printf("Message recu en multicast  : %s\n",tampon);
    if(strncmp(tampon,"DOWN",4)==0){
    entite.casse=TRUE;
     pthread_cancel(threads.th_tcp);
     pthread_cancel(threads.th_udp);
     printf("au revoir l' annaux est cassé \n");
     sleep(3);
     exit(0);
    }
  }
  return arg;
}
void* serveur_udp(void*arg){
  int sock=socket(PF_INET,SOCK_DGRAM,0);
  struct sockaddr  *address_sock;
  /*address_sock.sin_family=AF_INET;
  address_sock.sin_port=htons(entite.port_udp);
  address_sock.sin_addr.s_addr=htonl(INADDR_ANY);*/
   struct addrinfo *first_info;
  struct addrinfo hints;
  memset(&hints, 0, sizeof(struct addrinfo));
  hints.ai_family = AF_INET;
  hints.ai_socktype=SOCK_DGRAM;
  char port [4];
  sprintf(port,"%d",entite.port_udp);
  int r=getaddrinfo(entite.ip,port,&hints,&first_info);
    if(r==0){
      if(first_info!=NULL){
	address_sock=first_info->ai_addr;
      }
   r=bind(sock,(struct sockaddr *)address_sock,sizeof(struct sockaddr_in));
  if(r==0){
    char tampon[512];
    while(1){
      int rec=recv(sock,tampon,512,0);
      tampon[rec]='\0';
      printf("message recu en udp %s \n",tampon);
       //traitement_mudp(tampon);
    }
  }
    }
  return arg;
}
void send_udp(char*mess){
  if(entite.is_connected) sendto(addrs.sock,mess,strlen(mess),0,addrs.addrudp1,(socklen_t)sizeof(struct sockaddr_in));
  if(entite.is_dupl)
    sendto(addrs.sock,mess,strlen(mess),0,addrs.addrudp2,(socklen_t)sizeof(struct sockaddr_in));
   
}
void send_multi(char*mess){
  sendto(addrs.sock,mess,strlen(mess),0,addrs.addrmulti,(socklen_t)sizeof(struct sockaddr_in));
}
