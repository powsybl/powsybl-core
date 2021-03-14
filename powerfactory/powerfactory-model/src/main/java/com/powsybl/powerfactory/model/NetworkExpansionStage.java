/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.model;

import java.time.Instant;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NetworkExpansionStage {

    private final DataObject intSstage;

    public NetworkExpansionStage(DataObject intSstage) {
        this.intSstage = Objects.requireNonNull(intSstage);
    }

    public String getName() {
        return intSstage.getName();
    }

    public Instant getActivationTime() {
        return intSstage.getInstantAttributeValue("tAcTime");
    }

    public void traverse(Consumer<DataObject> handler) {
        intSstage.traverse(handler);
    }
}
