/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.histodb.client.impl;

import eu.itesla_project.commons.io.CacheManager;
import eu.itesla_project.commons.config.PlatformConfig;
import eu.itesla_project.modules.histo.cache.HistoDbCache;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class HistoDbCacheImpl implements HistoDbCache {

    private static final String DEFAULT_CACHE_NAME = "histodb";

    private final String cacheName;

    public HistoDbCacheImpl() {
        this(DEFAULT_CACHE_NAME);
    }

    public HistoDbCacheImpl(String cacheName) {
        this.cacheName = Objects.requireNonNull(cacheName);
    }

    private static Path getUrlFile(Path entryDir) {
        return entryDir.resolve("url");
    }

    private static Path getDataFile(Path entryDir) {
        return entryDir.resolve("data");
    }

    private CacheManager.CacheEntry getCacheEntry(String url) throws IOException {
        return PlatformConfig.defaultCacheManager().newCacheEntry(cacheName)
                .withKey(url)
                .build();
    }

    @Override
    public synchronized InputStream getData(String url) throws IOException {
        CacheManager.CacheEntry cacheEntry = getCacheEntry(url);
        if (cacheEntry.exists()) {
            Path urlFile = getUrlFile(cacheEntry.toPath());
            if (!Files.exists(urlFile)) {
                throw new RuntimeException("Url file not found");
            }
            return Files.newInputStream(getDataFile(cacheEntry.toPath()));
        }
        return null;
    }

    @Override
    public synchronized OutputStream putData(String url) throws IOException {
        CacheManager.CacheEntry cacheEntry = getCacheEntry(url);
        if (cacheEntry.exists()) {
            throw new IllegalArgumentException("Url entry already exists");
        }
        cacheEntry.create();
        try (Writer writer = Files.newBufferedWriter(getUrlFile(cacheEntry.toPath()), StandardCharsets.UTF_8)) {
            writer.write(url);
        }
        return Files.newOutputStream(getDataFile(cacheEntry.toPath()));
    }

    @Override
    public List<String> listUrls() throws IOException {
        Path dir = PlatformConfig.CACHE_DIR.resolve(cacheName);
        if (Files.exists(dir)) {
            try (Stream<Path> stream = Files.list(dir)) {
                return stream
                        .filter(p -> Files.isDirectory(p))
                        .map(entryDir -> getUrlFile(entryDir))
                        .map(urlFile -> {
                            try {
                                return new String(Files.readAllBytes(urlFile), StandardCharsets.UTF_8);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .collect(Collectors.toList());
            }
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public void close() throws Exception {
    }

}
