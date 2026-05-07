package com.fons.cloud.mq.kafka.config;

import com.fons.cloud.canal.core.CanalGlue;
import com.fons.cloud.canal.core.processor.BaseCanalBinlogEventProcessor;
import com.fons.cloud.mq.kafka.canal.DefaultKafkaCanalListener;
import com.fons.cloud.mq.kafka.canal.KafkaCanalListener;
import com.fons.cloud.mq.kafka.lang.KafkaConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;

/**
 * 注册KAFKA-CANAL监听器
 * 注册Canal listener要根据环境注册一下对应的bean.
 * @author qiyuan.hong
 * @version 1.0
 * @date 2023/5/18 15:26
 */
@Configuration
@RequiredArgsConstructor
public class CanalListenerConfiguration {

    @Value("${spring.application.name}")
    private String application;
    private final DefaultKafkaConsumerFactory<String, String> kafkaConsumerFactory;
    private final CanalGlue canalGlue;

    @Bean
    @ConditionalOnBean(value = BaseCanalBinlogEventProcessor.class)
    public KafkaMessageListenerContainer<String, String> kafkaMessageListenerContainer() {
        // 消费主题
        String topic = KafkaConstants.DEFAULT_CANAL_KAFKA_TOPIC;
        // 消费者组
        // 创建canal监听器
        KafkaCanalListener kafkaCanalListener = new DefaultKafkaCanalListener(canalGlue);
        ContainerProperties properties = new ContainerProperties(topic);
        properties.setGroupId(application);
        properties.setMessageListener(kafkaCanalListener);
        properties.setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return new KafkaMessageListenerContainer<>(kafkaConsumerFactory, properties);
    }


}
