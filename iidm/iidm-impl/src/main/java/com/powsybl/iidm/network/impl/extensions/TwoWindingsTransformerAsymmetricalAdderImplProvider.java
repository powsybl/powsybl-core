/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerAsymmetrical;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerAsymmetricalAdder;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(ExtensionAdderProvider.class)
public class TwoWindingsTransformerAsymmetricalAdderImplProvider
        implements ExtensionAdderProvider<TwoWindingsTransformer, TwoWindingsTransformerAsymmetrical, TwoWindingsTransformerAsymmetricalAdder> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return TwoWindingsTransformerAsymmetrical.NAME;
    }

    @Override
    public Class<TwoWindingsTransformerAsymmetricalAdder> getAdderClass() {
        return TwoWindingsTransformerAsymmetricalAdder.class;
    }

    @Override
    public TwoWindingsTransformerAsymmetricalAdder newAdder(TwoWindingsTransformer extendable) {
        return new TwoWindingsTransformerAsymmetricalAdderImpl(extendable);
    }
}
