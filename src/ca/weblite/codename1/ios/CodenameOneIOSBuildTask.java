/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.codename1.ios;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.taskdefs.Replace;
import org.apache.tools.ant.types.Path;


/**
 * @author shannah
 */
public class CodenameOneIOSBuildTask extends XMLVMIOSTask {
    
    /**
     * The path to the iOSPort directory where the iOSPort
     * source code can be found.  If this is left null then the version of 
     * iOSPort that is bundled with the plugin will be extracted to the 
     * iOSPort directory inside the project's base directory.
     */
    private File iOSPort;
    
    /**
     * The path to the codenameone "src" directory if you are building from source.
     * If this is omitted, the CodenameOne.jar in the current project is used
     * instead.
     */
    private File codenameOneSrc;
    
    /**
     * The path to the CLDC11 jar file.  This isn't used right now.
     */
    private File cldc;
    
    /**
     * Debug flag.  If this is true then the FAT version of libzbar will be 
     * copied into the xcode project so that it can be run in the simulator.
     */
    private boolean debug = true;
    
    
    /**
     * Creates a build task at with the given base directory.
     */
    public static CodenameOneIOSBuildTask create(String basePath){
        org.apache.tools.ant.Project proj = new org.apache.tools.ant.Project();
        
        
        
        proj.setBasedir(basePath);
        CodenameOneIOSBuildTask build = new CodenameOneIOSBuildTask();
        build.setProject(proj);
        return build;
        
    }
    
    /**
     * Adds the iOSPort to the class path.
     */
    private void addIOSPortToClasspath() throws IOException{
        if ( getiOSPort() == null || !getiOSPort().exists() ){
            if ( getiOSPort() == null ){
                setiOSPort(new File(getProject().getBaseDir(), "iOSPort"));
            }
            
            if ( !iOSPort.exists() ){
                extractIOSPort();
            }
        }
        
        // Properties file.  If present, it means that this particular
        // ios port is managed.   We may need to update it.
        File iosPortProperties = new File(getiOSPort(), "iosport.properties");
        if ( iosPortProperties.exists() ){
            System.out.println("Using a managed version of iOSPort.");
            Properties p = new Properties();
            p.load(new FileInputStream(iosPortProperties));
            String version = p.getProperty("version");
            if ( version != null ){
                System.out.println("iOSPort version "+version);
                int iVersion = Integer.parseInt(version);
                Properties currP = new Properties();
                currP.load(this.getClass().getResourceAsStream("resources/iosport.properties"));
                int currVersion = Integer.parseInt(currP.getProperty("version"));
                if ( currVersion > iVersion ){
                    System.out.println("iOSPort version is out of date.  Current version is "+currVersion);
                    System.out.println("Deleting "+iOSPort);
                    FileUtils.deleteDirectory(iOSPort);
                    extractIOSPort();
                }
            }
        } else {
            System.out.println("Using unmanaged version of iOSPort at "+iOSPort);
        }
        
        File javaSources = new File(getiOSPort(), "src");
        Path jsp = new Path(getProject(), javaSources.getPath());
        Path srcDir = getJavac().getSrcdir();
        if ( srcDir == null ){
            srcDir = new Path(getProject());
            getJavac().setSrcdir(srcDir);
        }
        getJavac().getSrcdir().add(jsp);
        
        File nativeSources = new File(getiOSPort(), "nativeSources");
        
        Replace repl = (Replace)getProject().createTask("replace");
        repl.setToken("#define INCLUDE_ZOOZ");
        repl.setValue("#define NOT_INCLUDE_ZOOZ");
        repl.setFile(new File(nativeSources, "CodenameOne_GLViewController.h"));
        repl.execute();
        
        Path np = new Path(getProject(), nativeSources.getPath());
        Path nativeSourcesPath = this.getJavaSources();
        if ( nativeSourcesPath == null ){
            nativeSourcesPath = new Path(getProject());
            this.setNativeSources(nativeSourcesPath);
        }
        this.getNativeSources().add(np);
        
        
    }
    
    
    private void runPreJavac(){
        System.out.println("Running Pre Javac");
        Javac javac = (Javac)getProject().createTask("javac");
       
        //Path cp = javac.createClasspath();
        
        //cp.setPath(getProject().getProperty("javac.classpath"));
        System.out.println("PRoperties "+getProject().getProperties());
        System.out.println(System.getProperties());
        javac.setClasspath(new Path(getProject(),getProject().getProperty("javac.classpath")));
        System.out.println("Prejavac classpath is "+javac.getClasspath());
        javac.setSrcdir(new Path(getProject(),"src" ));
        //javac.setDestdir(iOSPort);
        
        File build = new File(getProject().getBaseDir(), "build");
        if ( !build.exists()){
            build.mkdir();
        }
        
        File classes = new File(build, "classes");
        if ( !classes.exists() ){
            classes.mkdir();
        }
        javac.setDestdir(classes);
        javac.execute();
    }
    
