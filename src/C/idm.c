 #include <time.h>
       #include <stdio.h>
       #include <stdlib.h>

       int
       main(int argc, char *argv[])
       {
           char outstr[200];
           time_t t;
           struct tm *tmp;

           t = time(NULL);
           tmp = localtime(&t);
           if (tmp == NULL) {
               perror("localtime");
               exit(EXIT_FAILURE);
           }

           if (strftime(outstr, sizeof(outstr), argv[1], tmp) == 0) {
               fprintf(stderr, "strftime returned 0");
               exit(EXIT_FAILURE);
           }

           printf("Result string is \"%s\"\n", outstr);
           exit(EXIT_SUCCESS);
       }
char* idmachine(char* ip){
 time_t t;
  t=time(NULL);
  struct sockaddr_in address_sock;
  address_sock.sin_family=AF_INET;
  address_sock.sin_port=htons(1024);
  address_sock.sin_addr.s_addr=t;
  printf("le Time t est %ld \n",(long)t);
  printf("le Time est %s \n",inet_ntoa(address_sock.sin_addr));
}

