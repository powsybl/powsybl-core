/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseVersion;
import de.siegmar.fastcsv.reader.CsvRecord;

import java.util.ArrayList;
import java.util.List;

import static com.powsybl.psse.model.io.Util.parseDoubleFromRecord;
import static com.powsybl.psse.model.io.Util.parseIntFromRecord;
import static com.powsybl.psse.model.io.Util.parseStringFromRecord;

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

        private String name;
        private int nconv;
        private int ndcbs;
        private int ndcln;
        private int mdc = 0;
        private int vconv;
        private double vcmod = 0.0;
        private int vconvn = 0;

        public static PsseMultiTerminalDcMain fromRecord(CsvRecord rec, PsseVersion version, String[] headers) {
            PsseMultiTerminalDcMain psseMultiTerminalDcMain = new PsseMultiTerminalDcMain();
            psseMultiTerminalDcMain.setName(parseStringFromRecord(rec, headers, "name"));
            psseMultiTerminalDcMain.setNconv(parseIntFromRecord(rec, headers, "nconv"));
            psseMultiTerminalDcMain.setNdcbs(parseIntFromRecord(rec, headers, "ndcbs"));
            psseMultiTerminalDcMain.setNdcln(parseIntFromRecord(rec, headers, "ndcln"));
            psseMultiTerminalDcMain.setMdc(parseIntFromRecord(rec, 0, headers, "mdc"));
            psseMultiTerminalDcMain.setVconv(parseIntFromRecord(rec, headers, "vconv"));
            psseMultiTerminalDcMain.setVcmod(parseDoubleFromRecord(rec, 0d, headers, "vcmod"));
            psseMultiTerminalDcMain.setVconvn(parseIntFromRecord(rec, 0, headers, "vconvn"));
            return psseMultiTerminalDcMain;
        }

        public static String[] toRecord(PsseMultiTerminalDcMain psseMultiTerminalDcMain, String[] headers) {
            String[] row = new String[headers.length];
            for (int i = 0; i < headers.length; i++) {
                row[i] = switch (headers[i]) {
                    case "name" -> psseMultiTerminalDcMain.getName();
                    case "nconv" -> String.valueOf(psseMultiTerminalDcMain.getNconv());
                    case "ndcbs" -> String.valueOf(psseMultiTerminalDcMain.getNdcbs());
                    case "ndcln" -> String.valueOf(psseMultiTerminalDcMain.getNdcln());
                    case "mdc" -> String.valueOf(psseMultiTerminalDcMain.getMdc());
                    case "vconv" -> String.valueOf(psseMultiTerminalDcMain.getVconv());
                    case "vcmod" -> String.valueOf(psseMultiTerminalDcMain.getVcmod());
                    case "vconvn" -> String.valueOf(psseMultiTerminalDcMain.getVconvn());
                    default -> throw new PsseException("Unsupported header: " + headers[i]);
                };
            }
            return row;
        }

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

        public int getMdc() {
            return mdc;
        }

        public int getVconv() {
            return vconv;
        }

        public double getVcmod() {
            return vcmod;
        }

        public int getVconvn() {
            return vconvn;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setNconv(int nconv) {
            this.nconv = nconv;
        }

        public void setNdcbs(int ndcbs) {
            this.ndcbs = ndcbs;
        }

        public void setNdcln(int ndcln) {
            this.ndcln = ndcln;
        }

        public void setMdc(int mdc) {
            this.mdc = mdc;
        }

        public void setVconv(int vconv) {
            this.vconv = vconv;
        }

        public void setVcmod(double vcmod) {
            this.vcmod = vcmod;
        }

        public void setVconvn(int vconvn) {
            this.vconvn = vconvn;
        }
    }

    public static class PsseMultiTerminalDcConverter {

        private int ib;
        private int n;
        private double angmx;
        private double angmn;
        private double rc;
        private double xc;
        private double ebas;
        private double tr = 1.0;
        private double tap = 1.0;
        private double tpmx = 1.5;
        private double tpmn = 0.51;
        private double tstp = 0.00625;
        private double setvl;
        private double dcpf = 1.0;
        private double marg = 0.0;
        private int cnvcod = 1;

        public static PsseMultiTerminalDcConverter fromRecord(CsvRecord rec, PsseVersion version, String[] headers) {
            PsseMultiTerminalDcConverter psseMultiTerminalDcConverter = new PsseMultiTerminalDcConverter();
            psseMultiTerminalDcConverter.setIb(parseIntFromRecord(rec, headers, "ib"));
            psseMultiTerminalDcConverter.setN(parseIntFromRecord(rec, headers, "n"));
            psseMultiTerminalDcConverter.setAngmx(parseDoubleFromRecord(rec, headers, "angmx"));
            psseMultiTerminalDcConverter.setAngmn(parseDoubleFromRecord(rec, headers, "angmn"));
            psseMultiTerminalDcConverter.setRc(parseDoubleFromRecord(rec, headers, "rc"));
            psseMultiTerminalDcConverter.setXc(parseDoubleFromRecord(rec, headers, "xc"));
            psseMultiTerminalDcConverter.setEbas(parseDoubleFromRecord(rec, headers, "ebas"));
            psseMultiTerminalDcConverter.setTr(parseDoubleFromRecord(rec, 1.0, headers, "tr"));
            psseMultiTerminalDcConverter.setTap(parseDoubleFromRecord(rec, 1.0, headers, "tap"));
            psseMultiTerminalDcConverter.setTpmx(parseDoubleFromRecord(rec, 1.5, headers, "tpmx"));
            psseMultiTerminalDcConverter.setTpmn(parseDoubleFromRecord(rec, 0.51, headers, "tpmn"));
            psseMultiTerminalDcConverter.setTstp(parseDoubleFromRecord(rec, 0.00625, headers, "tstp"));
            psseMultiTerminalDcConverter.setSetvl(parseDoubleFromRecord(rec, headers, "setvl"));
            psseMultiTerminalDcConverter.setDcpf(parseDoubleFromRecord(rec, 1.0, headers, "dcpf"));
            psseMultiTerminalDcConverter.setMarg(parseDoubleFromRecord(rec, 0.0, headers, "marg"));
            psseMultiTerminalDcConverter.setCnvcod(parseIntFromRecord(rec, 1, headers, "cnvcod"));
            return psseMultiTerminalDcConverter;
        }

        public static String[] toRecord(PsseMultiTerminalDcConverter psseMultiTerminalDcConverter, String[] headers) {
            String[] row = new String[headers.length];
            for (int i = 0; i < headers.length; i++) {
                row[i] = switch (headers[i]) {
                    case "ib" -> String.valueOf(psseMultiTerminalDcConverter.getIb());
                    case "n" -> String.valueOf(psseMultiTerminalDcConverter.getN());
                    case "angmx" -> String.valueOf(psseMultiTerminalDcConverter.getAngmx());
                    case "angmn" -> String.valueOf(psseMultiTerminalDcConverter.getAngmn());
                    case "rc" -> String.valueOf(psseMultiTerminalDcConverter.getRc());
                    case "xc" -> String.valueOf(psseMultiTerminalDcConverter.getXc());
                    case "ebas" -> String.valueOf(psseMultiTerminalDcConverter.getEbas());
                    case "tr" -> String.valueOf(psseMultiTerminalDcConverter.getTr());
                    case "tap" -> String.valueOf(psseMultiTerminalDcConverter.getTap());
                    case "tpmx" -> String.valueOf(psseMultiTerminalDcConverter.getTpmx());
                    case "tpmn" -> String.valueOf(psseMultiTerminalDcConverter.getTpmn());
                    case "tstp" -> String.valueOf(psseMultiTerminalDcConverter.getTstp());
                    case "setvl" -> String.valueOf(psseMultiTerminalDcConverter.getSetvl());
                    case "dcpf" -> String.valueOf(psseMultiTerminalDcConverter.getDcpf());
                    case "marg" -> String.valueOf(psseMultiTerminalDcConverter.getMarg());
                    case "cnvcod" -> String.valueOf(psseMultiTerminalDcConverter.getCnvcod());
                    default -> throw new PsseException("Unsupported header: " + headers[i]);
                };
            }
            return row;
        }

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

        private int idc;
        private int ib = 0;
        private int area = 1;
        private int zone = 1;
        private String dcname;
        private int idc2 = 0;
        private double rgrnd = 0.0;
        private int owner = 1;

        public static PsseMultiTerminalDcBus fromRecord(CsvRecord rec, PsseVersion version, String[] headers) {
            PsseMultiTerminalDcBus psseMultiTerminalDcBus = new PsseMultiTerminalDcBus();
            psseMultiTerminalDcBus.setIdc(parseIntFromRecord(rec, headers, "idc"));
            psseMultiTerminalDcBus.setIb(parseIntFromRecord(rec, headers, "ib"));
            psseMultiTerminalDcBus.setArea(parseIntFromRecord(rec, headers, "area"));
            psseMultiTerminalDcBus.setZone(parseIntFromRecord(rec, headers, "zone"));
            psseMultiTerminalDcBus.setDcname(parseStringFromRecord(rec, "            ", headers, "dcname"));
            psseMultiTerminalDcBus.setIdc2(parseIntFromRecord(rec, headers, "idc2"));
            psseMultiTerminalDcBus.setRgrnd(parseDoubleFromRecord(rec, headers, "rgrnd"));
            psseMultiTerminalDcBus.setOwner(parseIntFromRecord(rec, headers, "owner"));
            return psseMultiTerminalDcBus;
        }

        public static String[] toRecord(PsseMultiTerminalDcBus psseMultiTerminalDcBus, String[] headers) {
            String[] row = new String[headers.length];
            for (int i = 0; i < headers.length; i++) {
                row[i] = switch (headers[i]) {
                    case "idc" -> String.valueOf(psseMultiTerminalDcBus.getIdc());
                    case "ib" -> String.valueOf(psseMultiTerminalDcBus.getIb());
                    case "area" -> String.valueOf(psseMultiTerminalDcBus.getArea());
                    case "zone" -> String.valueOf(psseMultiTerminalDcBus.getZone());
                    case "dcname" -> String.valueOf(psseMultiTerminalDcBus.getDcname());
                    case "idc2" -> String.valueOf(psseMultiTerminalDcBus.getIdc2());
                    case "rgrnd" -> String.valueOf(psseMultiTerminalDcBus.getRgrnd());
                    case "owner" -> String.valueOf(psseMultiTerminalDcBus.getOwner());
                    default -> throw new PsseException("Unsupported header: " + headers[i]);
                };
            }
            return row;
        }

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

        private int idc;
        private int jdc;
        private String dcckt;
        private int met = 1;
        private double rdc;
        private double ldc = 0.0;

        public static PsseMultiTerminalDcLink fromRecord(CsvRecord rec, PsseVersion version, String[] headers) {
            PsseMultiTerminalDcLink psseMultiTerminalDcLink = new PsseMultiTerminalDcLink();
            psseMultiTerminalDcLink.setIdc(parseIntFromRecord(rec, headers, "idc"));
            psseMultiTerminalDcLink.setJdc(parseIntFromRecord(rec, headers, "jdc"));
            psseMultiTerminalDcLink.setDcckt(parseStringFromRecord(rec, "1", headers, "dcckt"));
            psseMultiTerminalDcLink.setMet(parseIntFromRecord(rec, headers, "met"));
            psseMultiTerminalDcLink.setRdc(parseDoubleFromRecord(rec, headers, "rdc"));
            psseMultiTerminalDcLink.setLdc(parseDoubleFromRecord(rec, headers, "ldc"));
            return psseMultiTerminalDcLink;
        }

        public static String[] toRecord(PsseMultiTerminalDcLink psseMultiTerminalDcLink, String[] headers) {
            String[] row = new String[headers.length];
            for (int i = 0; i < headers.length; i++) {
                row[i] = switch (headers[i]) {
                    case "idc" -> String.valueOf(psseMultiTerminalDcLink.getIdc());
                    case "jdc" -> String.valueOf(psseMultiTerminalDcLink.getJdc());
                    case "dcckt" -> String.valueOf(psseMultiTerminalDcLink.getDcckt());
                    case "met" -> String.valueOf(psseMultiTerminalDcLink.getMet());
                    case "rdc" -> String.valueOf(psseMultiTerminalDcLink.getRdc());
                    case "ldc" -> String.valueOf(psseMultiTerminalDcLink.getLdc());
                    default -> throw new PsseException("Unsupported header: " + headers[i]);
                };
            }
            return row;
        }

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

        private String name;
        private PsseMultiTerminalDcConverter converter;

        public static PsseMultiTerminalDcConverterx fromRecord(CsvRecord rec, PsseVersion version, String[] headers) {
            PsseMultiTerminalDcConverterx psseMultiTerminalDcConverterx = new PsseMultiTerminalDcConverterx();
            psseMultiTerminalDcConverterx.setName(parseStringFromRecord(rec, headers, "name"));
            psseMultiTerminalDcConverterx.setConverter(PsseMultiTerminalDcConverter.fromRecord(rec, version, headers));
            return psseMultiTerminalDcConverterx;
        }

        public static String[] toRecord(PsseMultiTerminalDcConverterx psseMultiTerminalDcConverterx, String[] headers) {
            String[] row = new String[headers.length];
            for (int i = 0; i < headers.length; i++) {
                row[i] = switch (headers[i]) {
                    case "name" -> psseMultiTerminalDcConverterx.getName();
                    case "ib" -> String.valueOf(psseMultiTerminalDcConverterx.getConverter().getIb());
                    case "n" -> String.valueOf(psseMultiTerminalDcConverterx.getConverter().getN());
                    case "angmx" -> String.valueOf(psseMultiTerminalDcConverterx.getConverter().getAngmx());
                    case "angmn" -> String.valueOf(psseMultiTerminalDcConverterx.getConverter().getAngmn());
                    case "rc" -> String.valueOf(psseMultiTerminalDcConverterx.getConverter().getRc());
                    case "xc" -> String.valueOf(psseMultiTerminalDcConverterx.getConverter().getXc());
                    case "ebas" -> String.valueOf(psseMultiTerminalDcConverterx.getConverter().getEbas());
                    case "tr" -> String.valueOf(psseMultiTerminalDcConverterx.getConverter().getTr());
                    case "tap" -> String.valueOf(psseMultiTerminalDcConverterx.getConverter().getTap());
                    case "tpmx" -> String.valueOf(psseMultiTerminalDcConverterx.getConverter().getTpmx());
                    case "tpmn" -> String.valueOf(psseMultiTerminalDcConverterx.getConverter().getTpmn());
                    case "tstp" -> String.valueOf(psseMultiTerminalDcConverterx.getConverter().getTstp());
                    case "setvl" -> String.valueOf(psseMultiTerminalDcConverterx.getConverter().getSetvl());
                    case "dcpf" -> String.valueOf(psseMultiTerminalDcConverterx.getConverter().getDcpf());
                    case "marg" -> String.valueOf(psseMultiTerminalDcConverterx.getConverter().getMarg());
                    case "cnvcod" -> String.valueOf(psseMultiTerminalDcConverterx.getConverter().getCnvcod());
                    default -> throw new PsseException("Unsupported header: " + headers[i]);
                };
            }
            return row;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public PsseMultiTerminalDcConverter getConverter() {
            return converter;
        }

        public void setConverter(PsseMultiTerminalDcConverter converter) {
            this.converter = converter;
        }
    }

    public static class PsseMultiTerminalDcBusx {

        public PsseMultiTerminalDcBusx() {
        }

        public PsseMultiTerminalDcBusx(String name, PsseMultiTerminalDcBus bus) {
            this.name = name;
            this.bus = bus;
        }

        private String name;
        private PsseMultiTerminalDcBus bus;

        public static PsseMultiTerminalDcBusx fromRecord(CsvRecord rec, PsseVersion version, String[] headers) {
            PsseMultiTerminalDcBusx psseMultiTerminalDcBusx = new PsseMultiTerminalDcBusx();
            psseMultiTerminalDcBusx.setName(parseStringFromRecord(rec, headers, "name"));
            psseMultiTerminalDcBusx.setBus(PsseMultiTerminalDcBus.fromRecord(rec, version, headers));
            return psseMultiTerminalDcBusx;
        }

        public static String[] toRecord(PsseMultiTerminalDcBusx psseMultiTerminalDcBusx, String[] headers) {
            String[] row = new String[headers.length];
            for (int i = 0; i < headers.length; i++) {
                row[i] = switch (headers[i]) {
                    case "name" -> psseMultiTerminalDcBusx.getName();
                    case "idc" -> String.valueOf(psseMultiTerminalDcBusx.getBus().getIdc());
                    case "ib" -> String.valueOf(psseMultiTerminalDcBusx.getBus().getIb());
                    case "area" -> String.valueOf(psseMultiTerminalDcBusx.getBus().getArea());
                    case "zone" -> String.valueOf(psseMultiTerminalDcBusx.getBus().getZone());
                    case "dcname" -> String.valueOf(psseMultiTerminalDcBusx.getBus().getDcname());
                    case "idc2" -> String.valueOf(psseMultiTerminalDcBusx.getBus().getIdc2());
                    case "rgrnd" -> String.valueOf(psseMultiTerminalDcBusx.getBus().getRgrnd());
                    case "owner" -> String.valueOf(psseMultiTerminalDcBusx.getBus().getOwner());
                    default -> throw new PsseException("Unsupported header: " + headers[i]);
                };
            }
            return row;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public PsseMultiTerminalDcBus getBus() {
            return bus;
        }

        public void setBus(PsseMultiTerminalDcBus bus) {
            this.bus = bus;
        }
    }

    public static class PsseMultiTerminalDcLinkx {

        public PsseMultiTerminalDcLinkx() {
        }

        public PsseMultiTerminalDcLinkx(String name, PsseMultiTerminalDcLink link) {
            this.name = name;
            this.link = link;
        }

        private String name;
        private PsseMultiTerminalDcLink link;

        public static PsseMultiTerminalDcLinkx fromRecord(CsvRecord rec, PsseVersion version, String[] headers) {
            PsseMultiTerminalDcLinkx psseMultiTerminalDcLinkx = new PsseMultiTerminalDcLinkx();
            psseMultiTerminalDcLinkx.setName(parseStringFromRecord(rec, headers, "name"));
            psseMultiTerminalDcLinkx.setLink(PsseMultiTerminalDcLink.fromRecord(rec, version, headers));
            return psseMultiTerminalDcLinkx;
        }

        public static String[] toRecord(PsseMultiTerminalDcLinkx psseMultiTerminalDcLinkx, String[] headers) {
            String[] row = new String[headers.length];
            for (int i = 0; i < headers.length; i++) {
                row[i] = switch (headers[i]) {
                    case "name" -> psseMultiTerminalDcLinkx.getName();
                    case "idc" -> String.valueOf(psseMultiTerminalDcLinkx.getLink().getIdc());
                    case "jdc" -> String.valueOf(psseMultiTerminalDcLinkx.getLink().getJdc());
                    case "dcckt" -> String.valueOf(psseMultiTerminalDcLinkx.getLink().getDcckt());
                    case "met" -> String.valueOf(psseMultiTerminalDcLinkx.getLink().getMet());
                    case "rdc" -> String.valueOf(psseMultiTerminalDcLinkx.getLink().getRdc());
                    case "ldc" -> String.valueOf(psseMultiTerminalDcLinkx.getLink().getLdc());
                    default -> throw new PsseException("Unsupported header: " + headers[i]);
                };
            }
            return row;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public PsseMultiTerminalDcLink getLink() {
            return link;
        }

        public void setLink(PsseMultiTerminalDcLink link) {
            this.link = link;
        }
    }
}
