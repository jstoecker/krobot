#!/bin/bash

# set the classpath the include all libraries
export CLASSPATH=\
lib/universal/jogl.all.jar:\
lib/universal/gluegen-rt.jar:\
lib/universal/org.OpenNI.jar:\
lib/universal/jgloo.jar:\
lib/universal/snakeyaml-1.10.jar:\
src/

# create binary directory if it does not exist
BIN=bin/
if [ ! -d $BIN ]; then
  mkdir -p $BIN
fi

# compile source into bytecode
javac -d $BIN $(find src -name *.java)

# copy libraries and resources
rsync -r --exclude=.svn models $BIN/
rsync -r --exclude=.svn scripts/* $BIN/
rsync -r --exclude=.svn src/shaders $BIN/
rsync -r --exclude=.svn motions $BIN/
rsync --exclude=.svn lib/universal/* $BIN/lib/

if [[ "$(uname)" == 'Linux' ]]; then
  echo "no"
  if [[ "$(uname -m)" == 'x86_64' ]]; then
    rsync --exclude=.svn lib/linux_x86_64/* $BIN/lib/
  else
    rsync --exclude=.svn lib/linux_i586/* $BIN/lib/
  fi
elif [[ "$(uname)" == 'Darwin' ]]; then
  rsync --exclude=.svn lib/macosx/* $BIN/lib/
fi

# JAR the class files
cd $BIN
jar cf krobot.jar edu shaders
mv krobot.jar lib/
rm -rf edu
rm -rf shaders

if [ ! -d motions ]; then
  mkdir -p motions
fi
