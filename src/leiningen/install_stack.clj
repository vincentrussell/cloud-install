(ns leiningen.install-stack
  (:use [leiningen.stack-core])
  (:require [leiningen.download-dependencies :as deps]
            [leiningen.core.main :as main]
            [clojure.data.xml :as xml]
            [clojure.tools.cli :refer [parse-opts]])
  (:import [java.io File]
           [java.lang IllegalStateException]
           [org.apache.commons.io FileUtils])
  (:gen-class))

(defn replace-text-in-file
  [file-path regex-replacement-map]
  (doseq [keyval regex-replacement-map]
    (let [file-text (slurp file-path)
          new-text (.replaceAll file-text (key keyval) (val keyval))]
      (spit file-path new-text))))


(defn hdfs-site-config
  [data-node-dir name-node-dir]
  (xml/emit-str
    (xml/element :configuration {}
                 (xml/element :property {}
                              (xml/element :name {} "dfs.datanode.data.dir" )
                              (xml/element :value {} (str "file://" data-node-dir))
                              (xml/element :description {} "Comma separated list of paths on the local filesystem of a DataNode where it should store its blocks." ))
                 (xml/element :property {}
                              (xml/element :name {} "dfs.namenode.name.dir" )
                              (xml/element :value {} (str "file://" name-node-dir))
                              (xml/element :description {} "Path on the local filesystem where the NameNode stores the namespace and transaction logs persistently." ))
                 )))

(defn hdfs-core-config []
  (xml/emit-str
    (xml/element :configuration {}
                 (xml/element :property {}
                              (xml/element :name {} "fs.defaultFS" )
                              (xml/element :value {} "hdfs://localhost/")
                              (xml/element :description {} "NameNode URI" ))
                 )))


(defn hadoop-yarn-config []
  (xml/emit-str
    (xml/element :configuration {}
                 (xml/element :property {}
                              (xml/element :name {} "yarn.scheduler.minimum-allocation-mb" )
                              (xml/element :value {} "128"))
                 (xml/element :property {}
                              (xml/element :name {} "yarn.scheduler.maximum-allocation-mb" )
                              (xml/element :value {} "1048"))
                 (xml/element :property {}
                              (xml/element :name {} "yarn.scheduler.minimum-allocation-vcores" )
                              (xml/element :value {} "1"))
                 (xml/element :property {}
                              (xml/element :name {} "yarn.scheduler.maximum-allocation-vcores" )
                              (xml/element :value {} "2"))
                 (xml/element :property {}
                              (xml/element :name {} "yarn.scheduler.maximum-allocation-vcores" )
                              (xml/element :value {} "1096"))
                 (xml/element :property {}
                              (xml/element :name {} "yarn.nodemanager.resource.cpu-vcores" )
                              (xml/element :value {} "1"))
                 (xml/element :property {}
                              (xml/element :name {} "yarn.nodemanager.aux-services" )
                              (xml/element :value {} "mapreduce_shuffle"))
                 (xml/element :property {}
                              (xml/element :name {} "yarn.resourcemanager.hostname" )
                              (xml/element :value {} "localhost"))
                 )))

(defn hadoop-mapred-config []
  (xml/emit-str
    (xml/element :configuration {}
                 (xml/element :property {}
                              (xml/element :name {} "yarn.app.mapreduce.am.resource.mb" )
                              (xml/element :value {} "1024"))
                 (xml/element :property {}
                              (xml/element :name {} "yarn.app.mapreduce.am.command-opts" )
                              (xml/element :value {} "-Xmx768m"))
                 (xml/element :property {}
                              (xml/element :name {} "mapreduce.framework.name" )
                              (xml/element :value {} "yarn"))
                 (xml/element :property {}
                              (xml/element :name {} "mapreduce.map.cpu.vcores" )
                              (xml/element :value {} "1"))
                 (xml/element :property {}
                              (xml/element :name {} "mapreduce.reduce.cpu.vcores" )
                              (xml/element :value {} "1"))
                 (xml/element :property {}
                              (xml/element :name {} "mapreduce.map.memory.mb" )
                              (xml/element :value {} "1024"))
                 (xml/element :property {}
                              (xml/element :name {} "mapreduce.map.java.opts" )
                              (xml/element :value {} "-Xmx768m"))
                 (xml/element :property {}
                              (xml/element :name {} "mapreduce.reduce.memory.mb" )
                              (xml/element :value {} "1024"))
                 (xml/element :property {}
                              (xml/element :name {} "mapreduce.reduce.java.opts" )
                              (xml/element :value {} "-Xmx768m"))
                 )))


