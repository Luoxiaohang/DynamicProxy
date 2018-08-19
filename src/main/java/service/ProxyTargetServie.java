package service;

import Entity.ResultEntity;
import anotation.Service;
import org.junit.Test;

@Service
public class ProxyTargetServie {


    @Test
    public void test(){
        method1(2,"ddd");
    }

    public void method1(Integer num,String type){
        System.out.println("我是中间： "+num +" "+type);
    }

    public ResultEntity method2(Integer num, String type){
        System.out.println("我是中间： "+num +" "+type);
        ResultEntity resultEntity = new ResultEntity();
        resultEntity.setResult("原本的返回值");
        return resultEntity;
    }
}
