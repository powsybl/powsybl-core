/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.univocity.parsers.annotations.Nested;
import com.univocity.parsers.annotations.Parsed;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */

@JsonIgnoreProperties({"main"})
@JsonPropertyOrder({"name", "nconv", "ndcbs", "ndcln", "mdc", "vconv", "vcmod", "vconvn"})

public class PsseMultiTerminalDcTransmissionLine {

    public PsseMultiTerminalDcTransmissionLine(PsseMultiTerminalDcMain main) {
        this.main = main;
        dcConverters = new ArrayList<>();
        dcBuses = new ArrayList<>();
        dcLinks = new ArrayList<>();
    }

    public PsseMultiTerminalDcTransmissionLine(PsseMultiTerminalDcMain main,
        List<PsseMultiTerminalDcConverter> dcConverters, List<PsseMultiTerminalDcBus> dcBuses,
        List<PsseMultiTerminalDcLink> dcLinks) {
        this.main = main;
        this.dcConverters = dcConverters;
        this.dcBuses = dcBuses;
        this.dcLinks = dcLinks;
    }

    private PsseMultiTerminalDcMain main;
    private List<PsseMultiTerminalDcConverter> dcConverters;
    private List<PsseMultiTerminalDcBus> dcBuses;
    private List<PsseMultiTerminalDcLink> dcLinks;

    public String getName() {
        return main.name;
    }

    public int getNconv() {
        return main.nconv;
    }

    public int getNdcbs() {
        return main.ndcbs;
    }

    public int getNdcln() {
        return main.ndcln;
    }

    public int getMdc() {
        return main.mdc;
    }

    public int getVconv() {
        return main.vconv;
    }

    public double getVcmod() {
        return main.vcmod;
    }

    public int getVconvn() {
        return main.vconvn;
    }

    public PsseMultiTerminalDcMain getMain() {
        return main;
    }

    public List<PsseMultiTerminalDcConverter> getDcConverters() {
        return dcConverters;
    }

    public List<PsseMultiTerminalDcBus> getDcBuses() {
        return dcBuses;
    }

    public List<PsseMultiTerminalDcLink> getDcLinks() {
        return dcLinks;
    }

    public static class PsseMultiTerminalDcMain {
        @Parsed
        private String name;

        @Parsed
        private int nconv;

        @Parsed
        private int ndcbs;

        @Parsed
        private int ndcln;

        @Parsed
        private int mdc = 0;

        @Parsed
        private int vconv;

        @Parsed
        private double vcmod = 0.0;

        @Parsed
        private int vconvn = 0;

        public String getName() {
            return name;
        }

        public int getNconv() {
            return nconv;
        }

        public int getNdcbs() {
            return ndcbs;
        }

        public int getNdcln() {
            return ndcln;
        }
    }

    public static class PsseMultiTerminalDcConverter {

        @Parsed
        private int ib;

        @Parsed
        private int n;

        @Parsed
        private double angmx;

        @Parsed
        private double angmn;

        @Parsed
        private double rc;

        @Parsed
        private double xc;

        @Parsed
        private double ebas;

        @Parsed
        private double tr = 1.0;

        @Parsed
        private double tap = 1.0;

        @Parsed
        private double tpmx = 1.5;

        @Parsed
        private double tpmn = 0.51;

        @Parsed
        private double tstp = 0.00625;

        @Parsed
        private double setvl;

        @Parsed
        private double dcpf = 1.0;

        @Parsed
        private double marg = 0.0;

        @Parsed
        private int cnvcod = 1;

        public int getIb() {
            return ib;
        }

        public void setIb(int ib) {
            this.ib = ib;
        }

        public int getN() {
            return n;
        }

        public void setN(int n) {
            this.n = n;
        }

        public double getAngmx() {
            return angmx;
        }

        public void setAngmx(double angmx) {
            this.angmx = angmx;
        }

        public double getAngmn() {
            return angmn;
        }

        public void setAngmn(double angmn) {
            this.angmn = angmn;
        }

        public double getRc() {
            return rc;
        }

        public void setRc(double rc) {
            this.rc = rc;
        }

        public double getXc() {
            return xc;
        }

        public void setXc(double xc) {
            this.xc = xc;
        }

        public double getEbas() {
            return ebas;
        }

        public void setEbas(double ebas) {
            this.ebas = ebas;
        }

        public double getTr() {
            return tr;
        }

        public void setTr(double tr) {
            this.tr = tr;
        }

        public double getTap() {
            return tap;
        }

        public void setTap(double tap) {
            this.tap = tap;
        }

        public double getTpmx() {
            return tpmx;
        }

        public void setTpmx(double tpmx) {
            this.tpmx = tpmx;
        }

