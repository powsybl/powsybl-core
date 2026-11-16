/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.config;

import java.util.Objects;

/**
*
* @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
*/
public class BaseVoltageConfig {

    private String name;
    private double minValue;
    private double maxValue;
    private String profile;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public double getMinValue() {
        return minValue;
    }

    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = Objects.requireNonNull(profile);
    }

}
