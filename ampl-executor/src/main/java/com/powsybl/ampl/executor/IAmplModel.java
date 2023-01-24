/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ampl.executor;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.powsybl.ampl.converter.AmplNetworkReader;
import com.powsybl.ampl.converter.NetworkApplier;
import com.powsybl.ampl.converter.OutputFileFormat;

/**
 * Interface to represent an Ampl model to run on a network.
 * <p>
 * The AMPL must read/write files according to
 * {@link AmplExporter}/{@link AmplNetworkReader}.
 * <p>
 * Some customization is available through :
 * <ul>
 * <li>{@link IAmplModel#getOutputFilePrefix}: prefix for the files written by
 * the ampl execution</li>
 * <li>{@link IAmplModel#getNetworkApplier}: specific applier to select what is
 * modified on the network</li>
 * <li>{@link IAmplModel#getVariant}: ampl variants</li>
 * <li>{@link IAmplModel#getOutputFormat}: some information about the format of
 * the output files</li>
 * <li>{@link IAmplModel#getNetworkDataPrefix}: the prefix used to every network
 * input files</li>
 * </ul>
 */
public interface IAmplModel {
    /**
     * @return each pair contains the name and the InputStream of every ampl file of
     *         the model (.run .dat .mod)
     */
    public List<Pair<String, InputStream>> getModelAsStream();

    /**
     * @return the list of the files to run in ampl (.run files)
     */
    public List<String> getAmplRunFiles();

    public String getOutputFilePrefix();

    public NetworkApplier getNetworkApplier();

    public int getVariant();

    public OutputFileFormat getOutputFormat();

    public String getNetworkDataPrefix();

}
