/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.xmlvm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author shannah
 */
public class ConstantPoolHelper {
    /**
     * Internal class to represent a constant in the constant pool.
     */
    public static class Constant {
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
    public static Map<Integer,Constant> loadConstantPool(File file) throws IOException{
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
    
    
    public static void removeConstantPoolDependencies(Map<Integer,Constant> constantPool, File file) throws IOException{
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
    public static void removeConstantPoolDependencies(File srcDir) throws IOException{
        File poolFile = new File(srcDir, "constant_pool.m");
        Map<Integer,Constant> constantPool = loadConstantPool(poolFile);
        for ( File f : srcDir.listFiles() ){
            if ( f.isFile() && (f.getName().endsWith(".m") || f.getName().endsWith(".c") || f.getName().endsWith(".h"))){
                removeConstantPoolDependencies(constantPool, f);
            }
        }
    }
}