        public double getTpmn() {
            return tpmn;
        }

        public void setTpmn(double tpmn) {
            this.tpmn = tpmn;
        }

        public double getTstp() {
            return tstp;
        }

        public void setTstp(double tstp) {
            this.tstp = tstp;
        }

        public double getSetvl() {
            return setvl;
        }

        public void setSetvl(double setvl) {
            this.setvl = setvl;
        }

        public double getDcpf() {
            return dcpf;
        }

        public void setDcpf(double dcpf) {
            this.dcpf = dcpf;
        }

        public double getMarg() {
            return marg;
        }

        public void setMarg(double marg) {
            this.marg = marg;
        }

        public int getCnvcod() {
            return cnvcod;
        }

        public void setCnvcod(int cnvcod) {
            this.cnvcod = cnvcod;
        }
    }

    public static class PsseMultiTerminalDcBus {

        @Parsed
        private int idc;

        @Parsed
        private int ib = 0;

        @Parsed
        private int area = 1;

        @Parsed
        private int zone = 1;

        @Parsed(defaultNullRead = "            ")
        private String dcname;

        @Parsed
        private int idc2 = 0;

        @Parsed
        private double rgrnd = 0.0;

        @Parsed
        private int owner = 1;

        public int getIdc() {
            return idc;
        }

        public void setIdc(int idc) {
            this.idc = idc;
        }

        public int getIb() {
            return ib;
        }

        public void setIb(int ib) {
            this.ib = ib;
        }

        public int getArea() {
            return area;
        }

        public void setArea(int area) {
            this.area = area;
        }

        public int getZone() {
            return zone;
        }

        public void setZone(int zone) {
            this.zone = zone;
        }

        public String getDcname() {
            return dcname;
        }

        public void setDcname(String dcname) {
            this.dcname = dcname;
        }

        public int getIdc2() {
            return idc2;
        }

        public void setIdc2(int idc2) {
            this.idc2 = idc2;
        }

        public double getRgrnd() {
            return rgrnd;
        }

        public void setRgrnd(double rgrnd) {
            this.rgrnd = rgrnd;
        }

        public int getOwner() {
            return owner;
        }

        public void setOwner(int owner) {
            this.owner = owner;
        }
    }

    public static class PsseMultiTerminalDcLink {

        @Parsed
        private int idc;

        @Parsed
        private int jdc;

        @Parsed(defaultNullRead = "1")
        private String dcckt;

        @Parsed
        private int met = 1;

        @Parsed
        private double rdc;

        @Parsed
        private double ldc = 0.0;

        public int getIdc() {
            return idc;
        }

        public void setIdc(int idc) {
            this.idc = idc;
        }

        public int getJdc() {
            return jdc;
        }

        public void setJdc(int jdc) {
            this.jdc = jdc;
        }

        public String getDcckt() {
            return dcckt;
        }

        public void setDcckt(String dcckt) {
            this.dcckt = dcckt;
        }

        public int getMet() {
            return met;
        }

        public void setMet(int met) {
            this.met = met;
        }

        public double getRdc() {
            return rdc;
        }

        public void setRdc(double rdc) {
            this.rdc = rdc;
        }

        public double getLdc() {
            return ldc;
        }

        public void setLdc(double ldc) {
            this.ldc = ldc;
        }
    }

    public static class PsseMultiTerminalDcConverterx {

        public PsseMultiTerminalDcConverterx() {
        }

        public PsseMultiTerminalDcConverterx(String name, PsseMultiTerminalDcConverter converter) {
            this.name = name;
            this.converter = converter;
        }

        @Parsed
        private String name;

        @Nested
        private PsseMultiTerminalDcConverter converter;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public PsseMultiTerminalDcConverter getConverter() {
            return converter;
        }
    }

    public static class PsseMultiTerminalDcBusx {

        public PsseMultiTerminalDcBusx() {
        }

        public PsseMultiTerminalDcBusx(String name, PsseMultiTerminalDcBus bus) {
            this.name = name;
            this.bus = bus;
        }

        @Parsed
        private String name;

        @Nested
        private PsseMultiTerminalDcBus bus;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public PsseMultiTerminalDcBus getBus() {
            return bus;
        }
    }

    public static class PsseMultiTerminalDcLinkx {

        public PsseMultiTerminalDcLinkx() {
        }

        public PsseMultiTerminalDcLinkx(String name, PsseMultiTerminalDcLink link) {
            this.name = name;
            this.link = link;
        }

        @Parsed
        private String name;

        @Nested
        private PsseMultiTerminalDcLink link;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public PsseMultiTerminalDcLink getLink() {
            return link;
        }
    }
}
