/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.commons.ref.Ref;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
interface VoltageLevelExt extends VoltageLevel, MultiVariantObject, Validable {

    interface NodeBreakerViewExt extends NodeBreakerView {

    }

    interface BusBreakerViewExt extends BusBreakerView {

        @Override BusExt getBus(String id);

    }

    interface BusViewExt extends BusView {

        @Override BusExt getBus(String id);

    }

    @Override NodeBreakerViewExt getNodeBreakerView();

    @Override BusBreakerViewExt getBusBreakerView();

    @Override BusViewExt getBusView();

    NetworkImpl getNetwork();

    @Override
    NetworkExt getParentNetwork();

    TopologyModel getTopologyModel();

    String getSubnetworkId();

    Ref<NetworkImpl> getNetworkRef();
}
