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
public interface AcDcConverterAdder<T extends AcDcConverter<? super T> & Connectable<? super T> & DcConnectable<? super T>, A extends AcDcConverterAdder> extends IdentifiableAdder<T, A> {

    A setNode1(int node1);

    A setBus1(String bus1);

    A setConnectableBus1(String connectableBus1);

    A setNode2(int node2);

    A setBus2(String bus2);

    A setConnectableBus2(String connectableBus2);

    A setDcNode1(String dcNode1);

    A setDcConnected1(boolean connected1);

    A setDcNode2(String dcNode2);

    A setDcConnected2(boolean connected2);

    A setIdleLoss(double idleLoss);

    A setSwitchingLoss(double switchingLoss);

    A setResistiveLoss(double resistiveLoss);

    A setPccTerminal(Terminal pccTerminal);

    A setControlMode(AcDcConverter.ControlMode controlMode);

    A setTargetP(double targetP);

    A setTargetVdc(double targetVdc);
}
