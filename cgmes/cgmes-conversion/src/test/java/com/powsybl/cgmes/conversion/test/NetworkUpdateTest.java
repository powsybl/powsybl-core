/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.conformity.CgmesConformity1ModifiedCatalog;
import com.powsybl.cgmes.conformity.CgmesConformity3Catalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.test.export.ExportXmlCompare;
import com.powsybl.cgmes.model.CgmesModelFactory;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.*;
import com.powsybl.triplestore.api.TripleStoreFactory;
import com.powsybl.triplestore.api.TripleStoreOptions;
import org.junit.jupiter.api.Test;
import org.xmlunit.diff.DifferenceEvaluator;
import org.xmlunit.diff.DifferenceEvaluators;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */

class NetworkUpdateTest extends AbstractSerDeTest {

    static class Summary {
        final double totalLoad;
        final double totalGeneration;
        final double totalBShunts;

        Summary(Network network) {
            totalLoad = network
                    .getLoadStream()
                    .mapToDouble(Load::getP0)
                    .sum();
            totalGeneration = network
                    .getGeneratorStream()
                    .mapToDouble(Generator::getTargetP)
                    .sum();
            totalBShunts = network
                    .getShuntCompensatorStream()
                    .mapToDouble(s -> s.findSectionCount().isPresent() ? s.getB() : Double.NaN)
                    .sum();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Summary summary = (Summary) o;
            return Double.compare(totalLoad, summary.totalLoad) == 0 && Double.compare(totalGeneration, summary.totalGeneration) == 0 && Double.compare(totalBShunts, summary.totalBShunts) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(totalLoad, totalGeneration, totalBShunts);
        }
    }

    @Test
    void testReadSSHNodeBreakerSvInjection() {
        // FIXME(Luma) this test is not well defined,
        //  made just to check informally and easily that we have an SvInjection updated in node/breaker mode ...
        Network network = Network.read(CgmesConformity1ModifiedCatalog.miniNodeBreakerSvInjection().dataSource());
        CgmesImport importer = new CgmesImport();
        // We read an SV for bus/branch,
        // It has a SvInjection with a TopologicalNode,
        // that TP node is in the boundary, so it has a connectivity node from the EQ_TP
        // lucky me
        importer.update(network, CgmesConformity1ModifiedCatalog.smallBusBranchWithSvInjection().dataSource(), null, ReportNode.NO_OP);
    }

    @Test
    void testReadSSH() {
        Network expected = Network.read(CgmesConformity1Catalog.microGridBaseCaseBE().dataSource());
        Summary sexpected = new Summary(expected);

        Network network = Network.read(CgmesConformity1Catalog.microGridBaseCaseBEonlyEQ().dataSource());

        // reset default values and ensure we do not have ssh validation level, but equipment, the lowest
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        network.getLoadStream().forEach(l -> {
            l.setP0(Double.NaN);
            l.setQ0(Double.NaN);
        });
        network.getGeneratorStream().forEach(g -> g.setTargetP(Double.NaN));
        network.getShuntCompensatorStream().forEach(ShuntCompensator::unsetSectionCount);
        assertEquals(ValidationLevel.EQUIPMENT, network.getValidationLevel());

        // Now import only SSH data over the current network
        CgmesImport importer = new CgmesImport();
        importer.update(network, CgmesConformity1Catalog.microGridBaseCaseBEonlySSH().dataSource(), null, ReportNode.NO_OP);
        assertEquals(ValidationLevel.STEADY_STATE_HYPOTHESIS, network.getValidationLevel());
        Summary snetwork = new Summary(network);

        assertEquals(sexpected, snetwork);
        // FIXME(Luma) In addition to comparing the summary, ...
        //  assertTrue(compareNetworks(expected, network));
    }

