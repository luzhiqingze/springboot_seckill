package com.jesper.seckill.controller;

import com.jesper.seckill.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Result<Map<String, String>> home() {
        Map<String, String> links = new LinkedHashMap<>();
        links.put("goods_list", "/api/goods");
        links.put("goods_detail", "/api/goods/{id}");
        links.put("login", "/api/auth/login");
        links.put("me", "/api/users/me");
        links.put("seckill", "/api/seckill/do_seckill?goodsId={id}");
        links.put("seckill_result", "/api/seckill/result?goodsId={id}");
        return Result.success(links);
    }
}
