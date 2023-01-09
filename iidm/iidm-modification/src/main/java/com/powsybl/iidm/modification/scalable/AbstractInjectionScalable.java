/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.scalable;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

import java.util.Objects;

/**
 * Base class for scalables that consist in a unique injection with minimum and maximum
 * power value.
 *
 * @author Sebastien Murgey <sebastien.murgey at rte-france.com>
 */
abstract class AbstractInjectionScalable extends AbstractScalable {

    protected final String id;

    AbstractInjectionScalable(String id) {
        this(id, -Double.MAX_VALUE, Double.MAX_VALUE, ScalingConvention.GENERATOR);
    }

    AbstractInjectionScalable(String id, double minInjection, double maxInjection, ScalingConvention scalingConvention) {
        super(minInjection, maxInjection, scalingConvention);
        this.id = Objects.requireNonNull(id);
        if (maxInjection < minInjection) {
            throw new PowsyblException("Error creating Scalable " + id
                    + " : maxInjection should be bigger than minInjection");
        }
    }

    protected Injection getInjectionOrNull(Network n) {
        Identifiable identifiable = n.getIdentifiable(id);
        if (identifiable instanceof Injection) {
            return (Injection) identifiable;
        } else {
            return null;
        }
    }

    @Override
    public void setInitialInjectionToNetworkValue(Network n) {
        this.initialInjection = this.getCurrentInjection(n, ScalingConvention.GENERATOR);
    }
}