    private void runPreJavacOld(){
        Javac javac = (Javac)getProject().createTask("javac");
        Path cp = javac.createClasspath();
        //cp.add(new Path(getProject(), "src"));
        File lib = new File(getProject().getBaseDir(), "lib");
        
        File impl = new File(lib, "impl");
        if ( !impl.exists() ){
            impl.mkdir();
        }
        
        File cls = new File(impl, "cls");
        if ( !cls.exists() ){
            cls.mkdir();
        }
        
        File build = new File(getProject().getBaseDir(), "build");
        if ( !build.exists()){
            build.mkdir();
        }
        
        File classes = new File(build, "classes");
        if ( !classes.exists() ){
            classes.mkdir();
        }
        javac.setSrcdir(new Path(getProject(),"src" ));
        
        if ( codenameOneSrc != null && codenameOneSrc.exists() ){
            javac.getSrcdir().add(new Path(getProject(), codenameOneSrc.getAbsolutePath()));
        } else { 
            cp.add(new Path(getProject(), "lib/CodenameOne.jar"));
        }
        cp.add(new Path(getProject(), "lib/impl/cls"));
        cp.add(new Path(getProject(), "build/classes"));
        
        
        
        Path bcp = javac.createBootclasspath();
        if ( getCldc() == null || !getCldc().exists()){
            bcp.add(new Path(getProject(), "lib/CLDC11.jar"));
        } else {
            bcp.add(new Path(getProject(), getCldc().getAbsolutePath()));
        }
        
        
        javac.setDestdir(classes);
        
        
        javac.execute();
        
        
    }
    
    
    
    /**
     * Returns the the iOSPort directory as a File.
     */
    private File getIOSPortDir(){
        return new File(getProject().getBaseDir(), "iOSPort");
        
    }
    
    
    /**
     * Extracts the iOSPort that is bundled inside the module's jar file
     * and saves it in the iOSPort directory.
     */
    private void extractIOSPort() throws IOException{
        File dir = getIOSPortDir();
        if ( !dir.exists() ){
            dir.mkdir();
            
            URL src = getClass().getResource("resources/iOSPort.zip");
            
            File tmp = File.createTempFile("iosport", "zip");
            tmp.delete();
            FileUtils.copyURLToFile(src, tmp);
            Expand unzip = (Expand)getProject().createTask("unzip");
            unzip.setTaskType("unzip");
            
            unzip.setSrc(tmp);
            unzip.setDest(getiOSPort());
            unzip.execute();
        }
        
    }
    /**
     * Adds libraries to be included in XMLVM build.
     */
    private void addLibs(){
        String lib = getLib();
        String toAdd = "libxml2.2.dylib,Security.framework,libzbar.a,GLKit.framework";
        if ( lib == null ){
            lib = "";
        }
        if ( lib.length() == 0 ){
            lib = toAdd;
            
        } else {
            lib += ","+toAdd;
        }
        
        setLib(lib);
    }
    
