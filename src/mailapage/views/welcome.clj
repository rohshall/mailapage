(ns mailapage.views.welcome
  (:require [mailapage.views.common :as common]
            [noir.content.getting-started])
  (:use [noir.core :only [defpage]])
  (import '(org.apache.pdfbox.pdmodel PDDocument)
          '(org.apache.pdfbox.util PDFTextStripper)
          '(java.io File OutputStreamWriter FileOutputStream BufferedWriter)))

(defn convert-to-text [src dest]
  (with-open [ pd (PDDocument/load (File. src))
               wr (BufferedWriter. (OutputStreamWriter. (FileOutputStream. (File. dest))))]
    (let [ stripper (PDFTextStripper.)]
      (println "Number of pages" (.getNumberOfPages pd))
      (.writeText stripper pd wr))))

(defpage [:post "/books"] {:keys [filename]}
    (convert-to-text filename (str filename ".txt"))
    (common/layout
      [:p "Welcome to mailapage"]))
