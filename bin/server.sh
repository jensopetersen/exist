#!/bin/bash
# -----------------------------------------------------------------------------
# startup.sh - Start Script for Jetty + eXist
#
# $Id$
# -----------------------------------------------------------------------------

#
# In addition to the other parameter options for the standalone server 
# pass -j or --jmx to enable JMX agent. The port for it can be specified 
# with --jmx-port=1099
#

JMX_ENABLED=0
JMX_PORT=1099

declare -a JAVA_OPTS
NR_JAVA_OPTS=0
if `getopt -T >/dev/null 2>&1` ; [ $? = 4 ] ; then
    NON_JAVA_OPTS=`getopt -a -o h,j,d,p:,t: --long help,jmx,debug,http-port:,threads: \
	-n 'server.sh' -- "$@"`
else
    NON_JAVA_OPTS=`getopt h,j,d,p:,t: $*`
fi

eval set -- "$NON_JAVA_OPTS"
while true ; do
    case "$1" in
        -j|--jmx) JMX_ENABLED=1; shift ;;
        --jmx-port) JMX_PORT="$2"; shift 2 ;;
        -p|--http-port|-t|--threads) JAVA_OPTS[$NR_JAVA_OPTS]="$1 $2"; let "NR_JAVA_OPTS += 1"; shift 2 ;;
        --) shift ; break ;;
        *) JAVA_OPTS[$NR_JAVA_OPTS]="$1"; let "NR_JAVA_OPTS += 1"; shift ;;
    esac
done
# Collect the remaining arguments
for arg; do
    JAVA_OPTS[$NR_JAVA_OPTS]="$arg";
    let "NR_JAVA_OPTS += 1";
done

exist_home () {
	case "$0" in
		/*)
			p=$0
		;;
		*)
			p=`/bin/pwd`/$0
		;;
	esac
		(cd `/usr/bin/dirname $p` ; /bin/pwd)
}

if [ -z "$EXIST_HOME" ]; then
	EXIST_HOME_1=`exist_home`
	EXIST_HOME="$EXIST_HOME_1/.."
fi

if [ ! -f "$EXIST_HOME/start.jar" ]; then
	echo "Unable to find start.jar. Please set EXIST_HOME to point to your installation directory."
	exit 1
fi

OPTIONS="-Dexist.home=$EXIST_HOME"

# save LANG
if [ -n "$LANG" ]; then
	OLD_LANG="$LANG"
fi
# set LANG to UTF-8
LANG=en_US.UTF-8

# set java options
if [ -z "$JAVA_OPTIONS" ]; then
	JAVA_OPTIONS="-Xms16000k -Xmx256000k -Dfile.encoding=UTF-8"
fi

JAVA_ENDORSED_DIRS="$EXIST_HOME"/lib/endorsed
JAVA_OPTIONS="$JAVA_OPTIONS -Djava.endorsed.dirs=$JAVA_ENDORSED_DIRS"

# The following lines enable the JMX agent:
if [ $JMX_ENABLED -gt 0 ]; then
    JMX_OPTS="-Dcom.sun.management.jmxremote \
		-Dcom.sun.management.jmxremote.port=$JMX_PORT \
		-Dcom.sun.management.jmxremote.authenticate=false \
		-Dcom.sun.management.jmxremote.ssl=false"
    JAVA_OPTIONS="$JAVA_OPTIONS $JMX_OPTS"
fi


$JAVA_HOME/bin/java $JAVA_OPTIONS $OPTIONS -jar "$EXIST_HOME/start.jar" standalone ${JAVA_OPTS[@]}

if [ -n "$OLD_LANG" ]; then
	LANG="$OLD_LANG"
fi
