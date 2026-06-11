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
import java.util.Set;

import static com.powsybl.psse.model.io.Util.addField;
import static com.powsybl.psse.model.io.Util.createNewField;
import static com.powsybl.psse.model.io.Util.defaultDoubleFor;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.*;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_WDGRATE6;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_WDGRATE7;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_WDGRATE8;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_WDGRATE9;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PsseRates extends PsseVersioned {

    private static final Map<String, PsseFieldDefinition<PsseRates, ?>> FIELDS = createFields();
    private static final String[] FIELD_NAMES_32_33 = {STR_RATA, STR_RATB, STR_RATC};
    private static final String[] FIELD_NAMES_35 = {STR_RATE1, STR_RATE2, STR_RATE3, STR_RATE4, STR_RATE5, STR_RATE6,
        STR_RATE7, STR_RATE8, STR_RATE9, STR_RATE10, STR_RATE11, STR_RATE12};

    @Revision(until = 33)
    private double ratea = defaultDoubleFor(STR_RATEA, FIELDS);

    @Revision(until = 33)
    private double rateb = defaultDoubleFor(STR_RATEB, FIELDS);

    @Revision(until = 33)
    private double ratec = defaultDoubleFor(STR_RATEC, FIELDS);

    @Revision(since = 35)
    private double rate1 = defaultDoubleFor(STR_RATE1, FIELDS);

    @Revision(since = 35)
    private double rate2 = defaultDoubleFor(STR_RATE2, FIELDS);

    @Revision(since = 35)
    private double rate3 = defaultDoubleFor(STR_RATE3, FIELDS);

    @Revision(since = 35)
    private double rate4 = defaultDoubleFor(STR_RATE4, FIELDS);

    @Revision(since = 35)
    private double rate5 = defaultDoubleFor(STR_RATE5, FIELDS);

    @Revision(since = 35)
    private double rate6 = defaultDoubleFor(STR_RATE6, FIELDS);

    @Revision(since = 35)
    private double rate7 = defaultDoubleFor(STR_RATE7, FIELDS);

    @Revision(since = 35)
    private double rate8 = defaultDoubleFor(STR_RATE8, FIELDS);

    @Revision(since = 35)
    private double rate9 = defaultDoubleFor(STR_RATE9, FIELDS);

    @Revision(since = 35)
    private double rate10 = defaultDoubleFor(STR_RATE10, FIELDS);

    @Revision(since = 35)
    private double rate11 = defaultDoubleFor(STR_RATE11, FIELDS);

    @Revision(since = 35)
    private double rate12 = defaultDoubleFor(STR_RATE12, FIELDS);

    public static String[] getFieldNames3233() {
        return FIELD_NAMES_32_33;
    }

    public static String[] getFieldNames35() {
        return FIELD_NAMES_35;
    }

    public static PsseRates fromRecord(CsvRecord rec, String[] headers) {
        return fromRecord(rec, headers, "");
    }

    public static PsseRates fromRecord(CsvRecord rec, String[] headers, String headerSuffix) {
        return Util.fromRecord(rec.getFields(), headers, FIELDS, PsseRates::new, headerSuffix);
    }

    public static void toRecord(PsseRates psseRates, String[] headers, String[] row, Set<String> unexpectedHeaders,
                                String headerSuffix) {
        Util.toRecord(psseRates, headers, FIELDS, row, unexpectedHeaders, headerSuffix);
    }

    public static void toRecord(PsseRates psseRates, String[] headers, String[] row, Set<String> unexpectedHeaders) {
        toRecord(psseRates, headers, row, unexpectedHeaders, "");
    }

    public static String[] toRecord(PsseRates psseRates, String[] headers) {
        return Util.toRecord(psseRates, headers, FIELDS);
    }

    private static Map<String, PsseFieldDefinition<PsseRates, ?>> createFields() {
        Map<String, PsseFieldDefinition<PsseRates, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_RATA, Double.class, PsseRates::getRatea, PsseRates::setRatea, 0d));
        addField(fields, createNewField(STR_RATB, Double.class, PsseRates::getRateb, PsseRates::setRateb, 0d));
        addField(fields, createNewField(STR_RATC, Double.class, PsseRates::getRatec, PsseRates::setRatec, 0d));
        addField(fields, createNewField(STR_RATEA, Double.class, PsseRates::getRatea, PsseRates::setRatea, 0d));
        addField(fields, createNewField(STR_RATEB, Double.class, PsseRates::getRateb, PsseRates::setRateb, 0d));
        addField(fields, createNewField(STR_RATEC, Double.class, PsseRates::getRatec, PsseRates::setRatec, 0d));
        addField(fields, createNewField(STR_RATE1, Double.class, PsseRates::getRate1, PsseRates::setRate1, 0d));
        addField(fields, createNewField(STR_RATE2, Double.class, PsseRates::getRate2, PsseRates::setRate2, 0d));
        addField(fields, createNewField(STR_RATE3, Double.class, PsseRates::getRate3, PsseRates::setRate3, 0d));
        addField(fields, createNewField(STR_RATE4, Double.class, PsseRates::getRate4, PsseRates::setRate4, 0d));
        addField(fields, createNewField(STR_RATE5, Double.class, PsseRates::getRate5, PsseRates::setRate5, 0d));
        addField(fields, createNewField(STR_RATE6, Double.class, PsseRates::getRate6, PsseRates::setRate6, 0d));
        addField(fields, createNewField(STR_RATE7, Double.class, PsseRates::getRate7, PsseRates::setRate7, 0d));
        addField(fields, createNewField(STR_RATE8, Double.class, PsseRates::getRate8, PsseRates::setRate8, 0d));
        addField(fields, createNewField(STR_RATE9, Double.class, PsseRates::getRate9, PsseRates::setRate9, 0d));
        addField(fields, createNewField(STR_RATE10, Double.class, PsseRates::getRate10, PsseRates::setRate10, 0d));
        addField(fields, createNewField(STR_RATE11, Double.class, PsseRates::getRate11, PsseRates::setRate11, 0d));
        addField(fields, createNewField(STR_RATE12, Double.class, PsseRates::getRate12, PsseRates::setRate12, 0d));
        addField(fields, createNewField(STR_WDGRATE1, Double.class, PsseRates::getRate1, PsseRates::setRate1, 0d, (header, suffix) -> STR_WDG + suffix + STR_RATE1));
        addField(fields, createNewField(STR_WDGRATE2, Double.class, PsseRates::getRate2, PsseRates::setRate2, 0d, (header, suffix) -> STR_WDG + suffix + STR_RATE2));
        addField(fields, createNewField(STR_WDGRATE3, Double.class, PsseRates::getRate3, PsseRates::setRate3, 0d, (header, suffix) -> STR_WDG + suffix + STR_RATE3));
        addField(fields, createNewField(STR_WDGRATE4, Double.class, PsseRates::getRate4, PsseRates::setRate4, 0d, (header, suffix) -> STR_WDG + suffix + STR_RATE4));
        addField(fields, createNewField(STR_WDGRATE5, Double.class, PsseRates::getRate5, PsseRates::setRate5, 0d, (header, suffix) -> STR_WDG + suffix + STR_RATE5));
        addField(fields, createNewField(STR_WDGRATE6, Double.class, PsseRates::getRate6, PsseRates::setRate6, 0d, (header, suffix) -> STR_WDG + suffix + STR_RATE6));
        addField(fields, createNewField(STR_WDGRATE7, Double.class, PsseRates::getRate7, PsseRates::setRate7, 0d, (header, suffix) -> STR_WDG + suffix + STR_RATE7));
        addField(fields, createNewField(STR_WDGRATE8, Double.class, PsseRates::getRate8, PsseRates::setRate8, 0d, (header, suffix) -> STR_WDG + suffix + STR_RATE8));
        addField(fields, createNewField(STR_WDGRATE9, Double.class, PsseRates::getRate9, PsseRates::setRate9, 0d, (header, suffix) -> STR_WDG + suffix + STR_RATE9));
        addField(fields, createNewField(STR_WDGRATE10, Double.class, PsseRates::getRate10, PsseRates::setRate10, 0d, (header, suffix) -> STR_WDG + suffix + STR_RATE10));
        addField(fields, createNewField(STR_WDGRATE11, Double.class, PsseRates::getRate11, PsseRates::setRate11, 0d, (header, suffix) -> STR_WDG + suffix + STR_RATE11));
        addField(fields, createNewField(STR_WDGRATE12, Double.class, PsseRates::getRate12, PsseRates::setRate12, 0d, (header, suffix) -> STR_WDG + suffix + STR_RATE12));

        return fields;
    }

    public double getRatea() {
        checkVersion(STR_RATEA);
        return ratea;
    }

    public void setRatea(double ratea) {
        checkVersion(STR_RATEA);
        this.ratea = ratea;
    }

    public double getRateb() {
        checkVersion(STR_RATEB);
        return rateb;
    }

    public void setRateb(double rateb) {
        checkVersion(STR_RATEB);
        this.rateb = rateb;
    }

    public double getRatec() {
        checkVersion(STR_RATEC);
        return ratec;
    }

    public void setRatec(double ratec) {
        checkVersion(STR_RATEC);
        this.ratec = ratec;
    }

    public double getRate1() {
        checkVersion(STR_RATE1);
        return rate1;
    }

    public void setRate1(double rate1) {
        checkVersion(STR_RATE1);
        this.rate1 = rate1;
    }

    public double getRate2() {
        checkVersion(STR_RATE2);
        return rate2;
    }

    public void setRate2(double rate2) {
        checkVersion(STR_RATE2);
        this.rate2 = rate2;
    }

    public double getRate3() {
        checkVersion(STR_RATE3);
        return rate3;
    }

    public void setRate3(double rate3) {
        checkVersion(STR_RATE3);
        this.rate3 = rate3;
    }

    public double getRate4() {
        checkVersion(STR_RATE4);
        return rate4;
    }

    public void setRate4(double rate4) {
        checkVersion(STR_RATE4);
        this.rate4 = rate4;
    }

    public double getRate5() {
        checkVersion(STR_RATE5);
        return rate5;
    }

    public void setRate5(double rate5) {
        checkVersion(STR_RATE5);
        this.rate5 = rate5;
    }

    public double getRate6() {
        checkVersion(STR_RATE6);
        return rate6;
    }

    public void setRate6(double rate6) {
        checkVersion(STR_RATE6);
        this.rate6 = rate6;
    }

    public double getRate7() {
        checkVersion(STR_RATE7);
        return rate7;
    }

    public void setRate7(double rate7) {
        checkVersion(STR_RATE7);
        this.rate7 = rate7;
    }

    public double getRate8() {
        checkVersion(STR_RATE8);
        return rate8;
    }

    public void setRate8(double rate8) {
        checkVersion(STR_RATE8);
        this.rate8 = rate8;
    }

    public double getRate9() {
        checkVersion(STR_RATE9);
        return rate9;
    }

    public void setRate9(double rate9) {
        checkVersion(STR_RATE9);
        this.rate9 = rate9;
    }

    public double getRate10() {
        checkVersion(STR_RATE10);
        return rate10;
    }

    public void setRate10(double rate10) {
        checkVersion(STR_RATE10);
        this.rate10 = rate10;
    }

    public double getRate11() {
        checkVersion(STR_RATE11);
        return rate11;
    }

    public void setRate11(double rate11) {
        checkVersion(STR_RATE11);
        this.rate11 = rate11;
    }

    public double getRate12() {
        checkVersion(STR_RATE12);
        return rate12;
    }

    public void setRate12(double rate12) {
        checkVersion(STR_RATE12);
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
