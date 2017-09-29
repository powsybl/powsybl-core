/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * Use {@link AbstractEquipmentTopologyVisitor} instead
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class EquipmentTopologyVisitor extends AbstractEquipmentTopologyVisitor {

    public <I extends Connectable<I>> void visitEquipment(Connectable<I> eq) {
        throw new IllegalStateException("visitEquipment has to be implemented");
    }
}
