/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.databind.Module;

import java.util.List;

/**
 * @author Sylvain Leclerc {@literal <sylvain.leclerc@rte-france.com>}
 */
public interface SecurityAnalysisJsonPlugin {

    /**
     * Provide third-party jackson modules that should be registered as dependencies of the
     * main security analysis module.
     *
     * @return A list of modules to be registered for use in security analysis related serialization.
     */
    List<Module> getJsonModules();
}
