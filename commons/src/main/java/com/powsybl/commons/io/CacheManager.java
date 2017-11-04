/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.io;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.exceptions.UncheckedInterruptedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CacheManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheManager.class);

    private final Path cacheDir;

    private final Lock cacheEntriesLock = new ReentrantLock();

    private final Map<String, CacheEntry> cacheEntries = new HashMap<>();

    public static class CacheEntry {

        private final Path path;

        private final List<String> keys;

        private final Semaphore semaphore = new Semaphore(1);

        public CacheEntry(Path path, List<String> keys) {
            this.path = Objects.requireNonNull(path);
            this.keys = Objects.requireNonNull(keys);
        }

        public Path toPath() {
            return path;
        }

        public List<String> getKeys() {
            return keys;
        }

        public boolean exists() {
            return Files.exists(path) && Files.isDirectory(path);
        }

        private Path getMetadataFile() {
            return path.resolve(".metadata");
        }

        public Path create() {
            try {
                if (Files.exists(path)) {
                    List<String> otherKeys;
                    try (Stream<String> stream = Files.lines(getMetadataFile(), StandardCharsets.UTF_8)) {
                        otherKeys = stream.collect(Collectors.toList());
                    }
                    if (!keys.equals(otherKeys)) {
                        throw new PowsyblException("Inconsistent cache hash code");
                    }
                } else {
                    Files.createDirectories(path);
                    try (BufferedWriter writer = Files.newBufferedWriter(getMetadataFile(), StandardCharsets.UTF_8)) {
                        for (String key : keys) {
                            writer.write(key);
                            writer.newLine();
                        }
                    }
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return path;
        }

        public void remove() {
            if (exists()) {
                try {
                    FileUtil.removeDir(path);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }

        public void lock() {
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new UncheckedInterruptedException(e);
            }
        }

        public void unlock() {
            semaphore.release();
        }

        @Override
        public String toString() {
            return path.toString();
        }
    }

    public class CacheEntryBuilder {

        private final String name;

        private final List<String> keys = new ArrayList<>();

        public CacheEntryBuilder(String name) {
            this.name = Objects.requireNonNull(name);
        }

        public CacheEntryBuilder withKey(String key) {
            keys.add(key);
            return this;
        }

        public CacheEntryBuilder withKeys(List<String> keys) {
            this.keys.addAll(keys);
            return this;
        }

        public CacheEntry build() {
            Path baseDir = cacheDir.resolve(name);
            if (!keys.isEmpty()) {
                HashFunction hf = Hashing.md5();
                Hasher h = hf.newHasher();
                for (String key : keys) {
                    h.putString(key, Charsets.UTF_8);
                }
                HashCode hc = h.hash();
                baseDir = baseDir.resolve(hc.toString());
            }
            cacheEntriesLock.lock();
            try {
                CacheEntry cacheEntry = cacheEntries.get(baseDir.toString());
                if (cacheEntry != null) {
                    if (!cacheEntry.getKeys().equals(keys)) {
                        throw new PowsyblException("Inconsistent hash");
                    }
                } else {
                    cacheEntry = new CacheEntry(baseDir, keys);
                    cacheEntries.put(baseDir.toString(), cacheEntry);
                }
                return cacheEntry;
            } finally {
                cacheEntriesLock.unlock();
            }
        }
    }

    public CacheManager(Path cacheDir) {
        this.cacheDir = Objects.requireNonNull(cacheDir);
        LOGGER.info("Use cache directory {}", cacheDir);
    }

    public CacheEntryBuilder newCacheEntry(String name) {
        return new CacheEntryBuilder(name);
    }

}
