/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.xmlvm;

// IMPORTANT! You need to compile this class against ant.jar.
// The easiest way to do this is to add ${ant.core.lib} to your project's classpath.
// For example, for a plain Java project with no other dependencies, set in project.properties:
// javac.classpath=${ant.core.lib}
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.types.Commandline.Argument;

import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.ZipFileSet;
import org.apache.tools.ant.types.selectors.FileSelector;
import org.apache.tools.ant.types.selectors.FilenameSelector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author shannah
 */
public class XMLVM extends Task {

    private Javac javac;
    private Path classPath;
    private Path src;
    private File dest;
    private File javaBuildDir;
    private String target;
    private File xmlvmCacheDir;

    private Path xmlvmClasspath;
    
    private Map<String, ClassFile> classIndex = new HashMap<String, ClassFile>();
    
    

    /**
     * @return the javac
     */
    public Javac getJavac() {
        return javac;
    }

    /**
     * @param javac the javac to set
     */
    public void setJavac(Javac javac) {
        this.javac = javac;
    }

    /**
     * @return the classPath
     */
    public Path getClassPath() {
        return classPath;
    }

    /**
     * @param classPath the classPath to set
     */
    public void setClassPath(Path classPath) {
        this.classPath = classPath;
    }

    /**
     * @return the src
     */
    public Path getSrc() {
        return src;
    }

    /**
     * @param src the src to set
     */
    public void setSrc(Path src) {
        this.src = src;
    }

    /**
     * @return the dest
     */
    public File getDest() {
        return dest;
    }

    /**
     * @param dest the dest to set
     */
    public void setDest(File dest) {
        this.dest = dest;
    }

    /**
     * @return the javaBuildDir
     */
    public File getJavaBuildDir() {
        return javaBuildDir;
    }

    /**
     * @param javaBuildDir the javaBuildDir to set
     */
    public void setJavaBuildDir(File javaBuildDir) {
        this.javaBuildDir = javaBuildDir;
    }

    /**
     * @return the target
     */
    public String getTarget() {
        return target;
    }

    /**
     * @param target the target to set
     */
    public void setTarget(String target) {
        this.target = target;
    }

    /**
     * @return the classIndex
     */
    public Map<String, ClassFile> getClassIndex() {
        return classIndex;
    }

    /**
     * @param classIndex the classIndex to set
     */
    public void setClassIndex(Map<String, ClassFile> classIndex) {
        this.classIndex = classIndex;
    }

    /**
     * @return the xmlvmClasspath
     */
    public Path getXmlvmClasspath() {
        return xmlvmClasspath;
    }

    /**
     * @param xmlvmClasspath the xmlvmClasspath to set
     */
    public void setXmlvmClasspath(Path xmlvmClasspath) {
        this.xmlvmClasspath = xmlvmClasspath;
    }

    private class RootedFile {

        final File root;
        final File file;

        RootedFile(File root, File file) {
            this.root = root;
            this.file = file;
        }
    }

    public void runXmlvm(String[] args) {
        Path path = new Path(getProject(), System.getProperty("java.class.path"));
        if ( xmlvmClasspath != null ){
            path.add(xmlvmClasspath);
        }
        System.out.println("Checking classpath for xmlvm.jar: "+path);
        String xmlvm = null;
        for (String p : path.list()) {
            if (p.equals("xmlvm.jar") || p.endsWith(File.separator + "xmlvm.jar") ) {
                xmlvm = p;
            }
        }
        if (xmlvm == null) {
            throw new RuntimeException("Could not find XMLVM ");

        }
        runXmlvm(args, new File(xmlvm));

    }

