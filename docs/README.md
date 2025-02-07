# PowSyBl Core documentation

These are the documentation sources for PowSyBl core features.

Please keep them up to date with your developments.  
They are published on powsybl.readthedocs.io/ and pull requests are built and previewed automatically.

## Build the documentation

When modifying the website content, you can easily preview the result on your PC.

### From a terminal

Navigate to the "docs" directory of the project and run the following commands:

Install the requirements the first time:
~~~bash
pip install -r requirements.txt
~~~
Build the documentation:
~~~bash
sphinx-build -a . _build/html
~~~
Or
~~~bash
make html
~~~
Or to build in latex format
~~~bash
make latexpdf
~~~

### From your IDE GUI

Install the requirements the first time:
~~~bash
pip install -r requirements.txt
~~~

~~~bash
sphinx-build -a . _build/html
~~~

### Preview the result

Then open `_build/html/index.html` in your browser.

If you want to add links to another documentation, add the corresponding repository to the `conf.py` file.
To automatically get the version specified in the `pom.xml`, please use the same naming as the version: if you define the
Groovy version with `<groovy.version>`, then use `groovy` as a key. The specified URL should start with `https://` and end with `latest/` (the final `/` is mandatory).
For example, to add a link to the documentation of Sphinx, you need to add the following lines:
~~~python
# This parameter might already be present, just add the new value
intersphinx_mapping = {
    "sphinx": ("https://www.sphinx-doc.org/en/master/", None),
}
~~~

Then in your documentation file, you can add links to PowSyBl-Core documentation. If you want to link to a whole page,
use one of the following examples:
~~~Markdown
- {doc}`sphinx:usage/extensions/intersphinx`
- {doc}`Intersphinx <sphinx:usage/extensions/intersphinx>`
- [Intersphinx](inv:sphinx:std:doc#usage/extensions/intersphinx).
~~~

If you want to link a specific part of a page, use one of those examples:
~~~Markdown
- [Intersphinx roles](inv:#ref-role).
- [Intersphinx roles](inv:sphinx#ref-role).
- [Intersphinx roles](inv:sphinx:std:label:#ref-role).
- [Intersphinx roles](inv:sphinx:*:*:#ref-role).
~~~
*Note: for the last examples to work, there need to be a corresponding reference in the external documentation.
For those examples, `(ref-role)=` has been added right before the corresponding title
in the [Cross-referencing syntax page](inv:sphinx:std:doc#usage/referencing). Another way to make it work is to use the `autosectionlabel` module in Sphinx to
automatically generate anchors for each title.*

*Note²: if the build fails, try with the `-E` option to clear the cache:*
~~~bash
sphinx-build -a -E . _build/html
~~~