    /**
     * Sets the project info for the Xcode project.
     */
    private void setupProjectInfo() throws IOException{
        File basedir = getProject().getBaseDir();
        File props = new File(basedir, "codenameone_settings.properties");
        Properties p = new Properties();
        p.load(new FileInputStream(props));
        
        this.setAppName(p.getProperty("codename1.displayName"));
        this.setBundleDisplayName(this.getAppName());
        this.setBundleIdentifier(p.getProperty("codename1.ios.appid"));
        this.setBundleVersion(p.getProperty("codename1.version"));
        
    }
    
    private void extractCodenameOneSrc() throws IOException{
        if ( codenameOneSrc == null ){
            codenameOneSrc = new File(getProject().getBaseDir(), "CodenameOneSrc");
        }
        File dir = codenameOneSrc;
        
        if ( !dir.exists() ){
            dir.mkdir();
            
            URL src = getClass().getResource("resources/CodenameOne.zip");
            
            File tmp = File.createTempFile("codenameone", "zip");
            tmp.delete();
            FileUtils.copyURLToFile(src, tmp);
            System.out.println("Extracting CodenameOne Src to "+codenameOneSrc);
            Expand unzip = (Expand)getProject().createTask("unzip");
            unzip.setTaskType("unzip");
            
            unzip.setSrc(tmp);
            unzip.setDest(codenameOneSrc);
            unzip.execute();
        }
    }
    
    private void addCodenameOneToClasspath(Path src, Path cp) throws  IOException{
        if ( codenameOneSrc == null || !codenameOneSrc.exists() ){
            if ( codenameOneSrc == null ){
                setCodenameOneSrc(new File(getProject().getBaseDir(), "CodenameOneSrc"));
            }
            
            if ( !codenameOneSrc.exists() ){
                extractCodenameOneSrc();
            }
        }
        
        // Properties file.  If present, it means that this particular
        // ios port is managed.   We may need to update it.
        File cn1SrcProperties = new File(codenameOneSrc, "iosport.properties");
        if ( cn1SrcProperties.exists() ){
            System.out.println("Using a managed version of CodenameOne.");
            Properties p = new Properties();
            p.load(new FileInputStream(cn1SrcProperties));
            String version = p.getProperty("version");
            if ( version != null ){
                System.out.println("CodenameOne src version "+version);
                int iVersion = Integer.parseInt(version);
                Properties currP = new Properties();
                currP.load(this.getClass().getResourceAsStream("resources/iosport.properties"));
                int currVersion = Integer.parseInt(currP.getProperty("version"));
                if ( currVersion > iVersion ){
                    System.out.println("CodenameOne src version is out of date.  Current version is "+currVersion);
                    System.out.println("Deleting "+codenameOneSrc);
                    FileUtils.deleteDirectory(codenameOneSrc);
                    extractCodenameOneSrc();
                }
            }
        } else {
            System.out.println("Using unmanaged version of CodenameOne Src at "+codenameOneSrc);
        }
        if ( codenameOneSrc != null && codenameOneSrc.exists() ){
            
            src.add(new Path(getProject(), codenameOneSrc.getAbsolutePath()));
        } else {
            cp.add(new Path(getProject(), "lib/CodenameOne.jar"));
        }
    }
    
