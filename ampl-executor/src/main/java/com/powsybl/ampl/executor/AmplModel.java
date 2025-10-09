/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ampl.executor;

import com.powsybl.ampl.converter.*;
import org.apache.commons.lang3.tuple.Pair;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Interface to represent an Ampl model to run on a network.
 * <p>
 * The AMPL must read/write files according to
 * {@link AmplExporter}/{@link AmplNetworkReader}.
 * <p>
 * Some customization is available through :
 * <ul>
 * <li>{@link AmplModel#getOutputFilePrefix}: prefix for the files written by
 * the Ampl execution</li>
 * <li>{@link AmplModel#getNetworkUpdaterFactory()}: specific applier Factory to select what is
 * modified on the network</li>
 * <li>{@link AmplModel#getVariant}: Ampl variants</li>
 * <li>{@link AmplModel#getOutputFormat}: some information about the format of
 * the output files</li>
 * <li>{@link AmplModel#getNetworkDataPrefix}: the prefix used to every network
 * input files</li>
 * </ul>
 *
 * @author Nicolas Pierre {@literal <nicolas.pierre@artelys.com>}
 */
public interface AmplModel {

    /**
     * @return each pair contains the name, and the InputStream of every ampl file of
     * the model (.run .dat .mod)
     */
    List<Pair<String, InputStream>> getModelAsStream();

    /**
     * @return the list of the files to run in Ampl (.run files)
     */
    List<String> getAmplRunFiles();

    String getOutputFilePrefix();

    AmplNetworkUpdaterFactory getNetworkUpdaterFactory();

    /**
     * @return network variant to export for the Ampl solve
     */
    int getVariant();

    OutputFileFormat getOutputFormat();

    String getNetworkDataPrefix();

    /**
     * @return list of {@link AmplReadableElement} to read after the Ampl run,
     * it implies that the Ampl model outputs these files with the correct format
     */
    Collection<AmplReadableElement> getAmplReadableElement();

    /**
     * From the metrics read, tells if the model has converged or not.
     *
     * @param metrics output written by the ampl model
     * @return <code>true</code> if the metrics indicates that the ampl converged.
     * @see AmplParameters#getOutputParameters
     */
    boolean checkModelConvergence(Map<String, String> metrics);
}
