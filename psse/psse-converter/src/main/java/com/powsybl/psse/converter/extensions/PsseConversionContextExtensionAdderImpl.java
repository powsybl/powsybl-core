/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.converter.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Network;
import com.powsybl.psse.model.io.Context;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PsseConversionContextExtensionAdderImpl extends AbstractExtensionAdder<Network, PsseConversionContextExtension>
        implements PsseConversionContextExtensionAdder {

    private Context context;

    PsseConversionContextExtensionAdderImpl(Network extendable) {
        super(extendable);
    }

    @Override
    protected PsseConversionContextExtension createExtension(Network extendable) {
        return new PsseConversionContextExtensionImpl(context);
    }

    @Override
    public PsseConversionContextExtensionAdder withContext(Context context) {
        this.context = context;
        return this;
    }

}
