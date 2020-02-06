package soot.asm.frontend;

import soot.*;
import soot.options.Options;

import java.io.File;

/**
 * Contains helper methods for ASM frontend tests.
 */
public class TestHelper {

    /**
     * Loads the specified class from the test-classes path with default Java version into the scene.
     *
     * @param targetClass  The class to load.
     */
    public static void loadClass(String targetClass) {
        loadClass(targetClass, (new File("./target/test-classes")).getAbsolutePath());
    }

    /**
     * Loads the specified class with default Java version into the scene.
     *
     * @param targetClass  The class to load.
     * @param classPath    The class path to load the class from.
     */
    public static void loadClass(String targetClass, String classPath) {
        loadClass(targetClass, classPath, "default");
    }

    /**
     * Loads the specified class into the scene.
     *
     * @param targetClass  The class to load.
     * @param classPath    The class path to load the class from.
     * @param javaVersion  The required Java version.
     */
    public static void loadClass(String targetClass, String classPath, String javaVersion) {
        G.reset();
        // Location of the rt.jar
        String rtJar = "";
        if (Scene.isJavaGEQ9(System.getProperty("java.version"))) {
            rtJar = ModulePathSourceLocator.DUMMY_CLASSPATH_JDK9_FS;
        } else {
            rtJar = System.getProperty("java.home") + File.separator + "lib" + File.separator + "rt.jar";

        }
        String classpath = classPath + File.pathSeparator + rtJar;
        System.out.println("Class path: " + classpath);

        // configure
        Options.v().parse(new String[] { "-cp", classpath, "-src-prec", "only-class", "-output-format", "jimple", "-allow-phantom-refs",
                "-p", "jb", "use-original-names:true", "-java-version", javaVersion, targetClass });

        // load classes
        Scene.v().loadNecessaryClasses();

        // load bodies
        for (SootClass cl : Scene.v().getApplicationClasses()) {
            for (SootMethod m : cl.getMethods()) {
                if (m.isConcrete()) {
                    m.retrieveActiveBody();
                }
            }
        }
    }
}
