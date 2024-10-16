/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.*;
import com.powsybl.triplestore.api.PropertyBag;

import java.util.List;
import java.util.Objects;

import static com.powsybl.cgmes.conversion.Conversion.Config.DefaultValue.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class EnergySourceConversion extends AbstractConductingEquipmentConversion {
    public EnergySourceConversion(PropertyBag es, Context context) {
        super(CgmesNames.ENERGY_SOURCE, es, context);
        this.load = null;
    }

    public EnergySourceConversion(PropertyBag es, PropertyBag cgmesTerminal, Load load, Context context) {
        super(CgmesNames.ENERGY_SOURCE, es, cgmesTerminal, context);
        this.load = load;
    }

    @Override
    public void convert() {
        LoadType loadType = id.contains("fict") ? LoadType.FICTITIOUS : LoadType.UNDEFINED;

        LoadAdder adder = voltageLevel().newLoad()
                .setLoadType(loadType);
        identify(adder);
        connection(adder);
        Load load = adder.add();
        addAliasesAndProperties(load);
        mappingTerminals(load.getTerminal());

        addSpecificProperties(load);
    }

    private static void addSpecificProperties(Load load) {
        load.setProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS, CgmesNames.ENERGY_SOURCE);
    }

    @Override
    public void update(Network network) {
        Objects.requireNonNull(load);
        updateTerminals(context, load.getTerminal());
        load.setP0(updatedP0().orElse(defaultP(Double.NaN, load.getP0(), getDefaultValue(context))))
                .setQ0(qupdatedQ0().orElse(defaultQ(Double.NaN, load.getQ0(), getDefaultValue(context))));
    }

    private static Conversion.Config.DefaultValue getDefaultValue(Context context) {
        return selectDefaultValue(List.of(PREVIOUS, ZERO, NAN), context);
    }

    private final Load load;
}
