package com.srlab.frameworkInfo;

import java.awt.image.ImageObserver;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

public class FrameworkInfoLoader {

	private ArrayList<IType> iTypeList;
	private HashMap<String,IType> hmQualifiedNameToiType;
	
	private HashMap<String, HashSet<String>> typeRelation;
	private HashMap<String,HashSet<IType>> hmStringTypeToSubType;
	public FrameworkInfoLoader(){
		this.iTypeList = new ArrayList();
		this.hmQualifiedNameToiType = new HashMap();
		
		this.hmStringTypeToSubType = new HashMap();
		this.typeRelation = new HashMap();
	}
	
	//type signature cannot handle basic type and return null for those cases
	private String typeSignatureToFullName(IMethod method, String signature) throws JavaModelException{
		String name = method.getReturnType();
		
		String simpleName = Signature.getSignatureSimpleName(signature);
		IType type = method.getDeclaringType();
	
		
		String[][] allResults = type.resolveType(simpleName);
		
		System.out.println("String signature: "+signature);
		System.out.println("Simple Name: "+simpleName+"  All Results: ");
	
		String fullName = null;
		if(allResults != null) {
			String[] nameParts = allResults[0];
			if(nameParts != null) {
				fullName = new String();
				for(int i=0 ; i < nameParts.length ; i++) {
					if(fullName.length() > 0) {
						fullName += '.';
					}
					if(nameParts[i] != null) {
						fullName += nameParts[i];
					}
				}
			}
		}
		if(fullName==null) return simpleName.replaceAll("/",".");
		else return fullName.replaceAll("/",".");
	}
	
	private String typeSignatureToSimpleName(String signature){
		return Signature.getSignatureSimpleName(signature);
	}
	private void printIMethod(IMethod method) throws JavaModelException{
		String name = method.getElementName();
		ArrayList<String> parameterList = new ArrayList();
		for(String parameter:method.getParameterTypes()){
			parameterList.add(this.getTypeName(parameter));
			System.out.println("Original Parameter: "+parameter+" Simple Name: "+this.typeSignatureToSimpleName(parameter));
			System.out.println("Qualified Name: "+this.typeSignatureToFullName(method, parameter));
		}
		System.out.println("Method: "+name+" parameter: "+parameterList +"  is Public: "+Flags.isPublic(method.getFlags()));
	}
	
	public String getTypeName(String input){
		if(input.startsWith("L")&& input.endsWith(";")){
			String s = input.substring(1, input.length()-1);
			return s.replaceAll("<.*>","");
		}
		else{
			return input;
			//throw new RuntimeException("type name not expected");
		}
	}
	
	//find whether type2 is a sub type of type 1
	public boolean isSubTypeOf(IType type1,IType type2){
		return false;
	}
	
	public boolean findRelatedType(IType findType,HashSet<String> relatedTypeList) throws JavaModelException{
		System.out.println("Total: "+relatedTypeList.size());
		int current=0;
		for(IType type:this.iTypeList){
			System.out.println("Current: "+(current++));
			
			for(IMethod method:type.getMethods()){
				if(Flags.isPublic(method.getFlags())==false) continue;
				for(String parameterTypeSignature:method.getParameterTypes()){
					if(parameterTypeSignature.startsWith("L")){
						
						//p1. If method parameter type matches with with our find type
						String parameter = this.getTypeName(parameterTypeSignature);
						IType  parameterType = this.hmQualifiedNameToiType.get(parameter);
						//System.out.println("ParameterTypeSignature:" +parameterTypeSignature);
						//System.out.println("Parameter: "+parameter);
						//System.out.println("ParameterType: "+parameterType);
						//System.out.println("Method: "+method.getElementName());
						if(parameterType==null) continue;
						if(parameter.equals(findType.getFullyQualifiedName())){
							relatedTypeList.add(type.getFullyQualifiedName());
						}
						
						
						//p2. if not, we need to explore the  parameter type: whether the find type is a sub type of the parameter type
						/*if(this.isASubType(parameterType, findType)){
							relatedTypeList.add(type.getFullyQualifiedName());
						}*/
						
						//p3. look for other type that this parameter type can accept
						
						ArrayList<IType> subTypes = this.getSubTypes(parameter);
						for(IType t:subTypes){
							if(this.findRelatedType(t,findType,relatedTypeList,1)){
								relatedTypeList.add(type.getFullyQualifiedName());	
							}
						}
						
						/*if(this.findRelatedType(parameterType,findType,relatedTypeList,1)){
							relatedTypeList.add(type.getFullyQualifiedName());
						}
						else if(this.hmStringTypeToSubType.containsKey(parameter)){
							for(IType itype:this.hmStringTypeToSubType.get(parameter)){
								if(this.findRelatedType(parameterType,findType,relatedTypeList,1)){
									relatedTypeList.add(type.getFullyQualifiedName());	
								}
							}
						}*/
					}
				}
			}
		}
		return false;
	}
	
