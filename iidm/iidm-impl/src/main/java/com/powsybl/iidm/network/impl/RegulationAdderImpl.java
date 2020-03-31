/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.regulation.EquipmentSide;
import com.powsybl.iidm.network.regulation.RegulatingControl;
import com.powsybl.iidm.network.regulation.RegulationAdder;

import java.util.Objects;

import static com.powsybl.iidm.network.regulation.EquipmentSide.ONE;
import static com.powsybl.iidm.network.regulation.EquipmentSide.THREE;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class RegulationAdderImpl implements RegulationAdder {

    private final AbstractConnectable regulatingEquipment;
    private final RegulationListImpl regulationList;

    private boolean regulating;
    private EquipmentSide regulatingSide = null;
    private RegulatingControlImpl regulatingControl = null;

    RegulationAdderImpl(AbstractConnectable regulatingEquipment, RegulationListImpl regulationList) {
        this.regulatingEquipment = Objects.requireNonNull(regulatingEquipment);
        this.regulationList = Objects.requireNonNull(regulationList);
    }

    @Override
    public RegulationAdder setRegulating(boolean regulating) {
        this.regulating = regulating;
        return this;
    }

    @Override
    public RegulationAdder setRegulatingSide(EquipmentSide regulatingSide) {
        this.regulatingSide = regulatingSide;
        return this;
    }

    @Override
    public RegulationAdder setRegulatingControl(RegulatingControl regulatingControl) {
        this.regulatingControl = (RegulatingControlImpl) regulatingControl;
        return this;
    }

    @Override
    public RegulationImpl add() {
        if (regulatingControl == null) {
            throw new ValidationException(regulatingEquipment, "Regulating control is null");
        }
        if (regulatingEquipment.getNetwork() != regulatingControl.getNetwork()) {
            throw new ValidationException(regulatingEquipment, "Regulating control " + regulatingControl.getId() + " is not associated with network " + regulatingEquipment.getNetwork().getId());
        }
        if (regulationList.hasRegulation(regulatingControl.getId())) {
            throw new ValidationException(regulatingEquipment, regulatingEquipment.getId() + " has already a regulation associated to regulating control " + regulatingControl.getId());
        }
        if (regulatingSide == null && !(regulatingEquipment instanceof Injection)) {
            throw new ValidationException(regulatingEquipment, "Undefined side for regulating equipment " + regulatingEquipment.getId());
        }
        if (regulatingSide != null && !ONE.equals(regulatingSide) && regulatingEquipment instanceof Injection) {
            throw new ValidationException(regulatingEquipment, "Invalid side (" + regulatingSide.index() + ") for regulating equipment " + regulatingEquipment.getId());
        }
        if (THREE.equals(regulatingSide) && !(regulatingEquipment instanceof ThreeWindingsTransformer)) {
            throw new ValidationException(regulatingEquipment, "Invalid side (3) for regulating equipment " + regulatingEquipment.getId());
        }
        return new RegulationImpl(regulating, regulatingControl, regulatingEquipment, regulatingSide, regulationList);
    }
}
