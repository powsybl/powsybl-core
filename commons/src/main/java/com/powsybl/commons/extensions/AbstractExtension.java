/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.extensions;

import com.powsybl.commons.PowsyblException;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public abstract class AbstractExtension<T> implements Extension<T> {

    private T extendable;

    protected AbstractExtension() {
        this.extendable = null;
    }

    protected AbstractExtension(T extendable) {
        this.extendable = extendable;
    }

    public T getExtendable() {
        return extendable;
    }

    public void setExtendable(T extendable) {
        if ((extendable != null) && (this.extendable != null) && (this.extendable != extendable)) {
            throw new PowsyblException("Extension is already associated to the extendable " + extendable);
        }

        this.extendable = extendable;
    }
}
