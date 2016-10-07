package com.srlab.frameworkInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

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

public class TypeRelationAnalyzer {

	/**
	 * @param args
	 */
	private ArrayList<IType> iTypeList;
	private HashMap<String, HashSet<IType>> hmStringTypeToSubTypes;
	private HashMap<String, HashSet<IType>> hmStringTypeToAllSubTypes;
	private HashMap<String, IType> hmQualifiedNameToIType;

	public TypeRelationAnalyzer(ArrayList<IType> _iTypeList,
			HashMap<String, IType> _hmQualifiedNameToIType) {
		this.iTypeList = _iTypeList;
		this.hmQualifiedNameToIType = _hmQualifiedNameToIType;
		this.hmStringTypeToSubTypes = new HashMap();
	}
	
	private ArrayList<IType> getAllSubTypes(String typeName) {
		
		if(this.hmStringTypeToAllSubTypes.containsKey(typeName)){
			return this.getAllSubTypes(typeName);
		}
		else{
			HashSet<IType> subTypes = new HashSet();
			if (hmStringTypeToSubTypes.containsKey(typeName)) {
				subTypes.addAll(hmStringTypeToSubTypes.get(typeName));
	
				for (IType subType : hmStringTypeToSubTypes.get(typeName)) {
					subTypes.addAll(this.getAllSubTypes(subType
							.getFullyQualifiedName()));
				}
			}
			this.hmStringTypeToAllSubTypes.put(typeName,subTypes);
			return new ArrayList(subTypes);
		}
	}
	
	//only call this one time
	public void allSubTypeFinder(){
		for(IType iType:this.iTypeList){
			this.getAllSubTypes(iType.getFullyQualifiedName());
		}
	}
	
    //find whether type2 is a sub type of type 1
    public boolean isASubType(IType type1, IType type2) {
        HashSet<IType> typeSet = this.hmStringTypeToAllSubTypes.get(type1.getFullyQualifiedName());
        return typeSet.contains(type2);
    }

