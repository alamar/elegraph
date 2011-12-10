(ns ru.lj.alamar.elegraph
  (:require spiral data image))

(defn сумма [список] (apply + список))

(defn zip [keys vals]
  (loop [seqq []
         ks (seq keys)
         vs (seq vals)]
    (if (and ks vs)
      (recur (conj seqq [(first ks) (first vs)])
        (next ks) (next vs))
      seqq)))

(def данные (read-data "data/moscow-2011-12-07/data.csv"))
(def заголовки (first данные))
(def уики (rest данные))

(defn партии [уик]
  (subvec уик 19))

(def избиратели second)

(defn явка [уик]
  (-
    (Math/pow
      (- 1 (/ (сумма (партии уик)) (избиратели уик)))
      1.33)))

(defn доля-ер [уик]
  (/ (nth уик 24) (сумма (партии уик))))

(def окно-явки (window явка уики))
(def окно-ер (window доля-ер уики))

(def ширина 6000)
(def высота 8000)

(def отступ 0) 

(def размер-графика 6000)

(def холст (create-image ширина высота))

(def шаблон (spiral))

(def цвет-неявки (цвет 0xDD 0xDD 0xDD))
(def цвета-партий [
            (цвет 0x66 0x66 0xCC) ;; Справедливая Россия
            (цвет 0xFF 0xFF 0)    ;; ЛДПР
            (цвет 0x9A 0x33 0x9A) ;; Патриоты России
            (цвет 0xCC 0 0)       ;; КПРФ
            (цвет 0x33 0x99 0)    ;; Яблоко
            (цвет 0 0 0)          ;; Единая Россия
            (цвет 0x33 0x9A 0x9A) ;; Правое Дело
           ])

(defn выделить-фракции [уик]
  (let [все-партии (партии уик)]
    (reverse
;      (cons
;        [(- (избиратели уик) (сумма все-партии)) цвет-неявки]
        (sort-by first (zip все-партии цвета-партий)))))

(doseq [уик уики]
  (println (first уик) "явка" (float (явка уик)) "доля ер" (float (доля-ер уик))
    "окно" (float (window-weight явка уик окно-явки)) (float (window-weight доля-ер уик окно-ер))))

(doall
  (нанести холст отступ размер-графика
    (for [уик (take 2000 уики)]
      (let [смещение
            [(int (* размер-графика (window-weight явка уик окно-явки)))
             (int (* размер-графика (window-weight доля-ер уик окно-ер)))]]
        [смещение шаблон (выделить-фракции уик)]))))

(save-image холст "moscow.png")

