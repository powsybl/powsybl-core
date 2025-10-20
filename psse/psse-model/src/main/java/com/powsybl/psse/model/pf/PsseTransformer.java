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
import com.powsybl.psse.model.PsseVersioned;
import com.powsybl.psse.model.Revision;
import de.siegmar.fastcsv.reader.NamedCsvRecord;

import java.util.Optional;

import static com.powsybl.psse.model.io.Util.defaultIfEmpty;
import static com.powsybl.psse.model.io.Util.getFieldFromMultiplePotentialHeaders;
import static com.powsybl.psse.model.io.Util.parseDoubleOrDefault;
import static com.powsybl.psse.model.io.Util.parseIntOrDefault;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */

@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(value = { "impedances" })
public class PsseTransformer extends PsseVersioned {

    @Override
    public void setModel(PssePowerFlowModel model) {
        super.setModel(model);
        ownership.setModel(model);
        winding1.setModel(model);
        winding2.setModel(model);
        winding3.setModel(model);
        winding1Rates.setModel(model);
        winding2Rates.setModel(model);
        winding3Rates.setModel(model);
    }

    private int i;
    private int j;
    private int k = 0;
    private String ckt;
    private int cw = 1;
    private int cz = 1;
    private int cm = 1;
    private double mag1 = 0;
    private double mag2 = 0;
    private int nmetr = 2;
    private String name;
    private int stat = 1;
    private PsseOwnership ownership;

    // If the issue 432 in Univocity is fixed,
    // the previous annotation will be correctly processed
    // and there would be no need to initialize vecgrp with default value
    // (https://github.com/uniVocity/univocity-parsers/issues/432)
    @Revision(since = 33)
    private String vecgrp = "            ";

    @Revision(since = 35)
    private int zcod = 0;

    TransformerImpedances impedances;
    private PsseTransformerWinding winding1;
    private PsseRates winding1Rates;
    private PsseTransformerWinding winding2;
    private PsseRates winding2Rates;
    private PsseTransformerWinding winding3;
    private PsseRates winding3Rates;

