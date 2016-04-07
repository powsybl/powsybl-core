/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.db;

import eu.itesla_project.commons.io.ModuleConfig;
import eu.itesla_project.commons.io.PlatformConfig;
import java.nio.file.Path;
import java.util.Objects;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class OnlineDbMVStoreConfig {

	/*
	 * example of onlineDbMVStore.properties file.
	 *
	#path to the directory containing the db data
	onlineDbDir=/shared/allrw/onlinewf-db

	*/

	private final Path onlineDbDir;

	public OnlineDbMVStoreConfig(Path onlineDbDir) {
		Objects.requireNonNull(onlineDbDir,"onlie db directory is null");

		this.onlineDbDir = onlineDbDir;
	}

	public static OnlineDbMVStoreConfig load() {
        ModuleConfig config = PlatformConfig.defaultConfig().getModuleConfig("onlineDbMVStore");

        Path onlienDbDir = config.getPathProperty("onlineDbDir");

        return new OnlineDbMVStoreConfig(onlienDbDir);
	}


	public Path getOnlineDbDir() {
		return onlineDbDir;
	}

	@Override
	public String toString() {
		return "OnlineDbMVStoreConfig ["+ "onlineDbDir=" + onlineDbDir + "]";
	}

}
