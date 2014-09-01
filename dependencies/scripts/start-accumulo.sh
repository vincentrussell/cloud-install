#!/usr/bin/env bash

bin=`dirname "${BASH_SOURCE-$0}"`
bin=`cd "$bin"; pwd`

source $bin/cloud-install-bash-include.sh

$ACCUMULO_HOME/bin/start-all.sh
