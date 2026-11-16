/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Network;

/**
 * @author Jérémy Labous {@literal <jlabous at silicom.fr>}
 */
public class CgmesConversionContextExtensionAdderImpl extends AbstractExtensionAdder<Network, CgmesConversionContextExtension>
        implements CgmesConversionContextExtensionAdder {

    private Context context;

    public CgmesConversionContextExtensionAdderImpl(Network extendable) {
        super(extendable);
    }

    @Override
    protected CgmesConversionContextExtension createExtension(Network extendable) {
        return new CgmesConversionContextExtensionImpl(context);
    }

    @Override
    public CgmesConversionContextExtensionAdder withContext(Context context) {
        this.context = context;
        return this;
    }

}
