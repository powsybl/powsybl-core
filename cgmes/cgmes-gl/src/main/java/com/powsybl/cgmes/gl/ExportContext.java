/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.gl;

import java.util.Objects;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
public class ExportContext {

    private String basename;
    private String glContext;
    private String coordinateSystemId;

    public String getBasename() {
        return basename;
    }

    public void setBasename(String basename) {
        this.basename = Objects.requireNonNull(basename);
    }

    public String getGlContext() {
        return glContext;
    }

    public void setGlContext(String glContext) {
        this.glContext = Objects.requireNonNull(glContext);
    }

    public String getCoordinateSystemId() {
        return coordinateSystemId;
    }

    public void setCoordinateSystemId(String coordinateSystemId) {
        this.coordinateSystemId = Objects.requireNonNull(coordinateSystemId);
    }

}