	//determine whether second type is a a sub type of the first one 
	public boolean isTypeHierarchyMatches(IType firstType, IType secondType) throws JavaModelException{
		IType tmpType = secondType;
		while(tmpType!=null){
			if(tmpType.getFullyQualifiedName().equals(tmpType.getFullyQualifiedName()))
				return true;
			else {
				if(tmpType.getSuperclassName()!=null && this.hmQualifiedNameToiType.containsKey(tmpType.getSuperclassName())){
					tmpType = this.hmQualifiedNameToiType.get(tmpType.getSuperclassName());
				}
				else tmpType=null;
			}
		}
		
		for(String s:secondType.getSuperInterfaceNames()){
			if(firstType.getFullyQualifiedName().equals(s))
				return true;
		}
		return false;
	}
	public boolean isASubType(IType type,IType findType) throws JavaModelException{
		/*if(this.hmStringTypeToSubType.containsKey(type.getFullyQualifiedName())==false) return false;
		
		HashSet<IType> typeSet = this.hmStringTypeToSubType.get(type.getFullyQualifiedName());
		HashSet<IType> nextTypeSet = new HashSet();
		while(typeSet!=null){
			for(IType t:typeSet){
				if(t.getFullyQualifiedName().equals(findType.getFullyQualifiedName()))
					return true;
				else if(this.isASubType(t, findType)==true) return true;
			}
			
		}*/
		System.out.println("Type: "+type);
		if(type.isInterface()){
				for(String s:findType.getSuperInterfaceNames()){
					if(this.isASubType(type,hmQualifiedNameToiType.get(s))==true) return true;
				}
			
			return false;
		}
		
		else if(type.isClass()&& findType.isClass()){
			while(findType.getSuperclassName()!=null){
				findType = hmQualifiedNameToiType.get(findType.getSuperclassName());
				if(findType.getFullyQualifiedName().equals(type.getFullyQualifiedName()))
					return true;
			}
			return false;
		}
		else return false;
		/*while(findType!=null){
			
			if(findType.getSuperclassName()!=null){
				IType superType = this.hmIType.get(type.getSuperclassName()); 
				if(superType!=null && superType.getFullyQualifiedName().equals(type.getFullyQualifiedName())) return true;
			}
		}*/
		
		
	}
	
