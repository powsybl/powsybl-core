/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.DanglingLineNetworkFactory;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.function.Supplier;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;
import static org.junit.jupiter.api.Assertions.*;

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
        allFormatsRoundTripTest(network, "dl-loading-limits.xml", CURRENT_IIDM_VERSION);

        // backward compatibility from version 1.5
        allFormatsRoundTripFromVersionedXmlFromMinToCurrentVersionTest("dl-loading-limits.xml", IidmVersion.V_1_5);

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
        var options = new ExportOptions().setIidmVersionIncompatibilityBehavior(ExportOptions.IidmVersionIncompatibilityBehavior.LOG_ERROR);
        testWriteXmlAllPreviousVersions(network, options, "dl-loading-limits.xml", IidmVersion.V_1_5);
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
        allFormatsRoundTripTest(network, "eurostag-loading-limits.xml", CURRENT_IIDM_VERSION);

        // backward compatibility from version 1.5
        allFormatsRoundTripFromVersionedXmlFromMinToCurrentVersionTest("eurostag-loading-limits.xml", IidmVersion.V_1_5);

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
        var options = new ExportOptions().setIidmVersionIncompatibilityBehavior(ExportOptions.IidmVersionIncompatibilityBehavior.LOG_ERROR);
        testWriteXmlAllPreviousVersions(network, options, "eurostag-loading-limits.xml", IidmVersion.V_1_5);
    }

    @Test
    void testTieLine() throws IOException {
        Network network = NetworkSerDe.read(getVersionedNetworkAsStream("tieline.xml", CURRENT_IIDM_VERSION));
        TieLine tl = network.getTieLine("NHV1_NHV2_1");
        createLoadingLimits(tl.getDanglingLine1()::newActivePowerLimits);
        createLoadingLimits(tl.getDanglingLine2()::newActivePowerLimits);
        createLoadingLimits(tl.getDanglingLine1()::newApparentPowerLimits);
        createLoadingLimits(tl.getDanglingLine2()::newApparentPowerLimits);
        createLoadingLimits(tl.getDanglingLine1()::newCurrentLimits);
        createLoadingLimits(tl.getDanglingLine2()::newCurrentLimits);
        allFormatsRoundTripTest(network, "tl-loading-limits.xml", CURRENT_IIDM_VERSION);

        // backward compatibility from version 1.5
        allFormatsRoundTripFromVersionedXmlFromMinToCurrentVersionTest("tl-loading-limits.xml", IidmVersion.V_1_5);

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
        var options = new ExportOptions().setIidmVersionIncompatibilityBehavior(ExportOptions.IidmVersionIncompatibilityBehavior.LOG_ERROR);
        testWriteXmlAllPreviousVersions(network, options, "tl-loading-limits.xml", IidmVersion.V_1_5);
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
        allFormatsRoundTripTest(network, "t3w-loading-limits.xml", CURRENT_IIDM_VERSION);

        // backward compatibility from version 1.5
        allFormatsRoundTripFromVersionedXmlFromMinToCurrentVersionTest("t3w-loading-limits.xml", IidmVersion.V_1_5);

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
        var options = new ExportOptions().setIidmVersionIncompatibilityBehavior(ExportOptions.IidmVersionIncompatibilityBehavior.LOG_ERROR);
        testWriteXmlAllPreviousVersions(network, options, "t3w-loading-limits.xml", IidmVersion.V_1_5);
    }

    @Test
    void testImportWithoutPermanentLimit() {
        // Check that import succeeds for versions prior to 1.12
        // (the permanent limit is computed)
        ImportOptions options = new ImportOptions()
                .setMissingPermanentLimitPercentage(90.);
        testForAllPreviousVersions(IidmVersion.V_1_12, version -> {
            Network n = NetworkSerDe.read(getVersionedNetworkAsStream("withoutPermanentLimit.xml", version), options, null);
            Line line = n.getLine("NHV1_NHV2_1");
            assertEquals(900., line.getCurrentLimits1().orElseThrow().getPermanentLimit());
            assertEquals(300., line.getCurrentLimits2().orElseThrow().getPermanentLimit());
        });

        // The import should fail for versions after (or equals) 1.12
        testForAllVersionsSince(IidmVersion.V_1_12, version -> {
            PowsyblException e = assertThrows(PowsyblException.class,
                    () -> NetworkSerDe.read(getVersionedNetworkAsStream("withoutPermanentLimit.xml", version), options, null));
            assertTrue(e.getMessage().contains("permanentLimit is absent"));
        });
    }

    @Test
    void testImportWithoutPermanentLimit2() {
        // Check that import succeeds for all versions:
        // with the minimalValidationLevel option set to EQUIPMENT, the limits are imported to Double.NaN
        ImportOptions options = new ImportOptions()
                .setMinimalValidationLevel(ValidationLevel.EQUIPMENT.toString());
        testForAllVersionsSince(IidmVersion.V_1_0, version -> {
            Network n = NetworkSerDe.read(getVersionedNetworkAsStream("withoutPermanentLimit.xml", version), options, null);
            Line line = n.getLine("NHV1_NHV2_1");
            assertEquals(Double.NaN, line.getCurrentLimits1().orElseThrow().getPermanentLimit());
            assertEquals(Double.NaN, line.getCurrentLimits2().orElseThrow().getPermanentLimit());
        });
    }

    @Test
    void testWrongParametersValue() {
        ImportOptions options = new ImportOptions();
        PowsyblException e = assertThrows(PowsyblException.class, () -> options.setMinimalValidationLevel("Unknown value"));
        assertEquals("Unexpected value for minimalValidationLevel: Unknown value", e.getMessage());
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
}
