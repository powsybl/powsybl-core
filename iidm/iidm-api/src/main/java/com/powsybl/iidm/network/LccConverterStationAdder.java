/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * LCC converter station builder and adder.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian@rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public interface LccConverterStationAdder extends HvdcConverterStationAdder<LccConverterStationAdder> {

    LccConverterStationAdder setPowerFactor(float powerFactor);

    LccConverterStation add();
}
