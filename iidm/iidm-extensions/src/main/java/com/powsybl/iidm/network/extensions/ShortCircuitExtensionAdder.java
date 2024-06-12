/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.Extendable;
import com.powsybl.commons.extensions.ExtensionAdder;

/**
 *
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.fr>}
 */
public interface ShortCircuitExtensionAdder<T extends Extendable<T>, C extends ShortCircuitExtension<T>> extends ExtensionAdder<T, C> {

    @Override
    default Class<C> getExtensionClass() {
        throw new PowsyblException("Not yet implemented");
    }

    ShortCircuitExtensionAdder<T, C> withDirectTransX(double directTransX);

    ShortCircuitExtensionAdder<T, C> withDirectSubtransX(double direcSubtransX);

    ShortCircuitExtensionAdder<T, C> withStepUpTransformerX(double stepUpTransformerX);
}
