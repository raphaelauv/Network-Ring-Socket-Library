#include "protocole.h"
#include "hashmap.h"
#include "Thread.h"
#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
adresses addrs;
parametres entite;
hashmap_map *messages;
_threads threads;
int test_arguments(int argc ,char* argv[]){
  //test des arguments  
  if(argc<3){
    fprintf(stderr,"ils vous faut au moins deux arguments \n");
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
  init_parametres(n1,n2);
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
  char res[512];
  formate_mudp(res,6,"samir ", "ait ameur ", " 21406588 ","vdfgfhgfhfgjj","fsdffff");
  printf("le resulta est %s \n",res);
  pthread_create(&threads.th_tcp,NULL,serveur_tcp,&(entite.port_tcp));
  pthread_create(&threads.th_multi,NULL,serveur_multi,&(entite.port_multi));
  pthread_create(&threads.th_udp,NULL,serveur_udp,&(entite.port_udp));
  char buf[150];
  while(!entite.casse){
    read_command(buf); 
    if(strncmp(buf,"connecTo",8)==0){
      if(!entite.is_connected)insertion(buf,1);
    }else  if(strncmp(buf,"DUPL",4)==0){
      if(!entite.is_dupl) insertion(buf,2);
         
    }else{
      send_udp(buf);
    }  
  }
  return 0;
}

