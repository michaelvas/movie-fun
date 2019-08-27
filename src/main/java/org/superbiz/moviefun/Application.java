package org.superbiz.moviefun;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ServletRegistrationBean actionServletRegistration(ActionServlet actionServlet) {
        return new ServletRegistrationBean<>(actionServlet, "/moviefun/*");
    }

    @Bean
    public DatabaseServiceCredentials databaseServiceCredentials(Environment env) {
        return new DatabaseServiceCredentials(env.getProperty("VCAP_SERVICES"));
    }

    @Bean("albumsDataSource")
    public DataSource albumsDataSource(DatabaseServiceCredentials serviceCredentials) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(serviceCredentials.jdbcUrl("albums-mysql"));
        return dataSource;
    }

    @Bean("moviesDataSource")
    public DataSource moviesDataSource(DatabaseServiceCredentials serviceCredentials) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(serviceCredentials.jdbcUrl("movies-mysql"));
        return dataSource;
    }

    @Bean
    public HibernateJpaVendorAdapter hibernateJpaVendorAdapter() {
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setDatabase(Database.MYSQL);
        adapter.setDatabasePlatform("org.hibernate.dialect.MySQL5Dialect");
        adapter.setGenerateDdl(true);
        return adapter;
    }

    @Bean("moviesEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean moviesEntityManagerFactoryBean(@Qualifier("moviesDataSource") DataSource ds, HibernateJpaVendorAdapter adapter) {
        LocalContainerEntityManagerFactoryBean emFactory = new LocalContainerEntityManagerFactoryBean();
        emFactory.setDataSource(ds);
        emFactory.setJpaVendorAdapter(adapter);
        emFactory.setPackagesToScan("org.superbiz.moviefun.movies");
        emFactory.setPersistenceUnitName("movies");
        return emFactory;
    }

    @Bean("albumsEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean albumsEntityManagerFactoryBean(@Qualifier("albumsDataSource") DataSource ds, HibernateJpaVendorAdapter adapter) {
        LocalContainerEntityManagerFactoryBean emFactory = new LocalContainerEntityManagerFactoryBean();
        emFactory.setDataSource(ds);
        emFactory.setJpaVendorAdapter(adapter);
        emFactory.setPackagesToScan("org.superbiz.moviefun.albums");
        emFactory.setPersistenceUnitName("albums");
        return emFactory;
    }

    @Bean("moviesTransactionManager")
    public PlatformTransactionManager moviesTransactionManager(@Qualifier("moviesEntityManagerFactory") EntityManagerFactory emFactory) {
        return new JpaTransactionManager(emFactory);
    }

    @Bean("albumsTransactionManager")
    public PlatformTransactionManager albumsTransactionManager(@Qualifier("albumsEntityManagerFactory") EntityManagerFactory emFactory) {
        return new JpaTransactionManager(emFactory);
    }
}
