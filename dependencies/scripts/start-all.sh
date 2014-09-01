#!/usr/bin/env bash

bin=`dirname "${BASH_SOURCE-$0}"`
bin=`cd "$bin"; pwd`

source $bin/cloud-install-bash-include.sh

#start zookeeper

$bin/start-zookeeper.sh

sleep 2

#start hadoop

$bin/start-hadoop.sh

#start accumulo

$bin/start-accumulo.sh
