/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.*;
import com.powsybl.contingency.contingency.list.ContingencyList;
import com.powsybl.contingency.contingency.list.ContingencyListsList;
import com.powsybl.contingency.contingency.list.DefaultContingencyList;
import com.powsybl.contingency.contingency.list.LineCriterionContingencyList;
import com.powsybl.contingency.contingency.list.criterion.SingleNominalVoltageCriterion;
import com.powsybl.contingency.contingency.list.criterion.TwoCountriesCriterion;
import com.powsybl.iidm.network.Country;
import org.junit.Test;

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
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class ContingencyListsListJsonTest extends AbstractConverterTest {

    private static ContingencyListsList create() {
        List<ContingencyList> contingencyLists = new ArrayList<>();
        TwoCountriesCriterion countriesCriterion = new TwoCountriesCriterion(Collections.singletonList(Country.FR),
                Collections.singletonList(Country.BE));
        SingleNominalVoltageCriterion nominalVoltageCriterion = new SingleNominalVoltageCriterion(new SingleNominalVoltageCriterion
                .VoltageInterval(200.0, 230.0, true, true));
        contingencyLists.add(new LineCriterionContingencyList("list1", countriesCriterion, nominalVoltageCriterion,
                null));
        contingencyLists.add(new DefaultContingencyList(new Contingency("contingency1", new GeneratorContingency("GEN"))));
        return new ContingencyListsList("listslist1", contingencyLists);
    }

    @Test
    public void roundTripTest() throws IOException {
        roundTripTest(create(), ContingencyListsListJsonTest::write, ContingencyListsListJsonTest::readContingencyList,
                "/contingencyListsList.json");
    }

    private static ContingencyListsList readContingencyList(Path jsonFile) {
        return read(jsonFile, ContingencyListsList.class);
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
