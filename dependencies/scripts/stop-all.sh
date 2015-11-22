#!/usr/bin/env bash

bin=`dirname "${BASH_SOURCE-$0}"`
bin=`cd "$bin"; pwd`

source $bin/cloud-install-bash-include.sh

#stop spark

$bin/stop-spark.sh

#stop accumulo

$bin/stop-accumulo.sh


#stop hadoop

$bin/stop-hadoop.sh

#stop zookeeper

$bin/stop-zookeeper.sh