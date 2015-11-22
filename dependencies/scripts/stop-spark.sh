#!/usr/bin/env bash

bin=`dirname "${BASH_SOURCE-$0}"`
bin=`cd "$bin"; pwd`

source $bin/cloud-install-bash-include.sh

$SPARK_HOME/sbin/stop-slaves.sh

$SPARK_HOME/sbin/stop-master.sh

