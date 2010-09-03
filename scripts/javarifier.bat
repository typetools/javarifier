
:: Run Javarifier over a set of class files and output inferred mutabilities.
:: For usage information, run: javarifier.bat --help
:: See the javarifier.html documentation for more information.

set JAVARIFIER_DIR=%~d0
set JAVARIFIER_DIR=%JAVARIFIER_DIR%%~p0
set JAVARIFIER_LIB=%JAVARIFIER_DIR%lib

java -ea -cp "%JAVARIFIER_LIB%\javarifier.jar" javarifier.Main "-defaultStubCPEntries" "%JAVARIFIER_DIR%annotated-jdk" "-defaultWorldCPEntries" "%CLASSPATH%" "-programCPEntries" "%CLASSPATH%" %*
