/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import java.util.Set;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.TapChanger;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
interface TapChangerParent extends Validable {

    NetworkImpl getNetwork();

    Identifiable getTransformer();

    Set<TapChanger> getAllTapChangers();

    boolean hasRatioTapChanger();

    boolean hasPhaseTapChanger();
}
