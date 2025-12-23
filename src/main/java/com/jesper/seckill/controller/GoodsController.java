package com.jesper.seckill.controller;

import com.jesper.seckill.bean.User;
import com.jesper.seckill.result.Result;
import com.jesper.seckill.service.GoodsService;
import com.jesper.seckill.vo.GoodsDetailVo;
import com.jesper.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * Created by jiangyunxiong on 2018/5/22.
 */
@RestController
@RequestMapping("/api/goods")
public class GoodsController {

    @Autowired
    GoodsService goodsService;

    @GetMapping
    public Result<List<GoodsVo>> list(User user) {
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        return Result.success(goodsList);
    }

    /**
     * 商品详情页面
     */
    @GetMapping("/{goodsId}")
    public Result<GoodsDetailVo> detail(User user, @PathVariable("goodsId") long goodsId) {
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);

        long startTime = goods.getStartDate().getTime();
        long endTime = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();

        int seckillStatus = 0;
        int remainSeconds = 0;

        if (now < startTime) {//秒杀还没开始，倒计时
            seckillStatus = 0;
            remainSeconds = (int) ((startTime - now) / 1000);
        } else if (now > endTime) {//秒杀已经结束
            seckillStatus = 2;
            remainSeconds = -1;
        } else {//秒杀进行中
            seckillStatus = 1;
            remainSeconds = 0;
        }
        GoodsDetailVo vo = new GoodsDetailVo();
        vo.setGoods(goods);
        vo.setUser(user);
        vo.setRemainSeconds(remainSeconds);
        vo.setSeckillStatus(seckillStatus);

        return Result.success(vo);
    }
}
