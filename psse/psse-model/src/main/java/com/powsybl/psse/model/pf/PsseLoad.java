/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf;

import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseVersion;
import com.powsybl.psse.model.PsseVersioned;
import com.powsybl.psse.model.Revision;
import de.siegmar.fastcsv.reader.CsvRecord;

import static com.powsybl.psse.model.io.Util.parseDoubleFromRecord;
import static com.powsybl.psse.model.io.Util.parseIntFromRecord;
import static com.powsybl.psse.model.io.Util.parseStringFromRecord;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class PsseLoad extends PsseVersioned {

    private static final String STRING_INTRPT = "intrpt";
    private static final String STRING_DGENP = "dgenp";
    private static final String STRING_DGENQ = "dgenq";
    private static final String STRING_DGENM = "dgenm";
    private static final String STRING_LOAD_TYPE = "loadtype";

    private int i;
    private String id;
    private int status = 1;
    private int area = -1;
    private int zone = -1;
    private double pl = 0;
    private double ql = 0;
    private double ip = 0;
    private double iq = 0;
    private double yp = 0;
    private double yq = 0;
    private int owner = -1;
    private int scale = 1;

    @Revision(since = 33)
    private int intrpt = 0;

    @Revision(since = 35)
    private double dgenp = 0;

    @Revision(since = 35)
    private double dgenq = 0;

    @Revision(since = 35)
    private int dgenm = 0;

    @Revision(since = 35)
    private String loadtype;

    public static PsseLoad fromRecord(CsvRecord rec, PsseVersion version, String[] headers) {
        PsseLoad psseLoad = new PsseLoad();
        psseLoad.setI(parseIntFromRecord(rec, headers, "i", "ibus"));
        psseLoad.setId(parseStringFromRecord(rec, "1", headers, "id", "loadid"));
        psseLoad.setStatus(parseIntFromRecord(rec, 1, headers, "status", "stat"));
        psseLoad.setArea(parseIntFromRecord(rec, -1, headers, "area"));
        psseLoad.setZone(parseIntFromRecord(rec, -1, headers, "zone"));
        psseLoad.setPl(parseDoubleFromRecord(rec, 0d, headers, "pl"));
        psseLoad.setQl(parseDoubleFromRecord(rec, 0d, headers, "ql"));
        psseLoad.setIp(parseDoubleFromRecord(rec, 0d, headers, "ip"));
        psseLoad.setIq(parseDoubleFromRecord(rec, 0d, headers, "iq"));
        psseLoad.setYp(parseDoubleFromRecord(rec, 0d, headers, "yp"));
        psseLoad.setYq(parseDoubleFromRecord(rec, 0d, headers, "yq"));
        psseLoad.setOwner(parseIntFromRecord(rec, -1, headers, "owner"));
        psseLoad.setScale(parseIntFromRecord(rec, 1, headers, "scale"));
        if (version.getMajorNumber() >= 33) {
            psseLoad.setIntrpt(parseIntFromRecord(rec, 0, headers, STRING_INTRPT));
        }
        if (version.getMajorNumber() >= 35) {
            psseLoad.setDgenp(parseDoubleFromRecord(rec, 0d, headers, STRING_DGENP));
            psseLoad.setDgenq(parseDoubleFromRecord(rec, 0d, headers, STRING_DGENQ));
            psseLoad.setDgenm(parseIntFromRecord(rec, 0, headers, STRING_DGENM));
            psseLoad.setLoadtype(parseStringFromRecord(rec, "            ", headers, STRING_LOAD_TYPE));
        }
        return psseLoad;
    }

    public static String[] toRecord(PsseLoad psseLoad, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            row[i] = switch (headers[i]) {
                case "i", "ibus" -> String.valueOf(psseLoad.getI());
                case "id", "loadid" -> psseLoad.getId();
                case "status", "stat" -> String.valueOf(psseLoad.getStatus());
                case "area" -> String.valueOf(psseLoad.getArea());
                case "zone" -> String.valueOf(psseLoad.getZone());
                case "pl" -> String.valueOf(psseLoad.getPl());
                case "ql" -> String.valueOf(psseLoad.getQl());
                case "ip" -> String.valueOf(psseLoad.getIp());
                case "iq" -> String.valueOf(psseLoad.getIq());
                case "yp" -> String.valueOf(psseLoad.getYp());
                case "yq" -> String.valueOf(psseLoad.getYq());
                case "owner" -> String.valueOf(psseLoad.getOwner());
                case "scale" -> String.valueOf(psseLoad.getScale());
                case STRING_INTRPT -> String.valueOf(psseLoad.getIntrpt());
                case STRING_DGENP -> String.valueOf(psseLoad.getDgenp());
                case STRING_DGENQ -> String.valueOf(psseLoad.getDgenq());
                case STRING_DGENM -> String.valueOf(psseLoad.getDgenm());
                case STRING_LOAD_TYPE -> psseLoad.getLoadtype();
                default -> throw new PsseException("Unsupported header: " + headers[i]);
            };
        }
        return row;
    }

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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

    public double getPl() {
        return pl;
    }

    public void setPl(double pl) {
        this.pl = pl;
    }

    public double getQl() {
        return ql;
    }

    public void setQl(double ql) {
        this.ql = ql;
    }

    public double getIp() {
        return ip;
    }

    public void setIp(double ip) {
        this.ip = ip;
    }

    public double getIq() {
        return iq;
    }

    public void setIq(double iq) {
        this.iq = iq;
    }

    public double getYp() {
        return yp;
    }

    public void setYp(double yp) {
        this.yp = yp;
    }

    public double getYq() {
        return yq;
    }

    public void setYq(double yq) {
        this.yq = yq;
    }

    public int getOwner() {
        return owner;
    }

    public void setOwner(int owner) {
        this.owner = owner;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public int getIntrpt() {
        checkVersion(STRING_INTRPT);
        return intrpt;
    }

    public void setIntrpt(int intrpt) {
        checkVersion(STRING_INTRPT);
        this.intrpt = intrpt;
    }

    public double getDgenp() {
        checkVersion(STRING_DGENP);
        return dgenp;
    }

    public void setDgenp(double dgenp) {
        checkVersion(STRING_DGENP);
        this.dgenp = dgenp;
    }

    public double getDgenq() {
        checkVersion(STRING_DGENQ);
        return dgenq;
    }

    public void setDgenq(double dgenq) {
        checkVersion(STRING_DGENQ);
        this.dgenq = dgenq;
    }

    public int getDgenm() {
        checkVersion(STRING_DGENM);
        return dgenm;
    }

    public void setDgenm(int dgenm) {
        checkVersion(STRING_DGENM);
        this.dgenm = dgenm;
    }

    public String getLoadtype() {
        checkVersion(STRING_LOAD_TYPE);
        return loadtype;
    }

    public void setLoadtype(String loadtype) {
        checkVersion(STRING_LOAD_TYPE);
        this.loadtype = loadtype;
    }

    public PsseLoad copy() {
        PsseLoad copy = new PsseLoad();
        copy.i = this.i;
        copy.id = this.id;
        copy.status = this.status;
        copy.area = this.area;
        copy.zone = this.zone;
        copy.owner = this.owner;
        copy.pl = this.pl;
        copy.ql = this.ql;
        copy.ip = this.ip;
        copy.iq = this.iq;
        copy.yp = this.yp;
        copy.yq = this.yq;
        copy.scale = this.scale;
        copy.intrpt = this.intrpt;
        copy.dgenp = this.dgenp;
        copy.dgenq = this.dgenq;
        copy.dgenm = this.dgenm;
        copy.loadtype = this.loadtype;
        return copy;
    }
}
