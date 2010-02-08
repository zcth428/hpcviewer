#!/bin/sh
#
# Launch the hpcdata jar and set the variables.
#
# $Id $
#
JAVA_CMD=java

ORIG_DIR_NAME=`cd`
DIR_NAME=`dirname $0`
MAIN_CLASS=edu.rice.cs.hpc.data.framework.Application
CLASSPATH=lib/org.apache.xerces_2.9.0.v200909240008.jar:lib/org.eclipse.jface_3.5.1.M20090826-0800.jar:lib/hpcdata.jar
VM_ARGS="-Xms20m -Xmx180m"

"$JAVA_CMD" $VM_ARGS -cp "$CLASSPATH" $MAIN_CLASS $*
cd $ORIG_DIR_NAME