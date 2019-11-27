/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import java.util.Objects;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
abstract class AbstractAdapter<I> {

    private final I delegate;

    private final MergingViewIndex index;

    protected AbstractAdapter(final I delegate, final MergingViewIndex index) {
        this.delegate = Objects.requireNonNull(delegate, "delegate is null");
        this.index = Objects.requireNonNull(index, "merging view index is null");
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    protected I getDelegate() {
        return delegate;
    }

    protected MergingViewIndex getIndex() {
        return index;
    }
}
