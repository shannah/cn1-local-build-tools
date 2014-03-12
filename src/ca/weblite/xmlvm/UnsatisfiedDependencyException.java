/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.xmlvm;

/**
 *
 * @author shannah
 */
public class UnsatisfiedDependencyException extends Exception {
    private String className;
    public UnsatisfiedDependencyException(String className){
        super("Unsatisfied dependency: "+className);
        this.className = className;
    }
    
    public String getClassName(){
        return className;
    }
}
