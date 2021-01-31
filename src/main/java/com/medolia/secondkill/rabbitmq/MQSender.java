package com.medolia.secondkill.rabbitmq;

import com.medolia.secondkill.redis.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MQSender {

    private AmqpTemplate amqpTemplate;

    @Autowired
    public void setAmqpTemplate(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    public void sendSeckillMessage(SeckillMsg seckillMsg) {
        String msg = RedisService.beanToString(seckillMsg);
        log.info("send message: " + msg);
        amqpTemplate.convertAndSend(MQConfig.SECKILL_QUEUE, msg);
    }

    public void sendTestMsg(String msg) {
        log.info("send message: " + msg);
        amqpTemplate.convertAndSend(MQConfig.TEST_QUEUE, msg);
    }
}
