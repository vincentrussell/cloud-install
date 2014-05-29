(ns leiningen.download-dependencies
  (:use [leiningen.stack-core]))

(def hadoop-location "http://archive.cloudera.com/cdh4/cdh/4/hadoop-2.0.0-cdh4.6.0.tar.gz")
(def zookeeper-location "http://archive.cloudera.com/cdh4/cdh/4/zookeeper-3.4.5-cdh4.6.0.tar.gz")
(def accumulo-location "http://mirrors.ibiblio.org/apache/accumulo/1.5.1/accumulo-1.5.1-bin.tar.gz")

(defn
  download-dependencies
  "download dependencies"
  [project & args]
  (let [accumulo-filename (last (clojure.string/split accumulo-location #"/"))]
    (run-command-with-no-args (str "curl " hadoop-location " -o dependencies/"  (last (clojure.string/split hadoop-location #"/"))))
    (run-command-with-no-args (str "curl " zookeeper-location " -o dependencies/"  (last (clojure.string/split zookeeper-location #"/"))))
    (run-command-with-no-args (str "curl " accumulo-location " -o dependencies/"  (last (clojure.string/split accumulo-location #"/"))))
    (run-command-with-no-args (str "mv dependencies/"  accumulo-filename " dependencies/" (.replaceAll accumulo-filename "-bin" "")))))