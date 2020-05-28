/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UcteLine extends UcteElement {

    public UcteLine(UcteElementId id, UcteElementStatus status, float resistance, float reactance, float susceptance, Integer currentLimit, String elementName) {
        super(id, status, resistance, reactance, susceptance, currentLimit, elementName);

        if (id.getNodeCode1().getVoltageLevelCode() != id.getNodeCode2().getVoltageLevelCode()) {
            throw new IllegalArgumentException("Line " + id.toString() + " with two different nominal voltages");
        }
    }

}
