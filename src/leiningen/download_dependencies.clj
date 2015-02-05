(ns leiningen.download-dependencies
  (:use [leiningen.stack-core]))

(def hadoop-location "http://www.trieuvan.com/apache/hadoop/common/hadoop-1.2.1/hadoop-1.2.1.tar.gz")
(def zookeeper-location "http://archive.cloudera.com/cdh4/cdh/4/zookeeper-3.4.5-cdh4.6.0.tar.gz")
(def accumulo-location "https://archive.apache.org/dist/accumulo/1.5.1/accumulo-1.5.1-bin.tar.gz")
(def storm-location "http://mirror.metrocast.net/apache/incubator/storm/apache-storm-0.9.1-incubating/apache-storm-0.9.1-incubating.tar.gz")

(defn
  download-dependencies
  "download dependencies"
  [project & args]
  (let [accumulo-filename (last (clojure.string/split accumulo-location #"/"))]
    (run-command-with-no-args (str "curl " hadoop-location " -o dependencies/"  (last (clojure.string/split hadoop-location #"/"))))
    (run-command-with-no-args (str "curl " zookeeper-location " -o dependencies/"  (last (clojure.string/split zookeeper-location #"/"))))
    (run-command-with-no-args (str "curl " accumulo-location " -o dependencies/"  (last (clojure.string/split accumulo-location #"/"))))
    (run-command-with-no-args (str "curl " storm-location " -o dependencies/"  (last (clojure.string/split storm-location #"/"))))
    (run-command-with-no-args (str "mv dependencies/"  accumulo-filename " dependencies/" (.replaceAll accumulo-filename "-bin" "")))))
