/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.util.PropertiesBufferHolder;

import java.util.Properties;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public abstract class AbstractPropertiesHolder extends PropertiesBufferHolder {

    /**
     * <p>Returns the properties.</p>
     * <p>To limit memory usage, it is recommended to use {@link #hasProperty()} before calling this method.</p>
     * @return the properties
     */
    @Override
    public Properties getProperties() {
        return super.getProperties();
    }
}
