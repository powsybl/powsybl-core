/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.GeneratorContingency;
import com.powsybl.contingency.HvdcLineContingency;
import com.powsybl.contingency.contingency.list.*;
import com.powsybl.contingency.contingency.list.identifier.IdBasedNetworkElementIdentifier;
import com.powsybl.contingency.contingency.list.identifier.NetworkElementIdentifier;
import com.powsybl.contingency.contingency.list.identifier.NetworkElementIdentifierList;
import com.powsybl.contingency.contingency.list.identifier.VoltageLevelAndOrderNetworkElementIdentifier;
import com.powsybl.iidm.criteria.*;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.IdentifiableType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
class ListOfContingencyListsJsonTest extends AbstractSerDeTest {

    private static ListOfContingencyLists create() {
        List<ContingencyList> contingencyLists = new ArrayList<>();
        TwoCountriesCriterion countriesCriterion = new TwoCountriesCriterion(Collections.singletonList(Country.FR),
                Collections.singletonList(Country.BE));
        SingleCountryCriterion countryCriterion = new SingleCountryCriterion(Collections.singletonList(Country.DE));
        SingleNominalVoltageCriterion nominalVoltageCriterion = new SingleNominalVoltageCriterion(new SingleNominalVoltageCriterion
                .VoltageInterval(200.0, 230.0, true, true));
        TwoNominalVoltageCriterion twoNominalVoltageCriterion = new TwoNominalVoltageCriterion(new SingleNominalVoltageCriterion
                .VoltageInterval(200.0, 230.0, true, true),
                new SingleNominalVoltageCriterion
                .VoltageInterval(100.0, 120.0, true, true));
        TwoNominalVoltageCriterion twoNominalVoltageCriterion1 = new TwoNominalVoltageCriterion(new SingleNominalVoltageCriterion
                .VoltageInterval(200.0, 230.0, true, true), null);

        ThreeNominalVoltageCriterion threeNominalVoltageCriterion = new ThreeNominalVoltageCriterion(new SingleNominalVoltageCriterion
                .VoltageInterval(200.0, 230.0, true, true), null,
                new SingleNominalVoltageCriterion.VoltageInterval(380.0, 430.0,
                        true, true));
        RegexCriterion regexCriterion = new RegexCriterion("regex");
        contingencyLists.add(new LineCriterionContingencyList("list1", countriesCriterion, twoNominalVoltageCriterion1,
                Collections.emptyList(), regexCriterion));
        contingencyLists.add(new DefaultContingencyList(new Contingency("contingency1", new GeneratorContingency("GEN"))));
        contingencyLists.add(new InjectionCriterionContingencyList("list3", IdentifiableType.LOAD, countryCriterion,
                nominalVoltageCriterion, Collections.emptyList(), null));
        contingencyLists.add(new HvdcLineCriterionContingencyList("list4", countriesCriterion,
                twoNominalVoltageCriterion, Collections.emptyList(), null));
        contingencyLists.add(new TwoWindingsTransformerCriterionContingencyList("list5", countryCriterion,
                twoNominalVoltageCriterion, Collections.emptyList(), null));
        contingencyLists.add(new ThreeWindingsTransformerCriterionContingencyList("list6", countryCriterion,
                threeNominalVoltageCriterion, Collections.emptyList(), null));
        contingencyLists.add(new ListOfContingencyLists("listslist2",
                Collections.singletonList(new DefaultContingencyList(new Contingency("contingency2",
                        new HvdcLineContingency("HVDC1"))))));
        List<NetworkElementIdentifier> networkElementIdentifiers = new ArrayList<>();
        networkElementIdentifiers.add(new VoltageLevelAndOrderNetworkElementIdentifier("VL1",
                "VL2", '1', "contingency1"));
        networkElementIdentifiers.add(new IdBasedNetworkElementIdentifier("identifier", "contingency2"));
        networkElementIdentifiers.add(new NetworkElementIdentifierList(Collections
                .singletonList(new IdBasedNetworkElementIdentifier("identifier2")), "contingency3"));
        contingencyLists.add(new IdentifierContingencyList("identifierlist", networkElementIdentifiers));
        contingencyLists.add(new TieLineCriterionContingencyList("tieLineContingencyList",
            new TwoCountriesCriterion(Collections.singletonList(Country.FR)), nominalVoltageCriterion,
            Collections.emptyList(), null));
        return new ListOfContingencyLists("listslist1", contingencyLists);
    }

    @Test
    void roundTripTest() throws IOException {
        roundTripTest(create(), ListOfContingencyListsJsonTest::write, ListOfContingencyListsJsonTest::readContingencyList,
                "/contingencyListsList.json");
    }

    private static ListOfContingencyLists readContingencyList(Path jsonFile) {
        return read(jsonFile, ListOfContingencyLists.class);
    }

    private static <T> T read(Path jsonFile, Class<T> clazz) {
        Objects.requireNonNull(jsonFile);

        try (InputStream is = Files.newInputStream(jsonFile)) {
            ObjectMapper objectMapper = JsonUtil.createObjectMapper();
            ContingencyJsonModule module = new ContingencyJsonModule();
            objectMapper.registerModule(module);

            return (T) objectMapper.readValue(is, clazz);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static <T> void write(T object, Path jsonFile) {
        Objects.requireNonNull(object);
        Objects.requireNonNull(jsonFile);

        try (OutputStream os = Files.newOutputStream(jsonFile)) {
            ObjectMapper mapper = JsonUtil.createObjectMapper();
            ContingencyJsonModule module = new ContingencyJsonModule();
            mapper.registerModule(module);

            ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
            writer.writeValue(os, object);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
