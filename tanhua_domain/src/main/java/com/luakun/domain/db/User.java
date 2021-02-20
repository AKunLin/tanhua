package com.luakun.domain.db;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: luakun
 * @Date: 2021/02/03/18:22
 * @Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Long id;
    /**
     * 手机号
     */
    private String mobile;
    /**
     * 密码，json序列化时忽略
     */
    @JSONField(serialize = false)
    private String password;
    private Date created;
    private Date updated;
}
