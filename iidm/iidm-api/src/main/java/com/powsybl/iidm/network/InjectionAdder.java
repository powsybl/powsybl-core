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
public interface InjectionAdder<T extends InjectionAdder> extends IdentifiableAdder<T> {

    T setNode(int node);

    T setBus(String bus);

    default T setConnectionStatus(Terminal.ConnectionStatus connectionStatus) {
        return (T) this;
    }

    /**
     * @deprecated bus and connectableBus are redundant, so we use bus and connection status
     */
    @Deprecated
    default T setConnectableBus(String connectableBus) {
        return setBus(connectableBus);
    }

}
