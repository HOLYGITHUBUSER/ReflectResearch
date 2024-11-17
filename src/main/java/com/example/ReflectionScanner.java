package com.example;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReflectionScanner {

    public static void main(String[] args) {
        String directoryPath = "/Users/jl/Downloads/test";
        List<String> reflectionCalls = new ArrayList<>();

        try {
            Files.walk(Paths.get(directoryPath))
                .filter(path -> path.toString().endsWith(".java"))
                .forEach(path -> {
                    try {
                        // 创建 JavaParser 实例
                        JavaParser parser = new JavaParser();
                        // 通过实例调用 parse 方法
                        CompilationUnit cu = parser.parse(new File(path.toString())).getResult().orElse(null);
                        if (cu != null) {
                            Map<String, String> variables = new HashMap<>();
                            cu.accept(new ReflectionMethodCallVisitor(reflectionCalls), variables);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 输出所有反射类的调用
        System.out.println("Class.forName 反射调用列表:");
        for (String call : reflectionCalls) {
            System.out.println(call);
        }
    }

    private static class ReflectionMethodCallVisitor extends VoidVisitorAdapter<Map<String, String>> {
        private final List<String> reflectionCalls;

        public ReflectionMethodCallVisitor(List<String> reflectionCalls) {
            this.reflectionCalls = reflectionCalls;
        }

        @Override
        public void visit(FieldDeclaration n, Map<String, String> arg) {
            super.visit(n, arg);
            if (arg == null) {
                arg = new HashMap<>();
            }
            for (VariableDeclarator var : n.getVariables()) {
                if (var.getInitializer().isPresent()) {
                    String variableName = var.getName().asString();
                    String initialValue = var.getInitializer().get().toString();
                    arg.put(variableName, initialValue);
                }
            }
        }

        @Override
        public void visit(MethodCallExpr n, Map<String, String> arg) {
            super.visit(n, arg);

            // 检查方法调用是否是 Class.forName
            if (isClassForNameMethodCall(n)) {
                String className = getClassNameFromMethodCall(n, arg);
                if (className != null) {
                    reflectionCalls.add("Class.forName(" + className + ")");
                }
            }
        }

        private boolean isClassForNameMethodCall(MethodCallExpr methodCall) {
            // 检查方法名是否为 forName
            String methodName = methodCall.getNameAsString();
            if (!methodName.equals("forName")) {
                return false;
            }

            // 检查方法调用是否在 Class 类上
            if (methodCall.getScope().isPresent()) {
                SimpleName scope = methodCall.getScope().get().asNameExpr().getName();
                return "Class".equals(scope.asString());
            }

            return false;
        }

        private String getClassNameFromMethodCall(MethodCallExpr methodCall, Map<String, String> variables) {
            if (methodCall.getArguments().size() == 1) {
                String argument = methodCall.getArgument(0).toString();
                if (variables.containsKey(argument)) {
                    return variables.get(argument);
                } else {
                    return argument;
                }
            }
            return null;
        }
    }
}
