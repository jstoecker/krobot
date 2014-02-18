LIB_DIR=lib/

export CLASSPATH=\
$LIB_DIR/jogl.all.jar:\
$LIB_DIR/gluegen-rt.jar:\
$LIB_DIR/org.OpenNI.jar:\
$LIB_DIR/jgloo.jar:\
$LIB_DIR/snakeyaml-1.10.jar:\
$LIB_DIR/krobot.jar

java -Djava.library.path=$LIB_DIR edu.miami.cs.krobot.MainApp $1 $2
