package com.srlab.frameworkInfo;

import java.awt.image.ImageObserver;
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

public class FrameworkInfoLoader {

	private String jarPath;
	private ArrayList<IType> typeList;
	private HashMap<String,IType> hmIType;
	private HashMap<String, HashSet<String>> typeRelation;
	private HashMap<String,HashSet<IType>> hmStringTypeToSubType;
	public FrameworkInfoLoader(String _jarPath){
		this.jarPath = _jarPath;
		this.typeList = new ArrayList();
		this.hmStringTypeToSubType = new HashMap();
		this.hmIType = new HashMap();
		this.typeRelation = new HashMap();
	}
	
	private void printIMethod(IMethod method) throws JavaModelException{
		String name = method.getElementName();
		ArrayList<String> parameterList = new ArrayList();
		for(String parameter:method.getParameterTypes()){
			parameterList.add(this.getTypeName(parameter));
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
		for(IType type:this.typeList){
			System.out.println("Current: "+(current++));
			
			for(IMethod method:type.getMethods()){
				if(Flags.isPublic(method.getFlags())==false) continue;
				for(String parameterTypeSignature:method.getParameterTypes()){
					if(parameterTypeSignature.startsWith("L")){
						
						//p1. If method parameter type matches with with our find type
						String parameter = this.getTypeName(parameterTypeSignature);
						IType  parameterType = this.hmIType.get(parameter);
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
				if(tmpType.getSuperclassName()!=null && this.hmIType.containsKey(tmpType.getSuperclassName())){
					tmpType = this.hmIType.get(tmpType.getSuperclassName());
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
					if(this.isASubType(type,hmIType.get(s))==true) return true;
				}
			
			return false;
		}
		
		else if(type.isClass()&& findType.isClass()){
			while(findType.getSuperclassName()!=null){
				findType = hmIType.get(findType.getSuperclassName());
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
	
	public void generateTypeRelation() throws JavaModelException{
		
		int current=0;
		
		for(IType type:this.typeList){
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
								IType  parameterType = this.hmIType.get(parameter);
								
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
				classType = this.hmIType.get(classType.getSuperclassName());
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
						IType  parameterType = this.hmIType.get(parameter);
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
		System.out.println("Total Type List = "+this.typeList.size());
		for(IType iType:this.typeList){
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
				currentIType = this.hmIType.get(currentIType.getSuperclassName()); 
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
									
									typeList.add((IType)javaElement);
									hmIType.put(((IType)javaElement).getFullyQualifiedName(), iType);
									
									System.out.println("--------IType "+  ((IType)javaElement).getFullyQualifiedName());							
									System.out.println("Super Class Name: "+((IType)javaElement).getSuperclassName());
									System.out.println("Super Class Type Signature: "+((IType)javaElement).getSuperclassTypeSignature());
									
									for(IMethod method: ((IType)javaElement).getMethods()){
										this.printIMethod(method);
									}
								}
							}
						}				
					}
				}
			}
		}
		
		FrameworkStatistics fstatistics = new FrameworkStatistics(this.typeList);
		fstatistics.run();
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
	}

}
