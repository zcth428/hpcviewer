#!/bin/sh
#
# Launch the hpcviewer binary and set workspace directory.
#
# $Id$
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
# Launch the viewer.
#
if test -d "$HOME" ; then
    exec "$viewer" -data "$workspace" "$@"
else
    echo "$0: warning: HOME is not set, proceeding anyway" 1>&2
    exec "$viewer" "$@"
fi
