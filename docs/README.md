These are the documentation sources for PowSybl core features.

Please keep them up to date with your developments.  
They are published on powsybl.readthedocs.io/ and pull requests are built and previewed automatically.

When modifying the website content, you can easily preview the result on your PC.

**First option - in a terminal, navigate to the root of the project and run the following commands:**

~~~
pip install -r docs/requirements.txt
sphinx-build -a docs ./build-docs
~~~

**Second option - run the following commands directly from your IDE GUI**

~~~bash
pip install -r requirements.txt
~~~

~~~bash
sphinx-build -a . ../build-docs
~~~

**Preview the result**

Then open `../build-docs/index.html` in your browser.