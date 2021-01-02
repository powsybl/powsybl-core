/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.pf;

import com.powsybl.psse.model.PsseVersioned;
import com.powsybl.psse.model.Revision;
import com.univocity.parsers.annotations.Parsed;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class PsseTwoTerminalDcTransmissionLine extends PsseVersioned {

    @Parsed
    private String name;

    @Parsed
    private int mdc = 0;

    @Parsed
    private double rdc;

    @Parsed
    private double setvl;

    @Parsed
    private double vschd;

    @Parsed
    private double vcmod = 0.0;

    @Parsed
    private double rcomp = 0.0;

    @Parsed
    private double delti = 0.0;

    @Parsed(field = { "meter", "met" })
    private String meter = "I";

    @Parsed
    private double dcvmin = 0.0;

    @Parsed
    private int cccitmx = 20;

    @Parsed
    private double cccacc = 1.0;

    @Parsed
    private int ipr;

    @Parsed
    private int nbr;

    @Parsed
    private double anmxr;

    @Parsed
    private double anmnr;

    @Parsed
    private double rcr;

    @Parsed
    private double xcr;

    @Parsed
    private double ebasr;

    @Parsed
    private double trr = 1.0;

    @Parsed
    private double tapr = 1.0;

    @Parsed
    private double tmxr = 1.5;

    @Parsed
    private double tmnr = 0.51;

    @Parsed
    private double stpr = 0.00625;

    @Parsed
    private int icr = 0;

    @Parsed
    private int ifr = 0;

    @Parsed
    private int itr = 0;

    @Parsed(defaultNullRead = "1")
    private String idr;

    @Parsed
    private double xcapr = 0.0;

    @Parsed
    private int ipi;

    @Parsed
    private int nbi;

    @Parsed
    private double anmxi;

    @Parsed
    private double anmni;

    @Parsed
    private double rci;

    @Parsed
    private double xci;

    @Parsed
    private double ebasi;

    @Parsed
    private double tri = 1.0;

    @Parsed
    private double tapi = 1.0;

    @Parsed
    private double tmxi = 1.5;

    @Parsed
    private double tmni = 0.51;

    @Parsed
    private double stpi = 0.00625;

    @Parsed
    private int ici = 0;

    @Parsed
    private int ifi = 0;

    @Parsed
    private int iti = 0;

    @Parsed(defaultNullRead = "1")
    private String idi;

    @Parsed
    private double xcapi = 0.0;

    @Parsed
    @Revision(since = 35)
    private int ndr = 0;

    @Parsed
    @Revision(since = 35)
    private int ndi = 0;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMdc() {
        return mdc;
    }

    public void setMdc(int mdc) {
        this.mdc = mdc;
    }

    public double getRdc() {
        return rdc;
    }

    public void setRdc(double rdc) {
        this.rdc = rdc;
    }

    public double getSetvl() {
        return setvl;
    }

    public void setSetvl(double setvl) {
        this.setvl = setvl;
    }

    public double getVschd() {
        return vschd;
    }

    public void setVschd(double vschd) {
        this.vschd = vschd;
    }

    public double getVcmod() {
        return vcmod;
    }

    public void setVcmod(double vcmod) {
        this.vcmod = vcmod;
    }

    public double getRcomp() {
        return rcomp;
    }

    public void setRcomp(double rcomp) {
        this.rcomp = rcomp;
    }

    public double getDelti() {
        return delti;
    }

    public void setDelti(double delti) {
        this.delti = delti;
    }

    public String getMeter() {
        return meter;
    }

    public void setMeter(String meter) {
        this.meter = meter;
    }

    public double getDcvmin() {
        return dcvmin;
    }

    public void setDcvmin(double dcvmin) {
        this.dcvmin = dcvmin;
    }

    public int getCccitmx() {
        return cccitmx;
    }

    public void setCccitmx(int cccitmx) {
        this.cccitmx = cccitmx;
    }

    public double getCccacc() {
        return cccacc;
    }

    public void setCccacc(double cccacc) {
        this.cccacc = cccacc;
    }

    public int getIpr() {
        return ipr;
    }

    public void setIpr(int ipr) {
        this.ipr = ipr;
    }

    public int getNbr() {
        return nbr;
    }

    public void setNbr(int nbr) {
        this.nbr = nbr;
    }

    public double getAnmxr() {
        return anmxr;
    }

    public void setAnmxr(double anmxr) {
        this.anmxr = anmxr;
    }

    public double getAnmnr() {
        return anmnr;
    }

    public void setAnmnr(double anmnr) {
        this.anmnr = anmnr;
    }

    public double getRcr() {
        return rcr;
    }

    public void setRcr(double rcr) {
        this.rcr = rcr;
    }

    public double getXcr() {
        return xcr;
    }

    public void setXcr(double xcr) {
        this.xcr = xcr;
    }

    public double getEbasr() {
        return ebasr;
    }

    public void setEbasr(double ebasr) {
        this.ebasr = ebasr;
    }

    public double getTrr() {
        return trr;
    }

    public void setTrr(double trr) {
        this.trr = trr;
    }

    public double getTapr() {
        return tapr;
    }

    public void setTapr(double tapr) {
        this.tapr = tapr;
    }

    public double getTmxr() {
        return tmxr;
    }

    public void setTmxr(double tmxr) {
        this.tmxr = tmxr;
    }

    public double getTmnr() {
        return tmnr;
    }

    public void setTmnr(double tmnr) {
        this.tmnr = tmnr;
    }

    public double getStpr() {
        return stpr;
    }

    public void setStpr(double stpr) {
        this.stpr = stpr;
    }

    public int getIcr() {
        return icr;
    }

    public void setIcr(int icr) {
        this.icr = icr;
    }

    public int getIfr() {
        return ifr;
    }

    public void setIfr(int ifr) {
        this.ifr = ifr;
    }

    public int getItr() {
        return itr;
    }

    public void setItr(int itr) {
        this.itr = itr;
    }

    public String getIdr() {
        return idr;
    }

    public void setIdr(String idr) {
        this.idr = idr;
    }

    public double getXcapr() {
        return xcapr;
    }

    public void setXcapr(double xcapr) {
        this.xcapr = xcapr;
    }

    public int getIpi() {
        return ipi;
    }

    public void setIpi(int ipi) {
        this.ipi = ipi;
    }

    public int getNbi() {
        return nbi;
    }

    public void setNbi(int nbi) {
        this.nbi = nbi;
    }

    public double getAnmxi() {
        return anmxi;
    }

    public void setAnmxi(double anmxi) {
        this.anmxi = anmxi;
    }

    public double getAnmni() {
        return anmni;
    }

    public void setAnmni(double anmni) {
        this.anmni = anmni;
    }

    public double getRci() {
        return rci;
    }

    public void setRci(double rci) {
        this.rci = rci;
    }

    public double getXci() {
        return xci;
    }

    public void setXci(double xci) {
        this.xci = xci;
    }

    public double getEbasi() {
        return ebasi;
    }

    public void setEbasi(double ebasi) {
        this.ebasi = ebasi;
    }

    public double getTri() {
        return tri;
    }

    public void setTri(double tri) {
        this.tri = tri;
    }

    public double getTapi() {
        return tapi;
    }

    public void setTapi(double tapi) {
        this.tapi = tapi;
    }

    public double getTmxi() {
        return tmxi;
    }

    public void setTmxi(double tmxi) {
        this.tmxi = tmxi;
    }

    public double getTmni() {
        return tmni;
    }

    public void setTmni(double tmni) {
        this.tmni = tmni;
    }

    public double getStpi() {
        return stpi;
    }

    public void setStpi(double stpi) {
        this.stpi = stpi;
    }

    public int getIci() {
        return ici;
    }

    public void setIci(int ici) {
        this.ici = ici;
    }

    public int getIfi() {
        return ifi;
    }

    public void setIfi(int ifi) {
        this.ifi = ifi;
    }

    public int getIti() {
        return iti;
    }

    public void setIti(int iti) {
        this.iti = iti;
    }

    public String getIdi() {
        return idi;
    }

    public void setIdi(String idi) {
        this.idi = idi;
    }

    public double getXcapi() {
        return xcapi;
    }

    public void setXcapi(double xcapi) {
        this.xcapi = xcapi;
    }

    public int getNdr() {
        checkVersion("ndr");
        return ndr;
    }

    public void setNdr(int ndr) {
        this.ndr = ndr;
    }

    public int getNdi() {
        checkVersion("ndi");
        return ndi;
    }

    public void setNdi(int ndi) {
        this.ndi = ndi;
    }
}
