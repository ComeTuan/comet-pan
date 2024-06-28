package top.nomelin.cometpan.controller;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import top.nomelin.cometpan.cache.CurrentUserCache;
import top.nomelin.cometpan.common.Result;
import top.nomelin.cometpan.common.enums.CodeMessage;
import top.nomelin.cometpan.common.exception.BusinessException;
import top.nomelin.cometpan.pojo.Account;
import top.nomelin.cometpan.pojo.User;
import top.nomelin.cometpan.service.UserService;

import top.nomelin.cometpan.util.StaticPeramUtil;

import java.util.List;


@RestController
public class WebController {
    private final CurrentUserCache currentUserCache;

    private final UserService userService;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public WebController(CurrentUserCache currentUserCache, UserService userService) {
        this.currentUserCache = currentUserCache;
        this.userService = userService;
    }

    @GetMapping("/")
    public Result hello() {
        return Result.success("访问成功");
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result login(@RequestBody Account account) {
        //int role = account.getRole();
        if (ObjectUtil.isEmpty(account.getUserName()) || ObjectUtil.isEmpty(account.getPassword())) {
            //        || ObjectUtil.isNull(role)) {
            return Result.error(CodeMessage.PARAM_LOST_ERROR);
        }
        account = userService.login(account);
        logger.info("用户登录成功：{}", account);
        return Result.success(account);
    }

    @DeleteMapping("/logout")
    public Result logout() {
        currentUserCache.clear();
        return Result.success();
    }

    /**
     * 注册
     */
//    @PostMapping("/register")
//    public Result register(@RequestBody Account account) {
//        if (StrUtil.isBlank(account.getUserName()) || StrUtil.isBlank(account.getPassword())) {
//            return Result.error(CodeMessage.PARAM_LOST_ERROR);
//        }
//        User user;
//        user = userService.register(account);
//        logger.info("注册成功：{}", account);
//        return Result.success(user);
//    }
    // VerifyCodeController.java
    @PostMapping("/register")
    public Result register(@RequestBody Account account) {
        if (StrUtil.isBlank(account.getUserName()) || StrUtil.isBlank(account.getPassword()) || StrUtil.isBlank(account.getPhone())) {
            return Result.error(CodeMessage.PARAM_LOST_ERROR);
        }
        User user;
        try {
            user = userService.register(account);
            logger.info("注册成功：{}", account);
            return Result.success(user);
        } catch (BusinessException e) {
            // 这里处理验证码错误的情况
            e.printStackTrace();
            return Result.error(CodeMessage.VERIFICATION_CODE_ERROR);
        }
    }



    /**
     * 修改密码
     */
    @PutMapping("/updatePassword")
    public Result updatePassword(@RequestBody JSONObject requestBody) {
        String userName = requestBody.getStr("userName");
        String password = requestBody.getStr("oldPassword");
        String newPassword = requestBody.getStr("newPassword");
        if (StrUtil.isBlank(userName) || StrUtil.isBlank(password)
                || ObjectUtil.isNull(newPassword) || StrUtil.isBlank(newPassword)) {
            return Result.error(CodeMessage.PARAM_LOST_ERROR);// 参数缺失
        }
        logger.info("更改密码请求：userName:{},password:{},newPassword:{}", userName, password, newPassword);
        if (!currentUserCache.getCurrentUser().getUserName().equals(userName)) {
            return Result.error(CodeMessage.INVALID_USER_NAME_ERROR);// 非当前用户修改密码
        }
        if (currentUserCache.getCurrentUser().getPassword().equals(newPassword)) {
            return Result.error(CodeMessage.EQUAL_PASSWORD_ERROR);// 新密码不能与旧密码相同
        }
        Account account = new Account();
        account.setUserName(userName);
        account.setPassword(password);
        userService.updatePassword(account, newPassword);// 修改密码
        return Result.success();
    }

    @GetMapping("/valid/{username}")
    public Result validUserName(@PathVariable String username) {
        User user = new User();
        user.setUserName(username);
        List<User> users = userService.selectAll(user);//模糊查找，所以要遍历再验证
        boolean res = true;
        for (User u : users) {
            if (u.getUserName().equals(username)) {
                res = false;
                break;
            }
        }
        return Result.success(res);
    }

    @Autowired
    private StaticPeramUtil staticPeramUtil;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;;

    // 这个方法将从前端接收到电话号码，然后生成验证码并发送短信
    @PostMapping("/sendVerifyCode")
    public Result sendVerifyCode(@RequestBody String phoneNumber) {
        // 使用用户输入的电话号码生成验证码
        // 解析JSON字符串
        JSONObject jsonObject = new JSONObject(phoneNumber);
        // 从JSON对象中提取电话号码
        String phone = jsonObject.getStr("phone");
        String code = StaticPeramUtil.generateCode();
        // 调用sendShortMessage方法发送验证码到手机
        try {
            StaticPeramUtil.getPhonemsg(phone,code);
            // 将验证码和手机号存储到Redis中，这里假设你已经配置好了Redis并设置了对应的key和value
            // 这里只是为了示例，实际使用时需要根据你的Redis配置进行修改
            redisTemplate.opsForValue().set("verificationCode:" + phone, code);
            return Result.success(phone);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(CodeMessage.SEND_ERROR);
        }
    }

//    @PostMapping("/sendVerifyCode1")
//    public Result sendVerifyCode1(@RequestBody String phoneNumber) {
//        // 模拟耗时操作，如数据库查询，验证码生成等
//        Thread.sleep(2000); // 延迟两秒
//        // 模拟返回结果
//        return Result.error(CodeMessage.SEND_ERROR);
//    }
}

