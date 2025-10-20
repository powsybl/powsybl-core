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
import de.siegmar.fastcsv.reader.NamedCsvRecord;

import java.util.Optional;

import static com.powsybl.psse.model.io.Util.getFieldFromMultiplePotentialHeaders;
import static com.powsybl.psse.model.io.Util.parseDoubleOrDefault;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PsseRates extends PsseVersioned {

    @Revision(until = 33)
    private double ratea = 0;

    @Revision(until = 33)
    private double rateb = 0;

    @Revision(until = 33)
    private double ratec = 0;

    @Revision(since = 35)
    private double rate1 = 0;

    @Revision(since = 35)
    private double rate2 = 0;

    @Revision(since = 35)
    private double rate3 = 0;

    @Revision(since = 35)
    private double rate4 = 0;

    @Revision(since = 35)
    private double rate5 = 0;

    @Revision(since = 35)
    private double rate6 = 0;

    @Revision(since = 35)
    private double rate7 = 0;

    @Revision(since = 35)
    private double rate8 = 0;

    @Revision(since = 35)
    private double rate9 = 0;

    @Revision(since = 35)
    private double rate10 = 0;

    @Revision(since = 35)
    private double rate11 = 0;

    @Revision(since = 35)
    private double rate12 = 0;

    public static PsseRates fromRecord(NamedCsvRecord rec, PsseVersion version) {
        return fromRecord(rec, version, "");
    }

    public static PsseRates fromRecord(NamedCsvRecord rec, PsseVersion version, String headerSuffix) {
        PsseRates psseRates = new PsseRates();
        if (version.getMajorNumber() <= 33) {
            psseRates.setRatea(Double.parseDouble(getFieldFromMultiplePotentialHeaders(rec, "ratea" + headerSuffix, "rata" + headerSuffix)));
            psseRates.setRateb(Double.parseDouble(getFieldFromMultiplePotentialHeaders(rec, "rateb" + headerSuffix, "ratb" + headerSuffix)));
            psseRates.setRatec(Double.parseDouble(getFieldFromMultiplePotentialHeaders(rec, "ratec" + headerSuffix, "ratc" + headerSuffix)));
        }
        if (version.getMajorNumber() >= 35) {
            psseRates.setRate1(parseDoubleOrDefault(getFieldFromMultiplePotentialHeaders(rec, "rate1" + headerSuffix, "wdgrate1" + headerSuffix), 0.0));
            psseRates.setRate2(parseDoubleOrDefault(getFieldFromMultiplePotentialHeaders(rec, "rate2" + headerSuffix, "wdgrate2" + headerSuffix), 0.0));
            psseRates.setRate3(parseDoubleOrDefault(getFieldFromMultiplePotentialHeaders(rec, "rate3" + headerSuffix, "wdgrate3" + headerSuffix), 0.0));
            psseRates.setRate4(parseDoubleOrDefault(getFieldFromMultiplePotentialHeaders(rec, "rate4" + headerSuffix, "wdgrate4" + headerSuffix), 0.0));
            psseRates.setRate5(parseDoubleOrDefault(getFieldFromMultiplePotentialHeaders(rec, "rate5" + headerSuffix, "wdgrate5" + headerSuffix), 0.0));
            psseRates.setRate6(parseDoubleOrDefault(getFieldFromMultiplePotentialHeaders(rec, "rate6" + headerSuffix, "wdgrate6" + headerSuffix), 0.0));
            psseRates.setRate7(parseDoubleOrDefault(getFieldFromMultiplePotentialHeaders(rec, "rate7" + headerSuffix, "wdgrate7" + headerSuffix), 0.0));
            psseRates.setRate8(parseDoubleOrDefault(getFieldFromMultiplePotentialHeaders(rec, "rate8" + headerSuffix, "wdgrate8" + headerSuffix), 0.0));
            psseRates.setRate9(parseDoubleOrDefault(getFieldFromMultiplePotentialHeaders(rec, "rate9" + headerSuffix, "wdgrate9" + headerSuffix), 0.0));
            psseRates.setRate10(parseDoubleOrDefault(getFieldFromMultiplePotentialHeaders(rec, "rate10" + headerSuffix, "wdgrate10" + headerSuffix), 0.0));
            psseRates.setRate11(parseDoubleOrDefault(getFieldFromMultiplePotentialHeaders(rec, "rate11" + headerSuffix, "wdgrate11" + headerSuffix), 0.0));
            psseRates.setRate12(parseDoubleOrDefault(getFieldFromMultiplePotentialHeaders(rec, "rate12" + headerSuffix, "wdgrate12" + headerSuffix), 0.0));
        }
        return psseRates;
    }

    public static String[] toRecord(PsseRates psseRates, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            Optional<String> optionalValue = psseRates.headerToString(headers[i]);
            if (optionalValue.isEmpty()) {
                throw new PsseException("Unsupported header: " + headers[i]);
            }
            row[i] = optionalValue.get();
        }
        return row;
    }

    public Optional<String> headerToString(String header) {
        return switch (header) {
            case "ratea", "rata" -> Optional.of(String.valueOf(getRatea()));
            case "rateb", "ratb" -> Optional.of(String.valueOf(getRateb()));
            case "ratec", "ratc" -> Optional.of(String.valueOf(getRatec()));
            case "rate1", "wdgrate1" -> Optional.of(String.valueOf(getRate1()));
            case "rate2", "wdgrate2" -> Optional.of(String.valueOf(getRate2()));
            case "rate3", "wdgrate3" -> Optional.of(String.valueOf(getRate3()));
            case "rate4", "wdgrate4" -> Optional.of(String.valueOf(getRate4()));
            case "rate5", "wdgrate5" -> Optional.of(String.valueOf(getRate5()));
            case "rate6", "wdgrate6" -> Optional.of(String.valueOf(getRate6()));
            case "rate7", "wdgrate7" -> Optional.of(String.valueOf(getRate7()));
            case "rate8", "wdgrate8" -> Optional.of(String.valueOf(getRate8()));
            case "rate9", "wdgrate9" -> Optional.of(String.valueOf(getRate9()));
            case "rate10", "wdgrate10" -> Optional.of(String.valueOf(getRate10()));
            case "rate11", "wdgrate11" -> Optional.of(String.valueOf(getRate11()));
            case "rate12", "wdgrate12" -> Optional.of(String.valueOf(getRate12()));
            default -> Optional.empty();
        };
    }

    public double getRatea() {
        checkVersion("ratea");
        return ratea;
    }

    public void setRatea(double ratea) {
        checkVersion("ratea");
        this.ratea = ratea;
    }

    public double getRateb() {
        checkVersion("rateb");
        return rateb;
    }

    public void setRateb(double rateb) {
        checkVersion("rateb");
        this.rateb = rateb;
    }

    public double getRatec() {
        checkVersion("ratec");
        return ratec;
    }

    public void setRatec(double ratec) {
        checkVersion("ratec");
        this.ratec = ratec;
    }

    public double getRate1() {
        checkVersion("rate1");
        return rate1;
    }

    public void setRate1(double rate1) {
        checkVersion("rate1");
        this.rate1 = rate1;
    }

    public double getRate2() {
        checkVersion("rate2");
        return rate2;
    }

    public void setRate2(double rate2) {
        checkVersion("rate2");
        this.rate2 = rate2;
    }

    public double getRate3() {
        checkVersion("rate3");
        return rate3;
    }

    public void setRate3(double rate3) {
        checkVersion("rate3");
        this.rate3 = rate3;
    }

    public double getRate4() {
        checkVersion("rate4");
        return rate4;
    }

    public void setRate4(double rate4) {
        checkVersion("rate4");
        this.rate4 = rate4;
    }

    public double getRate5() {
        checkVersion("rate5");
        return rate5;
    }

    public void setRate5(double rate5) {
        checkVersion("rate5");
        this.rate5 = rate5;
    }

    public double getRate6() {
        checkVersion("rate6");
        return rate6;
    }

    public void setRate6(double rate6) {
        checkVersion("rate6");
        this.rate6 = rate6;
    }

    public double getRate7() {
        checkVersion("rate7");
        return rate7;
    }

    public void setRate7(double rate7) {
        checkVersion("rate7");
        this.rate7 = rate7;
    }

    public double getRate8() {
        checkVersion("rate8");
        return rate8;
    }

    public void setRate8(double rate8) {
        checkVersion("rate8");
        this.rate8 = rate8;
    }

    public double getRate9() {
        checkVersion("rate9");
        return rate9;
    }

    public void setRate9(double rate9) {
        checkVersion("rate9");
        this.rate9 = rate9;
    }

    public double getRate10() {
        checkVersion("rate10");
        return rate10;
    }

    public void setRate10(double rate10) {
        checkVersion("rate10");
        this.rate10 = rate10;
    }

    public double getRate11() {
        checkVersion("rate11");
        return rate11;
    }

    public void setRate11(double rate11) {
        checkVersion("rate11");
        this.rate11 = rate11;
    }

    public double getRate12() {
        checkVersion("rate12");
        return rate12;
    }

    public void setRate12(double rate12) {
        checkVersion("rate12");
        this.rate12 = rate12;
    }

    public PsseRates copy() {
        PsseRates copy = new PsseRates();
        copy.ratea = this.ratea;
        copy.rateb = this.rateb;
        copy.ratec = this.ratec;
        copy.rate1 = this.rate1;
        copy.rate2 = this.rate2;
        copy.rate3 = this.rate3;
        copy.rate4 = this.rate4;
        copy.rate5 = this.rate5;
        copy.rate6 = this.rate6;
        copy.rate7 = this.rate7;
        copy.rate8 = this.rate8;
        copy.rate9 = this.rate9;
        copy.rate10 = this.rate10;
        copy.rate11 = this.rate11;
        copy.rate12 = this.rate12;
        return copy;
    }
}
