#!/bin/bash
for i in *; do echo $i 
  cd $i
  plugin='hpctraceviewer/plugins/edu.rice.cs.hpc.traceviewer_5*'
  mv ${plugin}/scripts . 
  cd scripts 
  bash make-dist ../hpctraceviewer >/tmp/out
  mv hpctraceviewer.tar.gz ../../hpctraceviewer-5.2.1a-release-$i.tgz
  cd ../../ 
  ls -l hpctraceviewer-5.2.1a-release-$i.tgz
done

