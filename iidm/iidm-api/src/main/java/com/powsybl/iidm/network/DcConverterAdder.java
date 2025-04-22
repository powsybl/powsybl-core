/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public interface DcConverterAdder<T extends DcConverter<? super T> & Connectable<? super T> & DcConnectable<? super T>, A extends DcConverterAdder> extends IdentifiableAdder<T, A> {

    A setNode1(int node1);

    A setBus1(String bus1);

    A setConnectableBus1(String connectableBus1);

    A setNode2(int node2);

    A setBus2(String bus2);

    A setConnectableBus2(String connectableBus2);

    A setDcNode1Id(String dcNode1Id);

    A setDcConnected1(boolean connected1);

    A setDcNode2Id(String dcNode2Id);

    A setDcConnected2(boolean connected2);

    A setIdleLoss(double idleLoss);

    A setSwitchingLoss(double switchingLoss);

    A setResistiveLoss(double resistiveLoss);
}
