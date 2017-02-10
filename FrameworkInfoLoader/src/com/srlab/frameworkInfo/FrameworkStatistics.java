package com.srlab.frameworkInfo;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import java.util.ArrayList;


public class FrameworkStatistics {
    private long totalClasses;
    private long totalInterfaces;
    private long totalMethods;
    private long totalPublicMethods;
    private long totalPrivateMethods;
    private long totalProtectedMethods;
    private long totalWithoutAccessSpecifierMethods; // methods that does not have any access specifiers
    private long totalFields;
    private long totalPublicFields;
    private long totalPrivateFields;
    private long totalProtectedFields;
    private long totalWithoutAccessSpecifierFields; // Fields that does not have any access specifiers

    public FrameworkStatistics() {
        this.totalClasses = 0;
        this.totalInterfaces = 0;

        this.totalFields = 0;
        this.totalPublicFields = 0;
        this.totalPrivateFields = 0;
        this.totalProtectedFields = 0;
        this.totalWithoutAccessSpecifierFields = 0;

        this.totalMethods = 0;
        this.totalPublicMethods = 0;
        this.totalPrivateMethods = 0;
        this.totalProtectedMethods = 0;
        this.totalWithoutAccessSpecifierMethods = 0;
    }

    public void run(ArrayList<IType> typeList) {
        for (IType type : typeList) {
            try {
                if (type.isClass()) {
                    this.totalClasses++;
                } else if (type.isInterface()) {
                    this.totalInterfaces++;
                }

                for (IMethod method : type.getMethods()) {
                	if(method.getElementName().contains("$")) continue;
                    totalMethods++;

                    if (Flags.isPublic(method.getFlags())) {
                        this.totalPublicMethods++;
                    } else if (Flags.isPrivate(method.getFlags())) {
                        this.totalPrivateMethods++;
                    } else if (Flags.isProtected(method.getFlags())) {
                        this.totalProtectedMethods++;
                    } else if (Flags.isPackageDefault(method.getFlags())) {
                        this.totalWithoutAccessSpecifierMethods++;
                    }
                }

                for (IField field : type.getFields()) {
                    this.totalFields++;

                    if (Flags.isPublic(field.getFlags())) {
                        this.totalPublicFields++;
                    } else if (Flags.isPrivate(field.getFlags())) {
                        this.totalPrivateFields++;
                    } else if (Flags.isProtected(field.getFlags())) {
                        this.totalProtectedFields++;
                    } else if (Flags.isPackageDefault(field.getFlags())) {
                        this.totalWithoutAccessSpecifierFields++;
                    }
                }
            } catch (JavaModelException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void print() {
        assert (this.totalMethods == (this.totalPublicMethods +
        this.totalPrivateMethods + this.totalProtectedMethods +
        this.totalWithoutAccessSpecifierMethods)) : "Total method should be same to the sum of four different methods";
        assert (this.totalFields == (this.totalPublicFields +
        this.totalPrivateFields + this.totalProtectedFields +
        this.totalWithoutAccessSpecifierFields)) : "Total fields should be same to the sum of four different fields";

        long totalTypes = (this.totalClasses + this.totalInterfaces);

        System.out.println("Total Types: " + totalTypes);
        System.out.println("Total Classes: " + this.totalClasses);
        System.out.println("Total Interfaces: " + this.totalInterfaces);

        System.out.println("Total methods: " + totalMethods);
        System.out.println("Public Methods: " + this.totalPublicMethods + " [" +
            ((this.totalPublicMethods * 1.0f) / totalMethods) + "]");
        System.out.println("Private Methods: " + this.totalPrivateMethods +
            " [" + ((this.totalPrivateMethods * 1.0f) / totalMethods) + "]");
        System.out.println("Protected Methods: " + this.totalProtectedMethods +
            " [" + ((this.totalProtectedMethods * 1.0f) / totalMethods) + "]");
        System.out.println("Without Access Specifier Methods: " +
            this.totalWithoutAccessSpecifierMethods + " [" +
            ((this.totalWithoutAccessSpecifierMethods * 1.0f) / totalMethods) +
            "]");

        System.out.println("Total Fields: " + totalFields);
        System.out.println("Public Fields: " + this.totalPublicFields + " [" +
            ((this.totalPublicFields * 1.0f) / totalMethods) + "]");
        System.out.println("Private Fields: " + this.totalPrivateFields + " [" +
            ((this.totalPrivateFields * 1.0f) / totalMethods) + "]");
        System.out.println("Protected Fields: " + this.totalProtectedFields +
            " [" + ((this.totalProtectedFields * 1.0f) / totalMethods) + "]");
        System.out.println("Without Access Specifier Fields: " +
            this.totalWithoutAccessSpecifierFields + " [" +
            ((this.totalWithoutAccessSpecifierFields * 1.0f) / totalMethods) +
            "]");
    }
}
