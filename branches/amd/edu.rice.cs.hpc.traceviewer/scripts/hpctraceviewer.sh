#!/bin/bash
#
# Launch the hpcviewer/traceviewer binary and set the workspace directory.
#
# $Id: hpcviewer.sh 534 2010-07-21 13:15:27Z laksono $
#

name=hpctraceviewer

workspace="${HOME}/.hpctoolkit/${name}"

die()
{
    echo "$0: error: $*" 1>&2
    exit 1
}

warn()
{
    echo "$0: warning: $*" 1>&2
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
viewer="${bindir}/../libexec/${name}/${name}"
test -x "$viewer"  || die "executable $viewer not found"
test -n "$DISPLAY" || die "DISPLAY variable is not set"

#
# Check java version.
#
java_version=`java -version 2>&1 | grep -i java | grep -i vers | head -1`
if test "x$java_version" = x ; then
    die "unable to find program 'java' on your PATH"
fi
minor=`expr "$java_version" : '[^.]*\.\([0-9]*\)'`
test "$minor" -ge 5 >/dev/null 2>&1
if test $? -ne 0 ; then
    die "$java_version is too old, use Java 1.5 or later"
fi

#
# Launch the viewer.
#
if test -d "$HOME" ; then
    exec "$viewer" -data "$workspace" "$@"
else
    warn "HOME is not set, proceeding anyway"
    exec "$viewer" "$@"
fi
