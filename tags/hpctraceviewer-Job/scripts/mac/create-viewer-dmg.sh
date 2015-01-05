#! /bin/bash
test -f test2.dmg && rm test2.dmg
./create-dmg --window-size 500 300 --background hpcviewer/hpcviewer/plugins/edu.rice.cs.hpc.viewer_5.2.1/icons/hpcviewer128.gif --icon-size 128 --volname "hpcviewer" --icon "Applications" 380 205 --icon "Eclipse OS X Repackager" 110 205 hpcviewer-5.2.1.dmg /Users/laksonoadhianto/work/Download/creating-dmg/yoursway-create-dmg-1.0.0.2/hpcviewer/hpcviewer
