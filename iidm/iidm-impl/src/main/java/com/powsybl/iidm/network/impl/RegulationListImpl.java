/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.regulation.Regulation;
import com.powsybl.iidm.network.regulation.RegulationAdder;
import com.powsybl.iidm.network.regulation.RegulationList;

import java.util.*;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class RegulationListImpl<C extends Connectable> extends AbstractExtension<C> implements RegulationList<C> {

    private final Map<String, RegulationImpl> regulations = new HashMap<>();

    RegulationListImpl(C extendable) {
        super(extendable);
    }

    @Override
    public RegulationAdder newRegulation() {
        return new RegulationAdderImpl((AbstractConnectable) getExtendable(), this);
    }

    @Override
    public RegulationImpl getRegulation(String regulatingControlId) {
        return regulations.get(Objects.requireNonNull(regulatingControlId));
    }

    @Override
    public Optional<Regulation> getOptionalRegulation(String regulatingControlId) {
        return Optional.ofNullable(regulations.get(Objects.requireNonNull(regulatingControlId)));
    }

    @Override
    public boolean hasRegulation(String regulatingControlId) {
        return regulations.containsKey(Objects.requireNonNull(regulatingControlId));
    }

    @Override
    public RegulationList removeRegulation(String regulatingControlId) {
        regulations.get(Objects.requireNonNull(regulatingControlId)).remove();
        return this;
    }

    void addRegulation(RegulationImpl regulation) {
        regulations.put(regulation.getRegulatingControl().getId(), regulation);
    }

    void removeRegulation(RegulationImpl regulation) {
        regulations.remove(regulation.getRegulatingControl().getId());
    }
}
