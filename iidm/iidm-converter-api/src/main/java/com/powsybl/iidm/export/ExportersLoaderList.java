/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.export;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class ExportersLoaderList implements ExportersLoader {

    private final List<Exporter> exporters;

    private final List<ExportPostProcessor> exportPostProcessors;

    public ExportersLoaderList(Exporter... exporters) {
        this(Arrays.asList(exporters));
    }

    public ExportersLoaderList(List<Exporter> exporters) {
        this(exporters, Collections.emptyList());
    }

    public ExportersLoaderList(List<Exporter> exporters, List<ExportPostProcessor> exportPostProcessors) {
        this.exporters = Objects.requireNonNull(exporters);
        this.exportPostProcessors = Objects.requireNonNull(exportPostProcessors);
    }

    @Override
    public List<Exporter> loadExporters() {
        return exporters;
    }

    @Override
    public List<ExportPostProcessor> loadPostProcessors() {
        return exportPostProcessors;
    }
}
