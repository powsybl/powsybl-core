package com.powsybl.afs.storage;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import com.powsybl.afs.storage.events.NodeEvent;

public class KafKaEventStore implements EventStore {

    private KafkaTemplate<Object, Object> template;

    @Override
    public void pushEvent(NodeEvent event, String fileSystem) {
        if (this.template == null) {
            this.template = BeanUtil.getBean(KafkaTemplate.class);
        }
        if (this.template != null) {
            Message<NodeEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, "nodeEvent")
                .setHeader(KafkaHeaders.MESSAGE_KEY, "999")
                .setHeader(KafkaHeaders.PARTITION_ID, 0)
                .setHeader("X-Custom-Header", "Sending Custom Header with Imagrid")
                .setHeader("X-Event", NodeEvent.class.getName())
                .setHeader("X-Path", "fileSystems")
                .setHeader("X-FileSystemName", fileSystem)
                .build();
            this.template.send(message);
            this.template.flush();
        }
    }

    @Override
    public void addTopic() {
        // To DO
    }
}

@Service
class BeanUtil implements ApplicationContextAware {
    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    static <T> T getBean(Class<T> beanClass) {
        if (context == null) {
            return null;
        }
        return context.getBean(beanClass);
    }
}
