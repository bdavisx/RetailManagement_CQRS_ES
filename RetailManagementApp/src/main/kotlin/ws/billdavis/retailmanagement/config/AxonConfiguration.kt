package ws.billdavis.retailmanagement.config

import com.mongodb.Mongo
import org.axonframework.commandhandling.CommandBus
import org.axonframework.commandhandling.SimpleCommandBus
import org.axonframework.commandhandling.annotation.AnnotationCommandHandlerBeanPostProcessor
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.commandhandling.gateway.DefaultCommandGateway
import org.axonframework.eventhandling.EventBus
import org.axonframework.eventhandling.SimpleEventBus
import org.axonframework.eventhandling.annotation.AnnotationEventListenerBeanPostProcessor
import org.axonframework.eventstore.EventStore
import org.axonframework.eventstore.mongo.DefaultMongoTemplate
import org.axonframework.eventstore.mongo.MongoEventStore
import org.axonframework.eventstore.mongo.MongoTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import javax.sql.DataSource
import java.net.UnknownHostException

public open class AxonConfiguration() {
    private var dataSource: DataSource = null
    public open fun eventBus(): EventBus {
        return SimpleEventBus()
    }
    public open fun commandBus(): CommandBus {
        return SimpleCommandBus()
    }
    public open fun commandGateway(): CommandGateway {
        return DefaultCommandGateway(commandBus())
    }
    public open fun mongo(): Mongo {
        // TODO: parameterize
        try
        {
            val mongo = Mongo("localhost")
            return mongo
        }
        catch (ex: UnknownHostException) {
            // TODO: change default exception handling
            throw RuntimeException(ex)
        }

    }
    public open fun mongoTemplate(): MongoTemplate {
        val template = DefaultMongoTemplate(mongo())
        return template
    }
    public open fun eventStore(): EventStore {
        val eventStore = MongoEventStore(mongoTemplate())
        return eventStore
    }
    public open fun annotationEventListenerBeanPostProcessor(): AnnotationEventListenerBeanPostProcessor {
        val postProcessor = AnnotationEventListenerBeanPostProcessor()
        postProcessor.setEventBus(eventBus())
        return postProcessor
    }
    public open fun annotationCommandHandlerBeanPostProcessor(): AnnotationCommandHandlerBeanPostProcessor {
        val postProcessor = AnnotationCommandHandlerBeanPostProcessor()
        postProcessor.setCommandBus(commandBus())
        return postProcessor
    }


}
