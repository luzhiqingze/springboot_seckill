package com.jesper.seckill.rabbitmq;

import com.jesper.seckill.bean.SeckillOrder;
import com.jesper.seckill.bean.User;
import com.jesper.seckill.redis.RedisService;
import com.jesper.seckill.service.GoodsService;
import com.jesper.seckill.service.OrderService;
import com.jesper.seckill.service.SeckillService;
import com.jesper.seckill.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by jiangyunxiong on 2018/5/29.
 */
@Service
public class MQReceiver {

    private static Logger log = LoggerFactory.getLogger(MQReceiver.class);


    @Autowired
    RedisService redisService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    SeckillService seckillService;

    @RabbitListener(queues=MQConfig.QUEUE)
    public void receive(String message){
        log.info("receive message:{}", message);
        SeckillMessage m = RedisService.stringToBean(message, SeckillMessage.class);
        if (m == null) {
            log.warn("Invalid message format: {}", message);
            return;
        }
        
        User user = m.getUser();
        long goodsId = m.getGoodsId();
        
        if (user == null || goodsId <= 0) {
            log.warn("Invalid message content: user={}, goodsId={}", user, goodsId);
            return;
        }

        try {
            // 1. 再次检查商品库存
            GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);
            if (goodsVo == null || goodsVo.getStockCount() <= 0) {
                log.info("Goods sold out, goodsId: {}", goodsId);
                return;
            }

            // 2. 幂等检查：是否已下单
            SeckillOrder order = orderService.getOrderByUserIdGoodsId(user.getId(), goodsId);
            if (order != null) {
                log.info("Duplicate order detected, userId: {}, goodsId: {}", user.getId(), goodsId);
                return;
            }

            // 3. 执行秒杀（事务内包含：减库存+创建订单）
            seckillService.seckill(user, goodsVo);
            log.info("Order created successfully, userId: {}, goodsId: {}", user.getId(), goodsId);
            
        } catch (Exception e) {
            // 4. 处理唯一键冲突（可能由于并发导致）
            if (e.getCause() instanceof java.sql.SQLIntegrityConstraintViolationException ||
                e.getCause() instanceof org.springframework.dao.DuplicateKeyException) {
                log.info("Duplicate order detected (constraint violation), userId: {}, goodsId: {}", 
                        user != null ? user.getId() : null, goodsId);
                return;
            }
            // 其他异常记录错误日志（可以添加重试或告警逻辑）
            log.error("Seckill failed, userId: {}, goodsId: {}, error: {}", 
                    user != null ? user.getId() : null, goodsId, e.getMessage(), e);
            // 注意：这里可以抛出 AmqpRejectAndDontRequeueException 让消息进入死信队列
            // throw new AmqpRejectAndDontRequeueException(e);
        }
    }

    @RabbitListener(queues = MQConfig.TOPIC_QUEUE1)
    public void receiveTopic1(String message) {
        log.info(" topic  queue1 message:" + message);
    }

    @RabbitListener(queues = MQConfig.TOPIC_QUEUE2)
    public void receiveTopic2(String message) {
        log.info(" topic  queue2 message:" + message);
    }
}
