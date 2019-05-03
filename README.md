# BSBM Tools with RDF/JSON serialization

This is [BSBM Tools](http://sourceforge.net/projects/bsbmtools/) with added [RDF/JSON serialization](http://www.w3.org/2009/12/rdf-ws/papers/ws02).

> The Berlin SPARQL Benchmark (BSBM) defines a suite of benchmarks for comparing the performance of these systems across architectures. The benchmark is built around an e-commerce use case in which a set of products is offered by different vendors and consumers have posted reviews about products. The benchmark query mix illustrates the search and navigation pattern of a consumer looking for a product.
> - http://wifo5-03.informatik.uni-mannheim.de/bizer/berlinsparqlbenchmark/

## Input data files

Note that the input data files (dictionaries, queries, etc.) have been moved into `data` directory, which is used by default. However, you can use your own files by specifying appropriate arguments for the utilities:

    $ bin/generate -d1 dictionary1.txt -d2 dictionary2.txt -d3 dictionary3.txt


## Building

The project is built using Apache Ant and project dependencies are handled by Apache Ivy. You need to install these tools in order to build the project.

Build the project:

    $ ant

Make sure that ivy is properly configured and accessible by ant. For instance, you may want to set up a link to `ivy.jar` from `~/.ant/lib/`.

The dependencies are configured using `ivy.xml`. Note that some old dependencies reside in a not very popular maven repository, configured in `ivysettings.xml`.


## Executables

The scripts that you can use for testing are copied into `bin` directory during the build process:

  * bin/generate
  * bin/testdriver
  * bin/qualification


## Example usage

    $ bin/generate -fc -pc 10 -fn example -s json

Generated RDF/JSON samples are available in the [bsbmtools-json-samples](https://github.com/lszeremeta/bsbmtools-json-samples) repository.

You can see a list of all available options by running:

    $ bin/generate -help


Additional examples are also available in the [original documentation](http://wifo5-03.informatik.uni-mannheim.de/bizer/berlinsparqlbenchmark/spec/BenchmarkRules/index.html#datagenerator).


### HTTP Basic Authentication

If the SPARQL endpoint requires authentication, you can provide credentials in the URL:

    $ bin/testdriver http://admin:admin@localhost/sparql


## Author

(C) [Łukasz Szeremeta](https://github.com/lszeremeta) 2015

based on Christian Bizer and Andreas Schultz works
