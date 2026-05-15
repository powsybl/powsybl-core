/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.dc;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */
public class DCPole {

    private final DCEquipment converter1A;
    private final DCEquipment converter2A;
    private final DCEquipment dcLine1;
    private final boolean halfOfBipole;
    private DCEquipment converter1B = null;
    private DCEquipment converter2B = null;
    private DCEquipment dcLine2 = null;

    public DCPole(DCEquipment converter1A, DCEquipment converter2A, DCEquipment dcLine1, boolean halfOfBipole) {
        this.converter1A = converter1A;
        this.converter2A = converter2A;
        this.dcLine1 = dcLine1;
        this.halfOfBipole = halfOfBipole;
    }

    public void addSecondBridge(DCEquipment converter1B, DCEquipment converter2B) {
        this.converter1B = converter1B;
        this.converter2B = converter2B;
    }

    public void addMetallicReturnLine(DCEquipment dcLine2) {
        this.dcLine2 = dcLine2;
    }

    public DCEquipment getConverter1A() {
        return converter1A;
    }

    public DCEquipment getConverter2A() {
        return converter2A;
    }

    public DCEquipment getDcLine1() {
        return dcLine1;
    }

    public boolean isHalfOfBipole() {
        return halfOfBipole;
    }

    public DCEquipment getConverter1B() {
        return converter1B;
    }

    public DCEquipment getConverter2B() {
        return converter2B;
    }

    public DCEquipment getDcLine2() {
        return dcLine2;
    }
}
