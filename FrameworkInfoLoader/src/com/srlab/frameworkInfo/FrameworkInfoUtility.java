package com.srlab.frameworkInfo;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;


public class FrameworkInfoUtility {
    public static String[] frameworks = { "javax.swing." };
    public static final String framework_name = "swing";
    public static final String framework_full_info_path = "E:\\output\\" +
        framework_name + "_full_info" + ".txt";
    public static final String framework_class_info_path = "E:\\output\\" +
        framework_name + "_class_info" + ".txt";
    public static final String[] code_file_extensions = { ".java" };

    public static boolean isInteresting(ITypeBinding tb) {
        if (tb == null) {
            return false;
        } else {
            String qn = tb.getQualifiedName();

            for (String framework : frameworks) {
                if (qn.startsWith(framework)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isInteresting(String qn) {
        if (qn == null) {
            return false;
        } else {
            for (String framework : frameworks) {
                if (qn.startsWith(framework)) {
                    return true;
                }
            }
        }

        return false;
    }
    
    //type signature cannot handle basic type and return null for those cases
    public static  String typeSignatureToFullName(IMethod method, String signature)
        throws JavaModelException {
        //String name = method.getReturnType();

        String simpleName = Signature.getSignatureSimpleName(signature);
        IType type = method.getDeclaringType();

        String[][] allResults = type.resolveType(simpleName);

        System.out.println("String signature: " + signature);
        System.out.println("Simple Name: " + simpleName + "  All Results: ");

        String fullName = null;

        if (allResults != null) {
            String[] nameParts = allResults[0];

            if (nameParts != null) {
                fullName = new String();

                for (int i = 0; i < nameParts.length; i++) {
                    if (fullName.length() > 0) {
                        fullName += '.';
                    }

                    if (nameParts[i] != null) {
                        fullName += nameParts[i];
                    }
                }
            }
        }

        if (fullName == null) {
            return simpleName.replaceAll("/", ".");
        } else {
            return fullName.replaceAll("/", ".");
        }
    }

    public static String typeSignatureToSimpleName(String signature) {
        return Signature.getSignatureSimpleName(signature);
    }
}