    public static PsseTransformer fromRecord(NamedCsvRecord rec, PsseVersion version) {
        PsseTransformer psseTransformer = new PsseTransformer();
        psseTransformer.setI(Integer.parseInt(getFieldFromMultiplePotentialHeaders(rec, "i", "ibus")));
        psseTransformer.setJ(Integer.parseInt(getFieldFromMultiplePotentialHeaders(rec, "j", "jbus")));
        psseTransformer.setK(Integer.parseInt(getFieldFromMultiplePotentialHeaders(rec, "k", "kbus")));
        psseTransformer.setCkt(defaultIfEmpty(rec.getField("ckt"), "1"));
        psseTransformer.setCw(Integer.parseInt(rec.getField("cw")));
        psseTransformer.setCz(Integer.parseInt(rec.getField("cz")));
        psseTransformer.setCm(Integer.parseInt(rec.getField("cm")));
        psseTransformer.setMag1(Integer.parseInt(rec.getField("mag1")));
        psseTransformer.setMag2(Integer.parseInt(rec.getField("mag2")));
        psseTransformer.setNmetr(Integer.parseInt(getFieldFromMultiplePotentialHeaders(rec, "nmetr", "nmet")));
        psseTransformer.setName(defaultIfEmpty(rec.getField("name"), "            "));
        psseTransformer.setStat(Integer.parseInt(rec.getField("stat")));
        psseTransformer.setOwnership(PsseOwnership.fromRecord(rec, version));
        if (version.getMajorNumber() >= 33) {
            psseTransformer.setVecgrp(defaultIfEmpty(rec.getField("vecgrp"), "            "));
        }
        if (version.getMajorNumber() >= 35) {
            psseTransformer.setZcod(parseIntOrDefault(rec.getField("zcod"), 0));
        }
        TransformerImpedances transformerImpedances = new TransformerImpedances();
        psseTransformer.setImpedances(transformerImpedances);
        psseTransformer.setR12(Double.parseDouble(getFieldFromMultiplePotentialHeaders(rec, "r12", "r1_2")));
        psseTransformer.setX12(Double.parseDouble(getFieldFromMultiplePotentialHeaders(rec, "x12", "x1_2")));
        psseTransformer.setSbase12(Double.parseDouble(getFieldFromMultiplePotentialHeaders(rec, "sbase12", "sbase1_2")));
        psseTransformer.setR23(parseDoubleOrDefault(getFieldFromMultiplePotentialHeaders(rec, "r23", "r2_3"), 0.0));
        psseTransformer.setX23(parseDoubleOrDefault(getFieldFromMultiplePotentialHeaders(rec, "x23", "x2_3"), Double.NaN));
        psseTransformer.setSbase23(parseDoubleOrDefault(getFieldFromMultiplePotentialHeaders(rec, "sbase23", "sbase2_3"), Double.NaN));
        psseTransformer.setR31(parseDoubleOrDefault(getFieldFromMultiplePotentialHeaders(rec, "r31", "r3_1"), 0.0));
        psseTransformer.setX31(parseDoubleOrDefault(getFieldFromMultiplePotentialHeaders(rec, "x31", "x3_1"), Double.NaN));
        psseTransformer.setSbase31(parseDoubleOrDefault(getFieldFromMultiplePotentialHeaders(rec, "sbase31", "sbase3_1"), Double.NaN));
        psseTransformer.setVmstar(parseDoubleOrDefault(rec.getField("vmstar"), 1.0));
        psseTransformer.setAnstar(parseDoubleOrDefault(rec.getField("anstar"), 0.0));
        psseTransformer.setWinding1(PsseTransformerWinding.fromRecord(rec, version, "1"), PsseRates.fromRecord(rec, version, "1"));
        psseTransformer.setWinding2(PsseTransformerWinding.fromRecord(rec, version, "2"), PsseRates.fromRecord(rec, version, "2"));
        psseTransformer.setWinding3(PsseTransformerWinding.fromRecord(rec, version, "3"), PsseRates.fromRecord(rec, version, "3"));
        return psseTransformer;
    }

