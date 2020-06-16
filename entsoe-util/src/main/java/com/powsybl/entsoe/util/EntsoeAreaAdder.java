/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.entsoe.util;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.Substation;

/**
 * @author Jérémy Labous <jlabous at silicom.fr>
 */
public interface EntsoeAreaAdder extends ExtensionAdder<Substation, EntsoeArea> {

    @Override
    default Class<EntsoeArea> getExtensionClass() {
        return EntsoeArea.class;
    }

    EntsoeAreaAdder withCode(EntsoeGeographicalCode code);
}
