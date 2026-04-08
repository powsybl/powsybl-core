/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.model.CgmesModel;

/**
 * <p>
 * Interface for CGMES pre-processor plugins. All CGMES pre-processors have to implement this interface and implementation
 * class has to be annotated with <code>@AutoService(CgmesImportPreProcessor.class)</code>.
 * </p>
 * Example:
 * <pre>
 *   {@literal @}AutoService(CgmesImportPreProcessor.class)
 *    public class MyPlugin implements CgmesImportPreProcessor {
 *
 *       {@literal @}Override
 *        public String getName() {
 *            return "MyPlugin";
 *        }
 *
 *       {@literal @}Override
 *        public void process(CgmesModel cgmesModel) {
 *            ...
 *        }
 *    }
 * </pre>
 *
 * @see java.util.ServiceLoader
 * @see com.google.auto.service.AutoService
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public interface CgmesImportPreProcessor {

    /**
     * Get post processor name. It has to be unique among all CGMES post-processors.
     * @return post processor name
     */
    String getName();

    /**
     * Method called after initial CGMES model has been read and before starting the conversion.
     * It is called one time per CGMES conversion.
     * In this method we could fix a potentially incomplete CGMES model before the conversion starts.
     *
     * @param cgmes the CgmesModel to be processed
     */
    void process(CgmesModel cgmes);
}
