/**
 * Copyright (c) 2021-2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe.network.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.dataframe.network.ExtensionInformation;
import com.powsybl.dataframe.network.NetworkDataframeMapper;
import com.powsybl.dataframe.network.NetworkDataframeMapperBuilder;
import com.powsybl.dataframe.network.adders.NetworkElementAdder;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.GeneratorEntsoeCategory;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
@AutoService(NetworkExtensionDataframeProvider.class)
public class GeneratorEntsoeCategoryDataframeProvider extends AbstractSingleDataframeNetworkExtension {

    @Override
    public String getExtensionName() {
        return GeneratorEntsoeCategory.NAME;
    }

    @Override
    public ExtensionInformation getExtensionInformation() {
        return new ExtensionInformation(GeneratorEntsoeCategory.NAME,
            "Provides Entsoe category code for a generator", "index : id (str), code (int)");
    }

    private Stream<GeneratorEntsoeCategory> itemsStream(Network network) {
        return network.getGeneratorStream()
            .map(g -> (GeneratorEntsoeCategory) g.getExtension(GeneratorEntsoeCategory.class))
            .filter(Objects::nonNull);
    }

    private GeneratorEntsoeCategory getOrThrow(Network network, String id) {
        Generator gen = network.getGenerator(id);
        if (gen == null) {
            throw new PowsyblException("Generator '" + id + "' not found");
        }
        GeneratorEntsoeCategory gec = gen.getExtension(GeneratorEntsoeCategory.class);
        if (gec == null) {
            throw new PowsyblException("Generator '" + id + "' has no GeneratorEntsoeCategory extension");
        }
        return gec;
    }

    @Override
    public NetworkDataframeMapper createMapper() {
        return NetworkDataframeMapperBuilder.ofStream(this::itemsStream, this::getOrThrow)
            .stringsIndex("id", ext -> ext.getExtendable().getId())
            .ints("code", GeneratorEntsoeCategory::getCode, GeneratorEntsoeCategory::setCode)
            .build();
    }

    @Override
    public void removeExtensions(Network network, List<String> ids) {
        ids.stream().filter(Objects::nonNull)
            .map(id -> network.getGenerator(id))
            .filter(Objects::nonNull)
            .forEach(g -> g.removeExtension(GeneratorEntsoeCategory.class));
    }

    @Override
    public NetworkElementAdder createAdder() {
        return new GeneratorEntsoeCategoryDataframeAdder();
    }
}