    public void runXmlvm(String[] args, File xmlvmJar) {
        try {
            //System.out.println(getProject().getProperties());
            //System.out.println(this.getClassPath());
            //System.out.println(System.getProperties());
            Java j = (Java) getProject().createTask("java");
            j.setMaxmemory("2G");
            j.setJar(xmlvmJar);
            //j.setFailonerror(true);
            File log = File.createTempFile("foo", "bar");
            j.setOutput(log);
            j.setFork(true);
            for (String arg : args) {
                Argument a = j.createArg();
                a.setValue(arg);
                
            }
            System.out.println("About to execute XMLVM...");
            j.execute();
            System.out.println(FileUtils.readFileToString(log));
            System.out.println("Finished execution of XMLVM");
        } catch (IOException ex) {
            Logger.getLogger(XMLVM.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private class ClassFile {

        private String name;
        private String cPrefix;
        private String javaSourcePath;
        private String javaClassPath;

        private File javaSourceFile;
        private File javaClassFile;
        private File cHeaderFile;
        private File cBodyFile;

        ClassFile(String name) {
            this.name = name;
            this.cPrefix = classNameToCPrefix(name);
            this.javaClassPath = classNameToJavaClassPath(name);
            this.javaSourcePath = classNameToJavaSourcePath(name);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ClassFile) {
                ClassFile o = (ClassFile) obj;
                return "name".equals(o.getName());
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 43 * hash + Objects.hashCode(this.getName());
            return hash;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @param name the name to set
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * @return the cPrefix
         */
        public String getcPrefix() {
            return cPrefix;
        }

        /**
         * @param cPrefix the cPrefix to set
         */
        public void setcPrefix(String cPrefix) {
            this.cPrefix = cPrefix;
        }

        /**
         * @return the javaSourcePath
         */
        public String getJavaSourcePath() {
            return javaSourcePath;
        }

        /**
         * @param javaSourcePath the javaSourcePath to set
         */
        public void setJavaSourcePath(String javaSourcePath) {
            this.javaSourcePath = javaSourcePath;
        }

        /**
         * @return the javaClassPath
         */
        public String getJavaClassPath() {
            return javaClassPath;
        }

        /**
         * @param javaClassPath the javaClassPath to set
         */
        public void setJavaClassPath(String javaClassPath) {
            this.javaClassPath = javaClassPath;
        }

        /**
         * @return the javaSourceFile
         */
        public File getJavaSourceFile() {
            return javaSourceFile;
        }

        /**
         * @param javaSourceFile the javaSourceFile to set
         */
        public void setJavaSourceFile(File javaSourceFile) {
            this.javaSourceFile = javaSourceFile;
        }

        /**
         * @return the javaClassFile
         */
        public File getJavaClassFile() {
            return javaClassFile;
        }

        /**
         * @param javaClassFile the javaClassFile to set
         */
        public void setJavaClassFile(File javaClassFile) {
            this.javaClassFile = javaClassFile;
        }

        /**
         * @return the cHeaderFile
         */
        public File getcHeaderFile() {
            return cHeaderFile;
        }

        /**
         * @param cHeaderFile the cHeaderFile to set
         */
        public void setcHeaderFile(File cHeaderFile) {
            this.cHeaderFile = cHeaderFile;
        }

        /**
         * @return the cBodyFile
         */
        public File getcBodyFile() {
            return cBodyFile;
        }

        /**
         * @param cBodyFile the cBodyFile to set
         */
        public void setcBodyFile(File cBodyFile) {
            this.cBodyFile = cBodyFile;
        }

    }

    private String classNameToJavaSourcePath(String className) {
        return className.replaceAll("\\.", "/") + ".java";
    }

    private String classNameToJavaClassPath(String className) {
        return className.replaceAll("\\.", "/") + ".class";
    }

    private String classNameToCPrefix(String className) {
        return className.replaceAll("\\.", "_").replaceAll("\\$", "_");
    }

    private ClassFile getClassFile(File root, File file) {
        String className = getClassName(root, file);
        if (className == null) {
            return null;
        }
        if (getClassIndex().containsKey(className)) {
            return getClassIndex().get(className);
        } else {
            ClassFile f = new ClassFile(className);
            getClassIndex().put(className, f);
            return f;
        }
    }

    private String getClassName(File root, File file) {
        String fname = file.getName();
        if (fname.endsWith(".java")) {
            String subpath = file.getAbsolutePath().substring(root.getAbsolutePath().length());
            subpath = subpath.replaceAll(File.separator, ".");
            if (subpath.startsWith(".")) {
                subpath = subpath.substring(1);
            }
            //System.out.println("Getting class name for "+file+" : "+subpath.replaceAll("\\.java$", ""));
            return subpath.replaceAll("\\.java$", "");
        } else if (fname.endsWith(".class")) {
            String subpath = file.getAbsolutePath().substring(root.getAbsolutePath().length());
            //if (subpath.indexOf("$") != -1) {
            //    // It's an internal class.. not interested
            //    return null;
            //}
            subpath = subpath.replaceAll(File.separator, ".");
            if (subpath.startsWith(".")) {
                subpath = subpath.substring(1);
            }
            return subpath.replaceAll("\\.class$", "");
        } else if (fname.endsWith(".m")) {
            return fname.replaceAll("_", ".").replaceAll("\\.m$", "");
        } else if (fname.endsWith(".h")) {
            return fname.replaceAll("_", ".").replaceAll("\\.h$", "");
        } else if (fname.endsWith(".c")) {
            return fname.replaceAll("_", ".").replaceAll("\\.c$", "");
        } else if (fname.endsWith(".xmlvm")) {
            return fname.replaceAll("_", ".").replaceAll("\\.xmlvm$", "");
        } else {
            return null;
        }
    }

    private void getChangedFiles(File root, File file, List<RootedFile> out) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                //System.out.println("Checking for changed files in "+child);
                getChangedFiles(root, child, out);
            }
        } else {
            long mtime = file.lastModified();
            ArrayList<File> matches = new ArrayList<File>();
            //System.out.println("Looking for matching target files for "+file);
            findMatchingTargetFiles(getClassFile(root, file), matches);
            boolean changed = false;
            if (matches.isEmpty()) {
                changed = true;
            }
            for (File f : matches) {
                if (f.lastModified() < mtime) {
                    changed = true;
                    break;
                }
            }
            if (changed) {

                out.add(new RootedFile(root, file));
            }
        }
    }

    private void findMatchingTargetFiles(final ClassFile clsFile, List<File> out) {
        if (clsFile == null) {
            return;
        }
        File[] files = getDest().listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return pathname.getName().startsWith(clsFile.getcPrefix());
            }

        });

        for (File f : files) {
            out.add(f);
        }
    }

    private RootedFile[] changedSourceFiles = null;

    private RootedFile[] getChangedSourceFiles() {
        if (changedSourceFiles == null) {
            List<RootedFile> out = new ArrayList<RootedFile>();

            for (String path : getSrc().list()) {
                File root = new File(path);
                if (root.isDirectory()) {
                    //System.out.println("Checking for changed files in "+root);
                    getChangedFiles(root, root, out);
                }
            }
            changedSourceFiles = out.toArray(new RootedFile[0]);
            System.out.println("Found " + changedSourceFiles.length + " changed files");
        }
        return changedSourceFiles;

    }

    private RootedFile[] getChangedClassFiles() {
        List<RootedFile> out = new ArrayList<RootedFile>();

        for (String path : getClassPath().list()) {
            File root = new File(path);
            if (root.isDirectory()) {
                getChangedFiles(root, root, out);
            }
        }
        return out.toArray(new RootedFile[0]);
    }
    
    /**
     * Collects all of the classes that may be dirty due to changes in a specified
     * set of classes.
     * @param changedClasses
     * @return The set of dirty classes.  This is always a superset of changedClasses.
     * @throws IOException 
     */
    public Set<String> collectDirtyClasses(Set<String> changedClasses) throws IOException{
        HashSet<String> dirtyClasses = new HashSet<String>();
        collectDirtyClasses(changedClasses, dirtyClasses);
        return dirtyClasses;
    }
    
    
    public void collectDirtyClasses(
            Set<String> changedClasses,
            Set<String> dirtyClasses) throws IOException {
        File xmlvmDir = this.getXmlvmCacheDir("xmlvm");
        collectDirtyClasses(changedClasses, dirtyClasses, xmlvmDir);
    }
    
    /**
     * Finds all of the classes that may be dirty due to changes in 
     * an initial set of classes.
     * @param changedClasses An initial set of classes that were modified.
     * @param dirtyClasses The full set of classes that may be dirty due to these
     * changes.  This will necessarily be a superset of changedClasses.  
     * @param depsDir The directory the contains the deps.
     * @throws IOException 
     */
    public void collectDirtyClasses(
            Set<String> changedClasses, 
            Set<String> dirtyClasses, 
            File depsDir) throws IOException{
        
        Set<String> processed = new HashSet<String>();
        Stack<String> stack = new Stack<>();
        stack.addAll(changedClasses);
        dirtyClasses.addAll(changedClasses);
        while ( !stack.isEmpty() ){
            String cls = stack.pop();
            if ( processed.contains(cls)){
                continue;
            } else {
                processed.add(cls);
            }
            File depsFile = new File(depsDir, cls+".deps");
            if ( depsFile.exists()){
                List<String> lines = FileUtils.readLines(depsFile);
                for ( String line : lines ){
                    String[] parts = line.split(" ");
                    String clsName = parts[0];
                    if ( dirtyClasses.contains(clsName)){
                        // This class is already marked dirty.
                        continue;
                    }
                    String kind = parts[1];
                    
                    switch ( kind ){
                        case "usage":
                            dirtyClasses.add(clsName);
                            break;
                        case "super":
                        case "interface":
                            dirtyClasses.add(clsName);
                            stack.push(clsName);
                            break;
                    }
                }
            }
        }
        
        
    }
    
    public Set<String> getChangedClassNames(){
        Set<String> out = new HashSet<String>();
        RootedFile[] changedClassFiles = getChangedClassFiles();
        for ( RootedFile rf : changedClassFiles ){
            ClassFile cf = getClassFile(rf.root, rf.file);
            if ( cf != null ){
                out.add(cf.getName());
            }

        }
        return out;
    }
    
    public void findClassesInPath(Path p, Collection<String> classNames, Set<String> found, File destDir) throws IOException{
        for ( String part : p.list() ){
            File f = new File(part);
            if ( f.isDirectory()){
                // We have a directory
                findClassesInPath(f, f, classNames, found, destDir);
            } else if ( f.getName().endsWith(".jar")) {
                // We have a jar file
                JarFile jf = new JarFile(f);
                Enumeration<JarEntry> entries = jf.entries();
                
                Expand expand = (Expand)getProject().createTask("unzip");
                expand.setSrc(f);
                expand.setDest(destDir);
                boolean emptySet = true;
                
                while ( entries.hasMoreElements() ){
                    JarEntry nex = entries.nextElement();
                    if ( nex.getName().endsWith(".class") ){
                        String foundClassName = nex.getName().replaceAll("/", ".").replaceAll("\\.class$", "");
                        if ( classNames.contains(foundClassName)){
                            found.add(foundClassName);
                            FilenameSelector sel = new FilenameSelector();
                            sel.setName(nex.getName());
                            ZipFileSet fs = new ZipFileSet();
                            fs.setFullpath(nex.getName());
                            expand.addFileset(fs); 
                            emptySet = false;
                        }
                    }
                }
                
                if ( !emptySet ){
                    expand.execute();
                }
                
            }
        }
    }
    
    private void findClassesInPath(File root, File dir, Collection<String> classNames, Set<String> found, File destDir) throws IOException{
        for ( File f : dir.listFiles() ){
            if ( f.isDirectory()){
                findClassesInPath(root, f, classNames, found, destDir);
            } else {
                ClassFile cf = getClassFile(root, f);
                if ( cf != null && classNames.contains(cf.getName())){
                    File destFile = new File(destDir, cf.getJavaClassPath());
                    destFile.getParentFile().mkdirs();
                    FileUtils.copyFile(f, destFile);
                    found.add(cf.getName());
                }
            }
        }
    }
    
    public void doRegularBuild() throws IOException {
        try {
            //if (getJavac() == null) {
            //    setJavac((Javac) getProject().createTask("javac"));
            //}
            Javac javac = (Javac)getProject().createTask("javac");
            

            // Create a temporary directory as a destination for javac 
            File tmpBuild = File.createTempFile("build", "build");
            tmpBuild.delete();
            tmpBuild.mkdir();

            // Create a temporary directory to contain only the .java files that
            // have changed.
            File changedSrcDir = setupChangedSourcesDir();
            System.out.println("Found "+changedSrcDir.list().length+" changed files");

            javac.setDestdir(tmpBuild);
            
            javac.setSrcdir(new Path(getProject(), changedSrcDir.getAbsolutePath()));
            try {
                System.out.println("Classpath currently "+getJavac().getClasspath());
                System.out.println("Adding to classpath: "+getClassPath());
                if ( !getClassPath().equals(javac.getClasspath())){
                    javac.setClasspath(getClassPath());
                }
            } catch ( Exception ex){
                System.out.println(getClassPath());
                throw ex;
            }
            javac.getClasspath().add(new Path(getProject(), getJavaBuildDir().getAbsolutePath()));
            
            System.out.println("Running javac on changed sources");
            System.out.println("Src dir is "+javac.getSrcdir());
            javac.execute();

            // Copy the compiled files to the intermediate build dir
            
            System.out.println("Copying compiled sources to "+getJavaBuildDir());
            Copy copy = (Copy) getProject().createTask("copy");
            copy.setTodir(getJavaBuildDir());
            FileSet fs = new FileSet();
            fs.setDir(tmpBuild);
            fs.setIncludes("**");
            copy.addFileset(fs);
            copy.execute();
            
            
            // Now let's find out which classes may need to be updated due to our
            // changes
            Set<String> changedClasses = getChangedClassNames();
            System.out.println("Found "+changedClasses.size()+" changed classes.  Looking for dirty classes...");
            Set<String> dirtyClasses = collectDirtyClasses(changedClasses);
            
            System.out.println("Found "+dirtyClasses.size()+" dirty classes.");
            
            // Now try to find the dirty classes
            Set<String> found = new HashSet<String>();
            System.out.println("Locating dirty classes and copying to "+tmpBuild);
            this.findClassesInPath(javac.getClasspath(), dirtyClasses, found, tmpBuild);
            
            if ( found.size() != dirtyClasses.size()){
                
                Set<String> missing = new HashSet<String>();
                missing.addAll(dirtyClasses);
                missing.removeAll(found);
                System.out.println("Missing classes : "+missing);
                System.out.println("Classpath is "+javac.getClasspath());
                System.out.println("Missing include "+missing);
                throw new RuntimeException("Failed to find all dirty classes that need to be compiled : "+found.size()+" vs "+dirtyClasses.size());
                
            }
            
            
            


            // Now we should have all we need to generate our C source files
            // Delete the changed source dir, since we don't need it anymore
            System.out.println("Deleting "+changedSrcDir+".  We don't need it anymore.");
            Delete del = (Delete) getProject().createTask("delete");
            del.setDir(changedSrcDir);
            del.execute();

            File intermediateOut = this.getXmlvmCacheDir("c");
            System.out.print("Deleting intermediate build directory...");
            FileUtils.deleteDirectory(intermediateOut);
            System.out.println("Finished");
            
            intermediateOut.mkdirs();

            //System.out.println("Intermediates: ");
            //for ( File f : tmpOutput.listFiles()){
            //    System.out.println(f);
            //}
            
            if ( tmpBuild.list().length < 5 ){
                System.out.println(Arrays.asList(tmpBuild.list()));
            }
            System.out.println("Converting "+tmpBuild.list().length+" xmlvm files to c source files....");
            this.runXmlvm(new String[]{
                "--in=" + tmpBuild.getAbsolutePath(),
                "--out=" + intermediateOut.getAbsolutePath(),
                "--target=c",
                "--libraries=" + javac.getClasspath().toString().replaceAll(File.pathSeparator, ","),
                "--c-source-extension=m", //"--debug=all",
            });

            System.out.println(Arrays.asList(intermediateOut.list()));
            
            del = (Delete) getProject().createTask("delete");
            del.setDir(tmpBuild);
            del.execute();

            System.out.print("Removing constant pool dependencies...");
            this.removeConstantPoolDependencies(intermediateOut);
            System.out.println("Finished.");
            
            System.out.print("Fixing vtable references...");
            this.fixVtableReferences(intermediateOut, getDest());
            System.out.println("Finished.");
            
            System.out.print("Copying "+dirtyClasses.size()+" changed classes to "+getDest()+"...");
            copyClasses(dirtyClasses, intermediateOut, getDest());
            System.out.println("Finished.");
            
            //copyChangedSource(intermediateOut, getDest());

        } catch (Exception ex) {
            Logger.getLogger(XMLVM.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    /**
     * Copies converted .h and .m classes from srcDir to destDir
     * @param classes The classes to copy. These should be FQNs of java classes.
     * @param srcDir
     * @param destDir 
     */
    public void copyClasses(Collection<String> classes, File srcDir, File destDir) throws IOException{
        System.out.println("Copying "+classes.size()+" from "+srcDir+" to "+destDir);
        //Copy copy = (Copy)getProject().createTask("copy");
        //copy.setTodir(destDir);
        for ( String cls : classes ){
            ClassFile cf = new ClassFile(cls);
            
            //FileSet fs = new FileSet();
            //fs.setDir(srcDir);
            File header = new File(srcDir, cf.getcPrefix()+".h");
            File body = new File(srcDir, cf.getcPrefix()+".m");
            //fs.setIncludesfile(new File(srcDir, cf.getcPrefix()+".h"));
            //fs.setIncludesfile(new File(srcDir, cf.getcPrefix()+".m"));
            //System.out.println("Setting include to "+cf.getcPrefix());
            //fs.setIncludes("**/"+cf.getcPrefix()+".h **/"+cf.getcPrefix()+".m");
            //copy.addFileset(fs);
            
            File headerDest = new File(destDir, header.getName());
            File bodyDest = new File(destDir, body.getName());
            
            FileUtils.copyFile(header, headerDest);
            FileUtils.copyFile(body, bodyDest);
        }
        //copy.setOverwrite(true);
        //copy.execute();
    }
    
    
    /**
     * Tries to find the actual function to use for the given mangled method name in the 
     * given header file.
     * @param searchPaths  The paths to search for headers.
     * @param headerFile The header file in which to start the search.  Its name is used
     * as the base for the mangled function name.
     * @param mangledMethodName The mangled method name.  This should only be the portion
     * pertaining to the method - not the class portion of a mangled function name.  It will
     * be concatenated to headerFile.getName()+"_" to form the full function name.
     * @return Either a VTable ID (which will have the form XMLVM_VTABLE_IDX_{funcname}) or
     * the actual function name if there is no vtable index constant defined.
     * @throws IOException 
     */
    public String resolveVirtualMethod(Path searchPaths, File headerFile, String mangledMethodName) throws IOException{
        String mangledClassName = headerFile.getName().replaceAll("\\.h$", "");
        String funcName = mangledClassName+"_"+mangledMethodName;
        String headerContents = FileUtils.readFileToString(headerFile);
        Scanner scanner = new Scanner(headerContents);
        int state = 0;
        String superClassHeader = null;
        while ( scanner.hasNextLine()){
            String line = scanner.nextLine();
            if ( state == 0 && line.startsWith("// Super Class:")){
                state = 1;
            } else if ( state == 1 && line.startsWith("#include \"") ){
                superClassHeader = line.substring(line.indexOf("\"")+1, line.lastIndexOf("\""));
                state = 0;
                break;
            }
        }
        
        String idxConstant = "XMLVM_VTABLE_IDX_"+funcName;
        if ( headerContents.indexOf(idxConstant) > 0 ){
            return idxConstant;
        } else if ( headerContents.indexOf(funcName+"(") > 0 ){
            return funcName;
        } else if ( superClassHeader != null ){
            for ( String p : searchPaths.list() ){
                File d = new File(p);
                File h = new File(d, superClassHeader);
                if ( h.exists() ){
                    return resolveVirtualMethod(searchPaths, h, mangledMethodName);
                }
            }
            throw new RuntimeException("Failed to find header file "+superClassHeader);
        } else {
            throw new RuntimeException("Failed to find virtual method "+mangledMethodName+" in header "+headerFile);
        }
    }
    
    
    
    
    /**
     * Internal class to represent a constant in the constant pool.
     */
    class Constant {
        /**
         * The id of the constant
         */
        int id;
        /**
         * The string data of the constant (this will be C source code
         * for a JAVA_CHAR_ARRAY, not the actual string.
         */
        String data;
        /**
         * The length of the constant.
         */
        int len;
    }

    
    /**
     * Loads constant pool file (e.g. constant_pool.m) into a map that maps 
     * the integer id of the constant to a String of the c constant array.
     * @param file
     * @return
     * @throws IOException 
     */
    public Map<Integer,Constant> loadConstantPool(File file) throws IOException{
        Map<Integer,Constant> out = new HashMap<>();
        List<String> lines = FileUtils.readLines(file);
        Constant currConstant = null;
        
        
        for ( String line : lines ){
            if ( currConstant == null && line.startsWith("// ID=") && line.indexOf(":") > 0){
                String idStr = line.substring(line.indexOf("=")+1, line.indexOf(":"));
                int id = Integer.parseInt(idStr);
                currConstant = new Constant();
                currConstant.id = id;
            } else if ( currConstant != null && line.startsWith("(JAVA_ARRAY_CHAR[]) {")){
                String val = line.substring(0, line.lastIndexOf(","));
                //val = val.replaceAll("\\}", ",0}");
                //val = val.replaceAll("\\{,", "{");
                currConstant.data = val;
                out.put(currConstant.id, currConstant);
                currConstant = null;
            } else if ( currConstant == null && line.startsWith("// ID=")){
                String idStr = line.substring(line.indexOf("=")+1);
                int id = Integer.parseInt(idStr);
                currConstant = out.get(id);
                if ( currConstant == null ){
                    throw new RuntimeException("Failed to find constant with id "+id+" in constant pool when setting length");
                }
                
            } else if ( currConstant != null && line.matches("^\\d+,[ ]*$")){
                currConstant.len = Integer.parseInt(line.substring(0, line.indexOf(",")));
                currConstant = null;
            }
        }
        return out;
        
    }
    
    
    public void removeConstantPoolDependencies(Map<Integer,Constant> constantPool, File file) throws IOException{
        String strPattern = "xmlvm_create_java_string_from_pool\\((\\d+)\\)";
        Pattern regex = Pattern.compile(strPattern);
        
        
        List<String> lines = FileUtils.readLines(file);
        List<String> out = new ArrayList<String>();
        boolean changed = false;
        for ( String line : lines ){
            if ( line.indexOf("xmlvm_create_java_string_from_pool") != -1 ){
                Matcher m = regex.matcher(line);
                if ( m.find() ){
                    String idStr = m.group(1);
                    Constant theConstant = constantPool.get(Integer.parseInt(idStr));
                    String strVal = theConstant.data;
                    int len = theConstant.len;
                    if ( strVal == null ){
                        throw new RuntimeException("Constant pool does not contain string id "+idStr);
                    }
                    changed = true;
                    line = line.replaceAll(strPattern, "xmlvm_create_java_string_from_char_array((JAVA_OBJECT)"+strVal+","+len+")");
                }
            }
            out.add(line);
        }
        
        if ( changed ){
            FileUtils.writeLines(file, out);
        }
        
        
    }
    
    /**
     * Removes dependencies on the constant pool file for all .m, .c, and .h files in the 
     * given directory.  It will use the constant_pool.m file for that directory.
     * @param srcDir
     * @throws IOException 
     */
    public void removeConstantPoolDependencies(File srcDir) throws IOException{
        File poolFile = new File(srcDir, "constant_pool.m");
        Map<Integer,Constant> constantPool = loadConstantPool(poolFile);
        for ( File f : srcDir.listFiles() ){
            if ( f.isFile() && (f.getName().endsWith(".m") || f.getName().endsWith(".c") || f.getName().endsWith(".h"))){
                removeConstantPoolDependencies(constantPool, f);
            }
        }
    }
    
    
    
    
    /**
     * Fixes the vTable references for a .c (or .m) source file.
     * @param file
     * @param cFilesDirectory
     * @throws IOException 
     */
    public void fixVtableReferences(File file, File cFilesDirectory) throws IOException{
        if ( file.isDirectory()){
            for ( File child : file.listFiles() ){
                
                fixVtableReferences(child, cFilesDirectory);
            }
            return;
        }
        
        if ( file.getName().endsWith(".m") || file.getName().endsWith(".c")){
            
        } else {
            return;
        }
        
        List<String> lines = FileUtils.readLines(file);
        List<String> out = new ArrayList<String>();
        
        // Pattern to match vtable error comments.  group(1) will contain function name
        String pattern = "^//([a-zA-Z][a-zA-Z0-9_]+)\\[-1]$";
        Pattern regex = Pattern.compile(pattern);
        
        // Pattern to match class name on a line that has a missing vtable entry
        // group(1) will contain the c prefix for the class name
        String clsPattern = "\\(\\(([a-zA-Z][a-zA-Z0-9_]*)\\*\\) _r\\d+\\.o\\)->tib->vtable";
        Pattern clsRegex = Pattern.compile(clsPattern);
        
        String vtableLinePattern = "\\(\\*\\(.*\\)->tib->vtable\\[-1\\]\\)";
        Pattern vtableLineRegex = Pattern.compile(vtableLinePattern);
        
        boolean inErrorSection = false;
        String functionName = null;
        for ( String line : lines ){
            if ( line.startsWith("XMLVM_ERROR(\"Missing @vtable-index\"")){
                inErrorSection = true;
                line = "//"+line;
                
            } else if ( inErrorSection ) {
                if ( functionName == null ){
                    Matcher m = regex.matcher(line.trim());
                    if ( m.find() ){
                        functionName = m.group(1);
                    }
                    
                } else {
                    if ( line.indexOf("->tib->vtable[-1]") > 0 ){
                        //line = line.replaceAll("->tib->vtable\\[-1\\]", "->tib->vtable["+)
                        
                        //String vtableIdxPattern = "XMLVM_VTABLE_IDX_"+functionName;
                        boolean foundVtableEntry = false;
                        // Need to find the class name because we need to check
                        // if the class has a vtable entry defined for this method.
                        Matcher m = clsRegex.matcher(line);
                        if ( m.find() ){
                            String clsName = m.group(1);
                            String mangledMethodName = functionName.substring(clsName.length()+1);
                            File clsHeaderFile = new File(cFilesDirectory, clsName+".h");
                            
                            
                            if ( clsHeaderFile.exists() ){
                                Path srcPaths = new Path(getProject());
                                srcPaths.add(new Path(getProject(), file.getAbsolutePath()));
                                srcPaths.add(new Path(getProject(), cFilesDirectory.getAbsolutePath()));
                                String resolvedFunction = resolveVirtualMethod(srcPaths, clsHeaderFile, mangledMethodName);
                                if ( resolvedFunction.startsWith("XMLVM_VTABLE_IDX") ){
                                    line = line.replaceAll("->tib->vtable\\[-1\\]", "->tib->vtable["+resolvedFunction+"]");
                                    foundVtableEntry = true;
                                } else {
                                    line = line.replaceAll(vtableLinePattern, resolvedFunction);
                                }

                            } else {
                                throw new RuntimeException("No class header found for class "+clsName);
                            }
                        } else {
                            throw new RuntimeException("Found missing vtable entry but regex failed to find class name: "+line);
                        }
                        
                        functionName = null;
                        inErrorSection = false;
                        
                    }
                }
            }
            out.add(line);
        }
        
        FileUtils.writeLines(file, out);
        
    }
    
    public void doCleanBuild() throws IOException{
        
        
        // Step 1:  Clear out the XMLVM cache dirs (used for dependency analysis)
        
        File xmlvmDir = this.getXmlvmCacheDir("xmlvm");
        System.out.print("Deleting old xmlvm cache directory...");
        FileUtils.deleteDirectory(xmlvmDir);
        System.out.println("Finished.");
        xmlvmDir.mkdirs();
        
        // Step 2:  Compile java source files with javac.
        if (getJavac() == null) {
            setJavac((Javac) getProject().createTask("javac"));
        }
        getJavac().setDestdir(getJavaBuildDir());
        getJavac().setSrcdir(getSrc());
        getJavac().setClasspath(getClassPath());
        getJavac().execute();

        
        // Step 3: Generate Dependency graph
        createDependencyGraph(getJavaBuildDir(), xmlvmDir);
        
        
        // Step 4: Do a full XMLVM to C compilation
        
        // Delete the destination directory via rename
        System.out.print("Deleting destination directory...");
        FileUtils.deleteDirectory(getDest());
        System.out.println("Finished");
        
        
        
        this.runXmlvm(new String[]{
                "--in=" + getJavaBuildDir().getAbsolutePath(),
                "--out=" + getDest().getAbsolutePath(),
                "--target=c",
                "--libraries=" + getJavac().getClasspath().toString().replaceAll(File.pathSeparator, ","),
                "--c-source-extension=m",// "--debug=all",
                "--load-dependencies"
            });
        
        
        
    }
    
    /**
     * 
     * @param src Directory containing class files.
     * @param dest Directory where xmlvm files will be written, as well as .deps files
     */
    private void createDependencyGraph(File src, File dest){
        try {
            this.runXmlvm(new String[]{
                "--in=" + src.getAbsolutePath(),
                "--out=" + dest.getAbsolutePath(),
                "--target=vtable",
                "--libraries=" + getJavac().getClasspath().toString().replaceAll(File.pathSeparator, ","),
                "--load-dependencies"
            });
            
            for ( File f : dest.listFiles()){
                if ( !f.getName().endsWith(".xmlvm")){
                    continue;
                }
                addFileToDependencyGraph(f, dest);
            }
            
        } catch (Exception ex) {
            Logger.getLogger(XMLVM.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Almost the same as creating a dependency graph except that we assume that
     * the src directory only contains a subset of the files to convert.  It involves
     * Comparing the previous references of the files in the src dir to see if anything
     * has changed.
     * @param src Directory containing class files that have changed.
     * @param dest Directory containing xmlvm files and .deps files.
     */
    private void updateDependencyGraph(File src, File dest) throws IOException{
        
        File tmpDir = File.createTempFile("dep", "dir");
        try {
            tmpDir.delete();
            tmpDir.mkdir();
            this.runXmlvm(new String[]{
                    "--in=" + src.getAbsolutePath(),
                    "--out=" + tmpDir.getAbsolutePath(),
                    "--target=vtable",
                    //"--libraries=" + getJavac().getClasspath().toString().replaceAll(File.pathSeparator, ","),
                    //"--load-dependencies"
                });

            for ( File f : tmpDir.listFiles()){
                if ( f.getName().endsWith(".xmlvm")){
                    File oldXmlvmFile = new File(dest, f.getName());
                    Map<String,String> newRefs = loadReferences(f);
                    Map<String,String> oldRefs = null;
                    if ( oldXmlvmFile.exists()){
                        oldRefs = loadReferences(oldXmlvmFile);

                    } else {
                        oldRefs = new HashMap<>();
                    }
                    boolean changed = false;
                    if ( !oldRefs.entrySet().containsAll(newRefs.entrySet())){
                        changed = true;
                    }
                    if ( !changed && !newRefs.entrySet().containsAll(oldRefs.entrySet())){
                        changed = true;
                    }

                    if ( changed ){
                        updateDependencyGraphForFile(f, oldRefs, dest);
                        

                    }
                    FileUtils.copyFile(src, oldXmlvmFile);

                }

            }
        } finally {
        
            FileUtils.deleteDirectory(tmpDir);
        }
    }
    
    /**
     * Updates the dependency graph with new information for an XMLVM file.
     * 
     * @param xmlvmFile The xmlvm file that is being added to the dependency graph.
     * @param oldReferences Map of the previous references that this file has.  The inverse dependencies
     * Will need to be removed.
     * @param destDir The directory where dependencies are stored
     * @throws IOException 
     */
    private void updateDependencyGraphForFile(File xmlvmFile, Map<String,String> oldReferences, File destDir) throws IOException{
        try {
            String ns = "http://xmlvm.org";
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(xmlvmFile);
            
            NodeList classes = doc.getElementsByTagName("vm:class");
            if ( classes.getLength() == 0 ){
                return;
            }
            
            Element clsEl = (Element)classes.item(0);
            StringBuilder sb = new StringBuilder().append(clsEl.getAttribute("package"));
            if ( sb.length() > 0 ){
                sb.append(".");
            }
            sb.append(clsEl.getAttribute("name"));
            String fqn = sb.toString();
            
            // Remove old references
            for ( Map.Entry<String,String> entry : oldReferences.entrySet() ){
                String cls = entry.getKey();
                String kind = entry.getValue();
                File depsFile = new File(destDir, cls+".deps");
                if ( depsFile.exists()){
                    List<String> deps = FileUtils.readLines(depsFile);
                    List<String> strippedDeps = new ArrayList<String>();
                    for ( String line : deps ){
                        if ( line.equals(cls+" "+kind)){
                            // Do nothing
                        } else {
                            strippedDeps.add(line);
                        }
                    }
                    FileUtils.writeLines(depsFile,strippedDeps,false);
                }
            }
            
            // Now add new references
            
            
            
            NodeList refs = doc.getElementsByTagName("vm:reference");
            //System.out.println("Refs len: " + refs.getLength());
            int len = refs.getLength();
            for (int i = 0; i < len; i++) {
                Element el = (Element) refs.item(i);
                String className = el.getAttribute("name");
                File depsFile = new File(destDir, className+".deps");
                if ( !depsFile.exists() ){
                    depsFile.createNewFile();
                }
                String usage = el.getAttribute("kind");
                
                FileUtils.writeStringToFile(depsFile, fqn+" "+usage+"\n", true);
                
            }
        } catch (ParserConfigurationException | SAXException ex) {
            Logger.getLogger(XMLVM.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        } 
    }
    
    /**
     * Loads the class references from an xmlvm file into a Map that maps from 
     * class name to reference kind.
     * @param file An .xmlvm file.
     * @return Map from class name to reference kind.
     * @throws IOException 
     */
    private Map<String,String> loadReferences(File file) throws IOException{
        try {
            String ns = "http://xmlvm.org";
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(file);
            
            Map<String,String> out = new HashMap<>();
            
            NodeList refs = doc.getElementsByTagName("vm:reference");
            //System.out.println("Refs len: " + refs.getLength());
            int len = refs.getLength();
            for (int i = 0; i < len; i++) {
                Element el = (Element) refs.item(i);
                String className = el.getAttribute("name");
                
                String usage = el.getAttribute("kind");
                out.put(className, usage);
                
            }
            return out;
        } catch (ParserConfigurationException | SAXException ex) {
            Logger.getLogger(XMLVM.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        } 
    }
    
    /**
     * Returns a map of the classes that depend upon the given class.
     * @param className
     * @param dir The directory containing the .deps files.
     * @return Map mapping class names to dependency kind.  Returns null if dependencies haven't been generated yet.
     * @throws IOException 
     */
    private Map<String,String> loadDependencies(String className, File dir) throws IOException{
        File depsFile = new File(dir, className+".deps");
        if ( !depsFile.exists() ){
            return null;
        }
        Map<String,String> out = new HashMap<String,String>();
        List<String> lines = FileUtils.readLines(depsFile);
        for ( String line : lines ){
            String[] parts = line.split(" ");
            out.put(parts[0], parts[1]);
        }
        return out;
    }
    
    private void addFileToDependencyGraph(File file, File dir) throws IOException {
        try {
            String ns = "http://xmlvm.org";
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(file);
            
            NodeList classes = doc.getElementsByTagName("vm:class");
            if ( classes.getLength() == 0 ){
                return;
            }
            
            Element clsEl = (Element)classes.item(0);
            StringBuilder sb = new StringBuilder().append(clsEl.getAttribute("package"));
            if ( sb.length() > 0 ){
                sb.append(".");
            }
            sb.append(clsEl.getAttribute("name"));
            String fqn = sb.toString();
            
            NodeList refs = doc.getElementsByTagName("vm:reference");
            //System.out.println("Refs len: " + refs.getLength());
            int len = refs.getLength();
            for (int i = 0; i < len; i++) {
                Element el = (Element) refs.item(i);
                String className = el.getAttribute("name");
                File depsFile = new File(dir, className+".deps");
                if ( !depsFile.exists() ){
                    depsFile.createNewFile();
                }
                String usage = el.getAttribute("kind");
                FileUtils.writeStringToFile(depsFile, fqn+" "+usage+"\n", true);
                
            }
        } catch (ParserConfigurationException | SAXException ex) {
            Logger.getLogger(XMLVM.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        } 
        
    }

   // TODO customize method names to match custom task
    // property and type (handled by inner class) names

    /* For a simple option:
     private boolean opt;
     public void setOpt(boolean b) {
     opt = b;
     }
     // <customtask opt="true"/>
     */

    /* For a simple property based on a string:
     private String myprop;
     public void setMyprop(String s) {
     myprop = s;
     }
     // <customtask myprop="some text here"/>
     */

    /* For a simple property based on a file:
     private File myfile;
     public void setMyfile(File f) {
     // Note: f will automatically be absolute (resolved from project basedir).
     myfile = f;
     }
     // <customtask myfile="foo.txt"/>
     */

    /* Custom nested elements:
     public static class Nestme {
     String val; // accessible from execute()
     public void setVal(String s) {
     val = s;
     }
     }
     private List<Nestme> nestmes = new LinkedList<Nestme>();
     public Nestme createNestme() {
     Nestme n = new Nestme();
     nestmes.add(n);
     return n;
     }
     // Or:
     public void addNestme(Nestme n) {
     nestmes.add(n);
     }
     // <customtask>
     //     <nestme val="something"/>
     // </customtask>
     */

    /* To add embedded filesets:
     private List<FileSet> filesets = new LinkedList<FileSet>();
     public void addFileset(FileSet fs) {
     filesets.add(fs);
     }
     // <customtask>
     //     <fileset dir="foo">
     //         <include name="*.txt"/>
     //     </fileset>
     // </customtask>
     // In execute() you can do:
     for (FileSet fs : filesets) {
     DirectoryScanner ds = fs.getDirectoryScanner(project);
     File basedir = ds.getBasedir();
     for (String file : ds.getIncludedFiles()) {
     // process it...
     }
     }
     */

    /* For nested text:
     private StringBuilder text;
     public void addText(String raw) {
     String s = getProject().replaceProperties(raw.trim());
     if (text == null) {
     text = new StringBuilder(s);
     } else {
     text.append(s);
     }
     }
     // <customtask>
     //     Some text...
     // </customtask>
     */

    /* Some sort of path (like classpath or similar):
     private Path path;
     public void setPath(Path p) {
     if (path == null) {
     path = p;
     } else {
     path.append(p);
     }
     }
     public Path createPath () {
     if (path == null) {
     path = new Path(project);
     }
     return path.createPath();
     }
     public void setPathRef(Reference r) {
     createPath().setRefid(r);
     }
     // <customtask path="foo:bar"/>
     // <customtask>
     //     <path>
     //         <pathelement location="foo"/>
     //     </path>
     // </customtask>
     // Etc.
     */
    /**
     * Finds all .java files that have been changed since the last compile and
     * copies them to a single source directory.
     *
     * @return
     * @throws IOException
     */
    private File setupChangedSourcesDir() throws IOException {
        File tmp = File.createTempFile("changed", "sources");
        tmp.delete();

        tmp.mkdir();
        FileSet fs = new FileSet();

        for (RootedFile f : getChangedSourceFiles()) {
            //System.out.println("FIle changed "+f.file);
            Copy copy = (Copy) getProject().createTask("copy");
            File target = tmp;
            ClassFile cf = getClassFile(f.root, f.file);
            if (cf == null) {
                if (f.file.getName().endsWith(".java")) {
                    System.err.println("Could not find class file for " + f.file);
                }
                continue;
            }
            String[] path = cf.getJavaSourcePath().split("/");
            for (int i = 0; i < path.length; i++) {
                target = new File(target, path[i]);

            }
            target.getParentFile().mkdirs();
            copy.setTofile(target);
            copy.setFile(f.file);
            copy.execute();

        }

        return tmp;

    }

    /**
     * Copies dependencies required by the changed src dir.
     * @deprecated
     * @param changedSrcDir
     */
    private void copyDependencies(File changedSrcDir) throws UnsatisfiedDependencyException {
        File cachedir = this.getXmlvmCacheDir("xmlvm");
        for (RootedFile f : this.changedSourceFiles) {
            if (!f.file.getName().endsWith(".java")) {
                continue;
            }
            ClassFile cf = getClassFile(f.root, f.file);
            if (cf == null) {
                if (f.file.getName().endsWith(".java")) {
                    System.err.println("Could not find class file for " + f.file);
                }
                continue;
            }

            String xmlvmFileName = cf.getcPrefix() + ".xmlvm";
            File cacheFile = new File(cachedir, xmlvmFileName);
            if (!cacheFile.exists()) {
                throw new UnsatisfiedDependencyException(cf.name);
            }

            // We need to find out what dependencies this file is going to have
        }
    }

    public File[] classToXMLVM(File srcDir, File targetDir) throws IOException {

        File tmp = File.createTempFile("tempbuild", "dir");
        tmp.delete();
        tmp.mkdir();
        try {
            this.runXmlvm(new String[]{"--in=" + srcDir.getAbsolutePath(), "--out=" + tmp.getAbsolutePath(), "--target=xmlvm"});
        } catch (Exception ex) {
            Logger.getLogger(XMLVM.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
        List<File> out = new ArrayList<File>();
        for (File dest : tmp.listFiles()) {
            File finalDest = new File(targetDir, dest.getName());
            out.add(finalDest);
            FileUtils.copyFile(dest, finalDest);
            dest.delete();
        }

        tmp.delete();
        return out.toArray(new File[0]);
    }

    /**
     * Generates XMLVM file for a single java class file.
     *
     * @param srcRoot The source path root.
     * @param src The source .class file
     * @param targetDir The destination directory
     * @return The converted .xmlvm files. Returns array because there could be
     * multiple (e.g. constant pool).
     * @throws IOException
     */
    public File[] generateSingleXMLVM(File srcRoot, File src, File targetDir) throws IOException {
        ClassFile cf = getClassFile(srcRoot, src);
        if (cf == null) {
            throw new IOException("Could not find class file " + src);
        }
        File tmp = File.createTempFile("tempbuild", "dir");
        tmp.delete();
        tmp.mkdir();
        try {
            this.runXmlvm(new String[]{"--in=" + src.getAbsolutePath(), "--out=" + tmp.getAbsolutePath(), "--target=xmlvm"});
        } catch (Exception ex) {
            //Logger.getLogger(XMLVM.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
        List<File> out = new ArrayList<File>();
        for (File dest : tmp.listFiles()) {
            File finalDest = new File(targetDir, dest.getName());
            out.add(finalDest);
            FileUtils.copyFile(dest, finalDest);
            dest.delete();
        }

        tmp.delete();
        return out.toArray(new File[0]);
    }

    public void transformXMLFile(InputStream xsltFile, InputStream src, OutputStream target) throws TransformerConfigurationException, TransformerException {
        TransformerFactory factory = TransformerFactory.newInstance();
        Source xslt = new StreamSource(xsltFile);
        Transformer transformer = factory.newTransformer(xslt);
        Source text = new StreamSource(src);
        transformer.transform(text, new StreamResult(target));
    }

    public void createXMLVMClassStub(InputStream src, OutputStream target) throws TransformerException {
        transformXMLFile(
                this.getClass().getResourceAsStream("resources/XMLVMClassStubber.xsl"),
                src,
                target);

    }

    public void createXMLVMClassStub(File src, File target) throws FileNotFoundException, TransformerException {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(src);
            fos = new FileOutputStream(target);
            createXMLVMClassStub(fis, fos);
        } finally {
            try {
                fis.close();
            } catch (Exception ex) {
            }
            try {
                fos.close();
            } catch (Exception ex) {
            }
        }
    }

    /**
     * Gets the cache directory to store intermediate artifacts. Creates it if
     * it doesn't yet exist.
     *
     * @return
     */
    protected File getXmlvmCacheDir() {
        if (this.xmlvmCacheDir == null) {
            this.xmlvmCacheDir = new File(getProject().getBaseDir(), ".xmlvm");
            this.xmlvmCacheDir.mkdir();
        }
        return this.xmlvmCacheDir;
    }

    protected File getXmlvmCacheDir(String type) {
        File cacheDir = this.getXmlvmCacheDir();
        File out = new File(cacheDir, type);
        if (!out.exists()) {
            out.mkdir();

        }
        return out;

    }

    public String[] getRequiredClasses(File f, boolean includeUsage) throws ParserConfigurationException, SAXException, IOException {
        String ns = "http://xmlvm.org";
        Set<String> out = new HashSet<String>();
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(f);
        /*try {
            StringWriter writer = new StringWriter();

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            String output = writer.getBuffer().toString().replaceAll("\n|\r", "");
            System.out.println(output);
        } catch ( Exception ex){
            ex.printStackTrace();
        }*/
        NodeList refs = doc.getElementsByTagName("vm:reference");
        //System.out.println("Refs len: " + refs.getLength());
        int len = refs.getLength();
        for (int i = 0; i < len; i++) {
            Element el = (Element) refs.item(i);
            String className = el.getAttribute("name");
            String usage = el.getAttribute("kind");
            switch (usage) {
                case "usage":
                    // This is a usage dependency.  A stub is good enough
                    if (includeUsage) {
                        out.add(className);
                    }
                    break;
                case "interface":
                case "super":
                    out.add(className);

            }
        }
        return out.toArray(new String[0]);
    }

    public void generateXMLVMCache(File src) {
        try {
            System.out.println("Running XMLVM with input "+src);
            this.runXmlvm(new String[]{
                "--in=" + src.getAbsolutePath(),
                "--out=" + getXmlvmCacheDir("xmlvm").getAbsolutePath(),
                "--target=xmlvm",
                "--libraries=" + getJavac().getClasspath().toString().replaceAll(File.pathSeparator, ","),
                "--load-dependencies"
            });
        } catch (Exception ex) {
            Logger.getLogger(XMLVM.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Loads all of the dependencies associated with a collection of input
     * files. Dependencies will be loaded as stub files, so that XMLVM can be
     * run without producing a full transitive dependency chain.
     * @deprecated
     * @param dir The directory containing the current .xmlvm files to be
     * converted. Dependent stub files will be copied to this directory by this
     * method.
     * @param files Collection of .xmlvm files to be parsed for dependencies. It
     * is assumed that these files are already located inside the "dir"
     * directory.
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws FileNotFoundException
     * @throws UnsatisfiedDependencyException If a dependency cannot be found in
     * the cache. If you receive this exception, you should probably just run a
     * recursive dependent build with XMLVM to generate the cache.
     * @throws TransformerException
     */
    public void loadDependencies(File dir, Collection<File> files) throws ParserConfigurationException, SAXException, IOException, FileNotFoundException, UnsatisfiedDependencyException, TransformerException {
        Set<String> loadedClasses = new HashSet<String>();

        // First find out which classes are already loaded.
        for (File f : dir.listFiles()) {
            if (f.getName().endsWith(".xmlvm")) {
                ClassFile cf = getClassFile(f.getParentFile(), f);
                if (cf == null) {
                    throw new RuntimeException("Failed to get class file for " + f);
                }
                loadedClasses.add(cf.getName());
            }
        }

        System.out.println(loadedClasses.size() + " classes loaded.  Looking for dependencies...");

        // Now go through each of the loaded classes, and find dependencies.
        Set<File> newFiles = new HashSet<File>();
        File xmlvmstubDir = getXmlvmCacheDir("xmlvmstubs");
        File xmlvmDir = getXmlvmCacheDir("xmlvm");
        Stack<File> stack = new Stack<File>();
        stack.addAll(files);
        while (!stack.isEmpty()) {
            File f = stack.pop();
            String[] deps = getRequiredClasses(f, true);
            System.out.println(deps.length + " required classes in " + f.getName());
            for (String dep : deps) {
                if (!loadedClasses.contains(dep)) {
                    // Check for cached stub
                    System.out.println("Unloaded dependency found " + dep + ". Looking for cached version...");
                    ClassFile cf = new ClassFile(dep);
                    String fileName = cf.getcPrefix() + ".xmlvm";
                    File cachedStubFile = new File(xmlvmstubDir, fileName);
                    File cachedFullFile = new File(xmlvmDir, fileName);
                    if (cachedStubFile.exists()) {
                        File destStubFile = new File(dir, fileName);
                        FileUtils.copyFile(cachedStubFile, destStubFile);
                        stack.push(destStubFile);
                        loadedClasses.add(cf.getName());

                    } else if (cachedFullFile.exists()) {
                        File tempStubFile = new File(xmlvmstubDir, fileName);
                        createXMLVMClassStub(cachedFullFile, tempStubFile);
                        File destFile = new File(dir, fileName);
                        FileUtils.copyFile(tempStubFile, destFile);
                        stack.push(destFile);
                        loadedClasses.add(cf.getName());
                    } else {

                        // At this point, we'll just give up this foolish mission
                        // If the caller receives this exception, it should
                        // just run xmlvm with transitive dependency loading.
                        throw new UnsatisfiedDependencyException(dep);
                    }
                }
            }

        }

    }

    /**
     * @deprecated
     */
    protected File findCachedXMLVMClass(String cls) {
        ClassFile cf = new ClassFile(cls);
        String fileName = cf.getcPrefix() + ".xmlvm";
        File cacheDir = getXmlvmCacheDir("xmlvm");
        File cacheFile = new File(cacheDir, fileName);
        if (cacheFile.exists()) {
            return cacheFile;
        }
        return null;
    }

    
    
    
    
    private void doCompile() throws IOException {
        try {
            if (getJavac() == null) {
                setJavac((Javac) getProject().createTask("javac"));
            }

            // Create a temporary directory as a destination for javac 
            File tmpBuild = File.createTempFile("build", "build");
            tmpBuild.delete();
            tmpBuild.mkdir();

            // Create a temporary directory to contain only the .java files that
            // have changed.
            File changedSrcDir = setupChangedSourcesDir();

            getJavac().setDestdir(tmpBuild);

            getJavac().setSrcdir(new Path(getProject(), changedSrcDir.getAbsolutePath()));
            getJavac().setClasspath(getClassPath());
            getJavac().getClasspath().add(new Path(getProject(), getJavaBuildDir().getAbsolutePath()));
            getJavac().execute();

            // Copy the compiled files to the intermediate build dir
            Copy copy = (Copy) getProject().createTask("copy");
            copy.setTodir(getJavaBuildDir());
            FileSet fs = new FileSet();
            fs.setDir(tmpBuild);
            fs.setIncludes("**");
            copy.addFileset(fs);
            copy.execute();

            // Now generate .xmlvm files
            File tmpOutput = File.createTempFile("tmpOut", "dir");
            tmpOutput.delete();
            tmpOutput.mkdir();

            System.out.println(tmpBuild.list().length + " class files generated");

            File[] converted = this.classToXMLVM(tmpBuild, tmpOutput);

            System.out.println(converted.length + " xmlvm files generated");

            // Now look for dependencies in generated files.
            try {
                this.loadDependencies(tmpOutput, Arrays.asList(converted));

            } catch (Exception ex) {
                System.out.println("Generating XMLVM Cache...");
                this.generateXMLVMCache(tmpOutput);
            }

            try {
                this.loadDependencies(tmpOutput, Arrays.asList(converted));
            } catch (Exception ex) {
                System.out.println("First attempt at XMLVM Cache seemed to fail");
                System.out.println("Trying from the Java source");
                this.generateXMLVMCache(tmpBuild);
            }
            try {
                this.loadDependencies(tmpOutput, Arrays.asList(converted));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            

            // Now we should have all we need to generate our C source files
            // Delete the changed source dir, since we don't need it anymore
            Delete del = (Delete) getProject().createTask("delete");
            del.setDir(changedSrcDir);
            del.execute();

            File intermediateOut = this.getXmlvmCacheDir("c");

            //System.out.println("Intermediates: ");
            //for ( File f : tmpOutput.listFiles()){
            //    System.out.println(f);
            //}
            
            System.out.println("Converting "+tmpOutput.list().length+" xmlvm files to "+getTarget()+" source files....");
            this.runXmlvm(new String[]{
                "--in=" + tmpOutput.getAbsolutePath(),
                "--out=" + intermediateOut.getAbsolutePath(),
                "--target=" + getTarget(),
                "--libraries=" + getJavac().getClasspath().toString().replaceAll(File.pathSeparator, ","),
                "--c-source-extension=m", //"--debug=all",
            });

            del = (Delete) getProject().createTask("delete");
            del.setDir(tmpBuild);
            del.execute();

            copyChangedSource(intermediateOut, getDest());

        } catch (Exception ex) {
            Logger.getLogger(XMLVM.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void copyChangedSource(File srcDir, File destDir) {
        System.out.println(srcDir.listFiles().length + " Files were generated");
        RootedFile[] files = getChangedSourceFiles();
        for (RootedFile file : files) {
            Copy copy = (Copy) getProject().createTask("copy");
            FileSet fs = new FileSet();
            ClassFile cls = getClassFile(file.root, file.file);
            if (cls == null) {
                if (file.file.getName().endsWith(".java")) {
                    System.err.println("Failed to load clasfile for changed source file " + file.file);
                }
                continue;
            }
            //System.out.println("Copying prefixes with "+cls.getcPrefix());
            fs.setIncludes("**/" + cls.getcPrefix() + "*");
            copy.setTodir(destDir);
            copy.setFlatten(true);
            fs.setDir(srcDir);
            copy.addFileset(fs);
            copy.setVerbose(true);
            copy.setQuiet(false);
            copy.execute();
            copy.setOverwrite(true);
        }
    }

    /* One of a fixed set of choices:
     public static class FooBieBletch extends EnumeratedAttribute { // or use Java 5 enums
     public String[] getValues() {
     return new String[] {"foo", "bie", "bletch"};
     }
     }
     private String mode = "foo";
     public void setMode(FooBieBletch m) {
     mode = m.getValue();
     }
     // <customtask mode="bletch"/>
     */
    public @Override
    void execute() throws BuildException {
        try {
            // TODO code here what the task actually does:

            // To log something:
            // log("Some message");
            // log("Serious message", Project.MSG_WARN);
            // log("Minor message", Project.MSG_VERBOSE);
            // To signal an error:
            // throw new BuildException("Problem", location);
            // throw new BuildException(someThrowable, location);
            // throw new BuildException("Problem", someThrowable, location);
            // You can call other tasks too:
            // Zip zip = (Zip)project.createTask("zip");
            // zip.setZipfile(zipFile);
            // FileSet fs = new FileSet();
            // fs.setDir(baseDir);
            // zip.addFileset(fs);
            // zip.init();
            // zip.setLocation(location);
            // zip.execute();
            doCompile();
        } catch (IOException ex) {
            Logger.getLogger(XMLVM.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
