## Import

<span style="color: red">TODO</span>

### Options
These properties can be defined in the configuration file in the [import-export-parameters-default-value](../../user/configuration/import-export-parameters-default-value.md) module.

**iidm.import.xml.throw-exception-if-extension-not-found**  
The `iidm.import.xml.throw-exception-if-extension-not-found` property is an optional property that defines if the XIIDM importer throws an exception while trying to import an unknown or undeserializable extension or if it just ignores it. Its default value is `false`.

**iidm.import.xml.extensions**  
The `iidm.import.xml.extensions` property is an optional property that defines the list of extensions that will be imported by the XIIDM importer. By default all extensions will be imported.

#### Deprecated properties

**throwExceptionIfExtensionNotFound**  
The `throwExceptionIfExtensionNotFound` property is deprecated since v2.0.0. Use the `iidm.import.xml.throw-exception-if-extension-not-found` property instead.

#### Removed properties

**iidm.import.xml.import-mode**  
The `iidm.import.xml.import-mode` property is an optional property that defines the import mode of the XIIDM importer.

Its possible values are :
- `UNIQUE_FILE`: Imports the network and its extensions from a unique file.
- `EXTENSIONS_IN_ONE_SEPARATED_FILE`: Imports the network from a file and the extensions from another file. In this mode, if the network file name is network.xiidm, the extensions file name must be network-ext.xiidm.
- `ONE_SEPARATED_FILE_PER_EXTENSION_TYPE`: Imports the network from a file and each extension type from a separate file. In this mode, if the network file name is `network.xiidm`, each extension file name must be `network-extensionName.xiidm`. Example: if our network has two extensions `loadFoo` and `loadBar`, then the network will be imported from the `network.xiidm` file and `loadFoo` and `loadBar` will be imported respectively from `network-loadFoo.xiidm` and `network-loadBar.xiidm`.

The default value of this parameter is `NO_SEPARATED_FILE_FOR_EXTENSIONS`. This property has been removed in v3.3.0.
