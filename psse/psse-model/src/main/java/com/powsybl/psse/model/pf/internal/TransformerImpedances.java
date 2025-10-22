/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf.internal;

import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseVersion;
import de.siegmar.fastcsv.reader.CsvRecord;

import java.util.Optional;

import static com.powsybl.psse.model.io.Util.parseDoubleFromRecord;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class TransformerImpedances {

    private double r12 = 0;
    private double x12 = Double.NaN;
    private double sbase12 = Double.NaN;
    private double r23 = 0;
    private double x23 = Double.NaN;
    private double sbase23 = Double.NaN;
    private double r31 = 0;
    private double x31 = Double.NaN;
    private double sbase31 = Double.NaN;
    private double vmstar = 1;
    private double anstar = 0;

    public static TransformerImpedances fromRecord(CsvRecord rec, PsseVersion version, String[] headers) {
        TransformerImpedances transformerImpedances = new TransformerImpedances();
        transformerImpedances.setR12(parseDoubleFromRecord(rec, 0d, headers, "r12", "r1_2"));
        transformerImpedances.setX12(parseDoubleFromRecord(rec, headers, "x12", "x1_2"));
        transformerImpedances.setSbase12(parseDoubleFromRecord(rec, Double.NaN, headers, "sbase12", "sbase1_2"));
        transformerImpedances.setR23(parseDoubleFromRecord(rec, 0.0, headers, "r23", "r2_3"));
        transformerImpedances.setX23(parseDoubleFromRecord(rec, Double.NaN, headers, "x23", "x2_3"));
        transformerImpedances.setSbase23(parseDoubleFromRecord(rec, Double.NaN, headers, "sbase23", "sbase2_3"));
        transformerImpedances.setR31(parseDoubleFromRecord(rec, 0.0, headers, "r31", "r3_1"));
        transformerImpedances.setX31(parseDoubleFromRecord(rec, Double.NaN, headers, "x31", "x3_1"));
        transformerImpedances.setSbase31(parseDoubleFromRecord(rec, Double.NaN, headers, "sbase31", "sbase3_1"));
        transformerImpedances.setVmstar(parseDoubleFromRecord(rec, 1.0, headers, "vmstar"));
        transformerImpedances.setAnstar(parseDoubleFromRecord(rec, 0.0, headers, "anstar"));
        return transformerImpedances;
    }

    public static String[] toRecord(TransformerImpedances transformerImpedances, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            Optional<String> optionalValue = transformerImpedances.headerToString(headers[i]);
            if (optionalValue.isEmpty()) {
                throw new PsseException("Unsupported header: " + headers[i]);
            }
            row[i] = optionalValue.get();
        }
        return row;
    }

    public Optional<String> headerToString(String header) {
        return switch (header) {
            case "r12", "r1_2" -> Optional.of(String.valueOf(getR12()));
            case "x12", "x1_2" -> Optional.of(String.valueOf(getX12()));
            case "sbase12", "sbase1_2" -> Optional.of(String.valueOf(getSbase12()));
            case "r23", "r2_3" -> Optional.of(String.valueOf(getR23()));
            case "x23", "x2_3" -> Optional.of(String.valueOf(getX23()));
            case "sbase23", "sbase2_3" -> Optional.of(String.valueOf(getSbase23()));
            case "r31", "r3_1" -> Optional.of(String.valueOf(getR31()));
            case "x31", "x3_1" -> Optional.of(String.valueOf(getX31()));
            case "sbase31", "sbase3_1" -> Optional.of(String.valueOf(getSbase31()));
            case "vmstar" -> Optional.of(String.valueOf(getVmstar()));
            case "anstar" -> Optional.of(String.valueOf(getAnstar()));
            default -> Optional.empty();
        };
    }

    public TransformerImpedances copy() {
        TransformerImpedances copy = new TransformerImpedances();
        copy.r12 = this.r12;
        copy.x12 = this.x12;
        copy.sbase12 = this.sbase12;
        copy.r23 = this.r23;
        copy.x23 = this.x23;
        copy.sbase23 = this.sbase23;
        copy.r31 = this.r31;
        copy.x31 = this.x31;
        copy.sbase31 = this.sbase31;
        copy.vmstar = this.vmstar;
        copy.anstar = this.anstar;
        return copy;
    }

    public double getR12() {
        return r12;
    }

    public void setR12(double r12) {
        this.r12 = r12;
    }

    public double getX12() {
        return x12;
    }

    public void setX12(double x12) {
        this.x12 = x12;
    }

    public double getSbase12() {
        return sbase12;
    }

    public void setSbase12(double sbase12) {
        this.sbase12 = sbase12;
    }

    public double getR23() {
        return r23;
    }

    public void setR23(double r23) {
        this.r23 = r23;
    }

    public double getX23() {
        return x23;
    }

    public void setX23(double x23) {
        this.x23 = x23;
    }

    public double getSbase23() {
        return sbase23;
    }

    public void setSbase23(double sbase23) {
        this.sbase23 = sbase23;
    }

    public double getR31() {
        return r31;
    }

    public void setR31(double r31) {
        this.r31 = r31;
    }

    public double getX31() {
        return x31;
    }

    public void setX31(double x31) {
        this.x31 = x31;
    }

    public double getSbase31() {
        return sbase31;
    }

    public void setSbase31(double sbase31) {
        this.sbase31 = sbase31;
    }

    public double getVmstar() {
        return vmstar;
    }

    public void setVmstar(double vmstar) {
        this.vmstar = vmstar;
    }

    public double getAnstar() {
        return anstar;
    }

    public void setAnstar(double anstar) {
        this.anstar = anstar;
    }
}
