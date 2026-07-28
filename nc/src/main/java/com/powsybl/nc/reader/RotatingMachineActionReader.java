/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.nc.reader;

import com.powsybl.action.GeneratorAction;
import com.powsybl.action.GeneratorActionBuilder;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.nc.ImporterContext;
import com.powsybl.nc.QueryManager;
import com.powsybl.triplestore.api.PropertyBag;

import java.util.Optional;

import static com.powsybl.nc.NcConverter.LOGGER;
import static com.powsybl.nc.reader.ReaderUtils.MRID;

/**
 * @author Thomas Bouquet {@literal <thomas.bouquet at rte-france.com>}
 */
public class RotatingMachineActionReader extends AbstractActionReader<GeneratorAction> {
    private static final String ROTATING_MACHINE_ACTION = "RotatingMachineAction";
    private static final String ROTATING_MACHINE_ACTION_QUERY_NAME = "rotatingMachineAction";
    private static final String ROTATING_MACHINE = "rotatingMachine";
    private static final String PROPERTY_REFERENCE = "http://energy.referencedata.eu/PropertyReference/RotatingMachine.p";
    // TODO: handle incrementalPercentage?

    public RotatingMachineActionReader(QueryManager queryManager, ImporterContext importerContext, Network network) {
        super(queryManager, importerContext, network, ROTATING_MACHINE_ACTION, PROPERTY_REFERENCE,
            ROTATING_MACHINE_ACTION_QUERY_NAME, ROTATING_MACHINE, true, Generator.class);
    }

    @Override
    protected Optional<GeneratorAction> convertGridStateAlterationToAction(String actionId,
                                                                           Identifiable<?> networkElement,
                                                                           PropertyBag gridStateAlteration,
                                                                           PropertyBag staticPropertyRange,
                                                                           VariationType variationType) {
        return getSetPoint(staticPropertyRange, actionId)
            .map(setPoint -> {
                double multiplier = VariationType.INCREMENTAL_DOWN.equals(variationType) ? -1.0 : 1.0;
                boolean isRelative = VariationType.INCREMENTAL_DOWN.equals(variationType) || VariationType.INCREMENTAL_UP.equals(variationType);
                GeneratorActionBuilder builder = new GeneratorActionBuilder()
                    .withId(actionId)
                    .withGeneratorId(networkElement.getId())
                    .withActivePowerValue(multiplier * setPoint)
                    .withActivePowerRelativeValue(isRelative);
                return builder.build();
            });
    }

    private static Optional<Double> getSetPoint(PropertyBag staticPropertyRange, String generatorActionId) {
        String normalValue = staticPropertyRange.get("normalValue");
        try {
            return Optional.of(Double.parseDouble(normalValue));
        } catch (NumberFormatException e) {
            LOGGER.warn("StaticPropertyRange {} associated to RotatingMachineAction {} has an invalid normal value and will be ignored (expected integer, got {}).",
                staticPropertyRange.get(MRID), generatorActionId, normalValue);
            return Optional.empty();
        }
    }
}
