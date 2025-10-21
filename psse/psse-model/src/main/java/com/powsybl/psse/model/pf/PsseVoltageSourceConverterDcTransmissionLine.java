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
import de.siegmar.fastcsv.reader.CsvRecord;

import java.util.Optional;

import static com.powsybl.psse.model.io.Util.parseDoubleFromRecord;
import static com.powsybl.psse.model.io.Util.parseIntFromRecord;
import static com.powsybl.psse.model.io.Util.parseStringFromRecord;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PsseVoltageSourceConverterDcTransmissionLine extends PsseVersioned {

    @Override
    public void setModel(PssePowerFlowModel model) {
        super.setModel(model);
        ownership.setModel(model);
        converter1.setModel(model);
        converter2.setModel(model);
    }

    private String name;
    private int mdc = 1;
    private double rdc;
    private PsseOwnership ownership;
    private PsseVoltageSourceConverter converter1;
    private PsseVoltageSourceConverter converter2;

    public static PsseVoltageSourceConverterDcTransmissionLine fromRecord(CsvRecord rec, PsseVersion version, String[] headers) {
        PsseVoltageSourceConverterDcTransmissionLine psseVoltageSourceConverter = new PsseVoltageSourceConverterDcTransmissionLine();
        psseVoltageSourceConverter.setName(parseStringFromRecord(rec, headers, "name"));
        psseVoltageSourceConverter.setMdc(parseIntFromRecord(rec, headers, "mdc"));
        psseVoltageSourceConverter.setRdc(parseDoubleFromRecord(rec, headers, "rdc"));
        psseVoltageSourceConverter.setOwnership(PsseOwnership.fromRecord(rec, version, headers));
        psseVoltageSourceConverter.setConverter1(PsseVoltageSourceConverter.fromRecord(rec, version, headers, "1"));
        psseVoltageSourceConverter.setConverter2(PsseVoltageSourceConverter.fromRecord(rec, version, headers, "2"));
        return psseVoltageSourceConverter;
    }

    public static String[] toRecord(PsseVoltageSourceConverterDcTransmissionLine psseVoltageSourceConverterDcTransmissionLine, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            row[i] = switch (headers[i]) {
                case "name" -> psseVoltageSourceConverterDcTransmissionLine.getName();
                case "mdc" -> String.valueOf(psseVoltageSourceConverterDcTransmissionLine.getMdc());
                case "rdc" -> String.valueOf(psseVoltageSourceConverterDcTransmissionLine.getRdc());
                default -> {
                    Optional<String> optionalValue = psseVoltageSourceConverterDcTransmissionLine.getOwnership().headerToString(headers[i]);
                    if (optionalValue.isPresent()) {
                        yield optionalValue.get();
                    }
                    optionalValue = psseVoltageSourceConverterDcTransmissionLine.getConverter1().headerToString(headers[i].substring(0, headers[i].length() - 1));
                    if (optionalValue.isPresent()) {
                        yield optionalValue.get();
                    }
                    optionalValue = psseVoltageSourceConverterDcTransmissionLine.getConverter2().headerToString(headers[i].substring(0, headers[i].length() - 1));
                    if (optionalValue.isPresent()) {
                        yield optionalValue.get();
                    }
                    throw new PsseException("Unsupported header: " + headers[i]);
                }
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