    /**
     * Sets up a new Javac task for building all of the .java files
     */
    public void setupJavac() throws IOException{
        Javac javac = (Javac)getProject().createTask("javac");
        setJavac(javac);
        Path cp = javac.createClasspath();
        //cp.add(new Path(getProject(), "src"));
        Path src = javac.getSrcdir();
        if ( src == null ){
            src = new Path(getProject());
            
        }
        src.add(new Path(getProject(), "src"));
        getJavac().setClasspath(cp);
        addIOSPortToClasspath();
        addCodenameOneToClasspath(src, cp);
        
        
        cp.add(new Path(getProject(), "lib/impl/cls"));
        cp.add(new Path(getProject(), "build/classes"));
        //javac.setClasspath(cp);
        
        //Path bcp = javac.createBootclasspath();
        //bcp.add(new Path(getProject(), "lib/CLDC11.jar"));
        //javac.setBootclasspath(bcp);
        //cp.add(new Path(getProject(), "lib/CLDC11.jar"));
        
        //javac.setSrcdir(new Path(getProject(),"src" ));
        File build = new File(getProject().getBaseDir(), "build");
        File classes = new File(build, "classes");
        getJavac().setDestdir(classes);
        //getJavac().setSrcdir(src);
        getJavac().setSrcdir(src);
        setJavac(javac);
        
        setupMainStub();
        
    }
    
    
    
    
    /**
     * Sets up the stub file used as the entry point for the Codename One app.
     */
    private void setupMainStub() throws IOException{
        File build = new File(getProject().getBaseDir(), "build");
        File generated = new File(build, "generated-src");
        if ( !generated.exists() ){
            generated.mkdir();
        }
        
        File comD = new File(generated, "com");
        File codename1D = new File(comD, "codename1");
        File implD = new File(codename1D, "impl");
        File iosD = new File(implD, "ios");
        if ( !iosD.exists()){
            iosD.mkdirs();
        }
        
        File main = new File(iosD, "Main.java");
        URL mainStub = this.getClass().getResource("resources/Main.java.tpl");
        FileUtils.copyURLToFile(mainStub, main);
        
        
        
        Properties p = new Properties();
        File cn1Props = new File(getProject().getBaseDir(), "codenameone_settings.properties");
        p.load(new FileInputStream(cn1Props));
        StringBuilder mainClass = new StringBuilder();
        String pkgName = p.getProperty("codename1.packageName");
        if ( pkgName != null && !"".equals(pkgName) ){
            mainClass.append(pkgName).append(".");
        }
        mainClass.append(p.getProperty("codename1.mainName"));
        
        
        Replace repl = (Replace)getProject().createTask("replace");
        repl.setEncoding("UTF-8");
        repl.setFile(main);
        repl.setToken("#MAIN_CLASS#");
        repl.setValue(mainClass.toString());
        repl.execute();
        
        repl = (Replace)getProject().createTask("replace");
        repl.setEncoding("UTF-8");
        repl.setFile(main);
        repl.setToken("#CODENAMEONE_REGISTER_NATIVE_STUBS#");
        repl.setValue("");
        repl.execute();
       
        getJavac().getSrcdir().add(new Path(getProject(), generated.getAbsolutePath()));
        
        
        
    }

    /**
     * Fixes the properties in the Netbeans skeleton project that was set up by xmlvm.
     */
    @Override
    protected void fixSkeletonProperties() throws IOException {
        super.fixSkeletonProperties(); //To change body of generated methods, choose Tools | Templates.
        Properties p = new Properties();
        p.load(new FileInputStream(this.getSkeletonPropertiesFile()));
        String lib = p.getProperty("xmlvm.lib");
        if ( lib == null ){
            lib = "";
        }
        if ( lib.length() > 0 ){
            lib += ",libzbar.a";
        } else {
            lib = "libzbar.a";
        }
        p.setProperty("xmlvm.lib", lib);
        p.store(new FileOutputStream(this.getSkeletonPropertiesFile()), "Updated xmlvm.lib");
    }
    
    
    /**
     * Gets a list of the XMLVM generated files that are currently registered
     * in the Xcode project.
     */
    protected Set<String> getCurrentXcodeFiles(String pbxprojContent){
        Set<String> out = new HashSet<String>();
        Scanner scanner = new Scanner(pbxprojContent);
        int state = 0;
        Pattern regex = Pattern.compile("\\d+ /\\* ([^ ]+) \\*/");
        while ( scanner.hasNextLine()){
            String line = scanner.nextLine();
            if ( state == 0 && line.indexOf("/* Application */ = {") >= 0){
                state = 1;
            } else if ( state == 1 ){
                Matcher m = regex.matcher(line);
                if ( m.find()){
                    String path = m.group(1);
                    if ( path.indexOf("/") < 0 ){
                        out.add(path);
                    }
                } else if ( ");".equals(line.trim()) ){
                    return out;
                }
            }
            
        }
        throw new RuntimeException("Could not find the end of the file reference section");
    }
    
