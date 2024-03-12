/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.json;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.powsybl.contingency.*;
import com.powsybl.contingency.contingency.list.*;
import com.powsybl.contingency.contingency.list.identifier.NetworkElementIdentifier;
import com.powsybl.iidm.criteria.Criterion;
import com.powsybl.iidm.criteria.json.CriterionDeserializer;
import com.powsybl.iidm.criteria.json.CriterionSerializer;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Teofil Calin BANC {@literal <teofil-calin.banc at rte-france.com>}
 */
public class ContingencyJsonModule extends SimpleModule {

    public ContingencyJsonModule() {

        addSerializer(Criterion.class, new CriterionSerializer());
        // criterion lists
        addSerializer(InjectionCriterionContingencyList.class, new InjectionCriterionContingencyListSerializer());
        addSerializer(HvdcLineCriterionContingencyList.class, new EquipmentCriterionContingencyListSerializer<>(HvdcLineCriterionContingencyList.class));
        addSerializer(LineCriterionContingencyList.class, new EquipmentCriterionContingencyListSerializer<>(LineCriterionContingencyList.class));
        addSerializer(TieLineCriterionContingencyList.class, new EquipmentCriterionContingencyListSerializer<>(TieLineCriterionContingencyList.class));
        addSerializer(TwoWindingsTransformerCriterionContingencyList.class, new EquipmentCriterionContingencyListSerializer<>(TwoWindingsTransformerCriterionContingencyList.class));
        addSerializer(ThreeWindingsTransformerCriterionContingencyList.class, new EquipmentCriterionContingencyListSerializer<>(ThreeWindingsTransformerCriterionContingencyList.class));

        addSerializer(DefaultContingencyList.class, new DefaultContingencyListSerializer());
        addSerializer(Contingency.class, new ContingencySerializer());
        addSerializer(ContingencyElement.class, new ContingencyElementSerializer());
        addSerializer(ListOfContingencyLists.class, new ListOfContingencyListsSerializer());
        addSerializer(IdentifierContingencyList.class, new IdentifierContingencyListSerializer());
        addSerializer(NetworkElementIdentifier.class, new IdentifierSerializer());

        addDeserializer(Criterion.class, new CriterionDeserializer());
        // criterion lists
        addDeserializer(InjectionCriterionContingencyList.class, new InjectionCriterionContingencyListDeserializer());
        addDeserializer(HvdcLineCriterionContingencyList.class, new HvdcLineCriterionContingencyListDeserializer());
        addDeserializer(LineCriterionContingencyList.class, new LineCriterionContingencyListDeserializer());
        addDeserializer(TieLineCriterionContingencyList.class, new TieLineCriterionContingencyListDeserializer());
        addDeserializer(TwoWindingsTransformerCriterionContingencyList.class, new TwoWindingsTransformerCriterionContingencyListDeserializer());
        addDeserializer(ThreeWindingsTransformerCriterionContingencyList.class, new ThreeWindingsTransformerCriterionContingencyListDeserializer());

        addDeserializer(ContingencyList.class, new ContingencyListDeserializer());
        addDeserializer(DefaultContingencyList.class, new DefaultContingencyListDeserializer());
        addDeserializer(Contingency.class, new ContingencyDeserializer());
        addDeserializer(ContingencyElement.class, new ContingencyElementDeserializer());
        addDeserializer(ListOfContingencyLists.class, new ListOfContingencyListsDeserializer());
        addDeserializer(IdentifierContingencyList.class, new IdentifierContingencyListDeserializer());
        addDeserializer(NetworkElementIdentifier.class, new IdentifierDeserializer());
    }
}
