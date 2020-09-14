/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.iidm.mergingview.extensions.ExtensionMergingView;
import com.powsybl.iidm.network.Network;
import org.joda.time.DateTime;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
@AutoService(ExtensionMergingView.class)
public class CgmesSvMetadataMergingView implements ExtensionMergingView<Network, CgmesSvMetadata> {

    @Override
    public Class<? extends CgmesSvMetadata> getExtensionClass() {
        return CgmesSvMetadata.class;
    }

    @Override
    public String getName() {
        return "cgmesSvMetadata";
    }

    @Override
    public void merge(Network extendable, CgmesSvMetadata extension) {
        CgmesSvMetadata original = extendable.getExtension(CgmesSvMetadata.class);
        if (original == null) {
            CgmesSvMetadataAdder adder = extendable.newExtension(CgmesSvMetadataAdder.class)
                    .setScenarioTime(extension.getScenarioTime())
                    .setDescription(extension.getDescription())
                    .setModelingAuthoritySet(extension.getModelingAuthoritySet());
            extension.getDependencies().forEach(adder::addDependency);
            adder.add();
        } else {
            CgmesSvMetadataAdder adder = extendable.newExtension(CgmesSvMetadataAdder.class)
                    .setScenarioTime(getMergedScenarioTime(original.getScenarioTime(), extension.getScenarioTime()))
                    .setDescription(getMergedDescription(original.getDescription(), extension.getDescription()))
                    .setModelingAuthoritySet(original.getModelingAuthoritySet() + ";" + extension.getModelingAuthoritySet());
            original.getDependencies().forEach(adder::addDependency);
            extension.getDependencies().forEach(adder::addDependency);
            adder.add();
        }
    }

    private static String getMergedScenarioTime(String scenarioTime1, String scenarioTime2) {
        return DateTime.parse(scenarioTime1).compareTo(DateTime.parse(scenarioTime2)) < 0 ? scenarioTime2 : scenarioTime1;
    }

    private static String getMergedDescription(String description1, String description2) {
        if (description1.isEmpty()) {
            return description2;
        }
        if (description2.isEmpty()) {
            return description1;
        }
        return description1 + ", " + description2;
    }
}