    @Test
    void testReadSSH3() {
        Network expected = Network.read(CgmesConformity3Catalog.microGridBaseCaseBE().dataSource());
        Summary sexpected = new Summary(expected);

        Network network = Network.read(CgmesConformity3Catalog.microGridBaseCaseBEonlyEQ().dataSource());

        // reset default values and ensure we do not have ssh validation level, but equipment, the lowest
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        network.getLoadStream().forEach(l -> {
            l.setP0(Double.NaN);
            l.setQ0(Double.NaN);
        });
        network.getGeneratorStream().forEach(g -> g.setTargetP(Double.NaN));
        network.getShuntCompensatorStream().forEach(ShuntCompensator::unsetSectionCount);
        assertEquals(ValidationLevel.EQUIPMENT, network.getValidationLevel());

        // Now import only SSH data over the current network
        CgmesImport importer = new CgmesImport();
        importer.update(network, CgmesConformity3Catalog.microGridBaseCaseBEonlySSH().dataSource(), null, ReportNode.NO_OP);
        assertEquals(ValidationLevel.STEADY_STATE_HYPOTHESIS, network.getValidationLevel());
        Summary snetwork = new Summary(network);

        assertEquals(sexpected, snetwork);
        // FIXME(Luma) In addition to comparing the summary, ...
        //  assertTrue(compareNetworks(expected, network));
    }

    @Test
    void testReadSshSvTp3() {
        Network expected = Network.read(CgmesConformity3Catalog.microGridBaseCaseBE().dataSource());
        Summary sexpected = new Summary(expected);
        Map<String, Double> vexpected = expected.getBusView().getBusStream().collect(Collectors.toMap(Bus::getId, Bus::getV));

        Network network = Network.read(CgmesConformity3Catalog.microGridBaseCaseBEonlyEQ().dataSource());

        // reset default values and ensure we do not have ssh validation level, but equipment, the lowest
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        network.getLoadStream().forEach(l -> {
            l.setP0(Double.NaN);
            l.setQ0(Double.NaN);
        });
        network.getGeneratorStream().forEach(g -> g.setTargetP(Double.NaN));
        network.getShuntCompensatorStream().forEach(ShuntCompensator::unsetSectionCount);
        assertEquals(ValidationLevel.EQUIPMENT, network.getValidationLevel());

        // Now import SSH, SV, TP data over the current network
        CgmesImport importer = new CgmesImport();
        importer.update(network, CgmesConformity3Catalog.microGridBaseCaseBEonlySshSvTp().dataSource(), null, ReportNode.NO_OP);
        assertEquals(ValidationLevel.STEADY_STATE_HYPOTHESIS, network.getValidationLevel());
        Summary snetwork = new Summary(network);

        // Summaries should be equal
        assertEquals(sexpected, snetwork);

        // FIXME(Luma) This is just to check that we read TN, CN and voltages from SSH, TP, SV
        TripleStoreOptions tripleStoreOptions = new TripleStoreOptions();
        tripleStoreOptions.setQueryCatalog("-update");
        ReadOnlyDataSource alternativeDataSourceForBoundary = null;
        CgmesModelFactory.create(
                        CgmesConformity3Catalog.microGridBaseCaseBEonlySshSvTp().dataSource(),
                        alternativeDataSourceForBoundary,
                        TripleStoreFactory.DEFAULT_IMPLEMENTATION,
                        ReportNode.NO_OP,
                        tripleStoreOptions);

        // Voltages should be equal
        Map<String, Double> vactual = network.getBusView().getBusStream().collect(Collectors.toMap(Bus::getId, Bus::getV));
        assertEquals(vexpected, vactual);
        // FIXME(Luma) In addition to comparing the summary, ...
        //  assertTrue(compareNetworks(expected, network));
    }

    private boolean compareNetworks(Network expected, Network actual) {
        // FIXME(Luma) remove properties before comparison
        expected.getPropertyNames().stream().toList().forEach(expected::removeProperty);
        actual.getPropertyNames().stream().toList().forEach(actual::removeProperty);

        Path pexpected = tmpDir.resolve("expected.xiidm");
        Path pactual = tmpDir.resolve("actual.xiidm");
        expected.write("XIIDM", null, pexpected);
        actual.write("XIIDM", null, pactual);
        DifferenceEvaluator knownDiffsXiidm = DifferenceEvaluators.chain(
                DifferenceEvaluators.Default,
                ExportXmlCompare::ignoringCgmesMetadataModels);
        return ExportXmlCompare.compareNetworks(pexpected, pactual, knownDiffsXiidm);
    }
}
