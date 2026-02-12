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

import java.util.Optional;

import static com.powsybl.psse.model.io.Util.parseDoubleFromRecord;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PsseRates extends PsseVersioned {

    private static final String RATE_A = "ratea";
    private static final String RATE_B = "rateb";
    private static final String RATE_C = "ratec";
    private static final String RATE_1 = "rate1";
    private static final String RATE_2 = "rate2";
    private static final String RATE_3 = "rate3";
    private static final String RATE_4 = "rate4";
    private static final String RATE_5 = "rate5";
    private static final String RATE_6 = "rate6";
    private static final String RATE_7 = "rate7";
    private static final String RATE_8 = "rate8";
    private static final String RATE_9 = "rate9";
    private static final String RATE_10 = "rate10";
    private static final String RATE_11 = "rate11";
    private static final String RATE_12 = "rate12";

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

    public static PsseRates fromRecord(CsvRecord rec, PsseVersion version, String[] headers) {
        return fromRecord(rec, version, headers, "");
    }

    public static PsseRates fromRecord(CsvRecord rec, PsseVersion version, String[] headers, String headerSuffix) {
        PsseRates psseRates = new PsseRates();
        if (version.getMajorNumber() <= 33) {
            psseRates.setRatea(parseDoubleFromRecord(rec, 0d, headers, RATE_A + headerSuffix, "rata" + headerSuffix));
            psseRates.setRateb(parseDoubleFromRecord(rec, 0d, headers, RATE_B + headerSuffix, "ratb" + headerSuffix));
            psseRates.setRatec(parseDoubleFromRecord(rec, 0d, headers, RATE_C + headerSuffix, "ratc" + headerSuffix));
        }
        if (version.getMajorNumber() >= 35) {
            psseRates.setRate1(parseDoubleFromRecord(rec, 0d, headers, RATE_1 + headerSuffix, "wdg" + headerSuffix + RATE_1));
            psseRates.setRate2(parseDoubleFromRecord(rec, 0d, headers, RATE_2 + headerSuffix, "wdg" + headerSuffix + RATE_2));
            psseRates.setRate3(parseDoubleFromRecord(rec, 0d, headers, RATE_3 + headerSuffix, "wdg" + headerSuffix + RATE_3));
            psseRates.setRate4(parseDoubleFromRecord(rec, 0d, headers, RATE_4 + headerSuffix, "wdg" + headerSuffix + RATE_4));
            psseRates.setRate5(parseDoubleFromRecord(rec, 0d, headers, RATE_5 + headerSuffix, "wdg" + headerSuffix + RATE_5));
            psseRates.setRate6(parseDoubleFromRecord(rec, 0d, headers, RATE_6 + headerSuffix, "wdg" + headerSuffix + RATE_6));
            psseRates.setRate7(parseDoubleFromRecord(rec, 0d, headers, RATE_7 + headerSuffix, "wdg" + headerSuffix + RATE_7));
            psseRates.setRate8(parseDoubleFromRecord(rec, 0d, headers, RATE_8 + headerSuffix, "wdg" + headerSuffix + RATE_8));
            psseRates.setRate9(parseDoubleFromRecord(rec, 0d, headers, RATE_9 + headerSuffix, "wdg" + headerSuffix + RATE_9));
            psseRates.setRate10(parseDoubleFromRecord(rec, 0d, headers, RATE_10 + headerSuffix, "wdg" + headerSuffix + RATE_10));
            psseRates.setRate11(parseDoubleFromRecord(rec, 0d, headers, RATE_11 + headerSuffix, "wdg" + headerSuffix + RATE_11));
            psseRates.setRate12(parseDoubleFromRecord(rec, 0d, headers, RATE_12 + headerSuffix, "wdg" + headerSuffix + RATE_12));
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
            case RATE_A, "rata" -> Optional.of(String.valueOf(getRatea()));
            case RATE_B, "ratb" -> Optional.of(String.valueOf(getRateb()));
            case RATE_C, "ratc" -> Optional.of(String.valueOf(getRatec()));
            case RATE_1, "wdgrate1" -> Optional.of(String.valueOf(getRate1()));
            case RATE_2, "wdgrate2" -> Optional.of(String.valueOf(getRate2()));
            case RATE_3, "wdgrate3" -> Optional.of(String.valueOf(getRate3()));
            case RATE_4, "wdgrate4" -> Optional.of(String.valueOf(getRate4()));
            case RATE_5, "wdgrate5" -> Optional.of(String.valueOf(getRate5()));
            case RATE_6, "wdgrate6" -> Optional.of(String.valueOf(getRate6()));
            case RATE_7, "wdgrate7" -> Optional.of(String.valueOf(getRate7()));
            case RATE_8, "wdgrate8" -> Optional.of(String.valueOf(getRate8()));
            case RATE_9, "wdgrate9" -> Optional.of(String.valueOf(getRate9()));
            case RATE_10, "wdgrate10" -> Optional.of(String.valueOf(getRate10()));
            case RATE_11, "wdgrate11" -> Optional.of(String.valueOf(getRate11()));
            case RATE_12, "wdgrate12" -> Optional.of(String.valueOf(getRate12()));
            default -> Optional.empty();
        };
    }

    public double getRatea() {
        checkVersion(RATE_A);
        return ratea;
    }

    public void setRatea(double ratea) {
        checkVersion(RATE_A);
        this.ratea = ratea;
    }

    public double getRateb() {
        checkVersion(RATE_B);
        return rateb;
    }

    public void setRateb(double rateb) {
        checkVersion(RATE_B);
        this.rateb = rateb;
    }

    public double getRatec() {
        checkVersion(RATE_C);
        return ratec;
    }

    public void setRatec(double ratec) {
        checkVersion(RATE_C);
        this.ratec = ratec;
    }

    public double getRate1() {
        checkVersion(RATE_1);
        return rate1;
    }

    public void setRate1(double rate1) {
        checkVersion(RATE_1);
        this.rate1 = rate1;
    }

    public double getRate2() {
        checkVersion(RATE_2);
        return rate2;
    }

    public void setRate2(double rate2) {
        checkVersion(RATE_2);
        this.rate2 = rate2;
    }

    public double getRate3() {
        checkVersion(RATE_3);
        return rate3;
    }

    public void setRate3(double rate3) {
        checkVersion(RATE_3);
        this.rate3 = rate3;
    }

    public double getRate4() {
        checkVersion(RATE_4);
        return rate4;
    }

    public void setRate4(double rate4) {
        checkVersion(RATE_4);
        this.rate4 = rate4;
    }

    public double getRate5() {
        checkVersion(RATE_5);
        return rate5;
    }

    public void setRate5(double rate5) {
        checkVersion(RATE_5);
        this.rate5 = rate5;
    }

    public double getRate6() {
        checkVersion(RATE_6);
        return rate6;
    }

    public void setRate6(double rate6) {
        checkVersion(RATE_6);
        this.rate6 = rate6;
    }

    public double getRate7() {
        checkVersion(RATE_7);
        return rate7;
    }

    public void setRate7(double rate7) {
        checkVersion(RATE_7);
        this.rate7 = rate7;
    }

    public double getRate8() {
        checkVersion(RATE_8);
        return rate8;
    }

    public void setRate8(double rate8) {
        checkVersion(RATE_8);
        this.rate8 = rate8;
    }

    public double getRate9() {
        checkVersion(RATE_9);
        return rate9;
    }

    public void setRate9(double rate9) {
        checkVersion(RATE_9);
        this.rate9 = rate9;
    }

    public double getRate10() {
        checkVersion(RATE_10);
        return rate10;
    }

    public void setRate10(double rate10) {
        checkVersion(RATE_10);
        this.rate10 = rate10;
    }

    public double getRate11() {
        checkVersion(RATE_11);
        return rate11;
    }

    public void setRate11(double rate11) {
        checkVersion(RATE_11);
        this.rate11 = rate11;
    }

    public double getRate12() {
        checkVersion(RATE_12);
        return rate12;
    }

    public void setRate12(double rate12) {
        checkVersion(RATE_12);
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
