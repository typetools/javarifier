
:: Run Javarifier over a set of class files and output inferred mutabilities.
:: For usage information, run: javarifier.bat --help 
:: See the javarifier.html documentation for more information.

set JAVARIFIER_DIR=%~d0
set JAVARIFIER_DIR=%JAVARIFIER_DIR%%~p0
set JAVARIFIER_LIB=%JAVARIFIER_DIR%lib

java -cp "%JAVARIFIER_LIB%\javarifier.jar;%JAVARIFIER_LIB%\jasminclasses-2.2.3.jar;%JAVARIFIER_LIB%\polyglotclasses-1.3.2.jar;%JAVARIFIER_LIB%\sootclasses-2.2.3.jar" javarifier.Main "-defaultStubCPEntries" "%JAVARIFIER_DIR%annotated-jdk" "-defaultWorldCPEntries" "%CLASSPATH%" "-programCPEntries" "%CLASSPATH%" %*
