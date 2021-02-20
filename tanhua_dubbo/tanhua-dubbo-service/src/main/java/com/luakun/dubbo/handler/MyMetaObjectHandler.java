package com.luakun.dubbo.handler;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import java.util.Date;

/**
 * 自定义类实现公共字段自动写入
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        fillDateValue("created",new Date(),metaObject);
        fillDateValue("updated",new Date(),metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        fillDateValue("updated",new Date(),metaObject);
    }

    /**
     * 公共方法
     * @param fieldName
     * @param value
     * @param metaObject
     */
    private void fillDateValue(String fieldName,Object value,MetaObject metaObject){
        Object fieldValue = getFieldValByName(fieldName,metaObject);
        if(fieldValue != null){
            setFieldValByName(fieldName,value,metaObject);
        }
    }
}