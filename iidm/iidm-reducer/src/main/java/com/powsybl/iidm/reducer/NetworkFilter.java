/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.reducer;

import com.powsybl.iidm.network.*;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public interface NetworkFilter {

    /**
     * Return true if the given {@link Substation} should be kept in the network, false otherwise
     */
    boolean accept(Substation substation);

    /**
     * Return true if the given {@link VoltageLevel} should be kept in the network, false otherwise
     */
    boolean accept(VoltageLevel voltageLevel);

    /**
     * Return true if the given {@link Line} should be kept in the network, false otherwise
     */
    boolean accept(Line line);

    /**
     * Return true if the given {@link TwoWindingsTransformer} should be kept in the network, false otherwise
     */
    boolean accept(TwoWindingsTransformer transformer);

    /**
     * Return true if the given {@link ThreeWindingsTransformer} should be kept in the network, false otherwise
     */
    boolean accept(ThreeWindingsTransformer transformer);

    /**
     * Return true if the given {@link HvdcLine} should be kept in the network, false otherwise
     */
    boolean accept(HvdcLine hvdcLine);

}
