(ns leiningen.download-dependencies
  (:use [leiningen.stack-core]))

(def hadoop-version "hadoop-2.6.0-cdh5.4.7")
(def zookeeper-version "zookeeper-3.4.5-cdh5.4.7")
(def accumulo-version "accumulo-1.7.0")
(def storm-version "apache-storm-0.9.6")
(def spark-version "spark-1.5.2-bin-hadoop2.6")

(def hadoop-dist-location (str "dependencies/hadoop.tar.gz"))
(def zookeeper-dist-location (str "dependencies/zookeeper.tar.gz"))
(def accumulo-dist-location (str "dependencies/accumulo.tar.gz"))
(def storm-dist-location (str "dependencies/storm.tar.gz"))
(def spark-dist-location (str "dependencies/spark.tar.gz"))

(def hadoop-location "http://archive.cloudera.com/cdh5/cdh/5/hadoop-2.6.0-cdh5.4.7.tar.gz")
(def zookeeper-location "http://archive.cloudera.com/cdh5/cdh/5/zookeeper-3.4.5-cdh5.4.7.tar.gz")
(def accumulo-location "https://archive.apache.org/dist/accumulo/1.7.0/accumulo-1.7.0-bin.tar.gz")
(def storm-location "http://mirror.metrocast.net/apache/storm/apache-storm-0.9.6/apache-storm-0.9.6.tar.gz")
(def spark-location "http://apache.arvixe.com/spark/spark-1.5.2/spark-1.5.2-bin-hadoop2.6.tgz")

(defn
  download-dependencies
  "download dependencies"
  [project & args]
  (let [accumulo-filename (last (clojure.string/split accumulo-location #"/"))]
    (run-command-with-no-args (str "curl " hadoop-location " -o " hadoop-dist-location))
    (run-command-with-no-args (str "curl " zookeeper-location " -o " zookeeper-dist-location  ))
    (run-command-with-no-args (str "curl " accumulo-location " -o " accumulo-dist-location ))
    (run-command-with-no-args (str "curl " storm-location " -o " storm-dist-location ))
    (run-command-with-no-args (str "curl " spark-location " -o " spark-dist-location ))))