    /**
     * Gets a list of the current app source files.
     * 
     * @return 
     */
    protected Set<String> getCurrentAppSrcFiles(){
        File appDir = new File(getOut(), "build/xcode/src/app");
        Set<String> out = new HashSet<String>();
        out.addAll(Arrays.asList(appDir.list()));
        return out;
        
    }
    
    /**
     * Updates the Xcode project by removing non-existent source files
     * and adding source files that are present but not yet registered in
     * the project file.
     * @throws IOException 
     */
    protected void updatePbxProj() throws IOException{
        String contents = FileUtils.readFileToString(getPbxProjectFile());
        contents = updatePbxProj(contents);
        FileUtils.writeStringToFile(getPbxProjectFile(), contents);
        
    }
    
    /**
     * Generates an updated project file by adding new soruce files and removing
     * non-existing files.  This does not write the project file.  It simply processes
     * content and returns modified content.
     * @param pbxProjContent  The contents of the project file.
     * @return String with the updated project file contents.
     */
    protected String updatePbxProj(String pbxProjContent){
        Set<String> inProject = getCurrentXcodeFiles(pbxProjContent);
        Set<String> inFileSystem = getCurrentAppSrcFiles();
        Set<String> missingFromProject = new HashSet<String>();
        missingFromProject.addAll(inFileSystem);
        missingFromProject.removeAll(inProject);
        Set<String> missingFromFileSystem = new HashSet<String>();
        missingFromFileSystem.addAll(inProject);
        missingFromFileSystem.removeAll(inFileSystem);
        
        File appDir = new File(getOut(), "build/xcode/src/app");
        
        if ( !missingFromProject.isEmpty()){
            System.out.println("Found "+missingFromProject.size()+" missing from the Xcode project.  Adding them now...");
            System.out.println(missingFromProject);
            List<File> filesToAdd = new ArrayList<File>();
            for ( String s : missingFromProject ){
                filesToAdd.add(new File(appDir, s));
            }
            pbxProjContent = this.injectFilesIntoXcodeProject(pbxProjContent, filesToAdd.toArray(new File[0]));
        }
        
        if ( !missingFromFileSystem.isEmpty() ){
            System.out.println("Found "+missingFromFileSystem.size()+" missing from the fileSystem.  Removing them in Xcode...");
            System.out.println(missingFromFileSystem);
            List<File> filesToRemove = new ArrayList<File>();
            for ( String s : missingFromFileSystem ){
                filesToRemove.add(new File(appDir, s));
            }
            pbxProjContent = removeFilesFromXcodeProject(pbxProjContent, filesToRemove.toArray(new File[0]));
        }
        return pbxProjContent;
        
    }
    
