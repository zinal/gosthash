
JAVA_HOME=/opt/java/jdk1.6.0_38
OPTFLAGS=-O2 -fPIC -Wall
#OPTFLAGS=-g -fPIC -Wall

all: libgosthash.so

clean:
	rm -f libgosthash.so
	rm -f *.o

libgosthash.so: gostjava.o gosthash.o gost89.o
	gcc -shared -o $@ $(OPTFLAGS) $^

gostjava.o: gosthash.h gostjava.c
	gcc -c  $(OPTFLAGS) -pipe -o $@ -I. -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux gostjava.c

gosthash.o: gost89.h gosthash.h gosthash.c
	gcc -c  $(OPTFLAGS) -pipe -o $@ -I. gosthash.c

gost89.o: gost89.c gost89.h
	gcc -c  $(OPTFLAGS) -pipe -o $@ -I. gost89.c
