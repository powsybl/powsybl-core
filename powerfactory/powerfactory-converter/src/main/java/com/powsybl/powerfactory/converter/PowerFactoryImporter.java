/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.powerfactory.converter;

import com.google.auto.service.AutoService;
import com.google.common.base.Stopwatch;
import com.google.common.io.ByteStreams;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.network.Importer;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.network.util.ContainersMapping;
import com.powsybl.powerfactory.converter.AbstractConverter.NodeRef;
import com.powsybl.powerfactory.model.DataObject;
import com.powsybl.powerfactory.model.PowerFactoryDataLoader;
import com.powsybl.powerfactory.model.PowerFactoryException;
import com.powsybl.powerfactory.model.StudyCase;
import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(Importer.class)
public class PowerFactoryImporter implements Importer {

    private static final Logger LOGGER = LoggerFactory.getLogger(PowerFactoryImporter.class);

    private static final String FORMAT = "POWER-FACTORY";

    @Override
    public String getFormat() {
        return FORMAT;
    }

    @Override
    public String getComment() {
        return "PowerFactory to IIDM converter";
    }

    private Optional<PowerFactoryDataLoader<StudyCase>> findProjectLoader(ReadOnlyDataSource dataSource) {
        for (PowerFactoryDataLoader<StudyCase> studyCaseLoader : PowerFactoryDataLoader.find(StudyCase.class)) {
            try {
                if (dataSource.exists(null, studyCaseLoader.getExtension())) {
                    return Optional.of(studyCaseLoader);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean exists(ReadOnlyDataSource dataSource) {
        return findProjectLoader(dataSource).filter(studyCaseLoader -> {
            try (InputStream is = dataSource.newInputStream(null, studyCaseLoader.getExtension())) {
                return studyCaseLoader.test(is);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }).isPresent();
    }

    @Override
    public void copy(ReadOnlyDataSource fromDataSource, DataSource toDataSource) {
        Objects.requireNonNull(fromDataSource);
        Objects.requireNonNull(toDataSource);
        findProjectLoader(fromDataSource).ifPresent(studyCaseLoader -> {
            try (InputStream is = fromDataSource.newInputStream(null, studyCaseLoader.getExtension());
                 OutputStream os = toDataSource.newOutputStream(null, studyCaseLoader.getExtension(), false)) {
                ByteStreams.copy(is, os);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    static class ImportContext {

        final ContainersMapping containerMapping;

        // object id to node (IIDM node/breaker node) mapping
        final Map<Long, List<NodeRef>> objIdToNode = new HashMap<>();

        final Map<String, MutableInt> nodeCountByVoltageLevelId = new HashMap<>();

        // elmTerm object id to busbarSection id mapping
        final Map<Long, NodeRef> elmTermIdToNode = new HashMap<>();

        List<DataObject> cubiclesObjectNotFound = new ArrayList<>();

        ImportContext(ContainersMapping containerMapping) {
            this.containerMapping = containerMapping;
        }
    }

    private Network createNetwork(StudyCase studyCase, NetworkFactory networkFactory) {
        Network network = networkFactory.createNetwork(studyCase.getName(), FORMAT);

        List<DataObject> elmNets = studyCase.getElmNets();
        if (elmNets.isEmpty()) {
            throw new PowerFactoryException("No ElmNet object found");
        }
        LOGGER.info("Study case has {} network(s): {}", elmNets.size(), elmNets.stream().map(DataObject::getLocName).toList());

        // case date
        ZonedDateTime caseDate = ZonedDateTime.ofInstant(studyCase.getTime(), ZoneId.systemDefault());
        network.setCaseDate(caseDate);

        List<DataObject> elmTerms = studyCase.getElmNets().stream()
                .flatMap(elmNet -> elmNet.search(".*.ElmTerm").stream())
                .collect(Collectors.toList());

        LOGGER.info("Creating containers...");

        ContainersMapping containerMapping = ContainersMappingHelper.create(studyCase.getIndex(), elmTerms);
        ImportContext importContext = new ImportContext(containerMapping);

        LOGGER.info("Creating topology graphs...");

        // Identify Hvdc configurations
        List<DataObject> elmVscs = studyCase.getElmNets().stream()
            .flatMap(elmNet -> elmNet.search(".*.ElmVsc").stream())
            .collect(Collectors.toList());

        HvdcConverter hvdcConverter = new HvdcConverter(importContext, network);
        hvdcConverter.computeConfigurations(elmTerms, elmVscs);

        // process terminals
        for (DataObject elmTerm : elmTerms) {
            if (!hvdcConverter.isDcNode(elmTerm)) {
                new NodeConverter(importContext, network).createAndMapConnectedObjs(elmTerm);
            }
        }

        if (!importContext.cubiclesObjectNotFound.isEmpty()) {
            LOGGER.warn("{} cubicles have a missing connected object", importContext.cubiclesObjectNotFound.size());
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Cubicles with missing connected object: {}", importContext.cubiclesObjectNotFound);
            }
        }

        LOGGER.info("Creating equipment...");

        List<DataObject> slackObjects = new ArrayList<>();

        // Create main equipment
        convertEquipment(studyCase, importContext, hvdcConverter, network, slackObjects);

        // Create Hvdc Links
        hvdcConverter.create();

        // Attach a slack bus
        new SlackConverter(importContext, network).create(slackObjects);

        LOGGER.info("{} substations, {} voltage levels, {} lines, {} 2w-transformers, {} 3w-transformers, {} generators, {} loads, {} shunts have been created",
                network.getSubstationCount(), network.getVoltageLevelCount(), network.getLineCount(), network.getTwoWindingsTransformerCount(),
                network.getThreeWindingsTransformerCount(), network.getGeneratorCount(), network.getLoadCount(), network.getShuntCompensatorCount());

        setVoltagesAndAngles(network, importContext, elmTerms);

        return network;
    }

    private static void convertEquipment(StudyCase studyCase, ImportContext importContext, HvdcConverter hvdcConverter,
        Network network, List<DataObject> slackObjects) {
        var objs = studyCase.getElmNets().stream()
            .flatMap(elmNet -> elmNet.search(".*").stream())
            .toList();
        for (DataObject obj : objs) {
            switch (obj.getDataClassName()) {
                case "ElmCoup":
                    new SwitchConverter(importContext, network).createFromElmCoup(obj);
                    break;

                case "ElmSym":
                case "ElmAsm":
                case "ElmGenstat":
                    new GeneratorConverter(importContext, network).create(obj);
                    if (GeneratorConverter.isSlack(obj)) {
                        slackObjects.add(obj);
                    }
                    break;

                case "ElmLod":
                    new LoadConverter(importContext, network).create(obj);
                    break;

                case "ElmShnt":
                    new ShuntConverter(importContext, network).create(obj);
                    break;

                case "ElmLne":
                    if (!hvdcConverter.isDcLink(obj)) {
                        new LineConverter(importContext, network).create(obj);
                    }
                    break;
                case "ElmTow":
                    new LineConverter(importContext, network).createTower(obj);
                    break;

                case "ElmTr2":
                    new TransformerConverter(importContext, network).createTwoWindings(obj);
                    break;

                case "ElmTr3":
                    new TransformerConverter(importContext, network).createThreeWindings(obj);
                    break;
                case "ElmZpu":
                    new CommonImpedanceConverter(importContext, network).create(obj);
                    break;

                case "ElmNet":
                case "ElmSubstat":
                case "ElmTrfstat":
                case "StaCubic":
                case "StaSwitch":
                case DataAttributeNames.ELMTERM:
                    // already processed
                    break;

                case "TypLne":
                case "TypSym":
                case "TypLod":
                case "TypTr2":
                case "TypTr3":
                    // Referenced by other objects
                    break;

                case "BlkDef":
                case "ChaRef":
                case "ChaVec":

                case "ElmArea":
                case "ElmBmu":
                case "ElmBoundary":
                case "ElmBranch":
                case "ElmComp":
                case "ElmDcubi":
                case "ElmDsl":
                case "ElmFile":
                case "ElmPhi__pll":
                case "ElmRelay":
                case "ElmSecctrl":
                case "ElmSite":
                case "ElmStactrl":
                case "ElmValve":
                case "ElmVsc":
                case "ElmZone":

                case "IntCalcres":
                case "IntCondition":
                case "IntEvt":
                case "IntEvtrel":
                case "IntFolder":
                case "IntForm":
                case "IntGate":
                case "IntGrf":
                case "IntGrfcon":
                case "IntGrflayer":
                case "IntGrfnet":

                case "IntMat":
                case "IntMon":
                case "IntQlim":
                case "IntRas":
                case "IntRef":
                case "IntTemplate":
                case "IntWdt":

                case "OptElmgenstat":
                case "OptElmrecmono":
                case "OptElmsym":

                case "RelChar":
                case "RelDir":
                case "RelDisdir":
                case "RelDisloadenc":
                case "RelDismho":
                case "RelDispoly":
                case "RelDispspoly":
                case "RelFdetabb":
                case "RelFdetaegalst":
                case "RelFdetect":
                case "RelFdetsie":
                case "RelFmeas":
                case "RelFrq":
                case "RelIoc":
                case "RelLogdip":
                case "RelLogic":
                case "RelLslogic":

                case "RelMeasure":
                case "RelRecl":
                case "RelSeldir":
                case "RelTimer":
                case "RelToc":
                case "RelUlim":
                case "RelZpol":

                case "StaCt":
                case "StaPqmea":
                case "StaVmea":
                case "StaVt":

                case "TypChatoc":
                case "TypCon":
                case "TypCt":
                case "TypRelay":
                case "TypVt":

                    // not interesting
                    break;

                default:
                    LOGGER.warn("Unexpected data class '{}' ('{}')", obj.getDataClassName(), obj);
            }
        }
    }

    private static void setVoltagesAndAngles(Network network, ImportContext importContext, List<DataObject> elmTerms) {
        VoltageAndAngle va = new VoltageAndAngle(importContext, network);
        for (DataObject elmTerm : elmTerms) {
            va.update(elmTerm);
        }
    }

    @Override
    public Network importData(ReadOnlyDataSource dataSource, NetworkFactory networkFactory, Properties parameters) {
        return findProjectLoader(dataSource).map(studyCaseLoader -> {
            LOGGER.info("Starting PowerFactory import...");
            Stopwatch stopwatch = Stopwatch.createStarted();
            try (InputStream is = dataSource.newInputStream(null, studyCaseLoader.getExtension())) {
                StudyCase studyCase = studyCaseLoader.doLoad(dataSource.getBaseName(), is);
                return createNetwork(studyCase, networkFactory);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } finally {
                stopwatch.stop();
                LOGGER.info("PowerFactory import done in {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
            }
        }).orElseThrow(() -> new PowerFactoryException("This is not a supported PowerFactory file"));
    }
}
