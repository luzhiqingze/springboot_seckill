package com.jesper.seckill.controller;

import com.jesper.seckill.bean.User;
import com.jesper.seckill.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by jiangyunxiong on 2018/5/23.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/me")
    public Result<User> info(User user) {
        return Result.success(user);
    }
}