/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ampl.executor;

import com.powsybl.ampl.converter.AmplNetworkUpdaterFactory;
import com.powsybl.ampl.converter.AmplReadableElement;
import org.apache.commons.lang3.tuple.Pair;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Nicolas Pierre {@literal <nicolas.pierre@artelys.com>}
 */
public class DummyAmplModel extends AbstractAmplModel {

    @Override
    public List<Pair<String, InputStream>> getModelAsStream() {
        return List.of(
                Pair.of("sample_model.run", this.getClass().getClassLoader().getResourceAsStream("sample_model.run")));
    }

    @Override
    public List<String> getAmplRunFiles() {
        return Collections.emptyList();
    }

    @Override
    public String getOutputFilePrefix() {
        return "output";
    }

    @Override
    public AmplNetworkUpdaterFactory getNetworkUpdaterFactory() {
        return (mapper, network) -> new DummyAmplNetworkUpdater();
    }

    @Override
    public String getNetworkDataPrefix() {
        return "network";
    }

    @Override
    public Collection<AmplReadableElement> getAmplReadableElement() {
        return Collections.singleton(AmplReadableElement.GENERATOR);
    }

    @Override
    public boolean checkModelConvergence(Map<String, String> metrics) {
        return metrics.get("STATUS").equals("OK");
    }

}
