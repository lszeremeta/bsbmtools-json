# BSBM Tools with RDF/JSON serialization

This is [BSBM Tools](http://sourceforge.net/projects/bsbmtools/) with added [RDF/JSON serialization](http://www.w3.org/2009/12/rdf-ws/papers/ws02).

> The Berlin SPARQL Benchmark (BSBM) defines a suite of benchmarks for comparing the performance of these systems across architectures. The benchmark is built around an e-commerce use case in which a set of products is offered by different vendors and consumers have posted reviews about products. The benchmark query mix illustrates the search and navigation pattern of a consumer looking for a product.
> - http://wifo5-03.informatik.uni-mannheim.de/bizer/berlinsparqlbenchmark/

## Example usage

```
./generate -fc -pc 10 -fn example -s json
```

You can see a list of all available options and additional examples in the [original documentation](http://wifo5-03.informatik.uni-mannheim.de/bizer/berlinsparqlbenchmark/spec/BenchmarkRules/index.html#datagenerator).

## Author

(C) [Łukasz Szeremeta](https://github.com/lszeremeta) 2015

based on Christian Bizer and Andreas Schultz works
