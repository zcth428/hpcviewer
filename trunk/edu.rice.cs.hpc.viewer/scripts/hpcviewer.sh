#!/bin/bash
#
# Launch the hpcviewer binary and set workspace directory.
#
# $Id: hpcviewer.sh 534 2010-07-21 13:15:27Z laksono $
#

workspace="${HOME}/.hpctoolkit/hpcviewer"

die()
{
    echo "$0: $*" 1>&2
    exit 1
}

#
# Find the hpctoolkit directory.
#
script="$0"
if test -L "$script" ; then
    script=`readlink "$script"`
fi
bindir=`dirname "$script"`
bindir=`( cd "$bindir" && pwd )`
viewer="${bindir}/../libexec/hpcviewer/hpcviewer"
test -x "$viewer" || die "unable to find: $viewer"

#
# Check java version.
#
java_version=`java -version 2>&1 | awk '{print $3}'`
java_version=${java_version:3:1}
if test $java_version -ge 5
then
  echo "java version: $java_version"
else
  echo "Java version is not suppported: $java_version"
  echo "Please use Java 1.5 (at least)"
  exit
fi

#
# Launch the viewer.
#
if test -d "$HOME" ; then
    exec "$viewer" -data "$workspace" "$@"
else
    echo "$0: warning: HOME is not set, proceeding anyway" 1>&2
    exec "$viewer" "$@"
fi
