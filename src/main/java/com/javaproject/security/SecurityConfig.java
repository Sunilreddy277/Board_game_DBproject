package com.javaproject.security;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private LoggingAccessDeniedHandler accessDeniedHandler;

    @Autowired
    public void setAccessDeniedHandler(LoggingAccessDeniedHandler accessDeniedHandler) {
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    @Lazy
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private DataSource dataSource;

    /**
     * Creates a bean of type JdbcUserDetailsManager that will be used in
     * HomeController
     * 
     * @return an instance configured to use our datasource
     * @throws Exception
     */
    @Bean
    public JdbcUserDetailsManager jdbcUserDetailsManager() throws Exception {
        // provides crud operations for users
        JdbcUserDetailsManager jdbcUserDetailsManager = new JdbcUserDetailsManager();

        // Link up with our datasource
        jdbcUserDetailsManager.setDataSource(dataSource);
        return jdbcUserDetailsManager;
    }

    @Override
protected void configure(AuthenticationManagerBuilder auth) throws Exception {

    String bugsPassword = System.getenv("BUGS_USER_PASSWORD");
    String daffyPassword = System.getenv("DAFFY_USER_PASSWORD");

    if (bugsPassword == null || bugsPassword.trim().isEmpty()
            || daffyPassword == null || daffyPassword.trim().isEmpty()) {
        throw new IllegalStateException("User passwords are not configured");
    }

    auth.jdbcAuthentication()
            .dataSource(dataSource)
            .withDefaultSchema()
            .passwordEncoder(passwordEncoder)
            .withUser("bugs")
            .password(passwordEncoder.encode(bugsPassword)).roles("USER")
            .and()
            .withUser("daffy")
            .password(passwordEncoder.encode(daffyPassword)).roles("USER", "MANAGER");
}
}
