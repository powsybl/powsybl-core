/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.histo.cache;

import org.apache.commons.io.input.TeeInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * For backward compatibility.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TwoLevelsHistoDbCache implements HistoDbCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(TwoLevelsHistoDbCache.class);

    private final HistoDbCache cache1;

    private final HistoDbCache cache2;

    public TwoLevelsHistoDbCache(HistoDbCache cache1, HistoDbCache cache2) {
        this.cache1 = cache1;
        this.cache2 = cache2;
    }

    @Override
    public InputStream getData(String url) throws IOException {
        InputStream is = cache1.getData(url);
        if (is == null) {
            is = cache2.getData(url);
            if (is != null) {
                // synchronize with the first cache
                OutputStream os = cache1.putData(url);
                is = new TeeInputStream(is, os, true);
            }
        }
        return is;
    }

    @Override
    public OutputStream putData(String url) throws IOException {
        return cache1.putData(url);
    }

    @Override
    public List<String> listUrls() throws IOException {
        Set<String> urls = new LinkedHashSet<>();
        urls.addAll(cache1.listUrls());
        urls.addAll(cache2.listUrls());
        return new ArrayList<>(urls);
    }

    @Override
    public void close() throws Exception {
        try {
            cache1.close();
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
        try {
            cache2.close();
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
    }

}
