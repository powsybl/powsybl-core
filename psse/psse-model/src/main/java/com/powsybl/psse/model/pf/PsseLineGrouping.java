/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.pf;

import com.powsybl.psse.model.PsseException;
import com.univocity.parsers.annotations.Parsed;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class PsseLineGrouping {

    public PsseLineGrouping() {
    }

    public PsseLineGrouping(int i, int j, String id, int met) {
        this.i = i;
        this.j = j;
        this.id = id;
        this.met = met;
    }

    @Parsed
    private int i;

    @Parsed
    private int j;

    @Parsed(defaultNullRead = "&1")
    private String id;

    @Parsed
    private int met = 1;

    @Parsed
    private Integer dum1;

    @Parsed
    private Integer dum2;

    @Parsed
    private Integer dum3;

    @Parsed
    private Integer dum4;

    @Parsed
    private Integer dum5;

    @Parsed
    private Integer dum6;

    @Parsed
    private Integer dum7;

    @Parsed
    private Integer dum8;

    @Parsed
    private Integer dum9;

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public int getJ() {
        return j;
    }

    public void setJ(int j) {
        this.j = j;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getMet() {
        return met;
    }

    public void setMet(int met) {
        this.met = met;
    }

    public Integer getDum1() {
        return dum1;
    }

    public void setDum1(int dum1) {
        this.dum1 = dum1;
    }

    public Integer getDum2() {
        return dum2;
    }

    public void setDum2(int dum2) {
        this.dum2 = dum2;
    }

    public Integer getDum3() {
        return dum3;
    }

    public void setDum3(int dum3) {
        this.dum3 = dum3;
    }

    public Integer getDum4() {
        return dum4;
    }

    public void setDum4(int dum4) {
        this.dum4 = dum4;
    }

    public Integer getDum5() {
        return dum5;
    }

    public void setDum5(int dum5) {
        this.dum5 = dum5;
    }

    public Integer getDum6() {
        return dum6;
    }

    public void setDum6(int dum6) {
        this.dum6 = dum6;
    }

    public Integer getDum7() {
        return dum7;
    }

    public void setDum7(int dum7) {
        this.dum7 = dum7;
    }

    public Integer getDum8() {
        return dum8;
    }

    public void setDum8(int dum8) {
        this.dum8 = dum8;
    }

    public Integer getDum9() {
        return dum9;
    }

    public void setDum9(int dum9) {
        this.dum9 = dum9;
    }

    public void setDum(int point, Integer dum) {
        switch (point) {
            case 1:
                this.dum1 = dum;
                break;
            case 2:
                this.dum2 = dum;
                break;
            case 3:
                this.dum3 = dum;
                break;
            case 4:
                this.dum4 = dum;
                break;
            case 5:
                this.dum5 = dum;
                break;
            case 6:
                this.dum6 = dum;
                break;
            case 7:
                this.dum7 = dum;
                break;
            case 8:
                this.dum8 = dum;
                break;
            case 9:
                this.dum9 = dum;
                break;
            default:
                throw new PsseException("Unexpected point " + point);
        }
    }

    public static class PsseLineGroupingParserX {

        @Parsed
        private int ibus;

        @Parsed
        private int jbus;

        @Parsed(defaultNullRead = "&1")
        private String mslid;

        @Parsed
        private int met = 1;

        @Parsed(defaultNullRead = "null")
        private String dum1;

        @Parsed(defaultNullRead = "null")
        private String dum2;

        @Parsed(defaultNullRead = "null")
        private String dum3;

        @Parsed(defaultNullRead = "null")
        private String dum4;

        @Parsed(defaultNullRead = "null")
        private String dum5;

        @Parsed(defaultNullRead = "null")
        private String dum6;

        @Parsed(defaultNullRead = "null")
        private String dum7;

        @Parsed(defaultNullRead = "null")
        private String dum8;

        @Parsed(defaultNullRead = "null")
        private String dum9;

        public int getIbus() {
            return ibus;
        }

        public void setIbus(int ibus) {
            this.ibus = ibus;
        }

        public int getJbus() {
            return jbus;
        }

        public void setJbus(int jbus) {
            this.jbus = jbus;
        }

        public String getMslid() {
            return mslid;
        }

        public void setMslid(String mslid) {
            this.mslid = mslid;
        }

        public int getMet() {
            return met;
        }

        public void setMet(int met) {
            this.met = met;
        }

        public String getDum1() {
            return dum1;
        }

        public void setDum1(String dum1) {
            this.dum1 = dum1;
        }

        public String getDum2() {
            return dum2;
        }

        public void setDum2(String dum2) {
            this.dum2 = dum2;
        }

        public String getDum3() {
            return dum3;
        }

        public void setDum3(String dum3) {
            this.dum3 = dum3;
        }

        public String getDum4() {
            return dum4;
        }

        public void setDum4(String dum4) {
            this.dum4 = dum4;
        }

        public String getDum5() {
            return dum5;
        }

        public void setDum5(String dum5) {
            this.dum5 = dum5;
        }

        public String getDum6() {
            return dum6;
        }

        public void setDum6(String dum6) {
            this.dum6 = dum6;
        }

        public String getDum7() {
            return dum7;
        }

        public void setDum7(String dum7) {
            this.dum7 = dum7;
        }

        public String getDum8() {
            return dum8;
        }

        public void setDum8(String dum8) {
            this.dum8 = dum8;
        }

        public String getDum9() {
            return dum9;
        }

        public void setDum9(String dum9) {
            this.dum9 = dum9;
        }
    }
}
