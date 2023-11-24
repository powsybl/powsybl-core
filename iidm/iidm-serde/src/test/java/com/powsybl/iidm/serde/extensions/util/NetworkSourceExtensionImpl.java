/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions.util;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Network;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class NetworkSourceExtensionImpl extends AbstractExtension<Network> implements NetworkSourceExtension {
    private final String sourceData;

    public NetworkSourceExtensionImpl(String sourceData) {
        this.sourceData = sourceData;
    }

    @Override
    public String getSourceData() {
        return sourceData;
    }

}
