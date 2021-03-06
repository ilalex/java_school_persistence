package com.digdes.school.app;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;

/**
 * Annotation configuration of app.
 *
 * @author Ilya Ashikhmin (ashikhmin.ilya@gmail.com)
 */
@Configuration
@EnableTransactionManagement
@PropertySource({"classpath:db.properties"})
@ComponentScan("com.digdes.school.app")
public class AppConfig {
    private final Environment env;

    @Autowired
    public AppConfig(Environment env) {
        this.env = env;
    }

    @Bean
    public DataSource restDataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(env.getProperty("jdbc.driverClassName"));
        dataSource.setUrl(env.getProperty("jdbc.url"));
        dataSource.setUsername(env.getProperty("jdbc.user"));
        dataSource.setPassword(env.getProperty("jdbc.pass"));
        return dataSource;
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory() {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(restDataSource());
        sessionFactory.setPackagesToScan("com.digdes.school.app.model");
        sessionFactory.setHibernateProperties(hibernateProperties());
        return sessionFactory;
    }

    @Bean
    @Autowired
    public HibernateTransactionManager transactionManager(SessionFactory sessionFactory) {
        HibernateTransactionManager txManager = new HibernateTransactionManager();
        txManager.setSessionFactory(sessionFactory);
        return txManager;
    }

    /**
     * creating map of hibernate properties only
     *
     * @return Hibernate properties
     */
    private Properties hibernateProperties() {
        Properties props = new Properties();
        MutablePropertySources propSources = ((AbstractEnvironment) env).getPropertySources();
        Map<String, String> hibernateMap = stream(spliteratorUnknownSize(propSources.iterator(), ORDERED), false)
                .filter(ps -> ps instanceof MapPropertySource)
                .flatMap(ps -> ((MapPropertySource) ps).getSource().entrySet().stream())
                .filter(entry -> entry.getKey().contains("hibernate"))
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().toString()));
        props.putAll(hibernateMap);
        return props;
    }
}