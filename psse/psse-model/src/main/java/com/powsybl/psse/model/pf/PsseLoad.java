/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf;

import com.powsybl.psse.model.PsseVersioned;
import com.powsybl.psse.model.Revision;
import com.powsybl.psse.model.io.PsseFieldDefinition;
import com.powsybl.psse.model.io.Util;
import de.siegmar.fastcsv.reader.CsvRecord;

import java.util.HashMap;
import java.util.Map;

import static com.powsybl.psse.model.io.Util.addField;
import static com.powsybl.psse.model.io.Util.concatStringArrays;
import static com.powsybl.psse.model.io.Util.createNewField;
import static com.powsybl.psse.model.io.Util.defaultDoubleFor;
import static com.powsybl.psse.model.io.Util.defaultIntegerFor;
import static com.powsybl.psse.model.io.Util.defaultStringFor;
import static com.powsybl.psse.model.io.Util.stringHeaders;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.*;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class PsseLoad extends PsseVersioned {

    private static final Map<String, PsseFieldDefinition<PsseLoad, ?>> FIELDS = createFields();

    private static final String[] FIELD_NAMES_COMMON = {STR_AREA, STR_ZONE, STR_PL, STR_QL, STR_IP, STR_IQ, STR_YP, STR_YQ, STR_OWNER, STR_SCALE};
    private static final String[] FIELD_NAMES_START_32_33 = {STR_I, STR_ID, STR_STATUS};
    private static final String[] FIELD_NAMES_START_35 = {STR_IBUS, STR_LOADID, STR_STAT};
    private static final String[] FIELD_NAMES_COMMON_33 = {STR_INTRPT};
    private static final String[] FIELD_NAMES_COMMON_35 = {STR_DGENP, STR_DGENQ, STR_DGENM, STR_LOADTYPE};
    private static final String[] FIELD_NAMES_32 = concatStringArrays(FIELD_NAMES_START_32_33, FIELD_NAMES_COMMON);
    private static final String[] FIELD_NAMES_33 = concatStringArrays(FIELD_NAMES_START_32_33, FIELD_NAMES_COMMON, FIELD_NAMES_COMMON_33);
    private static final String[] FIELD_NAMES_35 = concatStringArrays(FIELD_NAMES_START_35, FIELD_NAMES_COMMON, FIELD_NAMES_COMMON_33, FIELD_NAMES_COMMON_35);
    private static final String[] FIELD_NAMES_35_RAWX = concatStringArrays(FIELD_NAMES_START_35, FIELD_NAMES_COMMON, FIELD_NAMES_COMMON_33, FIELD_NAMES_COMMON_35);

    private int i;
    private String id;
    private int status = defaultIntegerFor(STR_STATUS, FIELDS);
    private int area = defaultIntegerFor(STR_AREA, FIELDS);
    private int zone = defaultIntegerFor(STR_ZONE, FIELDS);
    private double pl = defaultDoubleFor(STR_PL, FIELDS);
    private double ql = defaultDoubleFor(STR_QL, FIELDS);
    private double ip = defaultDoubleFor(STR_IP, FIELDS);
    private double iq = defaultDoubleFor(STR_IQ, FIELDS);
    private double yp = defaultDoubleFor(STR_YP, FIELDS);
    private double yq = defaultDoubleFor(STR_YQ, FIELDS);
    private int owner = defaultIntegerFor(STR_OWNER, FIELDS);
    private int scale = defaultIntegerFor(STR_SCALE, FIELDS);

    @Revision(since = 33)
    private int intrpt = defaultIntegerFor(STR_INTRPT, FIELDS);

    @Revision(since = 35)
    private double dgenp = defaultDoubleFor(STR_DGENP, FIELDS);

    @Revision(since = 35)
    private double dgenq = defaultDoubleFor(STR_DGENQ, FIELDS);

    @Revision(since = 35)
    private int dgenm = defaultIntegerFor(STR_DGENM, FIELDS);

    @Revision(since = 35)
    private String loadtype = defaultStringFor(STR_LOADTYPE, FIELDS);

    public static String[] getFieldNames32() {
        return FIELD_NAMES_32;
    }

    public static String[] getFieldNames33() {
        return FIELD_NAMES_33;
    }

    public static String[] getFieldNames35() {
        return FIELD_NAMES_35;
    }

    public static String[] getFieldNames35RawX() {
        return FIELD_NAMES_35_RAWX;
    }

    public static String[] getFieldNamesString() {
        return stringHeaders(FIELDS);
    }

    public static PsseLoad fromRecord(CsvRecord rec, String[] headers) {
        return Util.fromRecord(rec.getFields(), headers, FIELDS, PsseLoad::new);
    }

    public static String[] toRecord(PsseLoad psseLoad, String[] headers) {
        return Util.toRecord(psseLoad, headers, FIELDS);
    }

    private static Map<String, PsseFieldDefinition<PsseLoad, ?>> createFields() {
        Map<String, PsseFieldDefinition<PsseLoad, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_I, Integer.class, PsseLoad::getI, PsseLoad::setI));
        addField(fields, createNewField(STR_IBUS, Integer.class, PsseLoad::getI, PsseLoad::setI));
        addField(fields, createNewField(STR_ID, String.class, PsseLoad::getId, PsseLoad::setId));
        addField(fields, createNewField(STR_LOADID, String.class, PsseLoad::getId, PsseLoad::setId));
        addField(fields, createNewField(STR_STATUS, Integer.class, PsseLoad::getStatus, PsseLoad::setStatus, 1));
        addField(fields, createNewField(STR_STAT, Integer.class, PsseLoad::getStatus, PsseLoad::setStatus, 1));
        addField(fields, createNewField(STR_AREA, Integer.class, PsseLoad::getArea, PsseLoad::setArea, -1));
        addField(fields, createNewField(STR_ZONE, Integer.class, PsseLoad::getZone, PsseLoad::setZone, -1));
        addField(fields, createNewField(STR_PL, Double.class, PsseLoad::getPl, PsseLoad::setPl, 0.0));
        addField(fields, createNewField(STR_QL, Double.class, PsseLoad::getQl, PsseLoad::setQl, 0.0));
        addField(fields, createNewField(STR_IP, Double.class, PsseLoad::getIp, PsseLoad::setIp, 0.0));
        addField(fields, createNewField(STR_IQ, Double.class, PsseLoad::getIq, PsseLoad::setIq, 0.0));
        addField(fields, createNewField(STR_YP, Double.class, PsseLoad::getYp, PsseLoad::setYp, 0.0));
        addField(fields, createNewField(STR_YQ, Double.class, PsseLoad::getYq, PsseLoad::setYq, 0.0));
        addField(fields, createNewField(STR_OWNER, Integer.class, PsseLoad::getOwner, PsseLoad::setOwner, -1));
        addField(fields, createNewField(STR_SCALE, Integer.class, PsseLoad::getScale, PsseLoad::setScale, -1));
        addField(fields, createNewField(STR_INTRPT, Integer.class, PsseLoad::getIntrpt, PsseLoad::setIntrpt, 0));
        addField(fields, createNewField(STR_DGENP, Double.class, PsseLoad::getDgenp, PsseLoad::setDgenp, 0.0));
        addField(fields, createNewField(STR_DGENQ, Double.class, PsseLoad::getDgenq, PsseLoad::setDgenq, 0.0));
        addField(fields, createNewField(STR_DGENM, Integer.class, PsseLoad::getDgenm, PsseLoad::setDgenm, 0));
        addField(fields, createNewField(STR_LOADTYPE, String.class, PsseLoad::getLoadtype, PsseLoad::setLoadtype, " ".repeat(12)));

        return fields;
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
        checkVersion(STR_INTRPT);
        return intrpt;
    }

    public void setIntrpt(int intrpt) {
        checkVersion(STR_INTRPT);
        this.intrpt = intrpt;
    }

    public double getDgenp() {
        checkVersion(STR_DGENP);
        return dgenp;
    }

    public void setDgenp(double dgenp) {
        checkVersion(STR_DGENP);
        this.dgenp = dgenp;
    }

    public double getDgenq() {
        checkVersion(STR_DGENQ);
        return dgenq;
    }

    public void setDgenq(double dgenq) {
        checkVersion(STR_DGENQ);
        this.dgenq = dgenq;
    }

    public int getDgenm() {
        checkVersion(STR_DGENM);
        return dgenm;
    }

    public void setDgenm(int dgenm) {
        checkVersion(STR_DGENM);
        this.dgenm = dgenm;
    }

    public String getLoadtype() {
        checkVersion(STR_LOADTYPE);
        return loadtype;
    }

    public void setLoadtype(String loadtype) {
        checkVersion(STR_LOADTYPE);
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
