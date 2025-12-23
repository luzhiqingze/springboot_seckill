package com.jesper.seckill.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jiangyunxiong on 2018/5/29.
 *
 * 配置bean
 */
@Configuration
public class MQConfig {

    // 秒杀队列配置
    public static final String SECKILL_QUEUE = "seckill.queue";
    public static final String SECKILL_DL_QUEUE = "seckill.dl.queue";
    public static final String SECKILL_EXCHANGE = "seckill.exchange";
    public static final String SECKILL_ROUTING_KEY = "seckill.message";
    
    // 示例用的其他队列（可保留）
    public static final String QUEUE = "queue";
    public static final String TOPIC_QUEUE1 = "topic.queue1";
    public static final String TOPIC_QUEUE2 = "topic.queue2";
    public static final String TOPIC_EXCHANGE = "topic.exchange";
    
    // 消息过期时间（毫秒）
    public static final int MESSAGE_TTL = 60000; // 60秒


    /**
     * Direct模式 交换机Exchange
     * 发送者先发送到交换机上，然后交换机作为路由再将信息发到队列，
     * */
    // 死信队列配置
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange("dead.letter.exchange");
    }

    // 秒杀队列（主队列）
    @Bean
    public Queue seckillQueue() {
        Map<String, Object> args = new HashMap<>();
        // 设置消息过期时间
        args.put("x-message-ttl", MESSAGE_TTL);
        // 设置死信交换机
        args.put("x-dead-letter-exchange", "dead.letter.exchange");
        // 设置死信路由键
        args.put("x-dead-letter-routing-key", SECKILL_DL_QUEUE);
        return new Queue(SECKILL_QUEUE, true, false, false, args);
    }
    
    // 死信队列（处理失败的消息）
    @Bean
    public Queue deadLetterQueue() {
        return new Queue(SECKILL_DL_QUEUE, true);
    }
    
    // 直连交换机（用于秒杀）
    @Bean
    public DirectExchange seckillExchange() {
        return new DirectExchange(SECKILL_EXCHANGE);
    }
    
    // 绑定主队列到交换机
    @Bean
    public Binding bindingSeckillQueue() {
        return BindingBuilder.bind(seckillQueue())
                .to(seckillExchange())
                .with(SECKILL_ROUTING_KEY);
    }
    
    // 绑定死信队列到死信交换机
    @Bean
    public Binding bindingDeadLetterQueue() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(SECKILL_DL_QUEUE);
    }

    /**
     * Topic模式 交换机Exchange
     * */
    // 示例用的Topic队列（可保留）
    @Bean
    public Queue topicQueue1() {
        return new Queue(TOPIC_QUEUE1, true);
    }
    
    @Bean
    public Queue topicQueue2() {
        return new Queue(TOPIC_QUEUE2, true);
    }
    
    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange(TOPIC_EXCHANGE);
    }
    
    @Bean
    public Binding topicBinding1() {
        return BindingBuilder.bind(topicQueue1())
                .to(topicExchange())
                .with("topic.key1");
    }
    
    @Bean
    public Binding topicBinding2() {
        return BindingBuilder.bind(topicQueue2())
                .to(topicExchange())
                .with("topic.#");
    }


}
