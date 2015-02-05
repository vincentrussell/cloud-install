(ns leiningen.install-stack
  (:use [leiningen.stack-core])
  (:require [leiningen.download-dependencies :as deps])
  (:import [javax.swing JFileChooser JOptionPane]
           [java.io File]))

(def cloud-install-install-directory "cloud-install")
(def hadoop-version "hadoop-1.2.1")
(def hadoop-dist-location (str "dependencies/" hadoop-version ".tar.gz"))
(def zookeeper-version "zookeeper-3.4.5-cdh4.6.0")
(def zookeeper-dist-location (str "dependencies/" zookeeper-version ".tar.gz"))
(def accumulo-version "accumulo-1.5.1")
(def accumulo-dist-location (str "dependencies/" accumulo-version ".tar.gz"))
(def storm-version "apache-storm-0.9.1-incubating")
(def storm-dist-location (str "dependencies/" storm-version ".tar.gz"))

(defn replace-text-in-file
  [file-path regex-replacement-map]
  (doseq [keyval regex-replacement-map]
    (let [file-text (slurp file-path)
          new-text (.replaceAll file-text (key keyval) (val keyval))]
      (spit file-path new-text))))


(defn install-hadoop
  [install-directory install-locs-map]
  (let [source-dest hadoop-dist-location
        destination-file-name (last (clojure.string/split source-dest #"/"))
        destination-full-path (str install-directory "/" destination-file-name)
        core-site-full-path (str install-directory "/" hadoop-version "/conf/core-site.xml")
        hdfs-site-full-path (str install-directory "/" hadoop-version "/conf/hdfs-site.xml")
        mapred-site-full-path (str install-directory "/" hadoop-version "/conf/mapred-site.xml")
        hadoop-config-full-path (str install-directory "/" hadoop-version "/bin/hadoop-config.sh")
        slaves-full-path (str install-directory "/" hadoop-version "/conf/slaves")]
    (copy-file source-dest destination-full-path)
    (shell-out "tar" "xfz" destination-full-path "-C" install-directory)
    (spit core-site-full-path
          "<?xml version=\"1.0\"?>
          <?xml-stylesheet type=\"text/xsl\" href=\"configuration.xsl\"?>
          
          <configuration>
               <property>
                   <name>fs.default.name</name>
                   <value>hdfs://localhost:9000</value>
               </property>
          </configuration>")
    (spit hdfs-site-full-path
          "<?xml version=\"1.0\"?>
          <?xml-stylesheet type=\"text/xsl\" href=\"configuration.xsl\"?>
          
          <configuration>
              <property>
                  <name>dfs.replication</name>
                  <value>1</value>
              </property>
          </configuration>")
    (spit mapred-site-full-path
          "<?xml version=\"1.0\"?>
          <?xml-stylesheet type=\"text/xsl\" href=\"configuration.xsl\"?>
          
          <configuration>
              <property>
                  <name>mapred.job.tracker</name>
                  <value>localhost:9001</value>
              </property>
          </configuration>")
    (spit slaves-full-path "localhost\n")
    (prn "Formatting name node!")
    (shell-out (str install-directory "/" hadoop-version "/bin/hadoop") "namenode" "-format" "-force" )
    (prn "Done Formatting name node!")
    (shell-out "rm" destination-full-path)))

(defn install-storm
  [install-directory install-locs-map]
  (let [source-dest storm-dist-location
        destination-file-name (last (clojure.string/split source-dest #"/"))
        destination-full-path (str install-directory "/" destination-file-name)]
    (copy-file source-dest destination-full-path)
    (shell-out "tar" "xfz" destination-full-path "-C" install-directory)
    (prn "Initializing Storm!")
    (shell-out "rm" destination-full-path)))

(defn install-zookeeper
  [install-directory install-locs-map]
  (let [source-dest zookeeper-dist-location
        destination-file-name (last (clojure.string/split source-dest #"/"))
        destination-full-path (str install-directory "/" destination-file-name)
        zoo-cfg-full-path (str install-directory "/" zookeeper-version "/conf/zoo.cfg")]
    (copy-file source-dest destination-full-path)
    (shell-out "tar" "xfz" destination-full-path "-C" install-directory)
    (spit zoo-cfg-full-path (str "tickTime=2000\ninitLimit=10\nsyncLimit=5\ndataDir=" (str install-directory "/" zookeeper-version "/dataDir") "\nclientPort=2181\n"))
    (prn "Initializing Zookeeper!")
    (run-command-with-no-args (str install-directory "/" zookeeper-version "/bin/zkServer-initialize.sh"))
    (shell-out "rm" destination-full-path)))

(defn install-accumulo
  [install-directory install-locs-map]
  (let [source-dest accumulo-dist-location
        destination-file-name (last (clojure.string/split source-dest #"/"))
        destination-full-path (str install-directory "/" destination-file-name)
        accumulo-site-xml-file (str install-directory "/" accumulo-version "/conf/accumulo-site.xml")
        accumulo-env-full-path (str install-directory "/" accumulo-version "/conf/accumulo-env.sh")]
    (copy-file source-dest destination-full-path)
    (shell-out "tar" "xfz" destination-full-path "-C" install-directory)
    (flat-copy (File. (str install-directory "/" accumulo-version "/conf/examples/1GB/standalone" )) (File. (str install-directory "/" accumulo-version "/conf" )))
    (spit accumulo-site-xml-file  (.replaceAll (slurp accumulo-site-xml-file) "\\$HADOOP_PREFIX\\/lib\\/\\[\\^\\.\\]\\.\\*\\.jar,"  "\\$HADOOP_PREFIX\\/lib\\/\\[\\^\\.\\]\\.\\*\\.jar,"))
    (replace-text-in-file accumulo-env-full-path {"/path/to/zookeeper" (get install-locs-map :zookeeper) "/path/to/hadoop" (get install-locs-map :hadoop)})
    (shell-out "rm" destination-full-path)))


(defn install-scripts
  [install-directory install-locs-map]
  (let [zookeeper-server-sh-file (str install-directory "/zookeeper-server.sh")
        zookeeper-init-sh-file (str install-directory "/zookeeper-init.sh")
        start-hadoop-sh-file (str install-directory "/start-hadoop.sh")
        stop-hadoop-sh-file (str install-directory "/stop-hadoop.sh")
        stop-zookeeper-sh-file (str install-directory "/stop-zookeper.sh")
        start-zookeeper-sh-file (str install-directory "/start-zookeeper.sh")
        cloud-install-bash-include-sh-file (str install-directory "/cloud-install-bash-include.sh")
        chmod-script (str install-directory "/chmod-script.sh")]
    (flat-copy (File. "dependencies/scripts") (File. install-directory))
    (replace-text-in-file zookeeper-server-sh-file {"/path/to/zookeeper" (get install-locs-map :zookeeper)})
    (replace-text-in-file  zookeeper-init-sh-file {"/path/to/zookeeper" (get install-locs-map :zookeeper)})
    (replace-text-in-file start-hadoop-sh-file {"/path/to/hadoop" (get install-locs-map :hadoop)})
    (replace-text-in-file stop-hadoop-sh-file {"/path/to/hadoop" (get install-locs-map :hadoop)})
    (replace-text-in-file cloud-install-bash-include-sh-file {"/path/to/hadoop" (get install-locs-map :hadoop)})
    (replace-text-in-file cloud-install-bash-include-sh-file {"/path/to/accumulo" (get install-locs-map :accumulo) "/path/to/zookeeper" (get install-locs-map :zookeeper)})
    (spit chmod-script (str "#!/bin/sh\n\nfor file in " install-directory "/*.sh; do chmod +x $file; done"))
    (run-command-with-no-args  (str "chmod +x " chmod-script))
    (run-command-with-no-args  (str "sh " chmod-script))
    (shell-out "rm" chmod-script)))

(defn configure-accumulo
  [install-directory install-locs-map accumulo-instance-name accumulo-root-password]
  (let [zookeeper-server-sh-file (str install-directory "/zookeeper-server.sh")
        start-hadoop-sh-file (str install-directory "/start-hadoop.sh")
        stop-hadoop-sh-file (str install-directory "/stop-hadoop.sh")
        stop-zookeeper-sh-file (str install-directory "/stop-zookeper.sh")
        start-zookeeper-sh-file (str install-directory "/start-zookeeper.sh")
        cloud-install-bash-include-sh-file (str install-directory "/cloud-install-bash-include.sh")
        chmod-script (str install-directory "/chmod-script.sh")
        initialize-script-path (str install-directory "/" "initialize-accumulo.sh")]
        (replace-text-in-file initialize-script-path {"instance_name" accumulo-instance-name "root_password" accumulo-root-password})
        (prn "Starting Hadoop cluster, Initializing Accumulo cluster and then Stopping Hadoop cluster!")
        (run-command-with-no-args  (str "chmod +x " initialize-script-path))
        (shell-err "sh" initialize-script-path)
        (shell-out "rm" initialize-script-path)))

(defn check-dependencies
  [project args]
  (let [hadoop-dep-file (File. hadoop-dist-location)
        accumulo-dep-file (File. accumulo-dist-location)
        zookeeper-dep-file (File. zookeeper-dist-location)
        storm-dep-file (File. storm-dist-location)]
    (if (or (not (.exists hadoop-dep-file)) (not (.exists accumulo-dep-file)) (not (.exists zookeeper-dep-file)) (not (.exists storm-dep-file)))
        (deps/download-dependencies project args))
    (prn "Download complete!")))

(defn
  install-stack
  "install the cloud stack"
  ([project]
    (do
      (prn (str "Please run the script like this" (newline)
    "lein install-stack <<install_dir>> <<accumulo_instance_name>> <<accumulo_root_password>>"))
      (System/exit 1)))
  ([project & args]
  (do
    (prn (str "We are going to download the big files in order to not have to check them into git." (newline) (newline)
     "THIS MAY TAKE A WHILE TO DOWNLOAD THEM!"))
    (check-dependencies project args)
    (let [selected-directory (if (seq args) (first args) (select-directory))
          install-directory (str selected-directory (if (.endsWith selected-directory "/") "" "/") cloud-install-install-directory)
          selected-directory-file (File. selected-directory)
          accumulo-instance-name (if (= 3 (count (seq args))) (nth args 1) "accumulo")
          accumulo-root-password (if (= 3 (count (seq args))) (nth args 2) "secret")
          install-locs-map {:zookeeper (str install-directory "/" zookeeper-version) :accumulo (str install-directory "/" accumulo-version) :hadoop (str install-directory "/" hadoop-version)}]
      (if (empty? (System/getenv "JAVA_HOME"))
        (do (prn "JAVA_HOME environment variable not set")
            (System/exit 1)))
      (prn (str "Cloud will be installed in: " install-directory (newline) (newline)))
      (run-command-with-no-args  (str "mkdir -p " install-directory))
      (install-storm install-directory install-locs-map)
      (install-hadoop install-directory install-locs-map)
      (install-zookeeper install-directory install-locs-map)
      (install-accumulo install-directory install-locs-map)
      (install-scripts install-directory install-locs-map)
      (prn "Configuring accumulo!")
      (configure-accumulo install-directory install-locs-map accumulo-instance-name accumulo-root-password)
      (prn "Completed Successfully!")
      (prn (str "And we're done.  You should add the following to your
      bashrc in order to be able to run some of the executables like accumulo:" (newline) (newline)
      "source " (str install-directory "/cloud-install-bash-include.sh")))
      (prn (str (newline) (newline) "You may also need to add your hostname to /etc/hosts; especially if accumulo wont start"))
      ))))
