/**
 * Copyright (c) 2018, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow;

import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.Extendable;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionSerializer;

/**
 *
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
public interface ExtensionLFParametersSerializer<T extends Extendable, E extends Extension<T>> extends ExtensionSerializer<T, E> {

    <E extends Extension<T>> E deserialize(PlatformConfig platformConfig);
}
