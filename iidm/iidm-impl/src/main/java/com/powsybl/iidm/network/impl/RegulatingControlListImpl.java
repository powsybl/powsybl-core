/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.regulation.RegulatingControl;
import com.powsybl.iidm.network.regulation.RegulatingControlAdder;
import com.powsybl.iidm.network.regulation.RegulatingControlList;
import com.powsybl.iidm.network.regulation.Regulation;

import java.util.*;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class RegulatingControlListImpl extends AbstractExtension<Network> implements RegulatingControlList {

    private final Map<String, RegulatingControlImpl> regulatingControls = new HashMap<>();

    RegulatingControlListImpl(Network extendable) {
        super(extendable);
    }

    @Override
    public RegulatingControlAdder newRegulatingControl() {
        return new RegulatingControlAdderImpl((NetworkImpl) getExtendable(), this);
    }

    @Override
    public RegulatingControl getRegulatingControl(String id) {
        return regulatingControls.get(Objects.requireNonNull(id));
    }

    @Override
    public List<RegulatingControl> getRegulatingControls() {
        return Collections.unmodifiableList(new ArrayList<>(regulatingControls.values()));
    }

    void addRegulatingControl(RegulatingControlImpl regulatingControl) {
        Objects.requireNonNull(regulatingControl);
        regulatingControls.put(regulatingControl.getId(), regulatingControl);
    }

    void removeRegulatingControl(String id) {
        RegulatingControlImpl regulatingControl = regulatingControls.get(Objects.requireNonNull(id));
        new ArrayList<>(regulatingControl.getRegulations()).forEach(Regulation::remove);
        regulatingControls.remove(id);
    }
}
