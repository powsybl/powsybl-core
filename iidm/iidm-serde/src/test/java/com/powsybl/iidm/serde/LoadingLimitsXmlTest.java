/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.test.ComparisonUtils;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.DanglingLineNetworkFactory;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.function.Supplier;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_XML_VERSION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class LoadingLimitsXmlTest extends AbstractIidmSerDeTest {

    @Test
    void testDanglingLine() throws IOException {
        Network network = DanglingLineNetworkFactory.create();
        network.setCaseDate(ZonedDateTime.parse("2013-01-15T18:45:00.000+01:00"));
        DanglingLine dl = network.getDanglingLine("DL");
        createLoadingLimits(dl::newActivePowerLimits);
        createLoadingLimits(dl::newApparentPowerLimits);
        createLoadingLimits(dl::newCurrentLimits);
        roundTripXmlTest(network,
                NetworkSerDe::writeAndValidate,
                NetworkSerDe::validateAndRead,
                getVersionedNetworkPath("dl-loading-limits.xml", CURRENT_IIDM_XML_VERSION));

        // backward compatibility from version 1.5
        roundTripVersionedXmlFromMinToCurrentVersionTest("dl-loading-limits.xml", IidmVersion.V_1_5);

        // check that it fails for versions previous to 1.5
        testForAllPreviousVersions(IidmVersion.V_1_5, version -> {
            try {
                ExportOptions options = new ExportOptions().setVersion(version.toString("."));
                NetworkSerDe.write(network, options, tmpDir.resolve("fail"));
                fail();
            } catch (PowsyblException e) {
                assertEquals("danglingLine.activePowerLimits is not null and not supported for IIDM version " + version.toString(".") + ". IIDM version should be >= 1.5",
                        e.getMessage());
            }
        });

        // check that it doesn't fail for versions previous to 1.5 when log error is the IIDM version incompatibility behavior
        testForAllPreviousVersions(IidmVersion.V_1_5, version -> {
            try {
                writeXmlTest(network, (n, p) -> write(n, p, version), getVersionedNetworkPath("dl-loading-limits.xml", version));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    @Test
    void testEurostag() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(ZonedDateTime.parse("2013-01-15T18:45:00.000+01:00"));
        Line line = network.getLine("NHV1_NHV2_2");
        createLoadingLimits(line::newActivePowerLimits1);
        createLoadingLimits(line::newActivePowerLimits2);
        createLoadingLimits(line::newApparentPowerLimits1);
        createLoadingLimits(line::newApparentPowerLimits2);
        createLoadingLimits(line::newCurrentLimits1);
        createLoadingLimits(line::newCurrentLimits2);
        TwoWindingsTransformer twt = network.getTwoWindingsTransformer("NGEN_NHV1");
        createLoadingLimits(twt::newActivePowerLimits1);
        createLoadingLimits(twt::newActivePowerLimits2);
        createLoadingLimits(twt::newApparentPowerLimits1);
        createLoadingLimits(twt::newApparentPowerLimits2);
        createLoadingLimits(twt::newCurrentLimits1);
        createLoadingLimits(twt::newCurrentLimits2);
        roundTripXmlTest(network,
                NetworkSerDe::writeAndValidate,
                NetworkSerDe::validateAndRead,
                getVersionedNetworkPath("eurostag-loading-limits.xml", CURRENT_IIDM_XML_VERSION));

        // backward compatibility from version 1.5
        roundTripVersionedXmlFromMinToCurrentVersionTest("eurostag-loading-limits.xml", IidmVersion.V_1_5);

        // check that it fails for versions previous to 1.5
        testForAllPreviousVersions(IidmVersion.V_1_5, version -> {
            try {
                ExportOptions options = new ExportOptions().setVersion(version.toString("."));
                NetworkSerDe.write(network, options, tmpDir.resolve("fail"));
                fail();
            } catch (PowsyblException e) {
                assertEquals("twoWindingsTransformer.activePowerLimits1 is not null and not supported for IIDM version " + version.toString(".") + ". IIDM version should be >= 1.5",
                        e.getMessage());
            }
        });

        // check that it doesn't fail for versions previous to 1.5 when log error is the IIDM version incompatibility behavior
        testForAllPreviousVersions(IidmVersion.V_1_5, version -> {
            try {
                writeTest(network, (n, p) -> write(n, p, version), ComparisonUtils::compareXml, getVersionedNetworkPath("eurostag-loading-limits.xml", version));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    @Test
    void testTieLine() throws IOException {
        Network network = NetworkSerDe.read(getVersionedNetworkAsStream("tieline.xml", CURRENT_IIDM_XML_VERSION));
        TieLine tl = network.getTieLine("NHV1_NHV2_1");
        createLoadingLimits(tl.getDanglingLine1()::newActivePowerLimits);
        createLoadingLimits(tl.getDanglingLine2()::newActivePowerLimits);
        createLoadingLimits(tl.getDanglingLine1()::newApparentPowerLimits);
        createLoadingLimits(tl.getDanglingLine2()::newApparentPowerLimits);
        createLoadingLimits(tl.getDanglingLine1()::newCurrentLimits);
        createLoadingLimits(tl.getDanglingLine2()::newCurrentLimits);
        roundTripXmlTest(network,
                NetworkSerDe::writeAndValidate,
                NetworkSerDe::validateAndRead,
                getVersionedNetworkPath("tl-loading-limits.xml", CURRENT_IIDM_XML_VERSION));

        // backward compatibility from version 1.5
        roundTripVersionedXmlFromMinToCurrentVersionTest("tl-loading-limits.xml", IidmVersion.V_1_5);

        // check that it fails for versions previous to 1.5
        testForAllPreviousVersions(IidmVersion.V_1_5, version -> {
            try {
                ExportOptions options = new ExportOptions().setVersion(version.toString("."));
                NetworkSerDe.write(network, options, tmpDir.resolve("fail"));
                fail();
            } catch (PowsyblException e) {
                assertEquals("tieLine.activePowerLimits1 is not null and not supported for IIDM version " + version.toString(".") + ". IIDM version should be >= 1.5",
                        e.getMessage());
            }
        });

        // check that it doesn't fail for versions previous to 1.5 when log error is the IIDM version incompatibility behavior
        testForAllPreviousVersions(IidmVersion.V_1_5, version -> {
            try {
                writeXmlTest(network, (n, p) -> write(n, p, version), getVersionedNetworkPath("tl-loading-limits.xml", version));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    @Test
    void testThreeWindingsTransformer() throws IOException {
        Network network = ThreeWindingsTransformerNetworkFactory.createWithCurrentLimits();
        ThreeWindingsTransformer twt = network.getThreeWindingsTransformer("3WT");
        createLoadingLimits(() -> twt.getLeg1().newActivePowerLimits());
        createLoadingLimits(() -> twt.getLeg1().newApparentPowerLimits());
        createLoadingLimits(() -> twt.getLeg2().newActivePowerLimits());
        createLoadingLimits(() -> twt.getLeg2().newApparentPowerLimits());
        createLoadingLimits(() -> twt.getLeg3().newActivePowerLimits());
        createLoadingLimits(() -> twt.getLeg3().newApparentPowerLimits());
        roundTripXmlTest(network,
                NetworkSerDe::writeAndValidate,
                NetworkSerDe::validateAndRead,
                getVersionedNetworkPath("t3w-loading-limits.xml", CURRENT_IIDM_XML_VERSION));

        // backward compatibility from version 1.5
        roundTripVersionedXmlFromMinToCurrentVersionTest("t3w-loading-limits.xml", IidmVersion.V_1_5);

        // check that it fails for versions previous to 1.5
        testForAllPreviousVersions(IidmVersion.V_1_5, version -> {
            try {
                ExportOptions options = new ExportOptions().setVersion(version.toString("."));
                NetworkSerDe.write(network, options, tmpDir.resolve("fail"));
                fail();
            } catch (PowsyblException e) {
                assertEquals("threeWindingsTransformer.activePowerLimits1 is not null and not supported for IIDM version " + version.toString(".") + ". IIDM version should be >= 1.5",
                        e.getMessage());
            }
        });

        // check that it doesn't fail for versions previous to 1.5 when log error is the IIDM version incompatibility behavior
        testForAllPreviousVersions(IidmVersion.V_1_5, version -> {
            try {
                writeXmlTest(network, (n, p) -> write(n, p, version), getVersionedNetworkPath("t3w-loading-limits.xml", version));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    private static <L extends LoadingLimits, A extends LoadingLimitsAdder<L, A>> void createLoadingLimits(Supplier<A> limitsAdderSupplier) {
        limitsAdderSupplier.get()
                .setPermanentLimit(350)
                .beginTemporaryLimit()
                .setValue(370)
                .setAcceptableDuration(20 * 60)
                .setName("20'")
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setValue(380)
                .setAcceptableDuration(10 * 60)
                .setName("10'")
                .endTemporaryLimit()
                .add();
    }

    private static void write(Network network, Path path, IidmVersion version) {
        ExportOptions options = new ExportOptions().setIidmVersionIncompatibilityBehavior(ExportOptions.IidmVersionIncompatibilityBehavior.LOG_ERROR)
                .setVersion(version.toString("."));
        NetworkSerDe.write(network, options, path);
    }
}
