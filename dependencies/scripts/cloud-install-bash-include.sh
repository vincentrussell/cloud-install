export HADOOP_PREFIX="/path/to/hadoop"
export HADOOP_CONF_DIR=$HADOOP_PREFIX/conf
export ACCUMULO_HOME="/path/to/accumulo"
export ZOOKEEPER_HOME="/path/to/zookeeper"

export PATH=$PATH:${HADOOP_PREFIX}/bin:${ZOOKEEPER_HOME}/bin:${ACCUMULO_HOME}/bin
