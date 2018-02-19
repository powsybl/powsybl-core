/**
 * Copyright (c) 2018, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionConfigLoader;

/**
 * Loads an extension of {@link SecurityAnalysisParameters} from {@link com.powsybl.commons.config.PlatformConfig}
 *
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 * @author Sylvain LECLERC <sylvain.leclerc@rte-france.com>
 */
public interface ParametersConfigLoader<E extends Extension<SecurityAnalysisParameters> > extends ExtensionConfigLoader<SecurityAnalysisParameters, E> {

}
