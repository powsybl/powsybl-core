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
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.LoadAdder;
import com.powsybl.iidm.network.LoadType;
import com.powsybl.iidm.network.Network;
import com.powsybl.triplestore.api.PropertyBag;

import java.util.List;
import java.util.Objects;

import static com.powsybl.cgmes.conversion.Conversion.Config.DefaultValue.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class AsynchronousMachineConversion extends AbstractConductingEquipmentConversion {

    public AsynchronousMachineConversion(PropertyBag asm, Context context) {
        super(CgmesNames.ASYNCHRONOUS_MACHINE, asm, context);
        this.load = null;
    }

    public AsynchronousMachineConversion(PropertyBag es, PropertyBag cgmesTerminal, Load load, Context context) {
        super(CgmesNames.ASYNCHRONOUS_MACHINE, es, cgmesTerminal, context);
        this.load = load;
    }

    @Override
    public void convert() {
        // Will convert it to a Load (a fixed injection)
        // We make no difference based on the type (motor/generator)
        LoadType loadType = id.contains("fict") ? LoadType.FICTITIOUS : LoadType.UNDEFINED;
        LoadAdder adder = voltageLevel().newLoad()
                .setLoadType(loadType);
        identify(adder);
        connection(adder);
        Load newLoad = adder.add();
        addAliasesAndProperties(newLoad);
        mappingTerminals(newLoad.getTerminal());

        addSpecificProperties(newLoad);
    }

    @Override
    public void update(Network network) {
        Objects.requireNonNull(load);
        updateTerminals(context, load.getTerminal());
        load.setP0(updatedP0().orElse(defaultP(Double.NaN, load.getP0(), gettDefaultValue(context))))
                .setQ0(qupdatedQ0().orElse(defaultQ(Double.NaN, load.getQ0(), gettDefaultValue(context))));
    }

    private static void addSpecificProperties(Load newLoad) {
        newLoad.setProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS, CgmesNames.ASYNCHRONOUS_MACHINE);
    }

    private static Conversion.Config.DefaultValue gettDefaultValue(Context context) {
        return selectDefaultValue(List.of(PREVIOUS, DEFAULT, EMPTY), context);
    }

    private final Load load;
}


