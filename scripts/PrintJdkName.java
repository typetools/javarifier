
import java.util.regex.Pattern;
import java.util.regex.Matcher;
/**
 * Prints the value of property "java.home", which is the location
 * where the JRE is installed.
 */
public class PrintJdkName {
    public static void main(String[] args) {
        System.out.println(findJdkJarName(getJreVersion()));
    }

    /**
     * Extract the first two version numbers from java.version (e.g. 1.6 from 1.6.whatever)
     * @return The first two version numbers from java.version (e.g. 1.6 from 1.6.whatever)
     */
    private static double getJreVersion() {
        final Pattern versionPattern = Pattern.compile("^(\\d\\.\\d+)\\..*$");
        final String  jreVersionStr = System.getProperty("java.version");
        final Matcher versionMatcher = versionPattern.matcher(jreVersionStr);

        final double version;
        if(versionMatcher.matches()) {
            version = Double.parseDouble(versionMatcher.group(1));
        } else {
            throw new RuntimeException("Could not determine version from property java.version=" + jreVersionStr);
        }

        return version;
    }


    /**
     * Determine the version of the JRE that we are currently running and select a jdk<V>.jar where
     * <V> is the version of java that is being run (e.g. 6, 7, ...)
     * @return The jdk<V>.jar where <V> is the version of java that is being run (e.g. 6, 7, ...)
     */
    private static String findJdkJarName(final Double jreVersion) {
        final String fileName;
        if(jreVersion == 1.4 || jreVersion == 1.5 || jreVersion == 1.6) {
            fileName = "jdk6.jar";
        } else if(jreVersion == 1.7) {
            fileName = "jdk7.jar";
        } else if(jreVersion == 1.8) {
            fileName = "jdk8.jar";
        } else {
            throw new AssertionError("Unsupported JRE version: " + jreVersion);
        }

        return fileName;
    }
}
