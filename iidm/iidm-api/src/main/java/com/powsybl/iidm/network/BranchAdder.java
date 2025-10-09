/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface BranchAdder<T extends Branch<? super T> & Connectable<? super T>, A extends BranchAdder> extends IdentifiableAdder<T, A> {

    A setVoltageLevel1(String voltageLevelId1);

    A setNode1(int node1);

    A setBus1(String bus1);

    A setConnectableBus1(String connectableBus1);

    A setVoltageLevel2(String voltageLevelId2);

    A setNode2(int node2);

    A setBus2(String bus2);

    A setConnectableBus2(String connectableBus2);

    @Override
    T add();
}
