package service;

import Entity.ResultEntity;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

import java.util.HashMap;
import java.util.Map;

public class Test {

    //非静态方法
    public void method1(Integer num,String type){
        System.out.println("target method 1");
    }

    // 静态方法
    public static int method2(Integer num,String type){
        System.out.println(5/2);
        return 2;
    }

    //非静态方法
    public void method3(Integer num,String type) throws Exception{
        System.out.println("target method 1");
    }

    // 静态方法
    public static int method4(Integer num,String type)throws Exception{
        System.out.println(5/2);
        return 2;
    }

    //非静态方法
    public final void method5(Integer num,String type) throws Exception{
        System.out.println("target method 1");
    }

    // 静态方法
    public static void method8(Integer num,String type){
        System.out.println(5/2);
    }

    //非静态方法
    public int method9(Integer num,String type){
        try {
            System.out.println("target method 1");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 2;
    }

    // 静态方法
    public static void method10(Integer num,String type){
        try {
            System.out.println(5/2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public  static final ResultEntity method6(Integer num,String type)throws Exception{
        return new ResultEntity();
    }

    public  static final ResultEntity method7(Integer num, String type)throws Exception{
        ResultEntity resultEntity = new ResultEntity();
        resultEntity.setResult("原本的返回值");
        return resultEntity;
    }

    @org.junit.Test
    public void methodTest() throws NotFoundException {
        ClassPool classPool = ClassPool.getDefault();
        CtClass ctClass = classPool.get("service.Test");
        for (int i = 6; i < 8; i++) {
            CtMethod declaredMethod = ctClass.getDeclaredMethod("method" + i);
            Map<String, String> methodParamNames = getMethodParamNames(declaredMethod);
            for (String s : methodParamNames.keySet()) {
                System.out.print(s+" ");
            }
            System.out.println();
        }
    }

    protected static Map<String,String> getMethodParamNames(CtMethod cm) {
        MethodInfo methodInfo = cm.getMethodInfo();
        CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
        LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.
                getAttribute(LocalVariableAttribute.tag);
        if (attr == null) {
            return null;
        }
        Map<String,String> paramInfo = new HashMap<>();
        try {
            for (int i = 0; ; i++) {
                paramInfo.put(attr.variableName(i),attr.descriptor(i));
            }
        } catch (Exception e) { }
        return paramInfo;
    }

    public static void main(String[] args) {
        System.out.println("second commit");
    }

}
