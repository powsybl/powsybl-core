/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ampl.converter;

import com.powsybl.commons.util.StringToIntMapper;
import com.powsybl.iidm.network.Network;
import org.apache.commons.lang3.NotImplementedException;

/**
 * Factory pattern to create {@link AmplNetworkUpdater}.
 * <p>
 * Used by {@link AmplNetworkReader} to create the {@link AmplNetworkUpdater} and pass some data.
 *
 * @apiNote One must override exactly one method to define a child class extending NetworkApplierFactory.
 * <p>
 * One must always call the public of function (calling protected ones will break some implementations).
 */
public abstract class AbstractAmplNetworkUpdaterFactory {
    protected AmplNetworkUpdater of() {
        throw new NotImplementedException("At least one of the methods of NetworkApplierFactory must be redefined");
    }

    protected AmplNetworkUpdater of(StringToIntMapper<AmplSubset> mapper) {
        return of();
    }

    public AmplNetworkUpdater of(StringToIntMapper<AmplSubset> mapper, Network network) {
        return of(mapper);
    }

}
