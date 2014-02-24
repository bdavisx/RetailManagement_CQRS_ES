package ws.billdavis.retailmanagement.config;

import com.mongodb.Mongo;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.commandhandling.annotation.AnnotationCommandHandlerBeanPostProcessor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.gateway.DefaultCommandGateway;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.SimpleEventBus;
import org.axonframework.eventhandling.annotation.AnnotationEventListenerBeanPostProcessor;
import org.axonframework.eventstore.EventStore;
import org.axonframework.eventstore.mongo.DefaultMongoTemplate;
import org.axonframework.eventstore.mongo.MongoEventStore;
import org.axonframework.eventstore.mongo.MongoTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.UnknownHostException;

@Configuration
@Profile("default")
public class AxonConfiguration {
    @Autowired private DataSource dataSource;

    @Bean
    public EventBus eventBus() {
        return new SimpleEventBus();
    }

    @Bean
    public CommandBus commandBus() {
        return new SimpleCommandBus();
    }

    @Bean
    public CommandGateway commandGateway() {
        return new DefaultCommandGateway( commandBus() );
    }

    @Bean
    public Mongo mongo() {
        // TODO: parameterize
        try {
            Mongo mongo = new Mongo( "localhost" );
            return mongo;
        } catch( UnknownHostException ex ) {
            // TODO: change default exception handling
            throw new RuntimeException( ex );
        }
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        MongoTemplate template = new DefaultMongoTemplate( mongo() );
        return template;
    }

    @Bean
    public EventStore eventStore() {
        MongoEventStore eventStore = new MongoEventStore( mongoTemplate() );
        return eventStore;
    }

    @Bean
    public AnnotationEventListenerBeanPostProcessor annotationEventListenerBeanPostProcessor() {
        final AnnotationEventListenerBeanPostProcessor postProcessor =
            new AnnotationEventListenerBeanPostProcessor();
        postProcessor.setEventBus( eventBus() );
        return postProcessor;
    }

    @Bean
    public AnnotationCommandHandlerBeanPostProcessor annotationCommandHandlerBeanPostProcessor() {
        AnnotationCommandHandlerBeanPostProcessor postProcessor =
            new AnnotationCommandHandlerBeanPostProcessor();
        postProcessor.setCommandBus( commandBus() );
        return postProcessor;
    }
}












