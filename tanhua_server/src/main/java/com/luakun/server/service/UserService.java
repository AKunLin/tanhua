package com.luakun.server.service;


import com.alibaba.fastjson.JSON;
import com.luakun.commons.exception.TanHuaException;
import com.luakun.commons.templates.FaceTemplate;
import com.luakun.commons.templates.OssTemplate;
import com.luakun.commons.templates.SmsTemplate;
import com.luakun.domain.db.User;
import com.luakun.domain.db.UserInfo;
import com.luakun.domain.vo.ErrorResult;
import com.luakun.dubbo.api.UserApi;
import com.luakun.dubbo.api.UserInfoApi;
import com.luakun.server.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: luakun
 * @Date: 2021/02/03/20:25
 * @Description: 消费者:用户业务处理层
 */

@Service
@Slf4j
public class UserService {
    @Reference
    private UserApi userApi;
    @Value("${tanhua.redisValidateCodeKeyPrefix}")
    private String redisValidateCodeKeyPrefix;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private SmsTemplate smsTemplate;

    @Autowired
    private JwtUtils jwtUtils;
    @Reference
    private UserInfoApi userInfoApi;
    @Autowired
    private OssTemplate ossTemplate;
    @Autowired
    private FaceTemplate faceTemplate;

    /**
     * 查找用户
     * @param phone
     * @return
     */
    public ResponseEntity  findByPhone(String phone) {
        User user = userApi.findByPhone(phone);
        return ResponseEntity.ok(user);
    }

    /**
     * 保存用户
     * @param phone
     * @param password
     * @return
     */
    public ResponseEntity saveUser(String phone, String password) {
        User user = new User();
        user.setMobile(phone);
        user.setPassword(password);
        userApi.saveUser(user);
        return ResponseEntity.ok(null);
    }


    public Map<String, Object> loginVerification(String phone, String verificationCode) {
        log.info("输入参数：手机号{},验证码{}：",phone,verificationCode);
        Map<String, Object> map = new HashMap();
        map.put("isNew",false);
        //1根据手机号查询redis是存在验证码
        String key = redisValidateCodeKeyPrefix+phone;
        //redis中验证码
        String redisCode = redisTemplate.opsForValue().get(key);
        //对比验证码之前删除redis验证码
        redisTemplate.delete(key);
        //2如果reidis中验证码不存在，说明验证码已经失效
        if(StringUtils.isEmpty(redisCode)){
            throw new TanHuaException(ErrorResult.loginError());
        }
        //3redis验证存在，拿着用户输入的验证码 跟 redis的验证码 对比
        if(!redisCode.equals(verificationCode)){
            //对比验证码错误，说明验证码输入错误
            throw new TanHuaException(ErrorResult.validateCodeError());
        }
        //4验证码正确，根据手机号码查询用户表是否存在用户，
        User user = userApi.findByPhone(phone);

        //5用户不存在，自动注册用户
        if(user == null){
            user = new User();
            //设置手机号  密码（默认手机号码 后6位 并加密）
            user.setMobile(phone);
            user.setPassword(DigestUtils.md5Hex(phone.substring(phone.length()-6)));
            Long userId = userApi.saveUser(user);
            //将保持用户后id放到user对象中
            user.setId(userId);
            //后续生成token的时候要使用
            //如果是新用户 isNew设置=true
            map.put("isNew",true);
            log.debug("用户自动注册了手机号{},userId{}",phone,userId);
        }
        //6用户存在
        //7调用Jwtutils生成token
        String token = jwtUtils.createJWT(phone, user.getId());
        //8将token存入redis(明天使用)
        String userStr = JSON.toJSONString(user);
        redisTemplate.opsForValue().set("TOKEN_"+token,userStr,1,TimeUnit.DAYS);
        //9.返回登录结果token  isNew（true:新用户  false:老用户）
        map.put("token",token);
        log.debug("登录成功了手机号码{},token:{}",phone,token);
        return map;
    }

    public void sendValidateCode(String phone) {
//1.设置key  VALIDATECODE_131112222111
        String key = redisValidateCodeKeyPrefix+phone;
        //2.根据key从redis获取验证码
        String redisCode = redisTemplate.opsForValue().get(key);
        //3.验证码存在 验证码还未失效
        if(StringUtils.isNotEmpty(redisCode)){
            throw new TanHuaException(ErrorResult.duplicate());
        }
        //4.验证码不存在，生成验证码  一般情况 4或6
        String code = RandomStringUtils.randomNumeric(6);
        //为了方便调试 打印日志
        log.debug("发送验证码：手机号{}验证码{}",phone,code);
        //5.调用阿里云短信发送验证码
        Map<String, String> rsMap = smsTemplate.sendValidateCode(phone, code);
        //6.发送失败，告知重新获取验证码
        if(rsMap != null){
            throw new TanHuaException(ErrorResult.error());
        }
        //7.发送成功，将验证码保存redis key value 有效期5分钟
        redisTemplate.opsForValue().set(key,code,5, TimeUnit.MINUTES);
        log.debug("验证码发送成功了 ");
    }
    /**
     * 通过token获取登陆用户信息
     * @param token
     * @return
     */
    public User getUserByToken(String token){
        String key = "TOKEN_" + token;
        String userJsonStr = redisTemplate.opsForValue().get(key);
        if(StringUtils.isEmpty(userJsonStr)){
            return null;
        }
        // 延长有效期，续期
        redisTemplate.expire(key,1, TimeUnit.DAYS);
        User user = JSON.parseObject(userJsonStr, User.class);
        return user;
    }
    /**
     * 完善用户信息
     * @param userInfo
     * @param token
     */
    public void saveUserInfo(UserInfo userInfo, String token) {
        User user = getUserByToken(token);
        if(null == user){
            throw new TanHuaException("登陆超时，请重新登陆");
        }
        userInfo.setId(user.getId());
        userInfoApi.save(userInfo);
    }

    /**
     * 上传用户头像处理
     * @param headPhoto
     * @param token
     */
    public void updateUserAvatar(MultipartFile headPhoto, String token) {
        User user = getUserByToken(token);
        if(null == user){
            throw new TanHuaException("登陆超时，请重新登陆");
        }
        try {
            String filename = headPhoto.getOriginalFilename();
            byte[] bytes = headPhoto.getBytes();
            // 人脸检测
            if(!faceTemplate.detect(bytes)){
                throw new TanHuaException("没有检测到人脸，请重新上传");
            }
            // 上传头像到阿里云Oss
            String avatar = ossTemplate.upload(filename, headPhoto.getInputStream());
            UserInfo userInfo = new UserInfo();
            userInfo.setId(user.getId());
            userInfo.setAvatar(avatar);
            // 更新用户头像
            userInfoApi.update(userInfo);
        } catch (IOException e) {
            //e.printStackTrace();
            log.error("上传头像失败",e);
            throw new TanHuaException("上传头像失败，请稍后重试");
        }
    }
}
