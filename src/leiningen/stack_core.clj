(ns leiningen.stack-core
  (:require [clojure.string :as string]
            [clojure.java.shell :as shell]
            [clojure.string :as string]
            [clojure.java.io :as io])
  (:import [javax.swing JFileChooser JOptionPane]
           [java.io File]
           [java.io ByteArrayOutputStream StringWriter]
           [java.nio.charset Charset]))


(defn shell-out [& args]
  (:out (apply shell/sh args)))

(defn shell-err [& args]
  (:err (apply shell/sh args)))



(defn- stream-to-bytes
  [in]
  (with-open [bout (ByteArrayOutputStream.)]
    (io/copy in bout)
    (.toByteArray bout)))


(defn- stream-to-string
  ([in] (stream-to-string in (.name (Charset/defaultCharset))))
  ([in enc]
    (with-open [bout (StringWriter.)]
      (io/copy in bout :encoding enc)
      (.toString bout))))

(defn- stream-to-enc
  [stream enc]
  (if (= enc :bytes)
    (stream-to-bytes stream)
    (stream-to-string stream enc)))


(defn run-command-with-no-args [command]
  (let [proc (.exec (Runtime/getRuntime) command)]
    (with-open [stdout (.getInputStream proc)
                stderr (.getErrorStream proc)]
      (let [out (future (stream-to-enc stdout "UTF-8"))
            err (future (stream-to-string stderr))
            exit-code (.waitFor proc)
            results {:exit exit-code :out @out :err @err}]
        (if-not (= 0 (:exit results))
          (throw (Exception. (:err results))))))))



(defn current-directory []
  (.getAbsolutePath (File. ".")))

(defn get-input [prompt]
  (-> prompt
    (JOptionPane/showInputDialog)
    (str)
    (.trim)))

(defn select-directory []
  (let [file-chooser (JFileChooser. (current-directory))]
    (.. file-chooser (setDialogTitle "Choose a directory where you want to install the cloud stack. (eg. /opt/cloud)"))
    (.. file-chooser (setFileSelectionMode JFileChooser/DIRECTORIES_ONLY))
    (let [option (.showOpenDialog file-chooser nil)]
      (if (= option JFileChooser/APPROVE_OPTION)
        (.getAbsolutePath (.getSelectedFile file-chooser))
        (throw (Exception. "You must select a directory where you would like to install the stack"))))))

(defn display-message
  [message]
  (JOptionPane/showMessageDialog nil message))

(defn copy-file [source-path dest-path]
  (io/copy (io/file source-path) (io/file dest-path)))

(defn flat-copy [from to]
  (doseq [f (.listFiles (io/as-file from))]
    (let [fn  (.getName (io/as-file f))]
      (if (.isDirectory f)
        nil
        (do
          (io/make-parents to fn)
          (io/copy f (io/file to fn)))))))
