/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.json;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.powsybl.contingency.*;
import com.powsybl.contingency.contingency.list.criterion.Criterion;
import com.powsybl.contingency.contingency.list.identifiant.Identifier;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
public class ContingencyJsonModule extends SimpleModule {

    public ContingencyJsonModule() {
        addSerializer(Criterion.class, new CriterionSerializer());
        addSerializer(CriterionContingencyList.class, new CriterionContingencyListSerializer());
        addSerializer(DefaultContingencyList.class, new DefaultContingencyListSerializer());
        addSerializer(Contingency.class, new ContingencySerializer());
        addSerializer(ContingencyElement.class, new ContingencyElementSerializer());
        addSerializer(ContingencyListsList.class, new ContingencyListsListSerializer());
        addSerializer(IdentifierContingencyList.class, new IdentifierContingencyListSerializer());
        addSerializer(Identifier.class, new IdentifierSerializer());

        addDeserializer(Criterion.class, new CriterionDeserializer());
        addDeserializer(CriterionContingencyList.class, new CriterionContingencyListDeserializer());
        addDeserializer(DefaultContingencyList.class, new DefaultContingencyListDeserializer());
        addDeserializer(Contingency.class, new ContingencyDeserializer());
        addDeserializer(ContingencyElement.class, new ContingencyElementDeserializer());
        addDeserializer(ContingencyListsList.class, new ContingencyListsListDeserializer());
        addDeserializer(ContingencyList.class, new ContingencyListDeserializer());
        addDeserializer(IdentifierContingencyList.class, new IdentifierContingencyListDeserializer());
        addDeserializer(Identifier.class, new IdentifierDeserializer());
    }
}