    public static String[] toRecord(PsseTransformer psseTransformer, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            row[i] = switch (headers[i]) {
                case "i", "ibus" -> String.valueOf(psseTransformer.getI());
                case "j", "jbus" -> String.valueOf(psseTransformer.getJ());
                case "k", "kbus" -> String.valueOf(psseTransformer.getK());
                case "ckt" -> String.valueOf(psseTransformer.getCkt());
                case "cw" -> String.valueOf(psseTransformer.getCw());
                case "cz" -> String.valueOf(psseTransformer.getCz());
                case "cm" -> String.valueOf(psseTransformer.getCm());
                case "mag1" -> String.valueOf(psseTransformer.getMag1());
                case "mag2" -> String.valueOf(psseTransformer.getMag2());
                case "nmetr", "nmet" -> String.valueOf(psseTransformer.getNmetr());
                case "name" -> psseTransformer.getName();
                case "stat" -> String.valueOf(psseTransformer.getStat());
                case "vecgrp" -> String.valueOf(psseTransformer.getVecgrp());
                case "zcod" -> String.valueOf(psseTransformer.getZcod());
                case "r12", "r1_2" -> String.valueOf(psseTransformer.getR12());
                case "x12", "x1_2" -> String.valueOf(psseTransformer.getX12());
                case "sbase12", "sbase1_2" -> String.valueOf(psseTransformer.getSbase12());
                case "r23", "r2_3" -> String.valueOf(psseTransformer.getR23());
                case "x23", "x2_3" -> String.valueOf(psseTransformer.getX23());
                case "sbase23", "sbase2_3" -> String.valueOf(psseTransformer.getSbase23());
                case "r31", "r3_1" -> String.valueOf(psseTransformer.getR31());
                case "x31", "x3_1" -> String.valueOf(psseTransformer.getX31());
                case "sbase31", "sbase3_1" -> String.valueOf(psseTransformer.getSbase31());
                case "vmstar" -> String.valueOf(psseTransformer.getVmstar());
                case "anstar" -> String.valueOf(psseTransformer.getAnstar());
                default -> {
                    Optional<String> optionalValue = psseTransformer.getOwnership().headerToString(headers[i]);
                    if (optionalValue.isPresent()) {
                        yield optionalValue.get();
                    }
                    optionalValue = psseTransformer.getWinding1().headerToString(headers[i].substring(0, headers[i].length() - 1));
                    if (optionalValue.isPresent()) {
                        yield optionalValue.get();
                    }
                    optionalValue = psseTransformer.getWinding1Rates().headerToString(headers[i].substring(0, headers[i].length() - 1));
                    if (optionalValue.isPresent()) {
                        yield optionalValue.get();
                    }
                    optionalValue = psseTransformer.getWinding2().headerToString(headers[i].substring(0, headers[i].length() - 1));
                    if (optionalValue.isPresent()) {
                        yield optionalValue.get();
                    }
                    optionalValue = psseTransformer.getWinding2Rates().headerToString(headers[i].substring(0, headers[i].length() - 1));
                    if (optionalValue.isPresent()) {
                        yield optionalValue.get();
                    }
                    optionalValue = psseTransformer.getWinding3().headerToString(headers[i].substring(0, headers[i].length() - 1));
                    if (optionalValue.isPresent()) {
                        yield optionalValue.get();
                    }
                    optionalValue = psseTransformer.getWinding3Rates().headerToString(headers[i].substring(0, headers[i].length() - 1));
                    if (optionalValue.isPresent()) {
                        yield optionalValue.get();
                    }
                    throw new PsseException("Unsupported header: " + headers[i]);
                }
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

    public int getJ() {
        return j;
    }

    public void setJ(int j) {
        this.j = j;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    public String getCkt() {
        return ckt;
    }

    public void setCkt(String ckt) {
        this.ckt = ckt;
    }

    public int getCw() {
        return cw;
    }

    public void setCw(int cw) {
        this.cw = cw;
    }

    public int getCz() {
        return cz;
    }

    public void setCz(int cz) {
        this.cz = cz;
    }

    public int getCm() {
        return cm;
    }

    public void setCm(int cm) {
        this.cm = cm;
    }

    public double getMag1() {
        return mag1;
    }

    public void setMag1(double mag1) {
        this.mag1 = mag1;
    }

    public double getMag2() {
        return mag2;
    }

    public void setMag2(double mag2) {
        this.mag2 = mag2;
    }

    public int getNmetr() {
        return nmetr;
    }

    public void setNmetr(int nmetr) {
        this.nmetr = nmetr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStat() {
        return stat;
    }

    public void setStat(int stat) {
        this.stat = stat;
    }

    public String getVecgrp() {
        checkVersion("vecgrp");
        return vecgrp;
    }

    public void setVecgrp(String vecgrp) {
        checkVersion("vecgrp");
        this.vecgrp = vecgrp;
    }

    public int getZcod() {
        checkVersion("zcod");
        return zcod;
    }

    public void setZcod(int zcod) {
        this.zcod = zcod;
    }

    public double getR12() {
        return impedances.r12;
    }

    public void setR12(double r12) {
        this.impedances.r12 = r12;
    }

    public double getX12() {
        return impedances.x12;
    }

    public void setX12(double x12) {
        this.impedances.x12 = x12;
    }

    public double getSbase12() {
        return impedances.sbase12;
    }

    public void setSbase12(double sbase12) {
        this.impedances.sbase12 = sbase12;
    }

    public double getR23() {
        return impedances.r23;
    }

    public void setR23(double r23) {
        this.impedances.r23 = r23;
    }

    public double getX23() {
        return impedances.x23;
    }

    public void setX23(double x23) {
        this.impedances.x23 = x23;
    }

    public double getSbase23() {
        return impedances.sbase23;
    }

    public void setSbase23(double sbase23) {
        this.impedances.sbase23 = sbase23;
    }

    public double getR31() {
        return impedances.r31;
    }

    public void setR31(double r31) {
        this.impedances.r31 = r31;
    }

    public double getX31() {
        return impedances.x31;
    }

    public void setX31(double x31) {
        this.impedances.x31 = x31;
    }

    public double getSbase31() {
        return impedances.sbase31;
    }

    public void setSbase31(double sbase31) {
        this.impedances.sbase31 = sbase31;
    }

    public double getVmstar() {
        return impedances.vmstar;
    }

    public void setVmstar(double vmstar) {
        this.impedances.vmstar = vmstar;
    }

    public double getAnstar() {
        return impedances.anstar;
    }

    public void setAnstar(double anstar) {
        this.impedances.anstar = anstar;
    }

    public PsseTransformerWinding getWinding1() {
        return winding1;
    }

    public PsseTransformerWinding getWinding2() {
        return winding2;
    }

    public PsseTransformerWinding getWinding3() {
        return winding3;
    }

    public PsseRates getWinding1Rates() {
        return winding1Rates;
    }

    public PsseRates getWinding2Rates() {
        return winding2Rates;
    }

    public PsseRates getWinding3Rates() {
        return winding3Rates;
    }

    public PsseOwnership getOwnership() {
        return ownership;
    }

    public void setWinding1(PsseTransformerWinding winding1, PsseRates winding1Rates) {
        this.winding1 = winding1;
        this.winding1Rates = winding1Rates;
    }

    public void setWinding2(PsseTransformerWinding winding2, PsseRates winding2Rates) {
        this.winding2 = winding2;
        this.winding2Rates = winding2Rates;
    }

    public void setWinding3(PsseTransformerWinding winding3, PsseRates winding3Rates) {
        this.winding3 = winding3;
        this.winding3Rates = winding3Rates;
    }

    public void setOwnership(PsseOwnership ownership) {
        this.ownership = ownership;
    }

    public TransformerImpedances getImpedances() {
        return impedances;
    }

    public void setImpedances(TransformerImpedances impedances) {
        this.impedances = impedances;
    }

    public PsseTransformer copy() {
        PsseTransformer copy = new PsseTransformer();
        copy.i = this.i;
        copy.j = this.j;
        copy.k = this.k;
        copy.ckt = this.ckt;
        copy.cw = this.cw;
        copy.cz = this.cz;
        copy.cm = this.cm;
        copy.mag1 = this.mag1;
        copy.mag2 = this.mag2;
        copy.nmetr = this.nmetr;
        copy.name = this.name;
        copy.stat = this.stat;
        copy.ownership = this.ownership.copy();
        copy.vecgrp = this.vecgrp;
        copy.zcod = this.zcod;
        copy.impedances = this.impedances.copy();
        copy.winding1 = this.winding1.copy();
        copy.winding1Rates = this.winding1Rates.copy();
        copy.winding2 = this.winding2.copy();
        copy.winding2Rates = this.winding2Rates.copy();
        copy.winding3 = this.winding3.copy();
        copy.winding3Rates = this.winding3Rates.copy();
        return copy;
    }

    public static class TransformerImpedances {

        private double r12 = 0;
        private double x12;
        private double sbase12 = Double.NaN;
        private double r23 = 0;
        private double x23 = Double.NaN;
        private double sbase23 = Double.NaN;
        private double r31 = 0;
        private double x31 = Double.NaN;
        private double sbase31 = Double.NaN;
        private double vmstar = 1;
        private double anstar = 0;

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
    }
}
