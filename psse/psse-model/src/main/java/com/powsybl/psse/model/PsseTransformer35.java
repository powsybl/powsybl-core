/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.univocity.parsers.annotations.Parsed;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */

//order using alphabetic order
@JsonPropertyOrder(alphabetic = true)

public class PsseTransformer35 extends PsseTransformer {

    @Parsed
    private int zcod = 0;

    @Parsed
    private double rate11 = 0.0;

    @Parsed
    private double rate21 = 0.0;

    @Parsed
    private double rate31 = 0.0;

    @Parsed
    private double rate41 = 0.0;

    @Parsed
    private double rate51 = 0.0;

    @Parsed
    private double rate61 = 0.0;

    @Parsed
    private double rate71 = 0.0;

    @Parsed
    private double rate81 = 0.0;

    @Parsed
    private double rate91 = 0.0;

    @Parsed
    private double rate101 = 0.0;

    @Parsed
    private double rate111 = 0.0;

    @Parsed
    private double rate121 = 0.0;

    @Parsed
    private int node1 = 0;

    @Parsed
    private double rate12 = 0.0;

    @Parsed
    private double rate22 = 0.0;

    @Parsed
    private double rate32 = 0.0;

    @Parsed
    private double rate42 = 0.0;

    @Parsed
    private double rate52 = 0.0;

    @Parsed
    private double rate62 = 0.0;

    @Parsed
    private double rate72 = 0.0;

    @Parsed
    private double rate82 = 0.0;

    @Parsed
    private double rate92 = 0.0;

    @Parsed
    private double rate102 = 0.0;

    @Parsed
    private double rate112 = 0.0;

    @Parsed
    private double rate122 = 0.0;

    @Parsed
    private int node2 = 0;

    @Parsed
    private double rate13 = 0.0;

    @Parsed
    private double rate23 = 0.0;

    @Parsed
    private double rate33 = 0.0;

    @Parsed
    private double rate43 = 0.0;

    @Parsed
    private double rate53 = 0.0;

    @Parsed
    private double rate63 = 0.0;

    @Parsed
    private double rate73 = 0.0;

    @Parsed
    private double rate83 = 0.0;

    @Parsed
    private double rate93 = 0.0;

    @Parsed
    private double rate103 = 0.0;

    @Parsed
    private double rate113 = 0.0;

    @Parsed
    private double rate123 = 0.0;

    @Parsed
    private int node3 = 0;

    public double getZcod() {
        return zcod;
    }

    public void setZcod(int zcod) {
        this.zcod = zcod;
    }

    @Override
    public WindingRecord getWindingRecord1() {
        return new WindingRecord35(windv1, nomv1, ang1, rate11, rate21, rate31, rate41, rate51, rate61, rate71, rate81,
            rate91, rate101, rate111, rate121, cod1, cont1, node1, rma1, rmi1, vma1, vmi1, ntp1, tab1, cr1, cx1, cnxa1);
    }

    @Override
    public WindingRecord getWindingRecord2() {
        return new WindingRecord35(windv2, nomv2, ang2, rate11, rate22, rate32, rate42, rate52, rate62, rate72, rate82,
            rate92, rate102, rate112, rate122, cod2, cont2, node2, rma2, rmi2, vma2, vmi2, ntp2, tab2, cr2, cx2, cnxa2);
    }

    @Override
    public WindingRecord getWindingRecord3() {
        return new WindingRecord35(windv3, nomv3, ang3, rate13, rate23, rate33, rate43, rate53, rate63, rate73, rate83,
            rate93, rate103, rate113, rate123, cod3, cont3, node3, rma3, rmi3, vma3, vmi3, ntp3, tab3, cr3, cx3, cnxa3);
    }

    public static class WindingRecord35 extends WindingRecord {
        private final double rate1;
        private final double rate2;
        private final double rate3;
        private final double rate4;
        private final double rate5;
        private final double rate6;
        private final double rate7;
        private final double rate8;
        private final double rate9;
        private final double rate10;
        private final double rate11;
        private final double rate12;
        private final int node;

        WindingRecord35(double windv, double nomv, double ang, double rate1, double rate2, double rate3,
            double rate4, double rate5, double rate6, double rate7, double rate8, double rate9, double rate10,
            double rate11, double rate12, int cod, int cont, int node, double rma, double rmi, double vma, double vmi,
            int ntp, int tab, double cr, double cx, double cnxa) {
            super(windv, nomv, ang, 0.0, 0.0, 0.0, cod, cont, rma, rmi, vma, vmi, ntp, tab, cr, cx, cnxa);
            this.rate1 = rate1;
            this.rate2 = rate2;
            this.rate3 = rate3;
            this.rate4 = rate4;
            this.rate5 = rate5;
            this.rate6 = rate6;
            this.rate7 = rate7;
            this.rate8 = rate8;
            this.rate9 = rate9;
            this.rate10 = rate10;
            this.rate11 = rate11;
            this.rate12 = rate12;
            this.node = node;
        }

        @Override
        public double getRata() {
            throw new PsseException("Rata not available in version 35");
        }

        @Override
        public double getRatb() {
            throw new PsseException("Ratb not available in version 35");
        }

        @Override
        public double getRatc() {
            throw new PsseException("Ratc not available in version 35");
        }

        public double getRate1() {
            return rate1;
        }

        public double getRate2() {
            return rate2;
        }

        public double getRate3() {
            return rate3;
        }

        public double getRate4() {
            return rate4;
        }

        public double getRate5() {
            return rate5;
        }

        public double getRate6() {
            return rate6;
        }

        public double getRate7() {
            return rate7;
        }

        public double getRate8() {
            return rate8;
        }

        public double getRate9() {
            return rate9;
        }

        public double getRate10() {
            return rate10;
        }

        public double getRate11() {
            return rate11;
        }

        public double getRate12() {
            return rate12;
        }

        public int getNode() {
            return node;
        }
    }
}
