/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface BranchAdder<T extends BranchAdder> extends IdentifiableAdder<T> {

    T setVoltageLevel1(String voltageLevelId1);

    T setNode1(int node1);

    T setBus1(String bus1);

    T setConnectableBus1(String connectableBus1);

    T setVoltageLevel2(String voltageLevelId2);

    T setNode2(int node2);

    T setBus2(String bus2);

    T setConnectableBus2(String connectableBus2);

}
