/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.model.test;

import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesModelFactory;
import com.powsybl.cgmes.model.CgmesOnDataSource;
import com.powsybl.cgmes.model.GridModelReference;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.triplestore.api.PropertyBags;
import com.powsybl.triplestore.api.TripleStoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class CgmesModelTester {

    public CgmesModelTester(GridModelReference gm) {
        this.gridModel = gm;
    }

    public void test() {
        ReadOnlyDataSource ds = gridModel.dataSource();

        // Check that the case exists
        // even if we do not have any available triple store implementation
        // cimNamespace() will throw an exception if no CGMES data is found
        CgmesOnDataSource cds = new CgmesOnDataSource(ds);
        cds.cimNamespace();

        List<String> implementations = TripleStoreFactory.allImplementations();
        assertFalse(implementations.isEmpty());

        for (String impl : implementations) {
            CgmesModel actual = load(ds, impl);
            CgmesModel expected = gridModel.expected();
            if (expected != null) {
                testCompare(gridModel.expected(), actual);
            } else {
                // TODO check that the loaded model is not empty
                // Or check that the number of elements for each type is correct
                // If complete expected model is not available, at least check
                // summary of elements of each type
            }
        }
    }

    private CgmesModel load(ReadOnlyDataSource ds, String impl) {
        CgmesModel actual = CgmesModelFactory.create(ds, impl);
        actual.print(LOG::info);
        return actual;
    }

    private void testCompare(CgmesModel expected, CgmesModel actual) {
        PropertyBags ots = actual.numObjectsByType();
        if (LOG.isInfoEnabled()) {
            LOG.info(ots.tabulateLocals());
        }

        assertEquals(gridModel.expected().version(), actual.version());
        assertEquals(gridModel.expected().isNodeBreaker(), actual.isNodeBreaker());
        // Model id is not checked
        if (LOG.isInfoEnabled()) {
            LOG.info("ignoring model identifiers expected {}, actual {}",
                    expected.modelId(),
                    actual.modelId());
        }
        testPropertyBags(expected.substations(), actual.substations());
        testPropertyBags(expected.voltageLevels(), actual.voltageLevels());
        testPropertyBags(expected.terminals(), actual.terminals());
        testPropertyBags(expected.operationalLimits(), actual.operationalLimits());
        testPropertyBags(expected.topologicalNodes(), actual.topologicalNodes());
        testPropertyBags(expected.switches(), actual.switches());
        testPropertyBags(expected.acLineSegments(), actual.acLineSegments());
        testPropertyBags(expected.transformerEnds(), actual.transformerEnds());
        testPropertyBags(expected.ratioTapChangers(), actual.ratioTapChangers());
        testPropertyBags(expected.phaseTapChangers(), actual.phaseTapChangers());
        testPropertyBags(expected.energyConsumers(), actual.energyConsumers());
        testPropertyBags(expected.shuntCompensators(), actual.shuntCompensators());
        testPropertyBags(expected.nonlinearShuntCompensatorPoints(), actual.nonlinearShuntCompensatorPoints());
        testPropertyBags(expected.staticVarCompensators(), actual.staticVarCompensators());
        testPropertyBags(expected.synchronousMachinesGenerators(), actual.synchronousMachinesGenerators());
        testPropertyBags(expected.synchronousMachinesCondensers(), actual.synchronousMachinesCondensers());
        testPropertyBags(expected.asynchronousMachines(), actual.asynchronousMachines());
        testPropertyBags(expected.modelProfiles(), actual.modelProfiles());
    }

    private void testPropertyBags(PropertyBags expecteds, PropertyBags actuals) {
        if (!actuals.isEmpty()) {
            // Assume the first property name in expected is the main property (id)
            // that we want to extract from actuals to debug current set of actuals
            if (!expecteds.isEmpty()) {
                if (LOG.isDebugEnabled()) {
                    String debugPropertyName = expecteds.get(0).propertyNames().get(0);
                    List<String> debugValues = actuals.pluckLocals(debugPropertyName);
                    LOG.debug("Actuals:");
                    LOG.debug(String.join(",", debugValues));
                }
            } else {
                String names = String.join(",", actuals.get(0).propertyNames());
                LOG.warn("Unexpected actuals with properties {}", names);
            }
        }

        // Check that all property values in expected are present in actuals and have
        // the same value
        assertNotNull(actuals);
        assertEquals(expecteds.size(), actuals.size());
        if (expecteds.isEmpty()) {
            return;
        }
        List<String> expectedPropertyNames = expecteds.get(0).propertyNames();
        // If the values extracted as "locals" (URL prefix removed) are not equals,
        // then they must be equals extracted as "identifiers" (URL prefix removed, initial "_" removed)
        expectedPropertyNames.forEach(p -> {
            if (!expecteds.pluckLocals(p).equals(actuals.pluckLocals(p))) {
                assertEquals(expecteds.pluckIdentifiers(p), actuals.pluckIdentifiers(p));
            }
        });
    }

    private final GridModelReference gridModel;

    private static final Logger LOG = LoggerFactory.getLogger(CgmesModelTester.class);
}
