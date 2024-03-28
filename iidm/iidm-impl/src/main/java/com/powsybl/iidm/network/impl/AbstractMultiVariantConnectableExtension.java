/**
 * Copyright (c) 2019, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Connectable;

/**
 * @deprecated use {@link AbstractMultiVariantIdentifiableExtension} instead.
 * @author Ghiles Abdellah {@literal <ghiles.abdellah at rte-france.com>}
 */
@Deprecated(since = "3.5.0")
public abstract class AbstractMultiVariantConnectableExtension<T extends Connectable<T>> extends AbstractMultiVariantIdentifiableExtension<T> {

    /**
     * Deprecated, use {@link AbstractMultiVariantIdentifiableExtension} instead.
     */
    @Deprecated(since = "3.5.0")
    public AbstractMultiVariantConnectableExtension(T extendable) {
        super(extendable);
    }

}
