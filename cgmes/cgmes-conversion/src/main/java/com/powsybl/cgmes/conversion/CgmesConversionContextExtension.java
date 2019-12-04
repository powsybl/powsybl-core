/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Network;

import java.util.Objects;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class CgmesConversionContextExtension extends AbstractExtension<Network> {

    private final Context context;

    public CgmesConversionContextExtension(Context context) {
        this.context = Objects.requireNonNull(context);
    }

    public Context getContext() {
        return context;
    }

    @Override
    public String getName() {
        return "CgmesConversionContext";
    }
}
