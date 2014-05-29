#!/bin/sh

bin=`dirname "${BASH_SOURCE-$0}"`
bin=`cd "$bin"; pwd`

source $bin/cloud-install-bash-include.sh

#start zookeeper

$bin/start-zookeeper.sh

sleep 2

#start hadoop

$bin/start-hadoop.sh

#init accumulo

$ACCUMULO_HOME/bin/accumulo init --instance-name "instance_name" --password "root_password"

#stop hadoop

$bin/stop-hadoop.sh

#stop zookeeper

$bin/stop-zookeeper.sh
