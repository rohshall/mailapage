(ns mailapage.core
  (:gen-class)
  (:use [clojure.java.jdbc])
  (:import (java.io File BufferedWriter OutputStreamWriter FileOutputStream IOException)
           (org.apache.pdfbox.pdmodel PDDocument)
           (org.apache.pdfbox.util PDFTextStripper)))

(def db
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "db/database.db"
   })


(defn create-db []
  (try (with-connection db
         (create-table :bookmarks
                       [:title :text]
                       [:page :int]))
       (catch Exception e (println e))))


(defn print-db []
  (let [output (with-connection db
                 (with-query-results rs ["select * from bookmarks"] (doall rs)))]
    (println (keys (first output)))))


; the main workhorse function that does the conversion
(defn process-file [dirName fileName]
  (let [src (File. (str dirName "/" fileName))
        dest (File. (str src ".txt"))
        stripper (PDFTextStripper.)]
    (if (.exists src)
      (with-open [pd (PDDocument/load src)
                  wr (BufferedWriter. (OutputStreamWriter. (FileOutputStream. dest)))]
        (println (.getName src) "pages:" (.getNumberOfPages pd))
        (.writeText stripper pd wr))
      (with-connection db
        (insert-records :bookmarks {:title (.getName src) :page 1})))))


(defn -main [& args]
  (if args
    (let [dirName (first args)
          dir (File. dirName)]
      ; get the pdf files in the directory and apply the conversion
      (if (.isDirectory dir)
        (do
          (create-db)
          (doseq [fileName (.list dir)]
            (if (.endsWith fileName ".pdf")
              (process-file dirName fileName)))
          (print-db))))
    (println "USAGE: <program-name> <dir-name>")))

