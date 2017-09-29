/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Component;
import com.powsybl.iidm.network.impl.util.Ref;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class ConnectedComponentImpl extends ComponentImpl implements Component {

    ConnectedComponentImpl(int num, int size, Ref<NetworkImpl> networkRef) {
        super(num, size, networkRef);
    }
}
