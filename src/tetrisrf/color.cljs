(ns tetrisrf.color)


(defn in-range [min-val max-val val]
  (min max-val (max min-val val)))


(defn rgbs->rgbv [color]
  (let [[parsed red green blue] (re-matches #"#([0-9a-fA-F]{2})([0-9a-fA-F]{2})([0-9a-fA-F]{2})" color)]
    (if parsed
      [(js/parseInt red 16) (js/parseInt green 16) (js/parseInt blue 16)])))


(defn rgbv->rgbs [rgb]
  (apply str "#" (mapv (fn [cc]
                         (let [cc-str (.toString cc 16)]
                              (if (< cc 16)
                                (str "0" cc-str)
                                cc-str)))
                       rgb)))


(defn rgbv->hsvv [rgb]
  (let [[r g b] (mapv #(/ % 255) rgb)
        m-min (min r g b)
        m-max (max r g b)
        m-delta (- m-max m-min)
        h (if (zero? m-delta)
            nil ;; undefined
            (cond (= r m-max) (/ (- g b) m-delta)
                  (= g m-max) (+ (/ (- b r) m-delta) 2)
                  (= b m-max) (+ (/ (- r b) m-delta) 4)))
        v m-max
        s (if (zero? v)
            0
            (/ m-delta v))]
    [h s v]))


(defn hsvv->rgbv [[h-apos s v]]
  (let [norm #(.round js/Math (* 255 %))]
    (if (nil? h-apos)
      (mapv norm [v v v])
      (let [h (if (< h-apos 0)
                (+ 6 (mod h-apos 6))
                (mod h-apos 6))
            i (.floor js/Math h)
            alpha (* v (- 1 s))
            beta (* v (* s (- 1 (- h i) )))
            gamma (* v (- 1 (* s (- 1 (- h i)))))]
        (mapv norm (cond (and (<= 0 h) (< h 1)) [v gamma alpha]
                         (and (<= 1 h) (< h 2)) [beta v alpha]
                         (and (<= 2 h) (< h 3)) [alpha v gamma]
                         (and (<= 3 h) (< h 4)) [alpha beta v]
                         (and (<= 4 h) (< h 5)) [gamma alpha v]
                         (and (<= 5 h) (< h 6)) [v alpha beta]))))))


(defn hsv-lighter [[h s v] offset]
  [h s (in-range 0 1 (+ v offset))])


(defn hsv-darker [[h s v] offset]
  [h s (in-range 0 1 (- v offset))])


(defn rgb-lighter [color offset]
  (-> color
      rgbs->rgbv
      rgbv->hsvv
      (hsv-lighter offset)
      hsvv->rgbv
      rgbv->rgbs))


(defn rgb-darker [color offset]
  (-> color
      rgbs->rgbv
      rgbv->hsvv
      (hsv-darker offset)
      hsvv->rgbv
      rgbv->rgbs))
