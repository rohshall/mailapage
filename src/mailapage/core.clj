(ns mailapage.core
  (:gen-class)
  (:import (java.io File BufferedWriter OutputStreamWriter FileOutputStream IOException)
           (org.apache.pdfbox.pdmodel PDDocument)
           (org.apache.pdfbox.util PDFTextStripper)))


; the main workhorse function that does the conversion
(defn convert-to-text [dirName fileName]
  (let [src (str dirName "/" fileName)
        dest (str src ".txt")
        stripper (PDFTextStripper.)]
    (with-open [pd (PDDocument/load (File. src))
                wr (BufferedWriter. (OutputStreamWriter. (FileOutputStream. (File. dest))))]
      (println src "pages:" (.getNumberOfPages pd))
      (.writeText stripper pd wr))))


(defn -main [& args]
  (if args
    (let [dirName (first args)
          dir (File. dirName)]
      ; get the pdf files in the directory and apply the conversion
      (if (.isDirectory dir)
        (doseq [fileName (.list dir)]
          (if (.endsWith fileName ".pdf")
            (convert-to-text dirName fileName)))))))

