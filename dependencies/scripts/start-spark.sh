#!/usr/bin/env bash

bin=`dirname "${BASH_SOURCE-$0}"`
bin=`cd "$bin"; pwd`

source $bin/cloud-install-bash-include.sh

$SPARK_HOME/sbin/start-master.sh

$SPARK_HOME/sbin/start-slaves.sh
