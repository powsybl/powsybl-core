/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.iidm.network.Network;
import com.powsybl.triplestore.api.TripleStore;

/**
 * <p>
 * Interface for CGMES post-processor plugins. All CGMES post-processors have to implement this interface and implementation
 * class has to be annotated with <code>@AutoService(CgmesImportPostProcessor.class)</code>.
 * </p>
 * Example:
 * <pre>
 *   {@literal @}AutoService(CgmesImportPostProcessor.class)
 *    public class MyPlugin implements CgmesImportPostProcessor {
 *
 *       {@literal @}Override
 *        public String getName() {
 *            return "MyPlugin";
 *        }
 *
 *       {@literal @}Override
 *        public void process(Network network, TripleStore tripleStore) {
 *            ...
 *        }
 *    }
 * </pre>
 *
 * @see java.util.ServiceLoader
 * @see com.google.auto.service.AutoService
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface CgmesImportPostProcessor {

    /**
     * Get post processor name. It has to be unique among all CGMES post-processors.
     * @return post processor name
     */
    String getName();

    /**
     * Method called after all base data have been processed. It is called one time per CGMES conversion.
     * It is expected in this method to query triple store for additional data and to attach IIDM extensions to network.
     *
     * @param network the IIDM network model
     * @param tripleStore the triple store
     */
    void process(Network network, TripleStore tripleStore);
}
