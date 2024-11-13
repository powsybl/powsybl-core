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
        this.load = Objects.requireNonNull(load);
    }

    @Override
    public void convert() {
        LoadType loadType = id.contains("fict") ? LoadType.FICTITIOUS : LoadType.UNDEFINED;

        LoadAdder adder = voltageLevel().newLoad()
                .setLoadType(loadType);
        identify(adder);
        connectWithOnlyEq(adder);
        Load newLoad = adder.add();
        addAliasesAndProperties(newLoad);
        convertedTerminalsWithOnlyEq(newLoad.getTerminal());

        addSpecificProperties(newLoad);
    }

    private static void addSpecificProperties(Load newLoad) {
        newLoad.setProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS, CgmesNames.ENERGY_SOURCE);
    }

    @Override
    public void update() {
        updateTerminals(context, load.getTerminal());
        load.setP0(updatedP0().orElse(defaultValue(Double.NaN, load.getP0(), 0.0, Double.NaN, getDefaultValueSelector(context))))
                .setQ0(updatedQ0().orElse(defaultValue(Double.NaN, load.getQ0(), 0.0, Double.NaN, getDefaultValueSelector(context))));
    }

    private static Conversion.Config.DefaultValue getDefaultValueSelector(Context context) {
        return getDefaultValueSelector(List.of(PREVIOUS, DEFAULT, EMPTY), context);
    }

    private final Load load;
}
