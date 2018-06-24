package proxy;


import Entity.ResultEntity;
import anotation.ProxyHandler;

import java.util.HashMap;
import java.util.Map;

@ProxyHandler(type="service.ProxyTargetServie")
public class MethodProxy {

    public Map<String,Object> before_method1(Integer num, String type) {
        HashMap<String,Object> result = new HashMap<>();
        System.out.println("我是前：");
        System.out.println(num+" "+type);
        result.put("num",5);
        result.put("type","ddddd");
        return result;
    }

    public void after_method1(){
        System.out.println("我是后：");
    }

    public Map<String,Object> before_method2(Integer num, String type) {
        HashMap<String,Object> result = new HashMap<>();
        System.out.println("我是前");
        System.out.println(num+" "+type);
        result.put("num",5);
        result.put("type","ddddd");
        return result;
    }

    public void after_method2(ResultEntity resultEntity){
        System.out.println("返回值增强之前");
        System.out.println(resultEntity.getResult());
        resultEntity.setResult("返回值增强之后");
    }
}
