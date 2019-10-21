package io.simplesource.example.demo;

import io.simplesource.api.CommandAPISet;
import io.simplesource.example.demo.domain.Account;
import io.simplesource.example.demo.projections.ElasticsearchProjectionService;
import io.simplesource.example.demo.repository.read.AccountReadElasticSearchRepository;
import io.simplesource.example.demo.repository.read.AccountReadRepository;
import io.simplesource.example.demo.repository.write.AccountWriteRepository;
import io.simplesource.example.demo.repository.write.CreateAccountError;
import io.simplesource.example.demo.repository.write.simplesource.*;
import io.simplesource.example.demo.repository.write.simplesource.wire.AccountId;
import io.simplesource.example.demo.service.AccountService;
import io.simplesource.kafka.api.AggregateSerdes;
import io.simplesource.kafka.api.CommandSerdes;
import io.simplesource.kafka.dsl.EventSourcedApp;
import io.simplesource.kafka.dsl.EventSourcedClient;
import io.simplesource.kafka.serialization.json.JsonAggregateSerdes;
import io.simplesource.kafka.serialization.json.JsonCommandSerdes;
import io.simplesource.kafka.util.PrefixResourceNamingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;


import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.simplesource.kafka.serialization.json.JsonGenericMapper.jsonDomainMapper;
import static io.simplesource.kafka.serialization.json.JsonOptionalGenericMapper.jsonOptionalDomainMapper;

@SpringBootApplication
public class App implements WebMvcConfigurer {

    @Autowired
    private ApplicationContext applicationContext;

    private AtomicBoolean simpleSourceServiceStarted = new AtomicBoolean(false);
    private AtomicBoolean simpleSourceComandApiSetStarted = new AtomicBoolean(false);
    private AtomicBoolean elasticsearchHealthy = new AtomicBoolean(false);

    private static final AggregateSerdes<String, AccountCommand, AccountEvent, Optional<io.simplesource.example.demo.repository.write.simplesource.Account>> ACCOUNT_AGGREGATE_SERDES  =
            new JsonAggregateSerdes<>(
                    jsonDomainMapper(),
                    jsonDomainMapper(),
                    jsonDomainMapper(),
                    jsonOptionalDomainMapper());

    public static final CommandSerdes<AccountId, AccountCommand> ACCOUNT_COMMAND_SERDES =
            new JsonCommandSerdes<>(jsonDomainMapper(), jsonDomainMapper());

    public static void main(String[] args) {
        // Start Spring boot
        SpringApplication springBoot = new SpringApplication(App.class);
        springBoot.setDefaultProperties(Collections.singletonMap("server.port","8083"));
        springBoot.run(args);
    }

    @PostConstruct
    private void starSimpleSourcingService() {
        new ElasticsearchProjectionService(config()).start();

        new EventSourcedApp()
                .withKafkaConfig(builder ->
                        builder
                                .withKafkaApplicationId("simplesourcing-demo")
                                .withKafkaBootstrap("kafka:9092")
                                .build()
                )
                .<String, AccountCommand, AccountEvent, Optional<io.simplesource.example.demo.repository.write.simplesource.Account>>addAggregate(aggregateBuilder ->
                        aggregateBuilder
                                .withName("account")
                                .withSerdes(ACCOUNT_AGGREGATE_SERDES)
                                .withInitialValue(k -> Optional.empty())
                                .withAggregator(AccountAggregator.getInstance())
                                .withCommandHandler(AccountCommandHandler.getInstance())
                                .withResourceNamingStrategy(new PrefixResourceNamingStrategy())
                                .build()
                )
                .start();
        simpleSourceServiceStarted.set(true);
    }

    // Setup the Templating engine
    @Bean
    public SpringResourceTemplateResolver templateResolver() {
        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setApplicationContext(applicationContext);
        templateResolver.setPrefix("/WEB-INF/views/");
        templateResolver.setSuffix(".html");
        return templateResolver;
    }


    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver());
        templateEngine.setEnableSpringELCompiler(true);
        return templateEngine;
    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(templateEngine());
        registry.viewResolver(resolver);
    }
    // End of templating stuff



    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public Config config() {
        return new Config("demo", "kafka:9092", "elasticsearch", 9200);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public HealthcheckService healthcheckService() {
        // Prevents the site being accessed until everything is up and ready
        return new HealthcheckService(() -> simpleSourceServiceStarted.get(), () -> simpleSourceComandApiSetStarted.get());
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public CommandAPISet commandAPISet(Config config){
        CommandAPISet result = new EventSourcedClient()
                .withKafkaConfig(builder ->
                        builder
                                .withKafkaApplicationId(config.kafkaGropId)
                                .withKafkaBootstrap(config.kafkaBootstrapServers)
                                .build()
                )
                .<AccountId, AccountCommand>addCommands(builder ->
                        builder
                                .withClientId(config.kafkaGropId)
                                .withCommandResponseRetention(3600L)
                                .withName("account")
                                .withSerdes(ACCOUNT_COMMAND_SERDES)
                                .withResourceNamingStrategy(new PrefixResourceNamingStrategy())
                                .withTopicSpec(1, 1)
                ).build();

        simpleSourceComandApiSetStarted.set(true);
        return result;
    }



    // TODO create a real implementation elsewhere backed by simplestreams for writes and es for reads
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public AccountService accountService(CommandAPISet commandAPISet) {
        AccountReadRepository accountReadRepository = new AccountReadElasticSearchRepository();
        AccountWriteRepository accountWriteRepository = new SimplesourceAccountRepository(commandAPISet.getCommandAPI("account"));

        return new AccountService() {
            @Override
            public boolean accountExists(String accountName) {
                return accountReadRepository.findByName(accountName).isPresent();
            }

            @Override
            public Optional<CreateAccountError> createAccount(String name, double openingBalance) {
                return accountWriteRepository.create(name, openingBalance);
            }
        };
    }
}
