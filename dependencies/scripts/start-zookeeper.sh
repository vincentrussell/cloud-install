#!/bin/sh

bin=`dirname "${BASH_SOURCE-$0}"`
bin=`cd "$bin"; pwd`

$bin/zookeeper-server.sh start
