package com.srlab.frameworkInfo;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class FrameworkInfoUtility {
	public static String frameworks[] = {
		"javax.swing.","java.awt." //,"org.eclipse.swt"
	};
	public static String code_file_extensions[] = {".java"};

	public static boolean isInteresting(ITypeBinding tb){
		if(tb==null) return false;
		else {
			String qn = tb.getQualifiedName();
			for(String framework:frameworks){
				if(qn.startsWith(framework)) return true;
			}
		}
		return false;
	}
	
	public static boolean isInteresting(String qn){
		if(qn==null) return false;
		else {
			for(String framework:frameworks){
				if(qn.startsWith(framework)) return true;
			}
		}
		return false;
	}
}
