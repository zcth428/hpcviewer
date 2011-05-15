#!/bin/bash
#
# Launch the hpctraceviewer binary and set workspace directory.
#
# $Id: hpcviewer.sh 534 2010-07-21 13:15:27Z laksono $
#

workspace="${HOME}/.hpctoolkit/hpctraceviewer"

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
viewer="${bindir}/../libexec/hpctraceviewer/hpctraceviewer"
test -x "$viewer"  || die "fatal error - executable $viewer not found"
test -n "$DISPLAY" || die "fatal error - DISPLAY variable must be set"

#
# Check java version.
#
java_version=`java -version 2>&1 | awk '{print $3}'`
java_version=${java_version:3:1}
if test $java_version -lt 5
  echo "$0 fatal error - Java version $java_version is too old; please use at least Java 1.5."
  exit
fi

#
# Launch the viewer.
#
if test -d "$HOME" ; then
    exec "$viewer" -data "$workspace" "$@"
else
    echo "$0 warning - HOME is not set, proceeding anyway" 1>&2
    exec "$viewer" "$@"
fi
