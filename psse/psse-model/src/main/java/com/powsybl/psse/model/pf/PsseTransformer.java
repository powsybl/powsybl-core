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
import com.powsybl.psse.model.pf.internal.TransformerImpedances;
import de.siegmar.fastcsv.reader.CsvRecord;

import java.util.Optional;

import static com.powsybl.psse.model.io.Util.parseDoubleFromRecord;
import static com.powsybl.psse.model.io.Util.parseIntFromRecord;
import static com.powsybl.psse.model.io.Util.parseStringFromRecord;

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

    @Revision(since = 33)
    private String vecgrp = "            ";

    @Revision(since = 35)
    private int zcod = 0;

    private TransformerImpedances impedances = new TransformerImpedances();
    private PsseTransformerWinding winding1 = new PsseTransformerWinding();
    private PsseRates winding1Rates = new PsseRates();
    private PsseTransformerWinding winding2 = new PsseTransformerWinding();
    private PsseRates winding2Rates = new PsseRates();
    private PsseTransformerWinding winding3 = new PsseTransformerWinding();
    private PsseRates winding3Rates = new PsseRates();

    public static PsseTransformer fromRecord(CsvRecord rec, PsseVersion version, String[] headers) {
        PsseTransformer psseTransformer = new PsseTransformer();
        psseTransformer.setI(parseIntFromRecord(rec, headers, "i", "ibus"));
        psseTransformer.setJ(parseIntFromRecord(rec, headers, "j", "jbus"));
        psseTransformer.setK(parseIntFromRecord(rec, 0, headers, "k", "kbus"));
        psseTransformer.setCkt(parseStringFromRecord(rec, "1", headers, "ckt"));
        psseTransformer.setCw(parseIntFromRecord(rec, 1, headers, "cw"));
        psseTransformer.setCz(parseIntFromRecord(rec, 1, headers, "cz"));
        psseTransformer.setCm(parseIntFromRecord(rec, 1, headers, "cm"));
        psseTransformer.setMag1(parseDoubleFromRecord(rec, 0d, headers, "mag1"));
        psseTransformer.setMag2(parseDoubleFromRecord(rec, 0d, headers, "mag2"));
        psseTransformer.setNmetr(parseIntFromRecord(rec, 2, headers, "nmetr", "nmet"));
        psseTransformer.setName(parseStringFromRecord(rec, "            ", headers, "name"));
        psseTransformer.setStat(parseIntFromRecord(rec, 1, headers, "stat"));
        psseTransformer.setOwnership(PsseOwnership.fromRecord(rec, version, headers));
        if (version.getMajorNumber() >= 33) {
            psseTransformer.setVecgrp(parseStringFromRecord(rec, "            ", headers, "vecgrp"));
        }
        if (version.getMajorNumber() >= 35) {
            psseTransformer.setZcod(parseIntFromRecord(rec, 0, headers, "zcod"));
        }
        psseTransformer.setImpedances(TransformerImpedances.fromRecord(rec, version, headers));
        psseTransformer.setWinding1(PsseTransformerWinding.fromRecord(rec, version, headers, "1"), PsseRates.fromRecord(rec, version, headers, "1"));
        psseTransformer.setWinding2(PsseTransformerWinding.fromRecord(rec, version, headers, "2"), PsseRates.fromRecord(rec, version, headers, "2"));
        psseTransformer.setWinding3(PsseTransformerWinding.fromRecord(rec, version, headers, "3"), PsseRates.fromRecord(rec, version, headers, "3"));
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
                default -> {
                    Optional<String> optionalValue = psseTransformer.getOwnership().headerToString(headers[i]);
                    if (optionalValue.isPresent()) {
                        yield optionalValue.get();
                    }
                    optionalValue = psseTransformer.getImpedances().headerToString(headers[i]);
                    if (optionalValue.isPresent()) {
                        yield optionalValue.get();
                    }
                    optionalValue = windingHeaderToString(psseTransformer, headers[i]);
                    if (optionalValue.isPresent()) {
                        yield optionalValue.get();
                    }
                    optionalValue = windingRatesHeaderToString(psseTransformer, headers[i]);
                    if (optionalValue.isPresent()) {
                        yield optionalValue.get();
                    }
                    throw new PsseException("Unsupported header: " + headers[i]);
                }
            };
        }
        return row;
    }

    private static Optional<String> windingHeaderToString(PsseTransformer psseTransformer, String header) {
        String shortHeader = header.substring(0, header.length() - 1);
        return switch (header.substring(header.length() - 1)) {
            case "1" -> psseTransformer.getWinding1().headerToString(shortHeader);
            case "2" -> psseTransformer.getWinding2().headerToString(shortHeader);
            case "3" -> psseTransformer.getWinding3().headerToString(shortHeader);
            default -> Optional.empty();
        };
    }

    private static Optional<String> windingRatesHeaderToString(PsseTransformer psseTransformer, String header) {
        String shortHeader = header.substring(0, 3) + header.substring(4);
        return switch (header.substring(3, 4)) {
            case "1" -> psseTransformer.getWinding1Rates().headerToString(shortHeader);
            case "2" -> psseTransformer.getWinding2Rates().headerToString(shortHeader);
            case "3" -> psseTransformer.getWinding3Rates().headerToString(shortHeader);
            default -> Optional.empty();
        };
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
        return impedances.getR12();
    }

    public void setR12(double r12) {
        this.impedances.setR12(r12);
    }

    public double getX12() {
        return impedances.getX12();
    }

    public void setX12(double x12) {
        this.impedances.setX12(x12);
    }

    public double getSbase12() {
        return impedances.getSbase12();
    }

    public void setSbase12(double sbase12) {
        this.impedances.setSbase12(sbase12);
    }

    public double getR23() {
        return impedances.getR23();
    }

    public void setR23(double r23) {
        this.impedances.setR23(r23);
    }

    public double getX23() {
        return impedances.getX23();
    }

    public void setX23(double x23) {
        this.impedances.setX23(x23);
    }

    public double getSbase23() {
        return impedances.getSbase23();
    }

    public void setSbase23(double sbase23) {
        this.impedances.setSbase23(sbase23);
    }

    public double getR31() {
        return impedances.getR31();
    }

    public void setR31(double r31) {
        this.impedances.setR31(r31);
    }

    public double getX31() {
        return impedances.getX31();
    }

    public void setX31(double x31) {
        this.impedances.setX31(x31);
    }

    public double getSbase31() {
        return impedances.getSbase31();
    }

    public void setSbase31(double sbase31) {
        this.impedances.setSbase31(sbase31);
    }

    public double getVmstar() {
        return impedances.getVmstar();
    }

    public void setVmstar(double vmstar) {
        this.impedances.setVmstar(vmstar);
    }

    public double getAnstar() {
        return impedances.getAnstar();
    }

    public void setAnstar(double anstar) {
        this.impedances.setAnstar(anstar);
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
}
