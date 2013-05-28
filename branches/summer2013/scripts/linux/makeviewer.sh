#!/bin/bash
for i in *; do echo $i 
  cd $i
  plugin='hpcviewer/plugins/edu.rice.cs.hpc.viewer_*'
  mv ${plugin}/scripts . 
  cd scripts 
  bash make-dist ../hpcviewer >/tmp/out
  mv hpcviewer.tar.gz ../../hpcviewer-5.2.1a-release-$i.tgz
  cd ../../ 
  ls -l hpcviewer-5.2.1a-release-$i.tgz
done