    /**
     * Returns modified Xcode project file contents after removing a set of files from the "Application" 
     * group.
     * @param projFileContent The Xcode project file contents.
     * @param filesToRemove A list of files to remove.
     * @return The modified project file contents.
     */
    protected String removeFilesFromXcodeProject(String projFileContent, File[] filesToRemove){
        StringBuilder sb = new StringBuilder();
        Scanner scanner = new Scanner(projFileContent);
        while ( scanner.hasNextLine()){
            String line = scanner.nextLine();
            boolean found = false;
            for ( File f : filesToRemove ){
                if ( line.indexOf("/* "+f.getName()+" */") >= 0){
                    found = true;
                }
            }
            if ( !found ){
                sb.append(line).append("\n");
            }
        }
        
        
        return sb.toString();
    }
    
    
   /**
    * Executes the task.
    * @throws BuildException 
    */
    public @Override
    void execute() throws BuildException {
        
        File build = new File(getProject().getBaseDir(), "build");
        if ( !build.exists() ){
            build.mkdir();
        }
        
        File ios = new File(build, "ios");
        boolean doingClean = clean || !ios.exists();
        if ( ios.exists() && clean ){
            try {
                FileUtils.deleteDirectory(ios);
            } catch (IOException ex) {
                Logger.getLogger(CodenameOneIOSBuildTask.class.getName()).log(Level.SEVERE, null, ex);
                throw new BuildException(ex);
            }
            ios.mkdirs();
        }
        
        
        
        //runPreJavac();
        try {
            
            setupJavac();
                    
            setupProjectInfo();
        } catch (IOException ex) {
           throw new BuildException(ex);
        }
        addLibs();
        
        super.execute();
        
        if ( !doingClean ){
            try {
                // Let's add all of the missing files
                updatePbxProj();
            } catch (IOException ex) {
                Logger.getLogger(CodenameOneIOSBuildTask.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        
        
    }

    /**
     * @return the codenameOneSrc
     */
    public File getCodenameOneSrc() {
        return codenameOneSrc;
    }

    /**
     * @param codenameOneSrc the codenameOneSrc to set
     */
    public void setCodenameOneSrc(File codenameOneSrc) {
        this.codenameOneSrc = codenameOneSrc;
    }

    @Override
    protected void copyNativeLibraries() {
        super.copyNativeLibraries(); //To change body of generated methods, choose Tools | Templates.
        if ( debug ){
            try {
                // We need to copy the fat version of libzbar so that it works in the simulator
                File nativeLibDir = this.getNativeLibsPath();
                File dest = new File(nativeLibDir, "libzbar.a");
                URL src = getClass().getResource("resources/libzbar.a");
                FileUtils.copyURLToFile(src, dest);
            } catch (IOException ex) {
                Logger.getLogger(CodenameOneIOSBuildTask.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
    }

    
    
    /**
     * @return the cldc
     */
    public File getCldc() {
        return cldc;
    }

    /**
     * @param cldc the cldc to set
     */
    public void setCldc(File cldc) {
        this.cldc = cldc;
    }

    /**
     * @return the iOSPort
     */
    public File getiOSPort() {
        return iOSPort;
    }

    /**
     * @param iOSPort the iOSPort to set
     */
    public void setiOSPort(File iOSPort) {
        this.iOSPort = iOSPort;
    }
    
    
    
    private static class FileResource {
        static Map<String,String> sourcefiles = new HashMap<String, String>();
        static Map<String,String> hiddensourcefiles = new HashMap<String, String>();
        static {
            
           //sourcefiles.put("a", "library");
           sourcefiles.put("c", "sourcecode.c.c");
           sourcefiles.put("m", "sourcecode.c.objc");
           sourcefiles.put("mm", "sourcecode.cpp.objcpp");
           sourcefiles.put("c++", "sourcecode.cpp.cpp");
           sourcefiles.put("cpp", "sourcecode.cpp.cpp");
           sourcefiles.put("xib", "sourcecode.xib.xib");

           
           hiddensourcefiles.put("h", "sourcecode.c.h");
        }
        
            private String  type        = null;
            private boolean isSource    = false;
            private boolean isValid     = false;
            private boolean isBuildable = false;


            private FileResource(String fname) {
                if (fname == null)
                    return;
                int point = fname.lastIndexOf(".");
                if (point < 0 || point == fname.length())
                    return;

                String ext = fname.substring(point + 1);

                type = sourcefiles.get(ext);
                if (type != null) {
                    isValid = true;
                    isBuildable = true;
                    isSource = true;
                    return;
                }

                type = hiddensourcefiles.get(ext);
                if (type != null) {
                    isValid = true;
                    isSource = true;
                    return;
                }
            }

            @Override
            public String toString() {
                return "[type=" + type + (isSource ? ", Source" : "") + (isValid ? ", Valid" : "")
                        + (isBuildable ? ", Buildable" : "") + "]";
            }
        }
    
    private File getPbxProjectFile(){
        return new File(this.getOut(), "dist/"+this.getBundleDisplayName()+".xcodeproj/project.pbxproj" );
        
    }
    
    private void injectFilesIntoXcodeProject(File[] files) throws IOException {
        injectFilesIntoXcodeProject(getPbxProjectFile(), files);
    }
    
    private void injectFilesIntoXcodeProject(File projectFile, File[] files) throws IOException{
        String contents = FileUtils.readFileToString(projectFile);
        contents = injectFilesIntoXcodeProject(contents, files);
        FileUtils.writeStringToFile(projectFile, contents);
    }
    
    /**
     * Based on https://github.com/shannah/cn1/blob/master/Ports/iOSPort/xmlvm/src/xmlvm/org/xmlvm/proc/out/build/XCodeFile.java
     * @param template
     * @param filter 
     */
    private String injectFilesIntoXcodeProject(String template, File[] files) {

        int nextid = 0;
        Pattern idPattern = Pattern.compile(" (\\d+) ");
        Matcher m = idPattern.matcher(template);
        while ( m.find() ){
            int curr = Integer.parseInt(m.group(1));
            if ( curr > nextid ){
                nextid = curr;
            }
        }
        nextid++;

        StringBuilder filerefs = new StringBuilder();
        StringBuilder buildrefs = new StringBuilder();
        StringBuilder display = new StringBuilder();
        StringBuilder source = new StringBuilder();
        StringBuilder resource = new StringBuilder();


        for (File f : files) {
            String fname = f.getName();
            if ( template.indexOf(" "+fname+" ") >= 0 ){
                continue;
            }
            FileResource fres = new FileResource(fname);
            if (f.exists()) {
                filerefs.append("\t\t").append(nextid);
                filerefs.append(" /* ").append(fname).append(" */");
                filerefs.append(" = { isa = PBXFileReference; fileEncoding = 4; lastKnownFileType = ");
                filerefs.append(fres.type).append("; path = \"");
                filerefs.append(fname).append("\"; sourceTree = \"<group>\"; };");
                filerefs.append('\n');

                display.append("\t\t\t\t").append(nextid);
                display.append(" /* ").append(fname).append(" */");
                display.append(",\n");

                if (fres.isBuildable) {
                    int fileid = nextid;
                    nextid++;
                    buildrefs.append("\t\t").append(nextid);
                    buildrefs.append(" /* ").append(fname);
                    buildrefs.append(" in ").append(fres.isSource ? "Sources" : "Resources");
                    buildrefs.append(" */ = {isa = PBXBuildFile; fileRef = ").append(fileid);
                    buildrefs.append(" /* ").append(fname);
                    buildrefs.append(" */; };\n");

                    if (fres.isSource) {
                        source.append("\t\t\t\t").append(nextid);
                        source.append(" /* ").append(fname).append(" */");
                        source.append(",\n");
                    }
                }
                nextid++;
            }
        }
        String data = template;
        data = data.replace("/* End PBXFileReference section */", filerefs.toString() + "/* End PBXFileReference section */");
        data = data.replace("/* End PBXBuildFile section */", buildrefs.toString() + "/* End PBXBuildFile section */");

        // The next two we probably shouldn't do by regex because there is no clear pattern.
        Stack<String> buffer = new Stack<String>();

        Stack<String> backtrackStack = new Stack<String>();

        Scanner scanner = new Scanner(data);
        while ( scanner.hasNextLine()){
            String line = scanner.nextLine();
            if ( line.indexOf("/* End PBXSourcesBuildPhase section */") >= 0 ){
                // Found the end, let's backtrack
                while ( !buffer.isEmpty()){
                    String l = buffer.pop();
                    backtrackStack.push(l);
                    if ( ");".equals(l.trim())){
                        // This is the closing of the sources list
                        // we can insert the sources here
                        buffer.push(source.toString());
                        while ( !backtrackStack.isEmpty()){
                            buffer.push(backtrackStack.pop());
                        }
                        break;
                    }
                }
            } else if ( line.indexOf("name = Application;") >= 0 ){
                while ( !buffer.isEmpty()){
                    String l = buffer.pop();
                    backtrackStack.push(l);
                    if ( ");".equals(l.trim())){
                        buffer.push(display.toString());
                        while ( !backtrackStack.isEmpty()){
                            buffer.push(backtrackStack.pop());
                        }
                        break;
                    }
                }
            }
            buffer.push(line);
        }
        StringBuilder sb = new StringBuilder();
        String[] lines = buffer.toArray(new String[0]);
        for ( String line : lines ){
            sb.append(line).append("\n");
        }
        data = sb.toString();
        return data;
    }
    
}
