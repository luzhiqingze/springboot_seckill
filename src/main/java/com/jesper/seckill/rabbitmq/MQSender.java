package com.jesper.seckill.rabbitmq;

import com.jesper.seckill.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.UUID;

@Service
public class MQSender implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnCallback {

    private static final Logger log = LoggerFactory.getLogger(MQSender.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnCallback(this);
        rabbitTemplate.setMandatory(true);
    }

    /**
     * 发送秒杀消息（核心方法）
     */
    public void sendSeckillMessage(SeckillMessage message) {
        if (message == null || message.getUser() == null || message.getGoodsId() <= 0) {
            log.warn("Invalid seckill message: {}", message);
            return;
        }

        String msg = RedisService.beanToString(message);
        String msgId = UUID.randomUUID().toString();
        CorrelationData correlationData = new CorrelationData(msgId);

        MessagePostProcessor processor = m -> {
            m.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            m.getMessageProperties().setMessageId(msgId);
            return m;
        };

        rabbitTemplate.convertAndSend(
                MQConfig.SECKILL_EXCHANGE,
                MQConfig.SECKILL_ROUTING_KEY,
                msg,
                processor,
                correlationData
        );

        log.info("Seckill MQ sent, msgId={}", msgId);
    }

    /**
     * 消息确认回调（到达 Broker）
     */
    @Override
    public void confirm(CorrelationData data, boolean ack, String cause) {
        if (ack) {
            log.info("MQ confirm success, msgId={}", data != null ? data.getId() : "null");
        } else {
            log.error("MQ confirm failed, msgId={}, cause={}",
                    data != null ? data.getId() : "null", cause);
        }
    }

    /**
     * 消息路由失败回调（Exchange → Queue 失败）
     */
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText,
                                String exchange, String routingKey) {
        log.error("MQ return message, body={}, replyCode={}, replyText={}, exchange={}, routingKey={}",
                new String(message.getBody()), replyCode, replyText, exchange, routingKey);
    }
}
