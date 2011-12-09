

(import '(java.awt Color)
        '(java.awt.image BufferedImage)
        '(java.io File)
        '(javax.imageio ImageIO))

(defn цвет [r g b]
  (Color. r g b 0xFF))

(def белый (.getRGB (цвет 0xFF 0xFF 0xFF)))
(defn create-image [w h]
  (let [image (BufferedImage. w h BufferedImage/TYPE_INT_RGB)]
    (doseq [x (range w) y (range h)]
      (.setRGB image x y белый))
    image))

(defn save-image [холст файл]
  (ImageIO/write холст "PNG" (File. файл)))

(defn взять-пиксель [отступ размер-графика смещение сдвиг]
  (let [x (+ отступ (first смещение) (first сдвиг))
        y (+ отступ (second смещение) (second сдвиг))]
    (and
      (>= x отступ) (>= y отступ)
      (< x (+ отступ размер-графика)) (< y (+ отступ размер-графика))
      [x y])))    

(defn можно-перекрасить? [^BufferedImage холст пиксель]
  (when пиксель
    (= белый (.getRGB холст (first пиксель) (second пиксель)))))

(defn перекрасить! [^BufferedImage холст [x y] ^Color цвет]
  (.setRGB холст x y (.getRGB цвет)))

(defn нанести [^BufferedImage холст отступ размер-графика смещение шаблон фракции]
  (let [[первое-число первый-цвет] (first фракции)]
    (loop [сдвиги шаблон, число первое-число, цвет первый-цвет, фракции (rest фракции)]
      (if (zero? число)
        (when-let [фракция (first фракции)]
          (recur сдвиги (first фракция) (second фракция) (rest фракции)))
        (let [[сдвиг & след] сдвиги
              пиксель (взять-пиксель отступ размер-графика смещение сдвиг)]
          (if (можно-перекрасить? холст пиксель)
            (do (перекрасить! холст пиксель цвет)
              (recur след (dec число) цвет фракции))
            (recur след число цвет фракции)))))))
