/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.ddb;

import eu.itesla_project.commons.io.CacheManager;
import eu.itesla_project.commons.io.PlatformConfig;
import eu.itesla_project.iidm.network.Generator;
import eu.itesla_project.iidm.network.Identifiable;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.util.Identifiables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DynamicDatabaseCacheClient implements DynamicDatabaseClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicDatabaseCacheClient.class);

    private final DynamicDatabaseClient delegate;

    public DynamicDatabaseCacheClient(DynamicDatabaseClient delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public String getVersion() {
        return delegate.getVersion();
    }

	private boolean filteredGenerator(Generator g, boolean isFiltered) {
		if (isFiltered) {
	  		if  (!Float.isNaN(g.getTerminal().getP()) && ((-g.getTerminal().getP() > g.getMaxP()) || (-g.getTerminal().getP() < g.getMinP())) ) {
	  			return true;
	  		}
		}
		return false;
	}

    private List<Generator> filterGenerators(Network network, boolean isFiltered) {
    	List<Generator> filtGens=new ArrayList<>();
		for (Generator g : Identifiables.sort(network.getGenerators())) {
			if ( !filteredGenerator(g,isFiltered) )
				filtGens.add(g);
		}
		return filtGens;
	}


    @Override
    public void dumpDtaFile(Path workingDir, String fileName, Network network, Map<String, Character> parallelIndexes, String eurostagVersion, Map<String, String> iidm2eurostagId) {
    	boolean isFiltered = DdExportConfig.load().getGensPQfilter();

        CacheManager.CacheEntry cacheEntry = PlatformConfig.defaultCacheManager().newCacheEntry("ddb")
                .withKey(fileName)
                .withKeys(StreamSupport.stream(filterGenerators(network, isFiltered).spliterator(), false)
                               .map(Identifiable::getId)
                               .sorted()
                               .collect(Collectors.toList()))
                .build();
        try {
            cacheEntry.lock();
            try {
                if (cacheEntry.exists()) {
                    LOGGER.info("Loading ddb cache {}, filterPQ {}", cacheEntry, isFiltered);
                } else {
                    LOGGER.info("Generating ddb cache {}, filterPQ {}", cacheEntry, isFiltered);

                    // create the directory
                    cacheEntry.create();

                    // generate data in the cache
                    delegate.dumpDtaFile(cacheEntry.toPath(), fileName, network, parallelIndexes, eurostagVersion, iidm2eurostagId);
                }
            } finally {
                cacheEntry.unlock();
            }

            // copy data from the cache to the working directory
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(cacheEntry.toPath())) {
                for (Path p : ds) {
                    if (Files.isRegularFile(p)) {
                        Files.copy(p, workingDir.resolve(p.getFileName()));
                    } else {
                        throw new RuntimeException("Only regular files are exptected in the cache");
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
