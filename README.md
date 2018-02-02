# SqlNormalisation udf for Teradata

The SqlNormalisation takes an SQL query and removes "noise" to enable comparison (e.g. hamming, Levenshtein distance etc) with another Normalised SQL query.
The characters defined as noise (and hence removed from a query) include
* comments
* literals
* excessive whitespace (contiguous sequences of spaces, tabs and/or newlines are collapsed to a single space)

The comparison of query texts can be useful to identify things such as:
* queries that have been "tweaked" (that perhaps should not be tweaked)
* queries that have been shared amongst users (and could perhaps be pre-calculated and persisted to eliminate some frequently executed workload)
* queries that are frequently executed with just "parameter" changes (i.e. the literal values)
* and so on

The code may be run as a GUI which permits SQL text to be entered (or pasted) into a text box.
The results of the normalisation are displayed in a popup message box

The code should work on any text that follows basic SQL conventions for comments and literals. The UDF glue code is written to work with Teradata.

The code should easily be convertable to work with other RDBMS' as a UDF or in a larger program that supplies SQL text from any source that it is
capable of obtaining it from (e.g. a file, a database, a user, a web service etc).
