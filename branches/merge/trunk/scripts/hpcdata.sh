#!/bin/sh
#
# Launch the hpcdata jar and set the variables.
#
# $Id $
#

# Mark's script to retrive the directory where the script runs
script="$0"
if test -L "$script" ; then
    script=`readlink "$script"`
fi
bindir=`dirname "$script"`

export HPCVIEWER_DIR_PATH="${bindir}/"

JAVA_CMD=java
MAIN_CLASS=edu.rice.cs.hpc.data.framework.Application
CLASSPATH=$HPCVIEWER_DIR_PATH/lib/org.apache.xerces_2.9.0.v200909240008.jar:$HPCVIEWER_DIR_PATH/lib/org.eclipse.jface_3.5.1.M20090826-0800.jar:$HPCVIEWER_DIR_PATH/lib/hpcdata.jar
VM_ARGS="-Xms20m -Xmx180m"

# run the java main class
"$JAVA_CMD" $VM_ARGS -cp "$CLASSPATH" $MAIN_CLASS $*
