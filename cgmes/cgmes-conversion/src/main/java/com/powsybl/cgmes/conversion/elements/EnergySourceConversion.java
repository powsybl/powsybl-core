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
import com.powsybl.cgmes.model.PowerFlow;
import com.powsybl.iidm.network.*;
import com.powsybl.triplestore.api.PropertyBag;

import java.util.List;

import static com.powsybl.cgmes.conversion.Conversion.Config.DefaultValue.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class EnergySourceConversion extends AbstractConductingEquipmentConversion {
    public EnergySourceConversion(PropertyBag es, Context context) {
        super(CgmesNames.ENERGY_SOURCE, es, context);
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

    public static void update(Load load, PropertyBag cgmesData, Context context) {
        updateTerminals(load, context, load.getTerminal());

        PowerFlow updatedPowerFlow = updatedPowerFlow(load, cgmesData, context);
        load.setP0(updatedPowerFlow.defined() ? updatedPowerFlow.p() : defaultValue(Double.NaN, load.getP0(), 0.0, Double.NaN, getDefaultValueSelector(context)));
        load.setQ0(updatedPowerFlow.defined() ? updatedPowerFlow.q() : defaultValue(Double.NaN, load.getQ0(), 0.0, Double.NaN, getDefaultValueSelector(context)));
    }

    private static Conversion.Config.DefaultValue getDefaultValueSelector(Context context) {
        return getDefaultValueSelector(List.of(PREVIOUS, DEFAULT, EMPTY), context);
    }
}
