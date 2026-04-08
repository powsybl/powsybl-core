/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.CoordinatedReactiveControl;
import com.powsybl.iidm.network.extensions.CoordinatedReactiveControlAdder;

/**
 * @author Thomas ADAM {@literal <tadam at silicom.fr>}
 */
public class CoordinatedReactiveControlAdderImpl
        extends AbstractExtensionAdder<Generator, CoordinatedReactiveControl>
        implements CoordinatedReactiveControlAdder {

    private double qPercent;

    protected CoordinatedReactiveControlAdderImpl(Generator extendable) {
        super(extendable);
    }

    @Override
    protected CoordinatedReactiveControlImpl createExtension(Generator extendable) {
        return new CoordinatedReactiveControlImpl(extendable, qPercent);
    }

    @Override
    public CoordinatedReactiveControlAdder withQPercent(double qPercent) {
        this.qPercent = qPercent;
        return this;
    }
}