    public boolean findRelatedType(IType findType,
        HashSet<String> relatedTypeList) throws JavaModelException {
        System.out.println("Total: " + relatedTypeList.size());

        int current = 0;

        for (IType type : this.iTypeList) {
            System.out.println("Current: " + (current++));

            for (IMethod method : type.getMethods()) {
                if (Flags.isPublic(method.getFlags()) == false) {
                    continue;
                }

                for (String parameterTypeSignature : method.getParameterTypes()) {
                    if (parameterTypeSignature.startsWith("L")) {
                        //p1. If method parameter type matches with with our find type
                        String parameter = FrameworkInfoUtility.typeSignatureToFullName(method,parameterTypeSignature);
                        IType parameterType = this.hmQualifiedNameToIType.get(parameter);

                        //System.out.println("ParameterTypeSignature:" +parameterTypeSignature);
                        //System.out.println("Parameter: "+parameter);
                        //System.out.println("ParameterType: "+parameterType);
                        //System.out.println("Method: "+method.getElementName());
                        if (parameterType == null) {
                            continue;
                        }

                        if (parameter.equals(findType.getFullyQualifiedName())) {
                            relatedTypeList.add(type.getFullyQualifiedName());
                        }

                        //p2. if not, we need to explore the  parameter type: whether the find type is a sub type of the parameter type
                        /*if(this.isASubType(parameterType, findType)){
                                relatedTypeList.add(type.getFullyQualifiedName());
                        }*/

                        //p3. look for other type that this parameter type can accept
                        HashSet<IType> subTypes = this.hmStringTypeToSubTypes.get(parameter);

                        /*
                        for (IType t : subTypes) {
                            if (this.findRelatedType(t, findType,
                                        relatedTypeList, 1)) {
                                relatedTypeList.add(type.getFullyQualifiedName());
                            }
                        }*/

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

    /*public void generateTypeRelation() throws JavaModelException {
        int current = 0;

        for (IType type : this.iTypeList) {
            System.out.println("Current: " + (current++));

            if ((Flags.isPublic(type.getFlags()) == false) ||
                    type.isInterface()) {
                continue;
            }

            IType classType = type;

            while (classType != null) {
                for (IMethod method : type.getMethods()) {
                    if (Flags.isPublic(method.getFlags())) {
                        //if(method.isConstructor())
                        {
                            for (String parameterTypeSignature : method.getParameterTypes()) {
                                if (parameterTypeSignature.startsWith("L")) {
                                    String parameter = this.getTypeName(parameterTypeSignature);
                                    IType parameterType = this.hmQualifiedNameToIType.get(parameter);

                                    //p1. explore direct relation
                                    if (parameterType == null) {
                                        continue;
                                    } else {
                                        if (typeRelation.containsKey(parameter) == true) {
                                            HashSet<String> set = typeRelation.get(parameter);
                                            set.add(type.getFullyQualifiedName());
                                            typeRelation.put(parameter, set);
                                        } else {
                                            HashSet<String> set = new HashSet();
                                            set.add(type.getFullyQualifiedName());
                                            typeRelation.put(parameter, set);
                                        }
                                    }

                                    //p2. now explore via relation
                                    //System.out.println("Parameter: "+parameter);
                                    HashSet<IType> subTypes = this.hmStringTypeToSubTypes.get(parameter);

                                    if (subTypes != null) {
                                        for (IType t : subTypes) {
                                            if (typeRelation.containsKey(
                                                        t.getFullyQualifiedName()) == true) {
                                                HashSet<String> set = typeRelation.get(t.getFullyQualifiedName());
                                                set.add(type.getFullyQualifiedName());
                                                typeRelation.put(t.getFullyQualifiedName(),
                                                    set);
                                            } else {
                                                HashSet<String> set = new HashSet();
                                                set.add(type.getFullyQualifiedName());
                                                typeRelation.put(t.getFullyQualifiedName(),
                                                    set);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (classType.getSuperclassName() != null) {
                    classType = this.hmQualifiedNameToIType.get(classType.getSuperclassName());
                } else {
                    classType = null;
                }
            }
        }
    }*/

    /*private boolean findRelatedType(IType sourceType, IType findType,
        HashSet<String> relatedTypeList, int level) throws JavaModelException {
        // TODO Auto-generated method stub
        //System.out.println("SourceType: "+sourceType);
        //System.out.println("SourceType: "+sourceType.getMethods().length);
        if (level > 4) {
            return false;
        }

        for (IMethod method : sourceType.getMethods()) {
            if (Flags.isPublic(method.getFlags()) == false) {
                continue;
            }

            if (method.isConstructor() && (method.getNumberOfParameters() > 0)) {
                for (String parameterTypeSignature : method.getParameterTypes()) {
                    if (parameterTypeSignature.startsWith("L")) {
                        //p1. If method parameter type matches with with our find type
                        String parameter = this.getTypeName(parameterTypeSignature);
                        IType parameterType = this.hmQualifiedNameToIType.get(parameter);

                        if (parameterType == null) {
                            continue;
                        } else if (parameter.equals(findType)) {
                            return true;
                        }

                        //p2. if not, we need to explore the  parameter type: whether the find type is a sub type of the parameter type
                        //if(this.isASubType(parameterType, findType)){
                        //	return true;
                        //}

                        //look for the sub types of this parameter type
                        //p3. look for other type that this parameter type can accept
                        ArrayList<IType> subTypes = this.getSubTypes(parameter);
                        for(IType t:subTypes){
                                if(this.findRelatedType(t,findType,relatedTypeList,level+1)){
                                        return true;
                                }
                        }

                        else if(this.findRelatedType(parameterType,findType,relatedTypeList,level+1)){
                                return true;
                        }
                    }
                }
            }
        }

        for(IType type:this.typeList){
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
        }
        return false;
    }
    */
	public void subTypeFinder() throws JavaModelException {
		System.out.println("Total Type List = " + this.iTypeList.size());

		for (IType iType : this.iTypeList) {
			IType currentIType = iType;

			if ((iType.getSuperclassName() == null)
					|| ((iType.getSuperclassName() != null) && this.hmStringTypeToSubTypes
							.containsKey(iType.getSuperclassName()))) {
				continue;
			}

			while (currentIType != null) {
				if (currentIType.getSuperclassName() != null) {
					if (this.hmStringTypeToSubTypes.containsKey(currentIType
							.getSuperclassName())) {
						HashSet<IType> hset = this.hmStringTypeToSubTypes
						.get(currentIType.getSuperclassName());
						hset.add(currentIType);

						if (this.hmStringTypeToSubTypes
								.containsKey(currentIType
										.getFullyQualifiedName())) {
							hset.addAll(this.hmStringTypeToSubTypes
									.get(currentIType.getFullyQualifiedName()));
						}

						this.hmStringTypeToSubTypes.put(
								currentIType.getSuperclassName(), hset);
					} else {
						HashSet<IType> hset = new HashSet();
						hset.add(currentIType);

						if (this.hmStringTypeToSubTypes
								.containsKey(currentIType
										.getFullyQualifiedName())) {
							hset.addAll(this.hmStringTypeToSubTypes
									.get(currentIType.getFullyQualifiedName()));
						}

						this.hmStringTypeToSubTypes.put(
								currentIType.getSuperclassName(), hset);
					}

					for (String superInterface : currentIType
							.getSuperInterfaceNames()) {
						if (this.hmStringTypeToSubTypes
								.containsKey(superInterface)) {
							HashSet<IType> hset = this.hmStringTypeToSubTypes
							.get(superInterface);
							hset.add(currentIType);

							if (this.hmStringTypeToSubTypes
									.containsKey(currentIType
											.getFullyQualifiedName())) {
								hset.addAll(this.hmStringTypeToSubTypes
										.get(currentIType
												.getFullyQualifiedName()));
							}

							this.hmStringTypeToSubTypes.put(superInterface,
									hset);
						} else {
							HashSet<IType> hset = new HashSet();
							hset.add(currentIType);

							if (this.hmStringTypeToSubTypes
									.containsKey(currentIType
											.getFullyQualifiedName())) {
								hset.addAll(this.hmStringTypeToSubTypes
										.get(currentIType
												.getFullyQualifiedName()));
							}

							this.hmStringTypeToSubTypes.put(superInterface,
									hset);
						}
					}
				}

				currentIType = this.hmQualifiedNameToIType.get(currentIType
						.getSuperclassName());
			}
		}

		// Now testing
		if (this.hmStringTypeToSubTypes.containsKey("java.awt.Container")) {
			for (IType iType : this.hmStringTypeToSubTypes
					.get("java.awt.Container")) {
				System.out.println("SubType: " + iType.getFullyQualifiedName());
			}
		}
	}

	
	public void run() throws CoreException {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        System.out.println("Root Location:" + root.getLocation().toOSString());

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
                               {
                                    IType iType = (IType)javaElement;
                                    this.iTypeList.add((IType) javaElement);
                                    this.hmQualifiedNameToIType.put(((IType) javaElement).getFullyQualifiedName(),
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

        FrameworkStatistics frameworkStatistics = new FrameworkStatistics();
        frameworkStatistics.run(this.iTypeList);
        frameworkStatistics.print();

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

	}

}
