/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
abstract class AbstractConnectableAdder<T extends AbstractConnectableAdder<T>> extends AbstractIdentifiableAdder<T> {

    private boolean equivalent;

    public T setEquivalent(boolean equivalent) {
        this.equivalent = equivalent;
        return (T) this;
    }

    protected boolean isEquivalent() {
        return equivalent;
    }

}
