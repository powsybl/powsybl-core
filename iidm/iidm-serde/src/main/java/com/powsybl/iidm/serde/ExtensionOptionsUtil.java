package com.powsybl.iidm.serde;

import com.powsybl.commons.config.ConfigurationException;
import com.powsybl.commons.parameters.Parameter;
import com.powsybl.commons.parameters.ParameterDefaultValueConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;

/**
 * This delegate class avoid code duplication in AbstractTreeDataImporter and AbstractTreeDataExporter classes
 */
public final class ExtensionOptionsUtil {

    /**
     * The logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionOptionsUtil.class);

    /**
     * Avoid instantiating this class
     */
    private ExtensionOptionsUtil() {
    }

    /**
     * Method to check and add extension inclusion and exclusion list from parameters to AbstractOptions<T>
     * @param parameters the parameters for the import / export
     * @param options the AbstractOptions<T> to fill and check
     * @param format the format for the parameter
     * @param defaultValueConfig the default value from the config for the parameter
     * @param includeListParameter the parameter for the inclusion list
     * @param excludeListParameter the parameter for the exclusion list
     * @param shouldWarnOnInclusionEmptiness flag to issue a warning when inclusion list is empty (all extensions are thus excluded)
     * @return true if some extensions should be included
     * @param <T>  Either ImportOptions or ExportOptions or any descendant of tha AbstractOption class
     */
    public static <T extends AbstractOptions<T>> boolean getAndCheckExtensionsToInclude(Properties parameters,
                                                                                        T options,
                                                                                        String format,
                                                                                        ParameterDefaultValueConfig defaultValueConfig,
                                                                                        Parameter includeListParameter,
                                                                                        Parameter excludeListParameter,
                                                                                        boolean shouldWarnOnInclusionEmptiness) {
        List<String> includedExtensions = Parameter.readStringList(format, parameters, includeListParameter, defaultValueConfig);
        List<String> excludedExtensions = Parameter.readStringList(format, parameters, excludeListParameter, defaultValueConfig);
        if (includedExtensions != null && excludedExtensions != null) {
            throw new ConfigurationException("You can't define both included and excluded extensions in parameters.");
        }
        if (excludedExtensions != null) {
            options.setExcludedExtensions(new HashSet<>(excludedExtensions));
        }
        if (includedExtensions != null) {
            options.setIncludedExtensions(new HashSet<>(includedExtensions));
            if (includedExtensions.isEmpty()) {
                if (shouldWarnOnInclusionEmptiness) {
                    LOGGER.info("All extensions are excluded.");
                }
                return false;
            }
        }
        return true;
    }
}
