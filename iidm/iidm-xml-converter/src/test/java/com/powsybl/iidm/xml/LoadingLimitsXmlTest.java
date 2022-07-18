/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.ComparisonUtils;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.export.ExportOptions;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.DanglingLineNetworkFactory;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.function.Supplier;

import static com.powsybl.iidm.xml.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class LoadingLimitsXmlTest extends AbstractXmlConverterTest {

    @Test
    public void testDanglingLine() throws IOException {
        Network network = DanglingLineNetworkFactory.create();
        network.setCaseDate(DateTime.parse("2013-01-15T18:45:00.000+01:00"));
        DanglingLine dl = network.getDanglingLine("DL");
        createLoadingLimits(dl::newActivePowerLimits);
        createLoadingLimits(dl::newApparentPowerLimits);
        createLoadingLimits(dl::newCurrentLimits);
        roundTripXmlTest(network,
                NetworkXml::writeAndValidate,
                NetworkXml::validateAndRead,
                getVersionedNetworkPath("dl-loading-limits.xml", CURRENT_IIDM_XML_VERSION));

        // backward compatibility from version 1.5
        roundTripVersionedXmlFromMinToCurrentVersionTest("dl-loading-limits.xml", IidmXmlVersion.V_1_5);

        // check that it fails for versions previous to 1.5
        testForAllPreviousVersions(IidmXmlVersion.V_1_5, version -> {
            try {
                ExportOptions options = new ExportOptions().setVersion(version.toString("."));
                NetworkXml.write(network, options, tmpDir.resolve("fail"));
                fail();
            } catch (PowsyblException e) {
                assertEquals("danglingLine.activePowerLimits is not null and not supported for IIDM-XML version " + version.toString(".") + ". IIDM-XML version should be >= 1.5",
                        e.getMessage());
            }
        });

        // check that it doesn't fail for versions previous to 1.5 when log error is the IIDM version incompatibility behavior
        testForAllPreviousVersions(IidmXmlVersion.V_1_5, version -> {
            try {
                writeXmlTest(network, (n, p) -> write(n, p, version), getVersionedNetworkPath("dl-loading-limits.xml", version));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    @Test
    public void testEurostag() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(DateTime.parse("2013-01-15T18:45:00.000+01:00"));
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
        roundTripTest(network,
                NetworkXml::writeAndValidate,
                NetworkXml::validateAndRead,
                getVersionedNetworkPath("eurostag-loading-limits.xml", CURRENT_IIDM_XML_VERSION));

        // backward compatibility from version 1.5
        roundTripVersionedXmlFromMinToCurrentVersionTest("eurostag-loading-limits.xml", IidmXmlVersion.V_1_5);

        // check that it fails for versions previous to 1.5
        testForAllPreviousVersions(IidmXmlVersion.V_1_5, version -> {
            try {
                ExportOptions options = new ExportOptions().setVersion(version.toString("."));
                NetworkXml.write(network, options, tmpDir.resolve("fail"));
                fail();
            } catch (PowsyblException e) {
                assertEquals("twoWindingsTransformer.activePowerLimits1 is not null and not supported for IIDM-XML version " + version.toString(".") + ". IIDM-XML version should be >= 1.5",
                        e.getMessage());
            }
        });

        // check that it doesn't fail for versions previous to 1.5 when log error is the IIDM version incompatibility behavior
        testForAllPreviousVersions(IidmXmlVersion.V_1_5, version -> {
            try {
                writeTest(network, (n, p) -> write(n, p, version), ComparisonUtils::compareTxt, getVersionedNetworkPath("eurostag-loading-limits.xml", version));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    @Test
    public void testTieLine() throws IOException {
        Network network = NetworkXml.read(getVersionedNetworkAsStream("tieline.xml", CURRENT_IIDM_XML_VERSION));
        TieLine tl = (TieLine) network.getLine("NHV1_NHV2_1");
        createLoadingLimits(tl::newActivePowerLimits1);
        createLoadingLimits(tl::newActivePowerLimits2);
        createLoadingLimits(tl::newApparentPowerLimits1);
        createLoadingLimits(tl::newApparentPowerLimits2);
        createLoadingLimits(tl::newCurrentLimits1);
        createLoadingLimits(tl::newCurrentLimits2);
        roundTripXmlTest(network,
                NetworkXml::writeAndValidate,
                NetworkXml::validateAndRead,
                getVersionedNetworkPath("tl-loading-limits.xml", CURRENT_IIDM_XML_VERSION));

        // backward compatibility from version 1.5
        roundTripVersionedXmlFromMinToCurrentVersionTest("tl-loading-limits.xml", IidmXmlVersion.V_1_5);

        // check that it fails for versions previous to 1.5
        testForAllPreviousVersions(IidmXmlVersion.V_1_5, version -> {
            try {
                ExportOptions options = new ExportOptions().setVersion(version.toString("."));
                NetworkXml.write(network, options, tmpDir.resolve("fail"));
                fail();
            } catch (PowsyblException e) {
                assertEquals("tieLine.activePowerLimits1 is not null and not supported for IIDM-XML version " + version.toString(".") + ". IIDM-XML version should be >= 1.5",
                        e.getMessage());
            }
        });

        // check that it doesn't fail for versions previous to 1.5 when log error is the IIDM version incompatibility behavior
        testForAllPreviousVersions(IidmXmlVersion.V_1_5, version -> {
            try {
                writeXmlTest(network, (n, p) -> write(n, p, version), getVersionedNetworkPath("tl-loading-limits.xml", version));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    @Test
    public void testThreeWindingsTransformer() throws IOException {
        Network network = ThreeWindingsTransformerNetworkFactory.createWithCurrentLimits();
        ThreeWindingsTransformer twt = network.getThreeWindingsTransformer("3WT");
        createLoadingLimits(() -> twt.getLeg1().newActivePowerLimits());
        createLoadingLimits(() -> twt.getLeg1().newApparentPowerLimits());
        createLoadingLimits(() -> twt.getLeg2().newActivePowerLimits());
        createLoadingLimits(() -> twt.getLeg2().newApparentPowerLimits());
        createLoadingLimits(() -> twt.getLeg3().newActivePowerLimits());
        createLoadingLimits(() -> twt.getLeg3().newApparentPowerLimits());
        roundTripXmlTest(network,
                NetworkXml::writeAndValidate,
                NetworkXml::validateAndRead,
                getVersionedNetworkPath("t3w-loading-limits.xml", CURRENT_IIDM_XML_VERSION));

        // backward compatibility from version 1.5
        roundTripVersionedXmlFromMinToCurrentVersionTest("t3w-loading-limits.xml", IidmXmlVersion.V_1_5);

        // check that it fails for versions previous to 1.5
        testForAllPreviousVersions(IidmXmlVersion.V_1_5, version -> {
            try {
                ExportOptions options = new ExportOptions().setVersion(version.toString("."));
                NetworkXml.write(network, options, tmpDir.resolve("fail"));
                fail();
            } catch (PowsyblException e) {
                assertEquals("threeWindingsTransformer.activePowerLimits1 is not null and not supported for IIDM-XML version " + version.toString(".") + ". IIDM-XML version should be >= 1.5",
                        e.getMessage());
            }
        });

        // check that it doesn't fail for versions previous to 1.5 when log error is the IIDM version incompatibility behavior
        testForAllPreviousVersions(IidmXmlVersion.V_1_5, version -> {
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

    private static void write(Network network, Path path, IidmXmlVersion version) {
        ExportOptions options = new ExportOptions().setIidmVersionIncompatibilityBehavior(ExportOptions.IidmVersionIncompatibilityBehavior.LOG_ERROR)
                .setVersion(version.toString("."));
        NetworkXml.write(network, options, path);
    }
}
