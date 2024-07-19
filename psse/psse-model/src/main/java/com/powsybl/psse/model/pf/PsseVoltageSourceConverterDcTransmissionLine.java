/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf;

import java.lang.reflect.Field;

import com.powsybl.psse.model.PsseVersioned;
import com.univocity.parsers.annotations.HeaderTransformer;
import com.univocity.parsers.annotations.Nested;
import com.univocity.parsers.annotations.Parsed;

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

    @Parsed
    private String name;

    @Parsed
    private int mdc = 1;

    @Parsed
    private double rdc;

    @Nested
    private PsseOwnership ownership;

    @Nested(headerTransformer = ConverterHeaderTransformer.class, args = "1")
    private PsseVoltageSourceConverter converter1;

    @Nested(headerTransformer = ConverterHeaderTransformer.class, args = "2")
    private PsseVoltageSourceConverter converter2;

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

    public static class ConverterHeaderTransformer extends HeaderTransformer {
        private final String converterChar;

        public ConverterHeaderTransformer(String... args) {
            converterChar = args[0];
        }

        @Override
        public String transformName(Field field, String name) {
            return name + converterChar;
        }
    }
}
