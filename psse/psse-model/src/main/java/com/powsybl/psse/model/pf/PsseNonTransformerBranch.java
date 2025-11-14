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

import java.util.Objects;
import java.util.Optional;

import static com.powsybl.psse.model.io.Util.parseDoubleFromRecord;
import static com.powsybl.psse.model.io.Util.parseIntFromRecord;
import static com.powsybl.psse.model.io.Util.parseStringFromRecord;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class PsseNonTransformerBranch extends PsseVersioned {

    @Override
    public void setModel(PssePowerFlowModel model) {
        super.setModel(model);
        ownership.setModel(model);
        rates.setModel(model);
    }

    private int i;
    private int j;
    private String ckt;
    private double r = 0.0;
    private double x;
    private double b = 0;
    private PsseRates rates;
    private double gi = 0;
    private double bi = 0;
    private double gj = 0;
    private double bj = 0;
    private int st = 1;
    private int met = 1;
    private double len = 0;
    private PsseOwnership ownership;

    @Revision(since = 35)
    private String name = " ";

    public static PsseNonTransformerBranch fromRecord(CsvRecord rec, PsseVersion version, String[] headers) {
        PsseNonTransformerBranch psseNonTransformerBranch = new PsseNonTransformerBranch();
        psseNonTransformerBranch.setI(parseIntFromRecord(rec, headers, "i", "ibus"));
        psseNonTransformerBranch.setJ(parseIntFromRecord(rec, headers, "j", "jbus"));
        psseNonTransformerBranch.setCkt(parseStringFromRecord(rec, "1", headers, "ckt"));
        psseNonTransformerBranch.setR(parseDoubleFromRecord(rec, 0.0, headers, "r", "rpu"));
        psseNonTransformerBranch.setX(parseDoubleFromRecord(rec, headers, "x", "xpu"));
        psseNonTransformerBranch.setB(parseDoubleFromRecord(rec, 0d, headers, "b", "bpu"));
        psseNonTransformerBranch.setRates(PsseRates.fromRecord(rec, version, headers));
        psseNonTransformerBranch.setGi(parseDoubleFromRecord(rec, 0d, headers, "gi"));
        psseNonTransformerBranch.setBi(parseDoubleFromRecord(rec, 0d, headers, "bi"));
        psseNonTransformerBranch.setGj(parseDoubleFromRecord(rec, 0d, headers, "gj"));
        psseNonTransformerBranch.setBj(parseDoubleFromRecord(rec, 0d, headers, "bj"));
        psseNonTransformerBranch.setSt(parseIntFromRecord(rec, 1, headers, "st", "stat"));
        psseNonTransformerBranch.setMet(parseIntFromRecord(rec, 1, headers, "met"));
        psseNonTransformerBranch.setLen(parseDoubleFromRecord(rec, 0d, headers, "len"));
        psseNonTransformerBranch.setOwnership(PsseOwnership.fromRecord(rec, headers));
        if (version.getMajorNumber() >= 35) {
            psseNonTransformerBranch.setName(parseStringFromRecord(rec, " ", headers, "name"));
        }
        return psseNonTransformerBranch;
    }

    public static String[] toRecord(PsseNonTransformerBranch psseNonTransformerBranch, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            row[i] = switch (headers[i]) {
                case "i", "ibus" -> String.valueOf(psseNonTransformerBranch.getI());
                case "j", "jbus" -> String.valueOf(psseNonTransformerBranch.getJ());
                case "ckt" -> psseNonTransformerBranch.getCkt();
                case "r", "rpu" -> String.valueOf(psseNonTransformerBranch.getR());
                case "x", "xpu" -> String.valueOf(psseNonTransformerBranch.getX());
                case "b", "bpu" -> String.valueOf(psseNonTransformerBranch.getB());
                case "ratea", "rata" -> String.valueOf(psseNonTransformerBranch.getRates().getRatea());
                case "rateb", "ratb" -> String.valueOf(psseNonTransformerBranch.getRates().getRateb());
                case "ratec", "ratc" -> String.valueOf(psseNonTransformerBranch.getRates().getRatec());
                case "rate1", "wdgrate1" -> String.valueOf(psseNonTransformerBranch.getRates().getRate1());
                case "rate2", "wdgrate2" -> String.valueOf(psseNonTransformerBranch.getRates().getRate2());
                case "rate3", "wdgrate3" -> String.valueOf(psseNonTransformerBranch.getRates().getRate3());
                case "rate4", "wdgrate4" -> String.valueOf(psseNonTransformerBranch.getRates().getRate4());
                case "rate5", "wdgrate5" -> String.valueOf(psseNonTransformerBranch.getRates().getRate5());
                case "rate6", "wdgrate6" -> String.valueOf(psseNonTransformerBranch.getRates().getRate6());
                case "rate7", "wdgrate7" -> String.valueOf(psseNonTransformerBranch.getRates().getRate7());
                case "rate8", "wdgrate8" -> String.valueOf(psseNonTransformerBranch.getRates().getRate8());
                case "rate9", "wdgrate9" -> String.valueOf(psseNonTransformerBranch.getRates().getRate9());
                case "rate10", "wdgrate10" -> String.valueOf(psseNonTransformerBranch.getRates().getRate10());
                case "rate11", "wdgrate11" -> String.valueOf(psseNonTransformerBranch.getRates().getRate11());
                case "rate12", "wdgrate12" -> String.valueOf(psseNonTransformerBranch.getRates().getRate12());
                case "gi" -> String.valueOf(psseNonTransformerBranch.getGi());
                case "bi" -> String.valueOf(psseNonTransformerBranch.getBi());
                case "gj" -> String.valueOf(psseNonTransformerBranch.getGj());
                case "bj" -> String.valueOf(psseNonTransformerBranch.getBj());
                case "st", "stat" -> String.valueOf(psseNonTransformerBranch.getSt());
                case "met" -> String.valueOf(psseNonTransformerBranch.getMet());
                case "len" -> String.valueOf(psseNonTransformerBranch.getLen());
                case "name" -> psseNonTransformerBranch.getName();
                default -> {
                    Optional<String> optionalValue = psseNonTransformerBranch.getOwnership().headerToString(headers[i]);
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

    public String getCkt() {
        return ckt;
    }

    public void setCkt(String ckt) {
        this.ckt = ckt;
    }

    public double getR() {
        return r;
    }

    public void setR(double r) {
        this.r = r;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getB() {
        return b;
    }

    public void setB(double b) {
        this.b = b;
    }

    public double getGi() {
        return gi;
    }

    public void setGi(double gi) {
        this.gi = gi;
    }

    public double getBi() {
        return bi;
    }

    public void setBi(double bi) {
        this.bi = bi;
    }

    public double getGj() {
        return gj;
    }

    public void setGj(double gj) {
        this.gj = gj;
    }

    public double getBj() {
        return bj;
    }

    public void setBj(double bj) {
        this.bj = bj;
    }

    public int getSt() {
        return st;
    }

    public void setSt(int st) {
        this.st = st;
    }

    public int getMet() {
        return met;
    }

    public void setMet(int met) {
        this.met = met;
    }

    public double getLen() {
        return len;
    }

    public void setLen(double len) {
        this.len = len;
    }

    public String getName() {
        checkVersion("name");
        return name;
    }

    public void setName(String name) {
        checkVersion("name");
        this.name = Objects.requireNonNull(name);
    }

    public PsseOwnership getOwnership() {
        return ownership;
    }

    public void setOwnership(PsseOwnership ownership) {
        this.ownership = ownership;
    }

    public PsseRates getRates() {
        return rates;
    }

    public void setRates(PsseRates rates) {
        this.rates = rates;
    }

    public PsseNonTransformerBranch copy() {
        PsseNonTransformerBranch copy = new PsseNonTransformerBranch();
        copy.i = this.i;
        copy.j = this.j;
        copy.ckt = this.ckt;
        copy.r = this.r;
        copy.x = this.x;
        copy.b = this.b;
        copy.rates = this.rates.copy();
        copy.gi = this.gi;
        copy.bi = this.bi;
        copy.gj = this.gj;
        copy.bj = this.bj;
        copy.st = this.st;
        copy.met = this.met;
        copy.len = this.len;
        copy.ownership = this.ownership.copy();
        copy.name = this.name;
        return copy;
    }
}
