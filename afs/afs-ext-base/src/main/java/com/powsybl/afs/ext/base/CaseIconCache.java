/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base;

import com.powsybl.afs.FileIcon;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.import_.ImportConfig;
import com.powsybl.iidm.import_.Importer;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.import_.ImportersLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public enum CaseIconCache {
    INSTANCE;

    private Map<String, FileIcon> cache;

    public synchronized FileIcon get(ImportersLoader loader, ComputationManager computationManager, String format) {
        if (cache == null) {
            cache = new HashMap<>();
            for (Importer importer : Importers.list(loader, computationManager, new ImportConfig())) {
                try (InputStream is = importer.get16x16Icon()) {
                    cache.put(importer.getFormat(), new FileIcon(importer.getFormat(), is));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }
        return cache.get(format);
    }
}
