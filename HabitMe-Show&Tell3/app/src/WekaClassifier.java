package edu.dartmouth.cs.myruns1;

/**
 * Created by Christal on 2/14/15.
 */

    class WekaClassifier {

        public static double classify(Object[] i)
                throws Exception {

            double p = Double.NaN;
            p = WekaClassifier.N595b5d6b0(i);
            return p;
        }
        static double N595b5d6b0(Object []i) {
            double p = Double.NaN;
            if (i[0] == null) {
                p = 1;
            } else if (((Double) i[0]).doubleValue() <= 348.392909) {
                p = WekaClassifier.N7d5249a11(i);
            } else if (((Double) i[0]).doubleValue() > 348.392909) {
                p = 2;
            }
            return p;
        }
        static double N7d5249a11(Object []i) {
            double p = Double.NaN;
            if (i[0] == null) {
                p = 0;
            } else if (((Double) i[0]).doubleValue() <= 44.819124) {
                p = 0;
            } else if (((Double) i[0]).doubleValue() > 44.819124) {
                p = WekaClassifier.N2560294e2(i);
            }
            return p;
        }
        static double N2560294e2(Object []i) {
            double p = Double.NaN;
            if (i[0] == null) {
                p = 1;
            } else if (((Double) i[0]).doubleValue() <= 100.036916) {
                p = WekaClassifier.N4c37d30d3(i);
            } else if (((Double) i[0]).doubleValue() > 100.036916) {
                p = WekaClassifier.N1500df0b6(i);
            }
            return p;
        }
        static double N4c37d30d3(Object []i) {
            double p = Double.NaN;
            if (i[26] == null) {
                p = 1;
            } else if (((Double) i[26]).doubleValue() <= 0.431198) {
                p = 1;
            } else if (((Double) i[26]).doubleValue() > 0.431198) {
                p = WekaClassifier.N650121924(i);
            }
            return p;
        }
        static double N650121924(Object []i) {
            double p = Double.NaN;
            if (i[3] == null) {
                p = 2;
            } else if (((Double) i[3]).doubleValue() <= 9.85988) {
                p = WekaClassifier.N2d432c0c5(i);
            } else if (((Double) i[3]).doubleValue() > 9.85988) {
                p = 0;
            }
            return p;
        }
        static double N2d432c0c5(Object []i) {
            double p = Double.NaN;
            if (i[7] == null) {
                p = 0;
            } else if (((Double) i[7]).doubleValue() <= 2.215497) {
                p = 0;
            } else if (((Double) i[7]).doubleValue() > 2.215497) {
                p = 2;
            }
            return p;
        }
        static double N1500df0b6(Object []i) {
            double p = Double.NaN;
            if (i[27] == null) {
                p = 1;
            } else if (((Double) i[27]).doubleValue() <= 3.107636) {
                p = 1;
            } else if (((Double) i[27]).doubleValue() > 3.107636) {
                p = WekaClassifier.N6c3355f27(i);
            }
            return p;
        }
        static double N6c3355f27(Object []i) {
            double p = Double.NaN;
            if (i[24] == null) {
                p = 1;
            } else if (((Double) i[24]).doubleValue() <= 4.197216) {
                p = 1;
            } else if (((Double) i[24]).doubleValue() > 4.197216) {
                p = WekaClassifier.N1e4fba5d8(i);
            }
            return p;
        }
        static double N1e4fba5d8(Object []i) {
            double p = Double.NaN;
            if (i[1] == null) {
                p = 0;
            } else if (((Double) i[1]).doubleValue() <= 58.422144) {
                p = 0;
            } else if (((Double) i[1]).doubleValue() > 58.422144) {
                p = 2;
            }
            return p;
        }
    }

