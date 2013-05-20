(ns mailapage.core
  (:gen-class)
  (:require [clojure.java [jdbc :as sql]])
  (:import (java.io File StringWriter BufferedWriter FileInputStream)
           (java.util Properties)
           (javax.mail Message MessagingException PasswordAuthentication Session Transport Authenticator)
           (javax.mail.internet InternetAddress MimeMessage)
           (org.apache.pdfbox.pdmodel PDDocument)
           (org.apache.pdfbox.util PDFTextStripper)))

(def db
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "mailapage.db"
   })


(defn create-db []
  (sql/with-connection
    db
    (sql/db-do-commands
      db
      false
      "CREATE TABLE IF NOT EXISTS bookmarks ( id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, filename TEXT NOT NULL, page INTEGER NOT NULL )"
      "CREATE UNIQUE INDEX IF NOT EXISTS filename_idx ON bookmarks(filename)")))


(defn get-bookmarks []
  (let [output (sql/with-connection
                 db
                 (sql/with-query-results rs ["SELECT filename, page FROM bookmarks"]
                   (doall rs)))
        bookmark-seq (flatten (map #(vector (:filename %) (:page %)) output))]
    (println output)
    (println bookmark-seq)
    (if (empty? bookmark-seq)
      (hash-map)
      (apply hash-map bookmark-seq))))


(defn update-db [bookmarks]
  (let [file-names (keys bookmarks)
        file-names-placeholders (clojure.string/join " OR " (repeat (count file-names) "filename = ?"))
        delete-cmd (str "DELETE FROM bookmarks WHERE filename NOT IN (SELECT filename FROM bookmarks b WHERE " file-names-placeholders " )")
        filename-page-placeholders (clojure.string/join ", " (repeat (count file-names) "(?, ?)"))
        update-cmd (str "INSERT OR REPLACE INTO bookmarks (filename, page) VALUES " filename-page-placeholders)
        bookmark-updater #(vector (first %) (inc (second %)))
        updated-bookmarks (flatten (map bookmark-updater (seq bookmarks)))] ; increment page number
    (sql/with-connection
      db
      (sql/do-prepared delete-cmd file-names)
      (sql/do-prepared update-cmd updated-bookmarks))))


; uses pdfbox library to extract a page from the document
(defn get-pdf-page [file-path page]
  (let [src (File. file-path)
        wr (StringWriter.)
        stripper (PDFTextStripper.)]
    (with-open [pd (PDDocument/load src)
                bwr (BufferedWriter. wr)]
      (.setStartPage stripper page)
      (.setEndPage stripper (inc page))
      (.writeText stripper pd bwr))
    (.toString wr)))


(defn get-current-bookmarks [dir]
  (let [dir-path (.getPath dir)
        bookmarks (get-bookmarks)
        pdf-files (filter #(.endsWith % ".pdf") (.list dir))
        default-bookmarks (zipmap pdf-files (repeat 1))]
    (merge default-bookmarks bookmarks)))


(defn get-pdf-pages [dir-path bookmarks]
  (let [pdf-files (keys bookmarks)
        pdf-pager (fn [file-name]
                    (let [file-path (str dir-path "/" file-name)
                          page (get bookmarks file-name)]
                      (get-pdf-page file-path page)))
        pdf-pages (map pdf-pager pdf-files)]
    (zipmap pdf-files pdf-pages)))


(defn send-mails [pdf-page-map]
  (let [props (doto (Properties.)
                (.load (FileInputStream. "mailer.properties")))
        authenticator (proxy [Authenticator] []
                        (getPasswordAuthentication [] (PasswordAuthentication. (.getProperty props "user.name") (.getProperty props "user.password"))))
        session (.getInstance Session props authenticator)
        addr (InternetAddress. (.getProperty props "user.email"))]
    (doseq [[filename contents] (seq pdf-page-map)]
      (let [message (doto (MimeMessage. session)
                      (.setFrom addr)
                      (.setRecipients Message/RecipientType/TO addr)
                      (.setSubject (str filename ": Page "))
                      (.setText contents))]
        (.send Transport message)))))


(defn -main [& args]
  (if args
    (let [dir-path (first args)
          dir (File. dir-path)]
      ; get the pdf files in the directory and apply the conversion
      (if (.isDirectory dir)
        (do
          (create-db)
          (let [bookmarks (get-current-bookmarks dir)
                pdf-page-map (get-pdf-pages dir-path bookmarks)]
            (send-mails pdf-page-map)
            (update-db bookmarks))
          )))
    (println "USAGE: <program-name> <dir-name>")))

