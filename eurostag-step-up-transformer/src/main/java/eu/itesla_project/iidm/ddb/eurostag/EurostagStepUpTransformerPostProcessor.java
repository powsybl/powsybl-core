/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.eurostag;

import com.google.auto.service.AutoService;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.iidm.import_.ImportPostProcessor;
import eu.itesla_project.iidm.network.Network;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.nio.file.ShrinkWrapFileSystems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ImportPostProcessor.class)
public class EurostagStepUpTransformerPostProcessor implements ImportPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(EurostagStepUpTransformerPostProcessor.class);

    private final Supplier<EurostagStepUpTransformerConfig> config = Suppliers.memoize(() -> {
        EurostagStepUpTransformerConfig config = EurostagStepUpTransformerConfig.load();
        LOGGER.info(config.toString());
        return config;
    });

    public EurostagStepUpTransformerPostProcessor() {
    }

    @Override
    public String getName() {
        return "stepUpTransformers";
    }

    @Override
    public void process(Network network, ComputationManager computationManager) throws Exception {
        List<Path> ddbPath = new ArrayList<>();
        IdDictionary genDict = new IdDictionary();
        IdDictionary auxDict = new IdDictionary();
        List<String> statorVoltageLevels = new ArrayList<>();
        List<FileSystem> zipFsLs = new ArrayList<>();
        try {
            for (Path p : config.get().getDdbPath()) {
                if (!Files.exists(p)) {
                    throw new RuntimeException("DDB dir " + p + " does not exist");
                }
                if (Files.isDirectory(p)) {
                    ddbPath.add(p);
                } else if (Files.isRegularFile(p) && p.getFileName().toString().endsWith(".zip")) {
                    GenericArchive archive = ShrinkWrap.createFromZipFile(GenericArchive.class, p.toFile());
                    FileSystem zipFs = ShrinkWrapFileSystems.newFileSystem(archive);
                    zipFsLs.add(zipFs);
                    ddbPath.add(zipFs.getPath("/"));
                } else {
                    throw new RuntimeException("Bad path element " + p);
                }
            }
            for (Path ddbDir : ddbPath) {
                Path genDictFile = ddbDir.resolve(config.get().getGenDictFileName());
                if (Files.exists(genDictFile)) {
                    genDict.loadCsv(genDictFile, 0, 1);
                }
                Path auxDictFile = ddbDir.resolve(config.get().getAuxDictFileName());
                if (Files.exists(auxDictFile)) {
                    auxDict.loadCsv(auxDictFile, 0, 1);
                }
                if (config.get().getStatorVoltageLevelsFileName() != null) {
                    statorVoltageLevels.addAll(EurostagStepUpTransformerInserter.readStatorVoltageLevels(ddbDir.resolve(config.get().getStatorVoltageLevelsFileName())));
                }
            }

            if (genDict.size() == 0) {
                throw new RuntimeException("Generator dictionary is empty");
            }
            if (auxDict.size() == 0) {
                throw new RuntimeException("Auxiliary dictionary is empty");
            }
            EurostagStepUpTransformerInserter.insert(network, config.get().getLoadFlowFactoryClass().newInstance(), computationManager, ddbPath, genDict, auxDict, statorVoltageLevels, config.get());
        } catch (IllegalAccessException|InstantiationException e) {
            throw new RuntimeException(e);
        } finally {
            for (FileSystem zipFs : zipFsLs) {
                zipFs.close();
            }
        }
    }

}
