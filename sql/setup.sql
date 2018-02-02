call sqlj.install_jar('cj!dist/SqlNormalisation.jar', 'NormalisationJAR', 0);


REPLACE FUNCTION SqlNormalisation(x varchar(63000))
RETURNS varchar(63000)
LANGUAGE JAVA
NO SQL
PARAMETER STYLE JAVA
RETURNS NULL ON NULL INPUT
EXTERNAL NAME 'NormalisationJAR:com.teradata.edwdecoded.sql.normalise.Main.normaliseSqlUDF';


drop function SqlNormalisation;

call sqlj.remove_jar('NormalisationJAR', 0);
