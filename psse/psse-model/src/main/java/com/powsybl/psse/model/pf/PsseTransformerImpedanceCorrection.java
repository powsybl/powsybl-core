/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.pf;

import java.util.ArrayList;
import java.util.List;

import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseVersioned;
import com.univocity.parsers.annotations.Nested;
import com.univocity.parsers.annotations.Parsed;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class PsseTransformerImpedanceCorrection extends PsseVersioned {

    private int i;
    private List<PsseTransformerImpedanceCorrectionPoint> points;

    public PsseTransformerImpedanceCorrection(int i) {
        this.i = i;
        this.points = new ArrayList<>();
    }

    public int getI() {
        return i;
    }

    public List<PsseTransformerImpedanceCorrectionPoint> getPoints() {
        return points;
    }

    public static class PsseTransformerImpedanceCorrection33ParserRecord {

        @Parsed
        private int i;

        @Parsed
        private double t1 = 0.0;

        @Parsed
        private double f1 = 0.0;

        @Parsed
        private double t2 = 0.0;

        @Parsed
        private double f2 = 0.0;

        @Parsed
        private double t3 = 0.0;

        @Parsed
        private double f3 = 0.0;

        @Parsed
        private double t4 = 0.0;

        @Parsed
        private double f4 = 0.0;

        @Parsed
        private double t5 = 0.0;

        @Parsed
        private double f5 = 0.0;

        @Parsed
        private double t6 = 0.0;

        @Parsed
        private double f6 = 0.0;

        @Parsed
        private double t7 = 0.0;

        @Parsed
        private double f7 = 0.0;

        @Parsed
        private double t8 = 0.0;

        @Parsed
        private double f8 = 0.0;

        @Parsed
        private double t9 = 0.0;

        @Parsed
        private double f9 = 0.0;

        @Parsed
        private double t10 = 0.0;

        @Parsed
        private double f10 = 0.0;

        @Parsed
        private double t11 = 0.0;

        @Parsed
        private double f11 = 0.0;

        public int getI() {
            return i;
        }

        public void setI(int i) {
            this.i = i;
        }

        public double getT1() {
            return t1;
        }

        public double getF1() {
            return f1;
        }

        public double getT2() {
            return t2;
        }

        public double getF2() {
            return f2;
        }

        public double getT3() {
            return t3;
        }

        public double getF3() {
            return f3;
        }

        public double getT4() {
            return t4;
        }

        public double getF4() {
            return f4;
        }

        public double getT5() {
            return t5;
        }

        public double getF5() {
            return f5;
        }

        public double getT6() {
            return t6;
        }

        public double getF6() {
            return f6;
        }

        public double getT7() {
            return t7;
        }

        public double getF7() {
            return f7;
        }

        public double getT8() {
            return t8;
        }

        public double getF8() {
            return f8;
        }

        public double getT9() {
            return t9;
        }

        public double getF9() {
            return f9;
        }

        public double getT10() {
            return t10;
        }

        public double getF10() {
            return f10;
        }

        public double getT11() {
            return t11;
        }

        public double getF11() {
            return f11;
        }

        public void setTF(int point, double t, double f) {
            switch (point) {
                case 1:
                    this.t1 = t;
                    this.f1 = f;
                    break;
                case 2:
                    this.t2 = t;
                    this.f2 = f;
                    break;
                case 3:
                    this.t3 = t;
                    this.f3 = f;
                    break;
                case 4:
                    this.t4 = t;
                    this.f4 = f;
                    break;
                case 5:
                    this.t5 = t;
                    this.f5 = f;
                    break;
                case 6:
                    this.t6 = t;
                    this.f6 = f;
                    break;
                case 7:
                    this.t7 = t;
                    this.f7 = f;
                    break;
                case 8:
                    this.t8 = t;
                    this.f8 = f;
                    break;
                case 9:
                    this.t9 = t;
                    this.f9 = f;
                    break;
                case 10:
                    this.t10 = t;
                    this.f10 = f;
                    break;
                case 11:
                    this.t11 = t;
                    this.f11 = f;
                    break;
                default:
                    throw new PsseException("Unexpected point " + point);
            }
        }
    }

    public static class PsseTransformerImpedanceCorrection35ParserRecord1 {

        @Parsed
        private int i;

        @Nested
        private PsseTransformerImpedanceCorrection35ParserRecord2 record2;

        public int getI() {
            return i;
        }

        public void setI(int i) {
            this.i = i;
        }

        public PsseTransformerImpedanceCorrection35ParserRecord2 getRecord2() {
            return record2;
        }

        public void setRecord2(PsseTransformerImpedanceCorrection35ParserRecord2 record2) {
            this.record2 = record2;
        }
    }

    public static class PsseTransformerImpedanceCorrection35ParserRecord2 {

        @Parsed
        private double t1 = 0.0;

        @Parsed
        private double ref1 = 0.0;

        @Parsed
        private double imf1 = 0.0;

        @Parsed
        private double t2 = 0.0;

        @Parsed
        private double ref2 = 0.0;

        @Parsed
        private double imf2 = 0.0;

        @Parsed
        private double t3 = 0.0;

        @Parsed
        private double ref3 = 0.0;

        @Parsed
        private double imf3 = 0.0;

        @Parsed
        private double t4 = 0.0;

        @Parsed
        private double ref4 = 0.0;

        @Parsed
        private double imf4 = 0.0;

        @Parsed
        private double t5 = 0.0;

        @Parsed
        private double ref5 = 0.0;

        @Parsed
        private double imf5 = 0.0;

        @Parsed
        private double t6 = 0.0;

        @Parsed
        private double ref6 = 0.0;

        @Parsed
        private double imf6 = 0.0;

        public double getT1() {
            return t1;
        }

        public double getRef1() {
            return ref1;
        }

        public double getImf1() {
            return imf1;
        }

        public double getT2() {
            return t2;
        }

        public double getRef2() {
            return ref2;
        }

        public double getImf2() {
            return imf2;
        }

        public double getT3() {
            return t3;
        }

        public double getRef3() {
            return ref3;
        }

        public double getImf3() {
            return imf3;
        }

        public double getT4() {
            return t4;
        }

        public double getRef4() {
            return ref4;
        }

        public double getImf4() {
            return imf4;
        }

        public double getT5() {
            return t5;
        }

        public double getRef5() {
            return ref5;
        }

        public double getImf5() {
            return imf5;
        }

        public double getT6() {
            return t6;
        }

        public double getRef6() {
            return ref6;
        }

        public double getImf6() {
            return imf6;
        }

        public void setTF(int point, double t, double ref, double imf) {
            switch (point) {
                case 1:
                    this.t1 = t;
                    this.ref1 = ref;
                    this.imf1 = imf;
                    break;
                case 2:
                    this.t2 = t;
                    this.ref2 = ref;
                    this.imf2 = imf;
                    break;
                case 3:
                    this.t3 = t;
                    this.ref3 = ref;
                    this.imf3 = imf;
                    break;
                case 4:
                    this.t4 = t;
                    this.ref4 = ref;
                    this.imf4 = imf;
                    break;
                case 5:
                    this.t5 = t;
                    this.ref5 = ref;
                    this.imf5 = imf;
                    break;
                case 6:
                    this.t6 = t;
                    this.ref6 = ref;
                    this.imf6 = imf;
                    break;
                default:
                    throw new PsseException("Unexpected point " + point);
            }
        }
    }

    public static class PsseTransformerImpedanceCorrection35xParserRecord {

        public PsseTransformerImpedanceCorrection35xParserRecord() {
        }

        public PsseTransformerImpedanceCorrection35xParserRecord(int itable, double tap, double refact, double imfact) {
            this.itable = itable;
            this.tap = tap;
            this.refact = refact;
            this.imfact = imfact;
        }

        @Parsed
        private int itable;

        @Parsed
        private double tap;

        @Parsed
        private double refact;

        @Parsed
        private double imfact;

        public int getItable() {
            return itable;
        }

        public double getTap() {
            return tap;
        }

        public double getRefact() {
            return refact;
        }

        public double getImfact() {
            return imfact;
        }
    }
}
