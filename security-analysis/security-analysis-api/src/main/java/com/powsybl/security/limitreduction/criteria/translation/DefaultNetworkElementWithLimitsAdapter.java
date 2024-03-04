/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitreduction.criteria.translation;

import com.powsybl.iidm.criteria.translation.DefaultNetworkElementAdapter;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.security.limitreduction.ContingencyWiseReducedLimitsComputer;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class DefaultNetworkElementWithLimitsAdapter extends DefaultNetworkElementAdapter implements ContingencyWiseReducedLimitsComputer.FilterableNetworkElement {
    public DefaultNetworkElementWithLimitsAdapter(Identifiable<?> identifiable) {
        super(identifiable);
    }
}