(defn install-hadoop
  [install-directory install-locs-map]
  (let [source-dest deps/hadoop-dist-location
        destination-file-name (last (clojure.string/split source-dest #"/"))
        destination-full-path (str install-directory "/" destination-file-name)
        core-site-full-path (str install-directory "/" deps/hadoop-version "/etc/hadoop/core-site.xml")
        hdfs-site-full-path (str install-directory "/" deps/hadoop-version "/etc/hadoop/hdfs-site.xml")
        yarn-site-full-path (str install-directory "/" deps/hadoop-version "/etc/hadoop/yarn-site.xml")
        slaves-full-path (str install-directory "/" deps/hadoop-version "/etc/hadoop/slaves")
        data-node-dir (str install-directory "/" deps/hadoop-version "/cache/hadoop/dfs/data")
        name-node-dir (str install-directory "/" deps/hadoop-version "/cache/hadoop/dfs/name")
        ]
    (prn "Installing Hadoop!")
    (copy-file source-dest destination-full-path)
    (shell-out "tar" "xfz" destination-full-path "-C" install-directory)
    (.mkdirs (File. data-node-dir))
    (.mkdirs (File. name-node-dir))
    (flat-copy (File. (str install-directory "/" deps/hadoop-version "/etc/hadoop" )) (File. (str install-directory "/" deps/hadoop-version "/etc/hadoop" )))
    (spit hdfs-site-full-path (hdfs-site-config data-node-dir name-node-dir))
    (spit core-site-full-path (hdfs-core-config))
    (spit yarn-site-full-path (hadoop-yarn-config))
    (spit slaves-full-path "localhost\n")
    (prn "Formatting name node!")
    (shell-out (str install-directory "/" deps/hadoop-version "/bin/hdfs") "namenode" "-format" "-force" )
    (prn "Done Formatting name node!")
    (shell-out "rm" destination-full-path)))

(defn install-storm
  [install-directory install-locs-map]
  (let [source-dest deps/storm-dist-location
        destination-file-name (last (clojure.string/split source-dest #"/"))
        destination-full-path (str install-directory "/" destination-file-name)]
    (copy-file source-dest destination-full-path)
    (shell-out "tar" "xfz" destination-full-path "-C" install-directory)
    (prn "Installing Storm!")
    (shell-out "rm" destination-full-path)))

(defn install-zookeeper
  [install-directory install-locs-map]
  (let [source-dest deps/zookeeper-dist-location
        destination-file-name (last (clojure.string/split source-dest #"/"))
        destination-full-path (str install-directory "/" destination-file-name)
        zoo-cfg-full-path (str install-directory "/" deps/zookeeper-version "/conf/zoo.cfg")]
    (copy-file source-dest destination-full-path)
    (shell-out "tar" "xfz" destination-full-path "-C" install-directory)
    (spit zoo-cfg-full-path (str "tickTime=2000\ninitLimit=10\nsyncLimit=5\ndataDir=" (str install-directory "/" deps/zookeeper-version "/dataDir") "\nclientPort=2181\n"))
    (prn "Installing Zookeeper!")
    (run-command-with-no-args (str install-directory "/" deps/zookeeper-version "/bin/zkServer-initialize.sh"))
    (shell-out "rm" destination-full-path)))

(defn install-accumulo
  [install-directory install-locs-map]
  (let [source-dest deps/accumulo-dist-location
        destination-file-name (last (clojure.string/split source-dest #"/"))
        destination-full-path (str install-directory "/" destination-file-name)
        accumulo-site-xml-file (str install-directory "/" deps/accumulo-version "/conf/accumulo-site.xml")
        accumulo-env-full-path (str install-directory "/" deps/accumulo-version "/conf/accumulo-env.sh")]
    (prn "Installing Accumulo!")
    (copy-file source-dest destination-full-path)
    (shell-out "tar" "xfz" destination-full-path "-C" install-directory)
    (flat-copy (File. (str install-directory "/" deps/accumulo-version "/conf/examples/1GB/standalone" )) (File. (str install-directory "/" deps/accumulo-version "/conf" )))
    (spit accumulo-site-xml-file  (.replaceAll (slurp accumulo-site-xml-file) "\\$HADOOP_PREFIX\\/lib\\/\\[\\^\\.\\]\\.\\*\\.jar,"  "\\$HADOOP_PREFIX\\/lib\\/\\[\\^\\.\\]\\.\\*\\.jar,\n\\$HADOOP_PREFIX/share/hadoop/common/.*.jar,\n\\$HADOOP_PREFIX/share/hadoop/common/lib/.*.jar,\n\\$HADOOP_PREFIX/share/hadoop/hdfs/.*.jar,\n\\$HADOOP_PREFIX/share/hadoop/mapreduce/.*.jar,\n\\$HADOOP_PREFIX/share/hadoop/yarn/.*.jar,"))
    (replace-text-in-file accumulo-env-full-path {"/path/to/zookeeper" (get install-locs-map :zookeeper) "/path/to/hadoop" (get install-locs-map :hadoop)})
    (shell-out "rm" destination-full-path)))


(defn install-spark
  [install-directory install-locs-map]
  (let [source-dest deps/spark-dist-location
        destination-file-name (last (clojure.string/split source-dest #"/"))
        destination-full-path (str install-directory "/" destination-file-name)
        core-site-full-path (str install-directory "/" deps/spark-version "/etc/hadoop/core-site.xml")
        hdfs-site-full-path (str install-directory "/" deps/spark-version "/etc/hadoop/hdfs-site.xml")
        yarn-site-full-path (str install-directory "/" deps/spark-version "/etc/hadoop/yarn-site.xml")
        slaves-full-path (str install-directory "/" deps/spark-version "/conf/slaves")
        data-node-dir (str install-directory "/" deps/spark-version "/cache/hadoop/dfs/data")
        name-node-dir (str install-directory "/" deps/spark-version "/cache/hadoop/dfs/name")
        ]
    (prn "Installing Spark!")
    (copy-file source-dest destination-full-path)
    (shell-out "tar" "xfz" destination-full-path "-C" install-directory)
    ;(flat-copy (File. (str install-directory "/" deps/ "/etc/hadoop" )) (File. (str install-directory "/" deps/ "/etc/hadoop" )))
    ;(spit hdfs-site-full-path (hdfs-site-config data-node-dir name-node-dir))
    ;(spit core-site-full-path (hdfs-core-config))
    ;(spit yarn-site-full-path (hadoop-yarn-config))
    (spit slaves-full-path "localhost\n")
    ;(prn "Formatting name node!")
    ;(shell-out (str install-directory "/" deps/ "/bin/hdfs") "namenode" "-format" "-force" )
    ;(prn "Done Formatting name node!")
    (shell-out "rm" destination-full-path)
    ))


(defn install-scripts
  [install-directory install-locs-map]
  (let [zookeeper-server-sh-file (str install-directory "/zookeeper-server.sh")
        zookeeper-init-sh-file (str install-directory "/zookeeper-init.sh")
        start-hadoop-sh-file (str install-directory "/start-hadoop.sh")
        stop-hadoop-sh-file (str install-directory "/stop-hadoop.sh")
        cloud-install-bash-include-sh-file (str install-directory "/cloud-install-bash-include.sh")
        chmod-script (str install-directory "/chmod-script.sh")]
    (flat-copy (File. "dependencies/scripts") (File. install-directory))
    (replace-text-in-file zookeeper-server-sh-file {"/path/to/zookeeper" (get install-locs-map :zookeeper)})
    (replace-text-in-file  zookeeper-init-sh-file {"/path/to/zookeeper" (get install-locs-map :zookeeper)})
    (replace-text-in-file start-hadoop-sh-file {"/path/to/hadoop" (get install-locs-map :hadoop)})
    (replace-text-in-file stop-hadoop-sh-file {"/path/to/hadoop" (get install-locs-map :hadoop)})
    (replace-text-in-file cloud-install-bash-include-sh-file {"/path/to/hadoop" (get install-locs-map :hadoop)})
    (replace-text-in-file cloud-install-bash-include-sh-file {"/path/to/spark" (get install-locs-map :spark)})
    (replace-text-in-file cloud-install-bash-include-sh-file {"/path/to/accumulo" (get install-locs-map :accumulo) "/path/to/zookeeper" (get install-locs-map :zookeeper)})
    (spit chmod-script (str "#!/bin/sh\n\nfor file in " install-directory "/*.sh; do chmod +x $file; done"))
    (run-command-with-no-args  (str "chmod +x " chmod-script))
    (run-command-with-no-args  (str "sh " chmod-script))
    (shell-out "rm" chmod-script)))

(defn configure-accumulo
  [install-directory install-locs-map accumulo-instance-name accumulo-root-password]
  (let [initialize-script-path (str install-directory "/" "initialize-accumulo.sh")]
        (replace-text-in-file initialize-script-path {"instance_name" accumulo-instance-name "root_password" accumulo-root-password})
        (main/info "Starting Hadoop cluster, Initializing Accumulo cluster and then Stopping Hadoop cluster!")
        (run-command-with-no-args  (str "chmod +x " initialize-script-path))
        (shell-err "sh" initialize-script-path)
        (shell-out "rm" initialize-script-path)))

(defn check-dependencies
  []
  (let [hadoop-dep-file (File. deps/hadoop-dist-location)
        accumulo-dep-file (File. deps/accumulo-dist-location)
        zookeeper-dep-file (File. deps/zookeeper-dist-location)
        storm-dep-file (File. deps/storm-dist-location)
        spark-dep-file (File. deps/spark-dist-location)]
    (if (or (not (.exists hadoop-dep-file)) (not (.exists accumulo-dep-file)) (not (.exists zookeeper-dep-file))
            (not (.exists storm-dep-file)) (not (.exists spark-dep-file)) )
        (deps/download-dependencies))
    (main/info "Download complete!")))


(defn is-valid-directory
  [directory]
    (try
      (let [directory-object (File. directory)]
        (.isDirectory directory-object))
      (catch Exception e false)))

(defn is-not-blank
  [string]
  (not (clojure.string/blank? string)))

(def cli-options
  ;; An option with a required argument
  [["-d" "--directory INSTALL_DIRECTORY"
    :required "Directory where stack will be installed"
    :validate [#(is-valid-directory %) "must be a valid directory"
               #(is-not-blank %) "directory must not be blank"]]
   ["-i" "--instance ACCUMULO_INSTANCE_NAME" "Accumulo Instance Name"
    :default "accumulo"
    :validate [#(is-not-blank %) "instance name must not be blank"]]
   ["-p" "--password ACCUMULO_ROOT_PASSWORD" "Accumulo Root Password"
    :default "secret"
    :validate [#(is-not-blank %) "password must not be blank"]]
   ;; A boolean option defaulting to nil
   ["-f" "--force"]])



(defn
  install-stack
  "This will install a standalone hadoop/accumulo/spark cloud to a specified directory
  -d: <directory>  The directory where to install the standalone cloud
  -i: <instance> (defaults to accumulo) The accumulo instance name
  -p: <password> (defaults to secret) The accumulo root password
  -f: will delete the install directory first if it exists
  -h: this help dialog
  -?: this help dialog
  "
  ([project]
   (main/abort "Required arguments missing:
   run: \"lein install-stack -h\" for help"))
  ([project & args]
   (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
     (if (clojure.string/blank? (:directory options))
       (main/abort "errors: Failed to validate \"-d \": directory is required and cannot be blank"))
     (if (clojure.string/blank? (System/getenv "JAVA_HOME"))
       (main/abort "JAVA_HOME environment variable not set"))
     (if (not (nil? errors))
       (main/abort (str "errors: " (clojure.string/join ", " (seq errors)))))
     (let [selected-directory (:directory options)
           install-dir (.getAbsolutePath (File. selected-directory))
           install-directory (str install-dir (if (.endsWith install-dir "/") "" "/"))
           install-directory-file (File. install-directory)
           accumulo-instance-name (:instance options)
           accumulo-root-password (:password options)
           install-locs-map {:zookeeper (str install-directory deps/zookeeper-version)
                             :accumulo (str install-directory deps/accumulo-version)
                             :hadoop (str install-directory deps/hadoop-version)
                             :spark (str install-directory deps/spark-version)}]

       (main/info "We are going to download the big files in order to not have to check them into git.

                  THIS MAY TAKE A WHILE TO DOWNLOAD THEM!")
       (check-dependencies)

       (if (true? (:force options))
         (do
           (FileUtils/forceMkdir install-directory-file)
           (FileUtils/cleanDirectory install-directory-file)
           ))

       (if  (or
              (and
                (.exists install-directory-file)
                (not (-> install-directory-file .list empty?)))
              (not (.exists install-directory-file)))
         (main/abort (str install-directory " exists and is not empty use -f to force install")))
       (main/info (str "Cloud will be installed in: " install-directory "\n\n"))
       (FileUtils/forceMkdir install-directory-file)
       (install-storm install-directory install-locs-map)
       (install-hadoop install-directory install-locs-map)
       (install-zookeeper install-directory install-locs-map)
       (install-accumulo install-directory install-locs-map)
       (install-spark install-directory install-locs-map)
       (install-scripts install-directory install-locs-map)
       (main/info "Configuring accumulo!")
       (configure-accumulo install-directory install-locs-map accumulo-instance-name accumulo-root-password)
       (main/info "And we're done!")
       (main/info "You will also need to add the following to your .bashrc (or run it every time you want to start the cloud)")
       (main/info (str "source " (str install-directory "cloud-install-bash-include.sh")))
       (main/info (str "You may also need to add your hostname to /etc/hosts; especially if accumulo wont start, ex:"))
       (main/info (str "127.0.0.1   localhost vrussell Vincents-MacBook-Pro.local Vincents-MacBook-Pro.home"))))))