	public void save(String filePath) throws IOException{
		System.out.println("Now saving the type list... Total Types: "+this.iTypeList.size());
		ArrayList<String> lineList = new ArrayList();
		for(int i=0;i<this.iTypeList.size();i++){
			IType iType = iTypeList.get(i);
			lineList.add("<class>"+iType.getFullyQualifiedName()+"</class>");	
			String methodLine = "";
			//now add all method name and their parameter
			try {
				for(IMethod method: iType.getMethods()){
					String methodName = method.getElementName();
					ArrayList<String> parameterList = new ArrayList();
					for(String parameter:method.getParameterTypes()){
						System.out.println("Parameter Signature: "+parameter);
						System.out.println("Parametre: "+this.typeSignatureToFullName(method, parameter));
						parameterList.add(this.typeSignatureToFullName(method, parameter));
					}
					methodLine = "<method>"+"<name>"+methodName+"</name>"+"<param>"+parameterList.toString()+"</param>"+"<return>"+typeSignatureToFullName(method,Signature.getReturnType(method.getSignature()))+"</return>"+"</method>";
					lineList.add(methodLine);
					System.out.println("Method: "+methodLine);
				}
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			lineList.add(methodLine);
			System.out.println("Completed method collection ["+i+"]of["+iTypeList.size()+"]");
		}
		
		System.out.println("Total Lines: "+lineList.size());
		//now write the lines into files
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(filePath)));
		for(int i=0;i<lineList.size();i++){
			bw.write(lineList.get(i));
			if(i<lineList.size()-1){
				bw.newLine();
			}
		}
		bw.close();
	}
	public void generateTypeRelation() throws JavaModelException{
		
		int current=0;
		
		for(IType type:this.iTypeList){
				System.out.println("Current: "+(current++));
			if(Flags.isPublic(type.getFlags())==false || type.isInterface()) continue;
			IType classType = type;
			while(classType!=null){
			for(IMethod method:type.getMethods()){
				if(Flags.isPublic(method.getFlags())){
					//if(method.isConstructor())
					{
						for(String parameterTypeSignature:method.getParameterTypes()){
							if(parameterTypeSignature.startsWith("L")){
								String parameter = this.getTypeName(parameterTypeSignature);
								IType  parameterType = this.hmQualifiedNameToiType.get(parameter);
								
								//p1. explore direct relation
								if(parameterType==null) continue;						
								else{
									if(typeRelation.containsKey(parameter)==true){
										HashSet<String> set = typeRelation.get(parameter);
										set.add(type.getFullyQualifiedName());
										typeRelation.put(parameter,set);
									}
									else{
										HashSet<String> set = new HashSet();
										set.add(type.getFullyQualifiedName());
										typeRelation.put(parameter,set);
									}
								}
								
								//p2. now explore via relation
								//System.out.println("Parameter: "+parameter);
								HashSet<IType> subTypes = this.hmStringTypeToSubType.get(parameter);
								if(subTypes!=null){
								for(IType t:subTypes){
									if(typeRelation.containsKey(t.getFullyQualifiedName())==true){
										HashSet<String> set = typeRelation.get(t.getFullyQualifiedName());
										set.add(type.getFullyQualifiedName());
										typeRelation.put(t.getFullyQualifiedName(),set);
									}
									else{
										HashSet<String> set = new HashSet();
										set.add(type.getFullyQualifiedName());
										typeRelation.put(t.getFullyQualifiedName(),set);
									}
								}
								}
							}
						}
					}
				}
			}
			if(classType.getSuperclassName()!=null){
				classType = this.hmQualifiedNameToiType.get(classType.getSuperclassName());
			}
			else classType = null;
		}
	}
}
	
	private boolean findRelatedType(IType sourceType, IType findType,
			HashSet<String> relatedTypeList,int level) throws JavaModelException {
		// TODO Auto-generated method stub
		//System.out.println("SourceType: "+sourceType);
		//System.out.println("SourceType: "+sourceType.getMethods().length);
		
		if(level>4) return false;
		for(IMethod method:sourceType.getMethods()){
			if(Flags.isPublic(method.getFlags())==false) continue;
			if(method.isConstructor()&& method.getNumberOfParameters()>0){
				for(String parameterTypeSignature:method.getParameterTypes()){
					if(parameterTypeSignature.startsWith("L")){

						//p1. If method parameter type matches with with our find type
						String parameter = this.getTypeName(parameterTypeSignature);
						IType  parameterType = this.hmQualifiedNameToiType.get(parameter);
						if(parameterType==null) continue;						
						else if(parameter.equals(findType)){
							return true;
						}

						//p2. if not, we need to explore the  parameter type: whether the find type is a sub type of the parameter type
						//if(this.isASubType(parameterType, findType)){
						//	return true;
						//}

						//look for the sub types of this parameter type
						//p3. look for other type that this parameter type can accept
						/*ArrayList<IType> subTypes = this.getSubTypes(parameter);
						for(IType t:subTypes){
							if(this.findRelatedType(t,findType,relatedTypeList,level+1)){
								return true;
							}
						}*/
						
						/*else if(this.findRelatedType(parameterType,findType,relatedTypeList,level+1)){
							return true;
						}*/
					}
				}
			}
		}
		
		/*for(IType type:this.typeList){
			for(IMethod method:type.getMethods()){
				if(method.getReturnType()!=null && method.getReturnType().startsWith("L")&& this.getTypeName(method.getReturnType()).equals(sourceType.getFullyQualifiedName())){
					//now see whether any of its parameter uses the object
					for(String parameterTypeSignature:method.getParameterTypes()){
						if(parameterTypeSignature.startsWith("L")){

							//p1. If method parameter type matches with with our find type
							String parameter = this.getTypeName(parameterTypeSignature);
							IType  parameterType = this.hmIType.get(parameter);
							if(parameter.equals(findType.getFullyQualifiedName()))
								return true;
						}
					}
				}
			}
		}*/
		
		return false;

	}

	public ArrayList<IType> getSubTypes(String type){
		ArrayList<IType> list = new ArrayList();
		ArrayList<IType> nextList = new ArrayList();
		
		ArrayList<IType> tmpList = new ArrayList();
		HashSet<String> hsVisited = new HashSet();
		if(hmStringTypeToSubType.containsKey(type)){
			//list.addAll(this.hmStringTypeToSubType.get(type));
			nextList.addAll(this.hmStringTypeToSubType.get(type));
		}
		int count=0;
		do{ 
			count++;
			list.addAll(nextList);
			tmpList.clear();
			for(IType t1:nextList){
				hsVisited.add(t1.getFullyQualifiedName());
				if(this.hmStringTypeToSubType.containsKey(t1.getFullyQualifiedName())){
					for(IType t2:this.hmStringTypeToSubType.get(t1.getFullyQualifiedName())){
						if(hsVisited.contains(t2.getFullyQualifiedName())==false){
							tmpList.add(t2);
							hsVisited.add(t2.getFullyQualifiedName());
						}
					}
				}
			}
			nextList.clear();
			nextList.addAll(tmpList);
		}while(tmpList.size()>0&& count<=3);
		
		return list;
	}
	public void subTypeFinder() throws JavaModelException{
		System.out.println("Total Type List = "+this.iTypeList.size());
		for(IType iType:this.iTypeList){
			IType currentIType = iType;
			if(iType.getSuperclassName()==null || iType.getSuperclassName()!=null && this.hmStringTypeToSubType.containsKey(iType.getSuperclassName())) continue;
			while(currentIType!=null){
				if(currentIType.getSuperclassName()!=null){
					if(this.hmStringTypeToSubType.containsKey(currentIType.getSuperclassName())){
						HashSet<IType> hset = this.hmStringTypeToSubType.get(currentIType.getSuperclassName());
						hset.add(currentIType);
						if(this.hmStringTypeToSubType.containsKey(currentIType.getFullyQualifiedName())){
							hset.addAll(this.hmStringTypeToSubType.get(currentIType.getFullyQualifiedName()));
						}
						this.hmStringTypeToSubType.put(currentIType.getSuperclassName(),hset);
			
					}
					else{
						HashSet<IType> hset = new HashSet();
						hset.add(currentIType);
						if(this.hmStringTypeToSubType.containsKey(currentIType.getFullyQualifiedName())){
							hset.addAll(this.hmStringTypeToSubType.get(currentIType.getFullyQualifiedName()));
						}
						this.hmStringTypeToSubType.put(currentIType.getSuperclassName(),hset);
					}
					
					for(String superInterface: currentIType.getSuperInterfaceNames()){
						if(this.hmStringTypeToSubType.containsKey(superInterface)){
							HashSet<IType> hset = this.hmStringTypeToSubType.get(superInterface);
							hset.add(currentIType);
							if(this.hmStringTypeToSubType.containsKey(currentIType.getFullyQualifiedName())){
								hset.addAll(this.hmStringTypeToSubType.get(currentIType.getFullyQualifiedName()));
							}
							this.hmStringTypeToSubType.put(superInterface,hset);
				
						}
						else{
							HashSet<IType> hset = new HashSet();
							hset.add(currentIType);
							if(this.hmStringTypeToSubType.containsKey(currentIType.getFullyQualifiedName())){
								hset.addAll(this.hmStringTypeToSubType.get(currentIType.getFullyQualifiedName()));
							}
							this.hmStringTypeToSubType.put(superInterface,hset);
						}
						
					}
				}
				currentIType = this.hmQualifiedNameToiType.get(currentIType.getSuperclassName()); 
			}
		}
		
		//Now testing
		if(this.hmStringTypeToSubType.containsKey("java.awt.Container")){
			for(IType iType:this.hmStringTypeToSubType.get("java.awt.Container")){
				System.out.println("SubType: "+iType.getFullyQualifiedName());
			}
		}
	}
	public void load() throws CoreException{
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		System.out.println("Root Location:" + root.getLocation().toOSString());
		IProject[] projects = root.getProjects();
		
		for (IProject project : projects) {
			if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
				IJavaProject javaProject = JavaCore.create(project);
				System.out.println("Project: "+javaProject.getElementName());
				IPackageFragment[] packages = javaProject.getPackageFragments();
				
				
				for(IPackageFragment packageFragment:packages ){
					//System.out.println("Package: "+packageFragment.getElementName());
				
					if (packageFragment.getKind() == IPackageFragmentRoot.K_BINARY){
						
						for (IClassFile classFile : packageFragment.getClassFiles()) {
							
							//System.out.println("----classFile: "+ classFile.getType().getFullyQualifiedName());
							for (IJavaElement javaElement : classFile.getChildren()) {
								if(javaElement instanceof IType && javaElement.getElementName().length()>0 && (Flags.isPublic(((IType)javaElement).getFlags()) ||Flags.isProtected(((IType)javaElement).getFlags()))
										&& FrameworkInfoUtility.isInteresting(((IType)javaElement).getFullyQualifiedName())){
									IType iType = ((IType)javaElement);
									
									this.iTypeList.add((IType)javaElement);
									this.hmQualifiedNameToiType.put(((IType)javaElement).getFullyQualifiedName(), iType);
									
									System.out.println("--------IType "+  ((IType)javaElement).getFullyQualifiedName());							
									System.out.println("Super Class Name: "+((IType)javaElement).getSuperclassName());
									System.out.println("Super Class Type Signature: "+((IType)javaElement).getSuperclassTypeSignature());
									
									/*Printing all methods 
									for(IMethod method: ((IType)javaElement).getMethods()){
										this.printIMethod(method);
									}*/
								}
							}
						}				
					}
				}
			}
		}
		
		
		FrameworkStatistics fstatistics = new FrameworkStatistics();
		fstatistics.run(this.iTypeList);
		fstatistics.print();
		
		//Enable the following code for type relation analysis
		/*
		this.subTypeFinder();
		HashSet<String> list = new HashSet();
		IType type = this.hmIType.get("javax.Swing.JButton");
		
		for(IType t:this.getSubTypes("java.io.Reader")){
			System.out.println("S= "+t.getFullyQualifiedName());
		}
		
		this.generateTypeRelation();
		HashSet<String> relatedTypes = new HashSet();
		HashSet<String> hs = this.typeRelation.get("javax.swing.JButton");
		relatedTypes.addAll(hs);
		for(String s:relatedTypes){
			if(this.typeRelation.containsKey(s)){
				for(String k: this.typeRelation.get(s)){
					hs.add(k);
				}
			}
		}
		for(String s:hs){
			System.out.println("RT: "+s);
		}
		
		*/
		//System.out.println("Type: "+type.getFullyQualifiedName());
		
		
		/*System.out.println("Completed............................................................");
		this.findRelatedType(type, list);
		for(String s:list){
			System.out.println("Related Type: "+s);
		}
		System.out.println("Completed2............................................................");
		*/
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//JButton b = new JButton();
		//JComponent c = new Jcomponent
		//System.out.println();
		System.out.println("Replace: "+"java/awt/font/NumericShaper".replaceAll("/", "."));
	}

}
