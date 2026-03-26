/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.extensions.Measurement;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.extensions.util.MeasurementValidationUtil;

import java.util.*;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class MeasurementImpl implements Measurement {

    private final MeasurementsImpl<? extends Connectable<?>> measurements;
    private final String id;
    private final Measurement.Type type;
    private final Map<String, String> properties = new HashMap<>();
    private final ThreeSides side;

    private double value;
    private double standardDeviation;
    private boolean valid;

    MeasurementImpl(MeasurementsImpl<? extends Connectable<?>> measurements, String id, Measurement.Type type, Map<String, String> properties, double value, double standardDeviation, boolean valid, ThreeSides side) {
        this.measurements = Objects.requireNonNull(measurements);
        this.id = id;
        this.type = type;
        this.properties.putAll(properties);
        this.value = value;
        this.standardDeviation = standardDeviation;
        this.valid = valid;
        this.side = side;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Set<String> getPropertyNames() {
        return Collections.unmodifiableSet(properties.keySet());
    }

    @Override
    public String getProperty(String name) {
        return properties.get(name);
    }

    @Override
    public Measurement putProperty(String name, String property) {
        properties.put(Objects.requireNonNull(name), property);
        return this;
    }

    @Override
    public Measurement removeProperty(String name) {
        properties.remove(name);
        return this;
    }

    @Override
    public Measurement setValue(double value) {
        MeasurementValidationUtil.checkValue(value, valid);
        this.value = value;
        return this;
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    public Measurement setStandardDeviation(double standardDeviation) {
        this.standardDeviation = standardDeviation;
        return this;
    }

    @Override
    public double getStandardDeviation() {
        return standardDeviation;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public Measurement setValid(boolean valid) {
        MeasurementValidationUtil.checkValue(value, valid);
        this.valid = valid;
        return this;
    }

    @Override
    public Measurement setValueAndValidity(double value, boolean valid) {
        MeasurementValidationUtil.checkValue(value, valid);
        this.valid = valid;
        this.value = value;
        return this;
    }

    @Override
    public ThreeSides getSide() {
        return side;
    }

    @Override
    public void remove() {
        measurements.remove(this);
    }
}
