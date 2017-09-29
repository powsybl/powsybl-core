/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.commons.config;

import eu.itesla_project.commons.ITeslaException;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class ConfigurationException extends ITeslaException {

    public ConfigurationException(Throwable throwable) {
        super(throwable);
    }
}
