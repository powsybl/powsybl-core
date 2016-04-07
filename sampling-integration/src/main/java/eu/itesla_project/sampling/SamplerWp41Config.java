/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.sampling;

import eu.itesla_project.commons.io.ModuleConfig;
import eu.itesla_project.commons.io.PlatformConfig;
import java.nio.file.Path;
import java.util.Objects;

/*
 * example of samplerwp41.properties file.
 * note that required matlab binaries and MCR are expected to be available at the specified path
 * on each computation node
 *
 * output files for module1 and module 2 are written in the cache folder, under wp41 directory
 * if these files are already there, module1 and module2 are not run (delete cache/wp41 to rebuild them)
 *
#path to the directory containing the compiled matlab binaries (chmod +x all the binaries)
# this parameter is optional, if not defined, binaries are expected to be in PATH
# binariesDir=/opt/itesla/matlab/merged2

#path to the MCR installation
runtimeHomeDir=/usr/local/MATLAB/MATLAB_Compiler_Runtime/v81

#validationDir, if not empty, .mat files to be used to validate wp41 will be stashed here
validationDir=/tmp/validationWp41


#usually 0.95
ir=0.5

#Truncated model flag (set to 0 for a simpler model that does not store large correlation matrices in module2 .mat files)
tflag=0;


debug=false

*/
/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class SamplerWp41Config {

    private static final int MODULE3_DEFAULT_TIMEOUT = 10 * 60;

	private final Path binariesDir;

    private final Path runtimeHomeDir;

    private final Path validationDir;

	private final double ir;
    private final Integer rngSeed;

    private final int module3Timeout;
	private final double tflag;

	private final int par_k;

	private final boolean debug;

	public static SamplerWp41Config load() {
        ModuleConfig config = PlatformConfig.defaultConfig().getModuleConfig("samplerwp41");

        Path runtimeHomeDir = config.getPathProperty("runtimeHomeDir");
        Path binariesDir = config.getPathProperty("binariesDir", null);

        Path validationDir=config.getPathProperty("validationDir", null);

        double ir = config.getDoubleProperty("ir");
        double tflag = config.getDoubleProperty("tflag");

        Integer rngSeed = config.getOptionalIntProperty("rngSeed");

        int module3Timeout = config.getIntProperty("module3Timeout", MODULE3_DEFAULT_TIMEOUT);

		int par_k = config.getIntProperty("par_k", -1);

        boolean debug = config.getBooleanProperty("debug", false);

        return new SamplerWp41Config(binariesDir, runtimeHomeDir, validationDir, ir, tflag, rngSeed, module3Timeout, par_k, debug);
	}

	public SamplerWp41Config(
			Path binariesDir,
			Path runtimeHomeDir,
            Path validationDir,
			double ir,
			double tflag,
            Integer rngSeed,
            int module3Timeout,
			int par_k,
			boolean debug) {
		this.binariesDir=binariesDir;
		this.runtimeHomeDir = Objects.requireNonNull(runtimeHomeDir, "matlab runtime directory is null");
        this.validationDir = validationDir;
		this.ir = ir;
		this.tflag=tflag;
        this.rngSeed = rngSeed;
        this.module3Timeout = module3Timeout;
		this.par_k = par_k;
		this.debug=debug;
	}

	public Path getBinariesDir() {
		return binariesDir;
	}

	public Path getRuntimeHomeDir() {
		return runtimeHomeDir;
	}

    public Path getValidationDir() {
        return validationDir;
    }

	public double getIr() {
		return ir;
	}

	public double getTflag() {
		return tflag;
	}

    public Integer getRngSeed() {
        return rngSeed;
    }

    public int getModule3Timeout() {
        return module3Timeout;
    }

	public int getPar_k() {
		return par_k;
	}

	public boolean isDebug() {
	        return debug;
	}

	@Override
	public String toString() {
		String retString = "binariesDir=" + binariesDir
				+ ", runtimeHomeDir=" + runtimeHomeDir
                + ", validationDir=" + validationDir
				+ ", ir=" + ir
				+ ", tflag=" + tflag
				+ ", rngSeed=" + rngSeed
		        + ", module3Timeout=" + module3Timeout
		        + ", par_k=" + par_k
				+ ", debug=" + debug;
		return "SamplerWp41Config ["+retString+"]";
	}






}
