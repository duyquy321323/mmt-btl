package com.mmt.btl.config;

import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import com.mmt.btl.filters.JwtTokenFilter;
import com.mmt.btl.security.CustomHandlerLogout;
import com.mmt.btl.security.UserSecurityService;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Bean
    public LogoutHandler logoutHandler() {
        return new CustomHandlerLogout();
    }

    @Bean
    @Override
    public UserDetailsService userDetailsService() {
        return new UserSecurityService();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @SuppressWarnings("deprecation")
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public JwtTokenFilter jwtTokenFilter() {
        return new JwtTokenFilter();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.headers((header) -> header.frameOptions().disable())
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeRequests(request -> request
                        .antMatchers(HttpMethod.POST, "/user/login", "/user/register", "/files/upload", "/ws/**")
                        .permitAll()
                        .antMatchers(HttpMethod.GET, "/tracker/all", "/files/file-uploaded", "/ws/**")
                        .permitAll()
                        .antMatchers(HttpMethod.POST, "/user/logout")
                        .authenticated()
                        .anyRequest().denyAll())
                .addFilterBefore(jwtTokenFilter(), LogoutFilter.class).logout(logout -> logout.logoutUrl("/user/logout")
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler(
                                (request, response, auth) -> response.setStatus(HttpServletResponse.SC_OK))
                        .invalidateHttpSession(true).addLogoutHandler(logoutHandler()));
    }
}