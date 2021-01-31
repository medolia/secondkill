package com.medolia.secondkill.rabbitmq;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class MQConfig {

    public static final String SECKILL_QUEUE = "seckill_queue";
    public static final String TEST_QUEUE = "test queue";


    /**
     * Direct 模式 交换机 Exchange
     *
     */
    @Bean
    public Queue seckillQueue() {
        return new Queue(SECKILL_QUEUE, true);
    }

    @Bean
    public Queue testQueue() {
        return new Queue(TEST_QUEUE, true);
    }
}
