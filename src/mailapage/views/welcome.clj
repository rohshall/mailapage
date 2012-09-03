(ns mailapage.views.welcome
  (:require [mailapage.views.common :as common]
            [noir.response :as resp]
            [noir.content.getting-started])
  (:use noir.core
        hiccup.core
        hiccup.page
        hiccup.form)
  (:import (org.apache.pdfbox.pdmodel PDDocument)
           (org.apache.pdfbox.util PDFTextStripper)
           (java.io File OutputStreamWriter FileOutputStream BufferedWriter)))

(defn convert-to-text [src dest]
  (with-open [ pd (PDDocument/load (File. src))
               wr (BufferedWriter. (OutputStreamWriter. (FileOutputStream. (File. dest))))]
    (let [ stripper (PDFTextStripper.)]
      (println "Number of pages" (.getNumberOfPages pd))
      (.writeText stripper pd wr))))

(defpartial book-fields [{:keys [title location]}]
  (label "title" "Title: ")
  (text-field "title" title)
  (label "location" "Location: ")
  (text-field "location" location))

(defpage "/book/add" {:as book}
  (common/layout
    (form-to [:post "/book/add"]
            (book-fields book)
            (submit-button "Add book"))))

(defn valid? [{:keys [title location]}]
  true)

(defpage [:post "/book/add"] {:keys [title location] :as book}
  (if (valid? book)
    (do
      (convert-to-text location (str location ".txt"))
      (common/layout
        [:p "Book added"]))
    (render "/book/add" book)))
