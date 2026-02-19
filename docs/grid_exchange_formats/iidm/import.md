# Import

(iidm-import-options)=
## Options
These properties can be defined in the configuration file in the [import-export-parameters-default-value](../../user/configuration/import-export-parameters-default-value.md#import-export-parameters-default-value) module.

**iidm.import.xml.throw-exception-if-extension-not-found**<br>
The `iidm.import.xml.throw-exception-if-extension-not-found` property is an optional property that defines if the XIIDM importer throws an exception while trying to import an unknown or undeserializable extension or if it just ignores it.

By default, the value is `false`.

**iidm.import.xml.included.extensions**<br>
The `iidm.import.xml.included.extensions` property is an optional property that defines the list of extensions that will be imported by the XIIDM importer. By default, all extensions will be imported.

When set to an empty string, all extensions will be ignored during the import.

**iidm.import.xml.excluded.extensions**<br>
The `iidm.import.xml.excluded.extensions` property is an optional property that defines the list of extensions that will not be imported by the XIIDM importer.
When both `iidm.import.xml.included.extensions` and `iidm.import.xml.excluded.extensions` are defined, a configuration exception is thrown.

By default, no extension is excluded from the import.

**iidm.import.xml.with-automation-systems**<br>
The `iidm.import.xml.with-automation-systems` property is an optional property that defines whether to import or not the network overload management systems. It should be set as a double value.

By default, the value is `true`, the overload management systems are imported when deserializing a network.

**iidm.import.xml.missing-permanent-limit-percentage**<br>
The `iidm.import.xml.missing-permanent-limit-percentage` property is an optional property that defines the percentage applied to the lowest temporary limit to compute the permanent limit when missing (for IIDM < 1.12 only).

By default, it is set to `100`, the computed permanent limit is then the same as the lowest temporary limit.

**iidm.import.minimal-validation-level**<br>
The `iidm.import.minimal-validation-level` property is an optional property that allows to override the network minimum validation level among the accepted validation levels.
The possible validation levels are  `EQUIPMENT` and `STEADY_STATE_HYPOTHESIS`.

By default, the value is `null`, the network validation level remains the same.

### Deprecated properties

**throwExceptionIfExtensionNotFound**<br>
The `throwExceptionIfExtensionNotFound` property is deprecated since v2.0.0. Use the `iidm.import.xml.throw-exception-if-extension-not-found` property instead.

### Removed properties

**iidm.import.xml.import-mode**<br>
The `iidm.import.xml.import-mode` property is an optional property that defines the import mode of the XIIDM importer.

Its possible values are :
- `UNIQUE_FILE`: Imports the network and its extensions from a unique file.
- `EXTENSIONS_IN_ONE_SEPARATED_FILE`: Imports the network from a file and the extensions from another file. In this mode, if the network file name is network.xiidm, the extension file name must be network-ext.xiidm.
- `ONE_SEPARATED_FILE_PER_EXTENSION_TYPE`: Imports the network from a file and each extension type from a separate file. In this mode, if the network file name is `network.xiidm`, each extension file name must be `network-extensionName.xiidm`. Example: if our network has two extensions `loadFoo` and `loadBar`, then the network will be imported from the `network.xiidm` file and `loadFoo` and `loadBar` will be imported respectively from `network-loadFoo.xiidm` and `network-loadBar.xiidm`.

The default value of this parameter is `NO_SEPARATED_FILE_FOR_EXTENSIONS`. This property has been removed in v3.3.0.
