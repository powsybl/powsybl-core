/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.config;

import com.powsybl.commons.PowsyblException;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class ConfigurationException extends PowsyblException {

    public ConfigurationException(String msg) {
        super(msg);
    }

    public ConfigurationException(Throwable throwable) {
        super(throwable);
    }
}
