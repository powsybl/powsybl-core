# Configuration file for the Sphinx documentation builder.
#
# This file only contains a selection of the most common options. For a full
# list see the documentation:
# https://www.sphinx-doc.org/en/master/usage/configuration.html

# -- Path setup --------------------------------------------------------------

# If extensions (or modules to document with autodoc) are in another directory,
# add these directories to sys.path here. If the directory is relative to the
# documentation root, use os.path.abspath to make it absolute, like shown here.
#
import os
import re
import sys

# Path to python sources, for doc generation on readthedocs
source_path = os.path.abspath('..')
sys.path.insert(0, source_path)
print(f'appended {source_path}')


# -- Project information -----------------------------------------------------

project = 'powsybl core'
copyright = '2024, RTE (http://www.rte-france.com)'

# Find the version and release information.
# We have a single source of truth for our version number: pip's __init__.py file.
# This next bit of code reads from it.
file_with_version = os.path.join(source_path, "pom.xml")
with open(file_with_version) as f:
    next_line_contains_version = False
    for line in f:
        if next_line_contains_version == False:
            m = re.match(r'^ {4}\<artifactId\>powsybl\-core\<\/artifactId\>', line)
            if m:
                next_line_contains_version = True
        else:
            m = re.match(r'^ {4}\<version\>(.*)\<\/version\>', line)
            if m:
                __version__ = m.group(1)
                # The short X.Y version.
                version = ".".join(__version__.split(".")[:2])
                # The full version, including alpha/beta/rc tags.
                release = __version__
                break
    else:  # AKA no-break
        version = release = "dev"


# -- General configuration ---------------------------------------------------

# Add any Sphinx extension module names here, as strings. They can be
# extensions coming with Sphinx (named 'sphinx.ext.*') or your custom
# ones.
extensions = ['sphinx.ext.autodoc',
              'sphinx.ext.autosummary',
              'sphinx.ext.viewcode',
              'sphinx.ext.doctest',
              'sphinx.ext.napoleon',
              'sphinx.ext.todo',
              'sphinx.ext.intersphinx',
              'sphinx_tabs.tabs',
              'myst_parser']
myst_enable_extensions = [
    "amsmath",
    "colon_fence",
    "dollarmath",
    "attrs_inline"
]
myst_heading_anchors = 6

# Add any paths that contain templates here, relative to this directory.
templates_path = ['_templates']

# List of patterns, relative to source directory, that match files and
# directories to ignore when looking for source files.
# This pattern also affects html_static_path and html_extra_path.
exclude_patterns = ['_build', 'Thumbs.db', '.DS_Store']


# -- Options for HTML output -------------------------------------------------

# The theme to use for HTML and HTML Help pages.  See the documentation for
# a list of builtin themes.
#
html_theme = "furo"

html_title = f"{project} v{release}"
html_short_title = f'Core v{release}'

html_logo = '_static/logos/logo_lfe_powsybl.svg'
html_favicon = "_static/favicon.ico"

html_context = {
    "sidebar_logo_href": "http://powsybl-core.readthedocs.io/"
}

html_theme_options = {
    # the following 3 lines enable edit button
    "source_repository": "https://github.com/powsybl/powsybl-core/",
    "source_branch": "main",
    "source_directory": "docs/",
}

# Add any paths that contain custom static files (such as style sheets) here,
# relative to this directory. They are copied after the builtin static files,
# so a file named "default.css" will overwrite the builtin "default.css".
html_static_path = ['_static']
html_css_files = ['styles/styles.css']

todo_include_todos = True

# Links to external documentations : python 3 and pandas
intersphinx_mapping = {
}
intersphinx_disabled_reftypes = ["*"]

# Generate one file per method
autosummary_generate = True