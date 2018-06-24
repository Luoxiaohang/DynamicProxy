package controller;

import Entity.ResultEntity;
import anotation.*;
import javassist.*;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;
import org.junit.Test;
import service.ProxyTargetServie;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MainController {

    @Import
    ProxyTargetServie target;

    public static void main(String[] args) throws Exception {

        ClassPool classPool = ClassPool.getDefault();
        classPool.appendClassPath(new ClassClassPath(MainController.class));

        //获取类路径
        String path = MainController.class.getResource("/").toURI().getPath();
        String calssPath = new File(path).getParentFile().getParentFile().getCanonicalPath() + "\\target\\classes";

        File dir = new File(calssPath);
        File[] files = dir.listFiles();

        Map<String, Object> instances = new HashMap<>();
        Map<String, Class> classes = new HashMap<>();

        // 扫描代理类，生成动态代理
        for (File file : files) {
            if (file.getName().contains("proxy")) {
                File[] classFiles = file.listFiles();
                for (File classFile : classFiles) {
                    String className = classFile.getAbsolutePath().replaceAll("\\\\", ".");
                    className = className.substring(className.lastIndexOf("classes") + "classes".length() + 1, className.lastIndexOf(".class"));
                    Class instance = MainController.class.getClassLoader().loadClass(className);
                    if (instance.isAnnotationPresent(ProxyHandler.class)) {
                        ProxyHandler annotation = (ProxyHandler) instance.getDeclaredAnnotation(ProxyHandler.class);
                        String type = annotation.type();
                        CtClass ctClass = classPool.get(type);
                        //解冻
                        ctClass.defrost();
                        //添加属性(类型为代理类型)
                        String fieldName = instance.getSimpleName().substring(0, 1).toLowerCase() + instance.getSimpleName().substring(1, instance.getSimpleName().length());
                        CtField field = new CtField(classPool.get(instance.getName()), fieldName, ctClass);
                        ctClass.addField(field);
                        //获取代理类的方法
                        Method[] declaredMethods = instance.getDeclaredMethods();
                        for (Method declaredMethod : declaredMethods) {
                            String[] name = declaredMethod.getName().split("_");
                            //修改目标类方法
                            CtMethod targetMethod = ctClass.getDeclaredMethod(name[1]);
                            Map<String, String> paramInfos = getMethodParamNames(targetMethod);
                            if(name[0].equals("before")){
                                StringBuilder temp = new StringBuilder();
                                for (String param : paramInfos.keySet()) {
                                    temp.append(param+" = ("+paramInfos.get(param)+")_param_result.get(\""+param+"\");\n");
                                }
                                targetMethod.insertBefore("java.util.Map _param_result = "+fieldName + "."+declaredMethod.getName()+"($$);\n"
                                        +temp.toString());
                            }else if(name[0].equals("after")){
                                if(!targetMethod.getReturnType().getName().equals("void")){
                                    targetMethod.insertAfter(fieldName + "."+declaredMethod.getName()+"($_);");
                                }else{
                                    targetMethod.insertAfter(fieldName + "."+declaredMethod.getName()+"();");
                                }
                            }
                        }
                        ctClass.writeFile(MainController.class.getResource("/").getPath());
                    }
                }
            }
        }

        // 实例化对象
        for (File file : files) {
            if (file.getName().contains("controller") || file.getName().contains("service") || file.getName().contains("proxy")) {
                File[] classFiles = file.listFiles();
                for (File classFile : classFiles) {
                    String className = classFile.getAbsolutePath().replaceAll("\\\\", ".");
                    className = className.substring(className.lastIndexOf("classes") + "classes".length() + 1, className.lastIndexOf(".class"));
                    Class instance = MainController.class.getClassLoader().loadClass(className);
                    instances.put(className, instance.newInstance());
                    classes.put(className, instance);
                }
            }
        }

        // IOC 注入
        for (String type : instances.keySet()) {
            Object instance = instances.get(type);
            Field[] fields = instance.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (instances.containsKey(field.getType().getName())) {
                    field.setAccessible(true);
                    field.set(instance, instances.get(field.getType().getName()));
                }
            }
        }

        //启动程序
        for (Object intance : instances.values()) {
            Method[] declaredMethods = intance.getClass().getDeclaredMethods();
            for (Method method : declaredMethods) {
                if (method.isAnnotationPresent(MainMethod.class)) {
                    method.invoke(intance);
                    break;
                }
            }
        }
    }

    @MainMethod
    public void proxyTest() {
        int num = 0;
        String type = "test";
        System.out.println("方法一：");
        target.method1(num, type);
        System.out.println("方法二：");
        ResultEntity resultEntity = target.method2(num, type);
        System.out.println(resultEntity.getResult());
    }

    /**
     *
     * @param cm
     * @return 键：属性名
     *          值：属性类型
     */
    protected static Map<String,String> getMethodParamNames(CtMethod cm) throws Exception {
        MethodInfo methodInfo = cm.getMethodInfo();
        CtClass[] parameterTypes = cm.getParameterTypes();
        CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
        LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
        if (attr == null) {
            return null;
        }
        Map<String,String> paramInfo = new HashMap<>();
        //非静态方法且没有try catch 语句第一个参数是this，如果有，第一个参数是e
        //静态方法且有try catch 语句第一个参数是e，如果有，参数列表即为参数列表.
        //如果参数有返回值，会把返回值放在最后一个
        List<String> names = new ArrayList<>();
        try {
            for (int i = 0; ; i++) {
                names.add(attr.variableName(i));
            }

        } catch (Exception e) {

        }
        names.remove("this");
        names.remove("e");
        try {
            for (int i = 0; i < names.size(); i++) {
                paramInfo.put(names.get(i),parameterTypes[i].getName());
            }
        } catch (Exception e) {
        }
        return paramInfo;
    }

    @Test
    public void methodTest() throws Exception {
        ClassPool classPool = ClassPool.getDefault();
        CtClass ctClass = classPool.get("service.Test");
        for (int i = 1; i < 11; i++) {
            CtMethod declaredMethod = ctClass.getDeclaredMethod("method" + i);
            Map<String, String> methodParamNames = getMethodParamNames(declaredMethod);
            for (String s : methodParamNames.keySet()) {
                System.out.print(s+" ");
            }
            System.out.println();
        }
    }

}
