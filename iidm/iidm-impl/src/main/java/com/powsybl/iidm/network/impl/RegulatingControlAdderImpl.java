/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.regulation.EquipmentSide;
import com.powsybl.iidm.network.regulation.RegulatingControl;
import com.powsybl.iidm.network.regulation.RegulatingControlAdder;
import com.powsybl.iidm.network.regulation.RegulationKind;

import java.util.Objects;

import static com.powsybl.iidm.network.regulation.EquipmentSide.ONE;
import static com.powsybl.iidm.network.regulation.EquipmentSide.THREE;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class RegulatingControlAdderImpl implements RegulatingControlAdder, Validable {

    private final NetworkImpl network;
    private final RegulatingControlListImpl parent;

    private String id = null;
    private double targetValue = Double.NaN;
    private double targetDeadband = Double.NaN;
    private RegulationKind regulationKind = null;
    private String equipmentId = null;
    private EquipmentSide equipmentSide = null;

    RegulatingControlAdderImpl(NetworkImpl network, RegulatingControlListImpl parent) {
        this.network = Objects.requireNonNull(network);
        this.parent = Objects.requireNonNull(parent);
    }

    @Override
    public RegulatingControlAdder setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public RegulatingControlAdder setTargetValue(double targetValue) {
        this.targetValue = targetValue;
        return this;
    }

    @Override
    public RegulatingControlAdder setTargetDeadband(double targetDeadband) {
        this.targetDeadband = targetDeadband;
        return this;
    }

    @Override
    public RegulatingControlAdder setRegulationKind(RegulationKind regulationKind) {
        this.regulationKind = regulationKind;
        return this;
    }

    @Override
    public RegulatingControlAdder setEquipmentId(String equipmentId) {
        this.equipmentId = equipmentId;
        return this;
    }

    @Override
    public RegulatingControlAdder setEquipmentSide(EquipmentSide equipmentSide) {
        this.equipmentSide = equipmentSide;
        return this;
    }

    @Override
    public RegulatingControl add() {
        if (id == null) {
            throw new ValidationException(this, "Id is null");
        }
        if (parent.getRegulatingControl(id) != null) {
            throw new ValidationException(this, "Regulating control " + id + " already exists");
        }
        if (Double.isNaN(targetValue)) {
            throw new ValidationException(this, "target value is undefined");
        }
        if (regulationKind == null) {
            throw new ValidationException(this, "regulation kind is null");
        }
        if (equipmentId == null) {
            throw new ValidationException(this, "ID of regulated equipment is null");
        }
        Identifiable regulatedEq = network.getIdentifiable(equipmentId);
        if (regulatedEq == null) {
            throw new ValidationException(this, "Regulated equipment does not exist in network " + network.getId());
        }
        if (equipmentSide == null && !(regulatedEq instanceof Injection)) {
            throw new ValidationException(this, "Undefined side for regulated equipment");
        }
        if (equipmentSide != null && !ONE.equals(equipmentSide) && regulatedEq instanceof Injection) {
            throw new ValidationException(this, "Invalid side (" + equipmentSide.index() + ") for regulated equipment " + equipmentId);
        }
        if (THREE.equals(equipmentSide) && !(regulatedEq instanceof ThreeWindingsTransformer)) {
            throw new ValidationException(this, "Invalid side (3) for regulated equipment " + equipmentId);
        }
        return new RegulatingControlImpl(network, id, targetValue, targetDeadband, regulationKind, equipmentId, equipmentSide, parent);
    }

    @Override
    public String getMessageHeader() {
        return "Regulating control '" + id + "': ";
    }
}
