package com.srlab.frameworkInfo;

import java.util.ArrayList;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

public class FrameworkStatistics {
	private ArrayList<IType> typeList;
	private long nClasses;
	private long nInterfaces;
	private long nPublicMethods;
	private long nPrivateMethods;
	private long nProtectedMethods;
	private long nWithoutASMethods; //methods that does not have any access specifiers
	
	private long nPublicFields;
	private long nProtectedFields;
	private long nPrivateFields;
	private long nWithoutASFields;
	
	
	public FrameworkStatistics(ArrayList<IType> typeList){
		this.typeList = typeList;
		
		this.nClasses = 0;
		this.nInterfaces =0;
		
		this.nPrivateFields=0;
		this.nProtectedFields=0;
		this.nWithoutASFields =0;
		
		this.nPublicMethods =0;
		this.nPrivateMethods = 0;
		this.nProtectedMethods = 0;
		this.nWithoutASMethods = 0;
	}
	
	public void run(){
		for(IType type:this.typeList){
			try {
				if(type.isClass()){
					this.nClasses++;
				}
				else if(type.isInterface()){
					this.nInterfaces++;
				}
				
				for(IMethod method:type.getMethods()){
					if(Flags.isPublic(method.getFlags())){
						this.nPublicMethods++;
					}
					else if(Flags.isPrivate(method.getFlags())){
						this.nPrivateMethods++;
					}
					else if(Flags.isProtected(method.getFlags())){
						this.nProtectedMethods++;
					}
					else if(Flags.isPackageDefault(method.getFlags())){
						this.nWithoutASMethods++;
					}
				}
				
				for(IField field:type.getFields()){
					if(Flags.isPublic(field.getFlags())){
						this.nPublicFields++;
					}
					else if(Flags.isPrivate(field.getFlags())){
						this.nPrivateFields++;
					}
					else if(Flags.isProtected(field.getFlags())){
						this.nProtectedFields++;
					}
					else if(Flags.isPackageDefault(field.getFlags())){
						this.nWithoutASFields++;
					}
				}
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void print(){
		long totalTypes =(this.nClasses + this.nInterfaces);
		long totalMethods = (this.nPrivateMethods+this.nProtectedMethods+this.nPublicMethods+this.nWithoutASMethods);
		long totalFields = (this.nPrivateFields+this.nProtectedFields+this.nPublicFields+this.nWithoutASFields);
		System.out.println("Classes: "+this.nClasses);
		System.out.println("Interfaces: "+this.nInterfaces);
		
		System.out.println("Total Types: "+totalTypes);
		System.out.println("Total methods: "+totalMethods);
		System.out.println("Public Methods: "+this.nPublicMethods+" ["+((this.nPublicMethods*1.0f)/totalMethods)+"]");
		System.out.println("Private Methods: "+this.nPrivateMethods+ " ["+((this.nPrivateMethods*1.0f)/totalMethods)+"]");
		System.out.println("Protected Methods: "+this.nProtectedMethods+" ["+((this.nProtectedMethods*1.0f)/totalMethods)+"]");
		System.out.println("Without Access Specifier Methods: "+this.nWithoutASMethods+" ["+((this.nWithoutASMethods*1.0f)/totalMethods)+"]");
		
		
		System.out.println("Total Fields: "+totalFields);
		System.out.println("Public Fields: "+this.nPublicFields+" ["+((this.nPublicFields*1.0f)/totalMethods)+"]");
		System.out.println("Private Fields: "+this.nPrivateFields+ " ["+((this.nPrivateFields*1.0f)/totalMethods)+"]");
		System.out.println("Protected Fields: "+this.nProtectedFields+ " ["+((this.nProtectedFields*1.0f)/totalMethods)+"]");
		System.out.println("Without Access Specifier Fields: "+this.nWithoutASFields+ " ["+((this.nWithoutASFields*1.0f)/totalMethods)+"]");
		

		
	}
}
