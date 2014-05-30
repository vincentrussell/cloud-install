export HADOOP_HOME="/path/to/hadoop"
export HADOOP_CONF_DIR=$HADOOP_HOME/etc/hadoop
export HADOOP_PREFIX=$HADOOP_HOME
export ACCUMULO_HOME="/path/to/accumulo"
export ZOOKEEPER_HOME="/path/to/zookeeper"

export PATH=$PATH:${HADOOP_HOME}/bin:${ZOOKEEPER_HOME}/bin:${ACCUMULO_HOME}/bin
