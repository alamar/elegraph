
(import '(java.awt Color Graphics2D Font)
        '(java.awt.image BufferedImage)
        '(java.io File)
        '(javax.imageio ImageIO))

(require 'util)

(defn цвет [r g b]
  (Color. r g b 0xFF))

(defn create-image [w h фон]
  (let [image (BufferedImage. w h BufferedImage/TYPE_INT_RGB)
        значение-фона (.getRGB фон)]
    (doseq [x (range w) y (range h)]
      (.setRGB image x y значение-фона))
    image))

(defn save-image [холст файл]
  (ImageIO/write холст "PNG" (File. файл)))

(defn взять-пиксель [отступ размер-графика смещение сдвиг]
  (let [x (+ отступ (first смещение) (first сдвиг))
        y (+ отступ (second смещение) (second сдвиг))]
    (and
      (>= x отступ) (>= y отступ)
      (< x (+ отступ (first размер-графика))) (< y (+ отступ (second размер-графика)))
      [x y])))    

(defn вывести-текст [^Graphics2D рисунок ^String текст размер-шрифта x y вывести-до]
  (let [символы (.toCharArray текст)
        отступ размер-шрифта
        шрифт (Font. Font/SANS_SERIF Font/PLAIN размер-шрифта)]
    (.setFont рисунок шрифт)
    (let [метрики (.getFontMetrics рисунок)]
      (.drawChars рисунок символы 0 (alength символы)
        (if вывести-до (- x (.charsWidth метрики символы 0 (alength символы))) x)
        (+ y (.getHeight метрики))))))

(defn сетка [^BufferedImage холст ширина высота шаг ширина-большей-линии ширина-малой-линии цвет размер-шрифта цвет-цифр окно-x окно-y]
  (let [рисунок (.createGraphics холст)]
    (doseq [процент (range шаг 1 шаг)]
      (let [ширина-линии (if (pos? (rem (/ процент шаг) 2)) ширина-малой-линии ширина-большей-линии)
            x (* ширина (- 1 (окно-x процент)))
            y (* высота (окно-y процент))
            поправка (int (/ ширина-линии 2))]
        (when (and (> x ширина-линии) (< x (- ширина ширина-линии)))
          (.setColor рисунок цвет-цифр)
          (вывести-текст рисунок (str (* 100 процент)) размер-шрифта (- x ширина-линии) 0 true)
          (вывести-текст рисунок " %" размер-шрифта (+ x ширина-линии) 0 false)
          (.setColor рисунок цвет)
          (doseq [n (range (inc ширина-линии))]
            (.drawLine рисунок (+ n (- x поправка)) 0 (+ n (- x поправка)) высота)))
        (when (and (> y ширина-линии) (< y (- высота ширина-линии)))
          (.setColor рисунок цвет-цифр)
          (вывести-текст рисунок (str (* 100 процент) " %") размер-шрифта ширина-линии y false)
          (.setColor рисунок цвет)
          (doseq [n (range (inc ширина-линии))]
            (.drawLine рисунок 0 (+ n (- y поправка)) ширина (+ n (- y поправка)))))))
    (.dispose рисунок)))

(defn можно-перекрасить? [^BufferedImage холст пиксель цвета-фона центры-уиков]
  (and пиксель
    (not (contains? центры-уиков пиксель))
    (some #(= (.getRGB холст (first пиксель) (second пиксель)) %) цвета-фона)))

(defn перекрасить! [^BufferedImage холст [x y] ^Color цвет]
  (.setRGB холст x y (.getRGB цвет)))

(defn выделить [[смещение шаблон [[число цвет] & остальные]]]
  [смещение шаблон число цвет остальные])

(defn нанести [холст отступ размер-графика данные цвета-фона]
  (let [центры-уиков (into #{} (map first данные))
        [первый & остальные] (map выделить данные)
        значения-фона (map #(.getRGB %) цвета-фона)]
    (loop [очередь-ещё остальные, очередь-уже [], [смещение шаблон число цвет остальные] первый, номер 1]
      (let [пиксель (взять-пиксель отступ размер-графика смещение (first шаблон))]
        (if (можно-перекрасить? холст пиксель значения-фона центры-уиков)
          (do (перекрасить! холст пиксель цвет)
            (let [число-цвет (if (pos? число) [(dec число) цвет] (first остальные))
                  след-остальные (if (pos? число) остальные (rest остальные))
                  в-очередь (if число-цвет [смещение (next шаблон) (first число-цвет) (second число-цвет) след-остальные])
                  след-очередь (if в-очередь (conj очередь-уже в-очередь) очередь-уже)]
              (if (empty? очередь-ещё)
                (when-not (empty? очередь-уже)
                  (println "- пройдена очередь" номер)
                  (recur (rest след-очередь) [] (first след-очередь) (inc номер)))
                (recur (rest очередь-ещё) след-очередь (first очередь-ещё) номер))))
          (recur очередь-ещё очередь-уже [смещение (next шаблон) число цвет остальные] номер))))))

(defn сложить-фракции [раз два]
  (for [[[число цвет] [разница _]] (zip раз два)]
    [(+ число разница) цвет]))

(defn нарисовать-кусочек [^BufferedImage холст отступ высота стар-смещение нов-смещение фракции]
  (let [сумма-фракций (сумма (map first фракции))]
    (doseq [x (range стар-смещение нов-смещение)]
      (loop [отступ-снизу 0, фракция (first фракции), остаток (rest фракции)]
        (let [высота-фракции (* высота (/ (first фракция) сумма-фракций))]
          (doseq [y (range (- (+ отступ высота) отступ-снизу) (- (+ отступ высота) отступ-снизу высота-фракции) -1)]
            (перекрасить! холст [x y] (second фракция)))
          (if (not-empty остаток)
            (recur (+ отступ-снизу высота-фракции) (first остаток) (rest остаток))))))))

(defn полоска [холст отступ высота данные]
  (let [сортированные (sort-by first данные)
        [первое-смещение первые-фракции] (first сортированные)]
    (loop [стар-смещение 0, нов-смещение первое-смещение, фракции первые-фракции, остальные (rest сортированные)]
      (нарисовать-кусочек холст отступ высота стар-смещение нов-смещение фракции)
      (if (not-empty остальные)
        (let [[след-смещение след-фракции] (first остальные)]
          (recur нов-смещение след-смещение (сложить-фракции фракции след-фракции) (rest остальные)))))))
