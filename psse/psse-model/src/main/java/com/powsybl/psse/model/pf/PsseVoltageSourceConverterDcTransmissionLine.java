/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf;

import com.powsybl.psse.model.PsseVersioned;
import com.powsybl.psse.model.io.PsseFieldDefinition;
import com.powsybl.psse.model.io.Util;
import de.siegmar.fastcsv.reader.CsvRecord;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.powsybl.psse.model.io.Util.addField;
import static com.powsybl.psse.model.io.Util.addSuffixToHeaders;
import static com.powsybl.psse.model.io.Util.checkForUnexpectedHeader;
import static com.powsybl.psse.model.io.Util.concatStringArrays;
import static com.powsybl.psse.model.io.Util.createNewField;
import static com.powsybl.psse.model.io.Util.defaultIntegerFor;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_MDC;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_NAME;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_RDC;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PsseVoltageSourceConverterDcTransmissionLine extends PsseVersioned {

    private static final Map<String, PsseFieldDefinition<PsseVoltageSourceConverterDcTransmissionLine, ?>> FIELDS = createFields();
    private static final String[] FIELD_NAMES_SPECIFIC = {STR_NAME, STR_MDC, STR_RDC};
    private static final String[] FIELD_NAMES_35 = concatStringArrays(FIELD_NAMES_SPECIFIC, PsseOwnership.getFieldNames());
    private static final String[] FIELD_NAMES_35_RAWX = concatStringArrays(FIELD_NAMES_35,
        addSuffixToHeaders(PsseVoltageSourceConverter.getFieldNames35(), "1"),
        addSuffixToHeaders(PsseVoltageSourceConverter.getFieldNames35(), "2"));

    private String name;
    private int mdc = defaultIntegerFor(STR_MDC, FIELDS);
    private double rdc;
    private PsseOwnership ownership;
    private PsseVoltageSourceConverter converter1 = new PsseVoltageSourceConverter();
    private PsseVoltageSourceConverter converter2 = new PsseVoltageSourceConverter();

    public static String[] getFieldNames35() {
        return FIELD_NAMES_35;
    }

    public static String[] getFieldNames35RawX() {
        return FIELD_NAMES_35_RAWX;
    }

    public static PsseVoltageSourceConverterDcTransmissionLine fromRecord(CsvRecord rec, String[] headers) {
        PsseVoltageSourceConverterDcTransmissionLine transmissionLine = Util.fromRecord(rec.getFields(), headers, FIELDS, PsseVoltageSourceConverterDcTransmissionLine::new);
        transmissionLine.setOwnership(PsseOwnership.fromRecord(rec, headers));
        transmissionLine.setConverter1(PsseVoltageSourceConverter.fromRecord(rec, headers, "1"));
        transmissionLine.setConverter2(PsseVoltageSourceConverter.fromRecord(rec, headers, "2"));
        return transmissionLine;
    }

    public static String[] toRecord(PsseVoltageSourceConverterDcTransmissionLine transmissionLine, String[] headers) {
        Set<String> unexpectedHeaders = new HashSet<>(List.of(headers));
        String[] recordValues = Util.toRecord(transmissionLine, headers, FIELDS, unexpectedHeaders);
        PsseOwnership.toRecord(transmissionLine.getOwnership(), headers, recordValues, unexpectedHeaders);
        PsseVoltageSourceConverter.toRecord(transmissionLine.getConverter1(), headers, recordValues, unexpectedHeaders, "1");
        PsseVoltageSourceConverter.toRecord(transmissionLine.getConverter2(), headers, recordValues, unexpectedHeaders, "2");
        checkForUnexpectedHeader(unexpectedHeaders);
        return recordValues;
    }

    private static Map<String, PsseFieldDefinition<PsseVoltageSourceConverterDcTransmissionLine, ?>> createFields() {
        Map<String, PsseFieldDefinition<PsseVoltageSourceConverterDcTransmissionLine, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_NAME, String.class, PsseVoltageSourceConverterDcTransmissionLine::getName, PsseVoltageSourceConverterDcTransmissionLine::setName));
        addField(fields, createNewField(STR_MDC, Integer.class, PsseVoltageSourceConverterDcTransmissionLine::getMdc, PsseVoltageSourceConverterDcTransmissionLine::setMdc, 1));
        addField(fields, createNewField(STR_RDC, Double.class, PsseVoltageSourceConverterDcTransmissionLine::getRdc, PsseVoltageSourceConverterDcTransmissionLine::setRdc));

        return fields;
    }

    @Override
    public void setModel(PssePowerFlowModel model) {
        super.setModel(model);
        ownership.setModel(model);
        converter1.setModel(model);
        converter2.setModel(model);
    }

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

    public PsseOwnership getOwnership() {
        return ownership;
    }

    public void setOwnership(PsseOwnership ownership) {
        this.ownership = ownership;
    }

    public void setConverter1(PsseVoltageSourceConverter converter1) {
        this.converter1 = converter1;
    }

    public PsseVoltageSourceConverter getConverter1() {
        return converter1;
    }

    public void setConverter2(PsseVoltageSourceConverter converter2) {
        this.converter2 = converter2;
    }

    public PsseVoltageSourceConverter getConverter2() {
        return converter2;
    }

    public PsseVoltageSourceConverterDcTransmissionLine copy() {
        PsseVoltageSourceConverterDcTransmissionLine copy = new PsseVoltageSourceConverterDcTransmissionLine();
        copy.name = this.name;
        copy.mdc = this.mdc;
        copy.rdc = this.rdc;
        copy.ownership = this.ownership.copy();
        copy.converter1 = this.converter1.copy();
        copy.converter2 = this.converter2.copy();
        return copy;
    }
}
