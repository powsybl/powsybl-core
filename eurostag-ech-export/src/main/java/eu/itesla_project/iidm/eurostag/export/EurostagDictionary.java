/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.eurostag.export;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import eu.itesla_project.eurostag.network.Esg8charName;
import eu.itesla_project.eurostag.network.EsgBranchName;
import eu.itesla_project.iidm.network.*;
import eu.itesla_project.iidm.network.util.Identifiables;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class EurostagDictionary {

    private static final EurostagNamingStrategy NAMING_STRATEGY = new CutEurostagNamingStrategy();

    private final BiMap<String, String> iidmId2esgId;

    public static EurostagDictionary create(Network network, BranchParallelIndexes parallelIndexes, EurostagEchExportConfig config) {
        EurostagDictionary dictionary = new EurostagDictionary();

        dictionary.addIfNotExist(EchUtil.FAKE_NODE_NAME1, EchUtil.FAKE_NODE_NAME1);
        dictionary.addIfNotExist(EchUtil.FAKE_NODE_NAME2, EchUtil.FAKE_NODE_NAME2);

        Set<String> busIds = Identifiables.sort(EchUtil.getBuses(network, config)).stream().map(Bus::getId).collect(Collectors.toSet());
        Set<String> loadIds = new LinkedHashSet<>();
        Identifiables.sort(network.getDanglingLines()).forEach(dl -> {
            busIds.add(EchUtil.getBusId(dl));
            loadIds.add(EchUtil.getLoadId(dl));
        });
        Identifiables.sort(network.getLoads()).forEach(l -> loadIds.add(l.getId()));
        Set<String> generatorIds = Identifiables.sort(network.getGenerators()).stream().map(Generator::getId).collect(Collectors.toSet());
        Set<String> shuntIds = Identifiables.sort(network.getShunts()).stream().map(ShuntCompensator::getId).collect(Collectors.toSet());
        Set<String> svcIds = Identifiables.sort(network.getStaticVarCompensators()).stream().map(StaticVarCompensator::getId).collect(Collectors.toSet());

        NAMING_STRATEGY.fillDictionary(dictionary, EurostagNamingStrategy.NameType.NODE, busIds);
        NAMING_STRATEGY.fillDictionary(dictionary, EurostagNamingStrategy.NameType.GENERATOR, generatorIds);
        NAMING_STRATEGY.fillDictionary(dictionary, EurostagNamingStrategy.NameType.LOAD, loadIds);
        NAMING_STRATEGY.fillDictionary(dictionary, EurostagNamingStrategy.NameType.BANK, shuntIds);
        NAMING_STRATEGY.fillDictionary(dictionary, EurostagNamingStrategy.NameType.SVC, svcIds);

        for (DanglingLine dl : Identifiables.sort(network.getDanglingLines())) {
            ConnectionBus bus1 = ConnectionBus.fromTerminal(dl.getTerminal(), config, EchUtil.FAKE_NODE_NAME1);
            ConnectionBus bus2 = new ConnectionBus(true, EchUtil.getBusId(dl));
            dictionary.addIfNotExist(dl.getId(), new EsgBranchName(new Esg8charName(dictionary.getEsgId(bus1.getId())),
                    new Esg8charName(dictionary.getEsgId(bus2.getId())),
                    '1').toString());
        }

        for (VoltageLevel vl : Identifiables.sort(network.getVoltageLevels())) {
            for (Switch sw : Identifiables.sort(EchUtil.getSwitches(vl, config))) {
                Bus bus1 = EchUtil.getBus1(vl, sw.getId(), config);
                Bus bus2 = EchUtil.getBus2(vl, sw.getId(), config);
                dictionary.addIfNotExist(sw.getId(),
                        new EsgBranchName(new Esg8charName(dictionary.getEsgId(bus1.getId())),
                                new Esg8charName(dictionary.getEsgId(bus2.getId())),
                                parallelIndexes.getParallelIndex(sw.getId())).toString());
            }
        }

        for (Line l : Identifiables.sort(network.getLines())) {
            ConnectionBus bus1 = ConnectionBus.fromTerminal(l.getTerminal1(), config, EchUtil.FAKE_NODE_NAME1);
            ConnectionBus bus2 = ConnectionBus.fromTerminal(l.getTerminal2(), config, EchUtil.FAKE_NODE_NAME2);
            dictionary.addIfNotExist(l.getId(), new EsgBranchName(new Esg8charName(dictionary.getEsgId(bus1.getId())),
                    new Esg8charName(dictionary.getEsgId(bus2.getId())),
                    parallelIndexes.getParallelIndex(l.getId())).toString());
        }

        for (TwoWindingsTransformer twt : Identifiables.sort(network.getTwoWindingsTransformers())) {
            ConnectionBus bus1 = ConnectionBus.fromTerminal(twt.getTerminal1(), config, EchUtil.FAKE_NODE_NAME1);
            ConnectionBus bus2 = ConnectionBus.fromTerminal(twt.getTerminal2(), config, EchUtil.FAKE_NODE_NAME2);
            dictionary.addIfNotExist(twt.getId(), new EsgBranchName(new Esg8charName(dictionary.getEsgId(bus1.getId())),
                    new Esg8charName(dictionary.getEsgId(bus2.getId())),
                    parallelIndexes.getParallelIndex(twt.getId())).toString());
        }

        for (ThreeWindingsTransformer twt : Identifiables.sort(network.getThreeWindingsTransformers())) {
            throw new AssertionError("TODO");
        }

        return dictionary;
    }

    public EurostagDictionary() {
        this(new HashMap<>());
    }

    public EurostagDictionary(Map<String, String> iidmId2esgId) {
        this.iidmId2esgId = HashBiMap.create(iidmId2esgId);
    }

    public void add(String iidmId, String esgId) {
        if (iidmId2esgId.containsKey(iidmId)) {
            throw new RuntimeException("IIDM id '" + iidmId + "' already exists in the dictionary");
        }
        iidmId2esgId.put(iidmId, esgId);
    }

    public void addIfNotExist(String iidmId, String esgId) {
        if (!iidmId2esgId.containsKey(iidmId)) {
            if (iidmId2esgId.inverse().containsKey(esgId)) {
                throw new RuntimeException("Esg id '" + esgId + "' is already associated to IIDM id '"
                        + iidmId2esgId.inverse().get(esgId) + "' impossible to associate it to IIDM id '" + iidmId + "'");
            }
            iidmId2esgId.put(iidmId, esgId);
        }
    }

    public String getEsgId(String iidmId) {
        if (!iidmId2esgId.containsKey(iidmId)) {
            throw new RuntimeException("IIDM id '" + iidmId + "' + not found in the dictionary");
        }
        return iidmId2esgId.get(iidmId);
    }

    public String getIidmId(String esgId) {
        if (!iidmId2esgId.containsValue(esgId)) {
            throw new RuntimeException("ESG id '" + esgId + "' + not found in the dictionary");
        }
        return iidmId2esgId.inverse().get(esgId);
    }

    public boolean iidmIdExists(String iidmId) {
        return iidmId2esgId.containsKey(iidmId);
    }

    public boolean esgIdExists(String esgId) {
        return iidmId2esgId.inverse().containsKey(esgId);
    }

    public Map<String, String> toMap() {
        return iidmId2esgId;
    }

    public void load(Path file) {
        try {
            try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    String[] tokens = line.split(";");
                    add(tokens[0], tokens[1]);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void dump(Path file) {
        try {
            try (BufferedWriter os = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                for (Map.Entry<String, String> entry : iidmId2esgId.entrySet()) {
                    os.write(entry.getKey() + ";" + entry.getValue() + ";");
                    os.newLine();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
