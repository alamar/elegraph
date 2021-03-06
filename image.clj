
(import '(java.awt Color Graphics2D Font)
        '(java.awt.image BufferedImage)
        '(java.io File)
        '(java.lang Math)
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

(defn вывести-текст [^Graphics2D рисунок ^String текст размер-шрифта x y вывести-до вывести-над]
  (let [символы (.toCharArray текст)
        отступ размер-шрифта
        шрифт (Font. Font/SANS_SERIF Font/PLAIN размер-шрифта)]
    (.setFont рисунок шрифт)
    (let [метрики (.getFontMetrics рисунок)]
      (.drawChars рисунок символы 0 (alength символы)
        (if вывести-до (- x (.charsWidth метрики символы 0 (alength символы))) x)
        (if вывести-над (- y (.getHeight метрики)) (+ y (.getHeight метрики)))))))

(defn повернуть [^Graphics2D рисунок]
  (.rotate рисунок (/ Math/PI 2)))

(defn подписать [холст цвет-текста блок]
  (let [рисунок (.createGraphics холст)]
    (.setColor рисунок цвет-текста)
    (блок рисунок)
    (.dispose рисунок)))

(defn образцы 
  ([холст цвета x y] (образцы холст цвета x y 50))
  ([холст цвета x y ширина]
    (let [рисунок (.createGraphics холст)]
      (doseq [i (range 0 (count цвета))]
        (.setColor рисунок (nth цвета i))
        (.fillRect рисунок x (+ y (* 4 ширина i)) ширина ширина))
      (.dispose рисунок))))

(defn сетка [^BufferedImage холст ширина высота шаг ширина-большей-линии ширина-малой-линии цвет размер-шрифта цвет-цифр окно-x окно-y]
  (let [рисунок (.createGraphics холст)]
    (doseq [процент (range шаг 1 шаг)]
      (let [ширина-линии (if (pos? (rem (/ процент шаг) 2)) ширина-малой-линии ширина-большей-линии)
            x (* ширина (- 1 (окно-x процент)))
            y (* высота (окно-y процент))
            dx (- x (* ширина (- 1 (окно-x (- процент шаг)))))
            поправка (int (/ ширина-линии 2))]
        (when (and (> x ширина-линии) (< x (- ширина ширина-линии)))
          (when (and (> x размер-шрифта) (< x (- ширина размер-шрифта)))
            (.setColor рисунок цвет-цифр)
            (вывести-текст рисунок (str (* 100 процент)) размер-шрифта (- x ширина-линии) -5 true false)
            (вывести-текст рисунок (str (* 100 процент)) размер-шрифта (- x ширина-линии) (+ высота размер-шрифта) true true)
            (if (> dx (* 4 размер-шрифта))
              (do
                (вывести-текст рисунок " %" размер-шрифта (+ x ширина-линии) -5 false false)
                (вывести-текст рисунок " %" размер-шрифта (+ x ширина-линии) (+ высота размер-шрифта) false true))))
          (.setColor рисунок цвет)
          (doseq [n (range (inc ширина-линии))]
            (.drawLine рисунок (+ n (- x поправка)) 0 (+ n (- x поправка)) высота)))
        (when (and (> y ширина-линии) (< y (- высота ширина-линии)))
          (when (and (> y размер-шрифта) (< y (- высота размер-шрифта)))
            (.setColor рисунок цвет-цифр)
            (вывести-текст рисунок (str (* 100 процент) " %") размер-шрифта 5 y false false)
            (вывести-текст рисунок (str (* 100 процент) " %") размер-шрифта (- ширина 5) y true false))
          (.setColor рисунок цвет)
          (doseq [n (range (inc ширина-линии))]
            (.drawLine рисунок 0 (+ n (- y поправка)) ширина (+ n (- y поправка)))))))
    (.dispose рисунок)))

(defn сетка-для-полоски [холст ширина отступ высота толщина- цвет шаг-x окно-x шаг-y окно-y]
  (let [рисунок (.createGraphics холст)]
    (.setColor рисунок цвет)
    (doseq [процент-x (range шаг-x 1 шаг-x)]
      (let [x (* ширина (- 1 (окно-x процент-x)))
            толщина толщина-
            поправка (int (/ толщина 2))]
        (when (and (> x толщина) (< x (- ширина толщина)))
          (doseq [сдвиг (range поправка высота (* 2 толщина))]
            (.fillRect рисунок (- x поправка) (+ отступ сдвиг) толщина толщина)))))
    (doseq [процент-y (range шаг-y 1 шаг-y)]
      (let [y (+ отступ (* высота (окно-y процент-y)))
            толщина (if (= процент-y (/ 1 2)) (* 2 толщина-) толщина-)
            поправка (int (/ толщина 2))]
        (when (and (> y (+ толщина отступ)) (< y (- (+ отступ высота) толщина)))
          (doseq [сдвиг (range поправка ширина (* 2 толщина))]
            (.fillRect рисунок сдвиг (- y поправка) толщина толщина)))))
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
                (when-not (empty? след-очередь)
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
