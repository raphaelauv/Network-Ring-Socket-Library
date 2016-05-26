#include "protocole.h"
#include "hashmap.h"
#include "Thread.h"
#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <sys/types.h>
#include <ifaddrs.h>
#include <sys/ioctl.h>
#include <net/if.h>
#include <unistd.h>
char* recupere_ip();
adresses addrs;
parametres entite;
hashmap_map *messages;
_threads threads;
int test_arguments(int argc ,char* argv[]){
  //test des arguments  
  if(argc<5){
    fprintf(stderr,"ils vous faut 4 arguments port_tcp port_udp addr_multi port_multi \n");
    return -1;
  }
  char *test;
  int n1= strtol(argv[1], &test,10);
  if((*test) != '\0'){
    fprintf(stderr,"le premiere argument doit etre un entier\n");
    return -1;
  }
  int n2 = strtol(argv[2],&test,10);
  if((*test)!='\0'){
    fprintf(stderr,"le deuxieme argument doit etre un entier \n");
    return -1;
  }
  int n3 = strtol(argv[4],&test,10);
  if((*test)!='\0'){
    fprintf(stderr,"le quatrième  argument doit etre un entier \n");
    return -1;
  }
  init_parametres(n1,n2,norm_addr(argv[3]),n3,norm_addr(recupere_ip()));
  return 0;
}
void read_command(char* argl) {
    if (fgets(argl,512, stdin) == NULL) {
        // Si l'utilisateur tape Ctrl+D,
        // on interprète ceci comme "exit".
        strcpy(argl, "exit");
        printf("exit\n");
    } 
}


int main(int argc ,char* argv[]){
  if(test_arguments(argc,argv)!=0)return -1;
  messages=hashmap_new();
  char mess[512];
  pthread_create(&threads.th_tcp,NULL,serveur_tcp,&(entite.port_tcp));
  pthread_create(&threads.th_multi,NULL,serveur_multi,&(entite.port_multi));
  pthread_create(&threads.th_udp,NULL,serveur_udp,&(entite.port_udp));
  char buf[150];
  while(!entite.casse){
    read_command(buf); 
    if(strncmp(buf,"connecTo",8)==0){
      if(!entite.is_connected)insertion(buf,1);
    }else  if(strncmp(buf,"dupL",4)==0){
      if(!entite.is_dupl) insertion(buf,2);
     }else  if(strncmp(buf,"dowN",4)==0){
        send_multi("DOWN");
    }else  if(strncmp(buf,"disconnecT",4)==0){
       formate_mudp(mess,4,"EBYE","6666",entite.ip);
      send_udp(buf);
    }else  if(strncmp(buf,"whoS",4)==0){


    }  
  }
  return 0;
}











char* recupere_ip(){
int i=0;
struct ifaddrs *myaddrs, *ifa;
struct sockaddr_in *s4;
int status;
char *ip=(char *)malloc(64*sizeof(char));
status = getifaddrs(&myaddrs);
if (status != 0){
perror("Probleme de recuperation d'adresse IP");
exit(1);
}
 printf("choisisser une adresse ip parmet les suivantes :\n");
for (ifa = myaddrs; ifa != NULL; ifa = ifa->ifa_next){
if (ifa->ifa_addr == NULL) continue;
if ((ifa->ifa_flags & IFF_UP) == 0) continue;
if ((ifa->ifa_flags & IFF_LOOPBACK) != 0)continue;
if (ifa->ifa_addr->sa_family == AF_INET){
s4 = (struct sockaddr_in *)(ifa->ifa_addr);
if (inet_ntop(ifa->ifa_addr->sa_family, (void *)&(s4->sin_addr),
ip, 64*sizeof(char)) != NULL){
printf("%d)%s\n",i,ip);
i++;
}
}
}
printf("%d)127.0.0.1\n",i);
char choix[4];
int n1;
char *test;
read(STDIN_FILENO,choix,4);
  n1= strtol(choix, &test,10);
if(n1>=i)return "127.0.0.1";
i=0;
for (ifa = myaddrs; ifa != NULL; ifa = ifa->ifa_next){
if (ifa->ifa_addr == NULL) continue;
if ((ifa->ifa_flags & IFF_UP) == 0) continue;
if ((ifa->ifa_flags & IFF_LOOPBACK) != 0)continue;
if (ifa->ifa_addr->sa_family == AF_INET){
s4 = (struct sockaddr_in *)(ifa->ifa_addr);
if (inet_ntop(ifa->ifa_addr->sa_family, (void *)&(s4->sin_addr),
ip, 64*sizeof(char)) != NULL){
if(i==n1)return ip;
i++;
}
}
}
freeifaddrs(myaddrs);
return "127.0.0.1";
}
