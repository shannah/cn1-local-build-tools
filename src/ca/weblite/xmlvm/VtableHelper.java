/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.xmlvm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;

/**
 *
 * @author shannah
 */
public class VtableHelper {
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
    public static String resolveVirtualMethod(Project project, Path searchPaths, File headerFile, String mangledMethodName) throws IOException{
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
                    return resolveVirtualMethod(project, searchPaths, h, mangledMethodName);
                }
            }
            throw new RuntimeException("Failed to find header file "+superClassHeader);
        } else {
            throw new RuntimeException("Failed to find virtual method "+mangledMethodName+" in header "+headerFile);
        }
    }
    
    
    
    
    
    
    
    /**
     * Fixes the vTable references for a .c (or .m) source file.
     * @param file
     * @param cFilesDirectory
     * @throws IOException 
     */
    public static void fixVtableReferences(Project project, File file, File cFilesDirectory) throws IOException{
        if ( file.isDirectory()){
            for ( File child : file.listFiles() ){
                
                fixVtableReferences(project, child, cFilesDirectory);
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
                                Path srcPaths = new Path(project);
                                srcPaths.add(new Path(project, file.getAbsolutePath()));
                                srcPaths.add(new Path(project, cFilesDirectory.getAbsolutePath()));
                                String resolvedFunction = resolveVirtualMethod(project, srcPaths, clsHeaderFile, mangledMethodName);
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
}
