/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.measurements;

import com.google.auto.service.AutoService;
import com.powsybl.cgmes.conversion.CgmesImportPostProcessor;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.*;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;
import com.powsybl.triplestore.api.TripleStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
@AutoService(CgmesImportPostProcessor.class)
public class CgmesMeasurementsPostProcessor implements CgmesImportPostProcessor {

    @Override
    public String getName() {
        return "measurements";
    }

    @Override
    public void process(Network network, TripleStore tripleStore) {
        CgmesMeasurementsModel model = new CgmesMeasurementsModel(tripleStore);
        PropertyBags bays = model.bays();
        for (PropertyBag analog : model.analogs()) {
            CgmesAnalogPostProcessor.process(network, analog.getId("Analog"), analog.getId("Terminal"), analog.getId("powerSystemResource"), analog.getId("type"), bays);
        }
        for (PropertyBag discrete : model.discretes()) {
            CgmesDiscretePostProcessor.process(network, discrete.getId("Discrete"), discrete.getId("Terminal"), discrete.getId("powerSystemResource"), discrete.getId("type"), bays);
        }
    }
}
