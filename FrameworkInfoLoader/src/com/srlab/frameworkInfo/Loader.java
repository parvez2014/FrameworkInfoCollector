package com.srlab.frameworkInfo;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JButton;
import javax.swing.JFrame;


public class Loader {
    private ArrayList<IType> iTypeList;
    private HashMap<String, IType> hmQualifiedNameToiType;

	public Loader() {
        this.iTypeList = new ArrayList();
        this.hmQualifiedNameToiType = new HashMap();
    }

    /**
     * you need to change this function to determine which framework classes and method you are interested in
     * in our case we collect public and protected classes. We also omit inner classes.
     * @param javaElement
     * @return
     * @throws JavaModelException
     */
    private boolean check(IJavaElement javaElement) throws JavaModelException{
    	if(javaElement instanceof IType){
    		IType iType = (IType)javaElement;
    		if(iType.getElementName().length() > 0 &&
              (Flags.isPublic(iType.getFlags()) || Flags.isProtected(iType.getFlags())) &&
              Utility.isInteresting(iType.getFullyQualifiedName()) &&
              iType.getFullyQualifiedName().contains("$") == false){
    			
    			return true;
    		}
    		else return false;
    	}
    	else return false;
    }
    
    public ArrayList<IType> getiTypeList() {
		return iTypeList;
	}

	public HashMap<String, IType> getHmQualifiedNameToiType() {
		return hmQualifiedNameToiType;
	}
	
    public void saveClassNameOnly(String filePath) throws IOException{
    	ArrayList<String> classLineList = new ArrayList();
    	for (int i = 0; i < this.iTypeList.size(); i++) {
            IType iType = iTypeList.get(i);
            classLineList.add("<class>" + iType.getFullyQualifiedName() +
                "</class>");
    	}
    	
        BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));

        for (int i = 0; i < classLineList.size(); i++) {
        	bw.write(classLineList.get(i));
        	if (i < (classLineList.size() - 1)) {
        		bw.newLine();
        	}
        }

        bw.close();
    }
    public void saveFullFrameworkInfo(String filePath) throws IOException {

        ArrayList<String> lineList = new ArrayList();
        
        for (int i = 0; i < this.iTypeList.size(); i++) {
            IType iType = iTypeList.get(i);
            lineList.add("<class>" + iType.getFullyQualifiedName() +
                "</class>");

            //now add all method name and their parameter
            try {
                for (IMethod method : iType.getMethods()) {
                    if (Flags.isPrivate(method.getFlags())) {
                        continue;
                    }

                    String methodName = method.getElementName();
                    ArrayList<String> parameterList = new ArrayList();

                    for (String parameter : method.getParameterTypes()) {
                        parameterList.add(Utility.typeSignatureToFullName(method,
                                parameter));
                    }

                    String methodLine = "<method>" + "<name>" + methodName +
                        "</name>" + "<param>" + parameterList.toString() +
                        "</param>" + "<return>" +
                        Utility.typeSignatureToFullName(method,
                            Signature.getReturnType(method.getSignature())) +
                        "</return>" + "</method>";
                    lineList.add(methodLine);
                    System.out.println("Method: " + methodLine);
                }
            } catch (JavaModelException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            System.out.println("Completed class collection [" + i + "]of[" +
                iTypeList.size() + "]");
        }

        System.out.println("Total Lines: " + lineList.size());

        //now write the lines into files
        BufferedWriter bw = new BufferedWriter(new FileWriter(
                    new File(filePath)));

        for (int i = 0; i < lineList.size(); i++) {
            bw.write(lineList.get(i));

            if (i < (lineList.size() - 1)) {
                bw.newLine();
            }
        }

        bw.close();
    }
    
    public void run() throws CoreException {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        System.out.println("Workspace Root: " + root.getLocation().toOSString());

        IProject[] projects = root.getProjects();

        for (IProject project : projects) {
            if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
                IJavaProject javaProject = JavaCore.create(project);
                System.out.println("Project: " + javaProject.getElementName());

                IPackageFragment[] packages = javaProject.getPackageFragments();

                for (IPackageFragment packageFragment : packages) {
                    if (packageFragment.getKind() == IPackageFragmentRoot.K_BINARY) {
                        for (IClassFile classFile : packageFragment.getClassFiles()) {
                            for (IJavaElement javaElement : classFile.getChildren()) {
                                if (check(javaElement)) {
                                    IType iType = (IType)javaElement;
                                    this.iTypeList.add(iType);
                                    this.hmQualifiedNameToiType.put(iType.getFullyQualifiedName(),
                                        iType);

                                    System.out.println("IType " +((IType) javaElement).getFullyQualifiedName());
                                    System.out.println("Super Class Name: " +((IType) javaElement).getSuperclassName());
                                    System.out.println("Super Class Type Signature: " +((IType) javaElement).getSuperclassTypeSignature());

                                 }
                            }
                        }
                    }
                }
            }
        }
    }
    public static void main(String[] args) {
        // TODO Auto-generated method stub
    }
}
