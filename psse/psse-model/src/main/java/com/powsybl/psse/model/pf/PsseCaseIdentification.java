/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseVersion;
import com.powsybl.psse.model.io.PsseFieldDefinition;
import com.powsybl.psse.model.io.Util;
import de.siegmar.fastcsv.reader.CsvRecord;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.powsybl.psse.model.io.Util.addField;
import static com.powsybl.psse.model.io.Util.createNewField;
import static com.powsybl.psse.model.io.Util.defaultDoubleFor;
import static com.powsybl.psse.model.io.Util.defaultFloatFor;
import static com.powsybl.psse.model.io.Util.defaultIntegerFor;
import static com.powsybl.psse.model.io.Util.stringHeaders;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_BASFRQ;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_IC;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_NXFRAT;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_REV;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_SBASE;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_TITLE1;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_TITLE2;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_XFRRAT;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class PsseCaseIdentification {

    private static final Map<String, PsseFieldDefinition<PsseCaseIdentification, ?>> FIELDS = createFields();
    private static final String[] FIELD_NAMES = {STR_IC, STR_SBASE, STR_REV, STR_XFRRAT, STR_NXFRAT, STR_BASFRQ, STR_TITLE1, STR_TITLE2};

    private int ic = defaultIntegerFor(STR_IC, FIELDS);
    private double sbase = defaultDoubleFor(STR_SBASE, FIELDS);

    // Similar way writing revision for Json,
    // but relying on PsseVersion class
    @JsonSerialize(using = RevisionSerializer.class)
    private float rev = defaultFloatFor(STR_REV, FIELDS);

    private double xfrrat = defaultDoubleFor(STR_XFRRAT, FIELDS);
    private double nxfrat = defaultDoubleFor(STR_NXFRAT, FIELDS);
    private double basfrq = defaultDoubleFor(STR_BASFRQ, FIELDS);
    private String title1;
    private String title2;

    public static String[] getFieldNames() {
        return FIELD_NAMES;
    }

    public static String[] getFieldNamesString() {
        return stringHeaders(FIELDS);
    }

    public static PsseCaseIdentification fromRecord(CsvRecord rec, String[] headers) {
        return Util.fromRecord(rec.getFields(), headers, FIELDS, PsseCaseIdentification::new);
    }

    public static String[] toRecord(PsseCaseIdentification psseCaseIdentification, String[] headers) {
        return Util.toRecord(psseCaseIdentification, headers, FIELDS);
    }

    private static Map<String, PsseFieldDefinition<PsseCaseIdentification, ?>> createFields() {
        Map<String, PsseFieldDefinition<PsseCaseIdentification, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_IC, Integer.class, PsseCaseIdentification::getIc, PsseCaseIdentification::setIc, 0));
        addField(fields, createNewField(STR_SBASE, Double.class, PsseCaseIdentification::getSbase, PsseCaseIdentification::setSbase, 100d));
        addField(fields, createNewField(STR_REV, Float.class, value -> PsseVersion.fromRevision(value).toString(), PsseCaseIdentification::getRev, PsseCaseIdentification::setRev, 33f));
        addField(fields, createNewField(STR_XFRRAT, Double.class, value -> String.format("%.0f", value), PsseCaseIdentification::getXfrrat, PsseCaseIdentification::setXfrrat, Double.NaN));
        addField(fields, createNewField(STR_NXFRAT, Double.class, value -> String.format("%.0f", value), PsseCaseIdentification::getNxfrat, PsseCaseIdentification::setNxfrat, Double.NaN));
        addField(fields, createNewField(STR_BASFRQ, Double.class, PsseCaseIdentification::getBasfrq, PsseCaseIdentification::setBasfrq, Double.NaN));
        addField(fields, createNewField(STR_TITLE1, String.class, PsseCaseIdentification::getTitle1, PsseCaseIdentification::setTitle1, ""));
        addField(fields, createNewField(STR_TITLE2, String.class, PsseCaseIdentification::getTitle2, PsseCaseIdentification::setTitle2, ""));

        return fields;
    }

    public int getIc() {
        return ic;
    }

    public void setIc(int ic) {
        this.ic = ic;
    }

    public double getSbase() {
        return sbase;
    }

    public void setSbase(double sbase) {
        this.sbase = sbase;
    }

    public float getRev() {
        return rev;
    }

    public void setRev(float rev) {
        this.rev = rev;
    }

    public double getXfrrat() {
        return xfrrat;
    }

    public void setXfrrat(double xfrrat) {
        this.xfrrat = xfrrat;
    }

    public double getNxfrat() {
        return nxfrat;
    }

    public void setNxfrat(double nxfrat) {
        this.nxfrat = nxfrat;
    }

    public double getBasfrq() {
        return basfrq;
    }

    public void setBasfrq(double basfrq) {
        this.basfrq = basfrq;
    }

    public String getTitle1() {
        return title1;
    }

    public void setTitle1(String title1) {
        this.title1 = title1;
    }

    public String getTitle2() {
        return title2;
    }

    public void setTitle2(String title2) {
        this.title2 = title2;
    }

    public void validate() {
        if (ic == 1) {
            throw new PsseException("Incremental load of data option (IC = 1) is not supported");
        }
        PsseVersion v = PsseVersion.fromRevision(rev);
        if (!v.isSupported()) {
            throw new PsseException(String.format("Version %s not supported. Supported versions are: %s", v, PsseVersion.supportedVersions()));
        }
    }

    private static final class RevisionSerializer extends JsonSerializer<Float> {
        @Override
        public void serialize(Float value, JsonGenerator generator, SerializerProvider provider) throws IOException {
            String rev = PsseVersion.fromRevision(value).toString();
            generator.writeRawValue(rev);
        }
    }
}
