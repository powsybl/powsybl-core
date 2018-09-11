/**
 * Copyright (c) 2018, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.extensions;

import com.powsybl.commons.config.PlatformConfig;

/**
 * Loads an extension from platform configuration.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public interface ExtensionConfigLoader<T extends Extendable, E extends Extension<T> > extends ExtensionProvider<T, E> {

    /**
     * Creates an extension instance from the provided platform configuration.
     */
    E load(PlatformConfig platformConfig);
}
