/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TwoWindingsTransformer;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
class CgmesTapChangersAdderImpl<C extends Connectable<C>> extends AbstractExtensionAdder<C, CgmesTapChangers<C>> implements CgmesTapChangersAdder<C> {

    CgmesTapChangersAdderImpl(C extendable) {
        super(extendable);
    }

    @Override
    protected CgmesTapChangers<C> createExtension(C extendable) {
        if (extendable instanceof TwoWindingsTransformer || extendable instanceof ThreeWindingsTransformer) {
            return new CgmesTapChangersImpl<>(extendable);
        }
        throw new PowsyblException("CGMES Tap Changers can only be added on transformers");
    }
}
