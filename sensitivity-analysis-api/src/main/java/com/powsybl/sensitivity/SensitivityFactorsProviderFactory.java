/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import java.io.InputStream;
import java.nio.file.Path;

/**
 * Sensitivity factors provider factory
 *
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 * @see SensitivityFactorsProvider
 */
public interface SensitivityFactorsProviderFactory {

    /**
     * Creates a new sensitivity factors provider object
     *
     * @param <T> Concrete type of sensitivity factors provider
     * @return the created sensitivity factors provider instance
     */
    <T extends SensitivityFactorsProvider> T create();

    /**
     * Creates a new sensitivity factors provider object
     *
     * @param <T> Concrete type of sensitivity factors provider
     * @param sensitivityFactorsFile Path of the sensitivity factors provider input file
     * @return the created sensitivity factors provider instance
     */
    <T extends SensitivityFactorsProvider> T create(Path sensitivityFactorsFile);

    /**
     * Creates a new sensitivity factors provider object
     *
     * @param <T> Concrete type of sensitivity factors provider
     * @param data data content of the sensitivity factors provider input file
     * @return the created sensitivity factors provider instance
     */
    <T extends SensitivityFactorsProvider> T create(InputStream data);
}
