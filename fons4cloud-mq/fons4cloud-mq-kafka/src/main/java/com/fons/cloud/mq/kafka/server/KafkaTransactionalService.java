package com.fons.cloud.mq.kafka.server;

import com.fons.cloud.mq.api.transactional.service.AbstractMqTransactionalService;
import com.fons.cloud.mq.api.transactional.service.MqMessageOperations;
import com.fons.cloud.stream.api.StreamProducer;

/**
 * @author qiyuan.hong
 * @version 1.0
 * @date 2024/6/11
 */
public class KafkaTransactionalService extends AbstractMqTransactionalService {

    public KafkaTransactionalService(MqMessageOperations mqMessageOperations, StreamProducer<?> producer) {
        super(mqMessageOperations, producer);
    }

}
