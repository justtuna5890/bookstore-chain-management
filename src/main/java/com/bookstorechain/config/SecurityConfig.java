package com.bookstorechain.config;

import com.bookstorechain.security.CustomAuthenticationSuccessHandler;
import com.bookstorechain.security.CustomUserDetailsService;
import com.bookstorechain.security.JwtAuthenticationFilter;

import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final CustomUserDetailsService userDetailsService;
	private final CustomAuthenticationSuccessHandler successHandler;
	private final PasswordEncoder passwordEncoder;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(userDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder);
		return authProvider;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))

				.authorizeHttpRequests(auth -> auth

						// Cho phép các request FORWARD/ERROR của Spring MVC
						.dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()

						// Static resources
						.requestMatchers("/css/**", "/js/**", "/images/**", "/static/**", "/bootstrap/**",
								"/webjars/**")
						.permitAll()

						// Các trang public
						.requestMatchers("/", "/home", "/login", "/register", "/error", "/buyback-request", "/books/**", "/api/auth/**", "/ws/**").permitAll()

						// Admin
						.requestMatchers("/admin/**").hasAnyRole("ADMIN", "MANAGER", "STAFF")

						// Các URL còn lại phải đăng nhập
						.anyRequest().authenticated())

				.formLogin(form -> form.loginPage("/login").loginProcessingUrl("/perform-login")
						.successHandler(successHandler).failureUrl("/login?error=true").permitAll())

				.logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/login?logout=true")
						.invalidateHttpSession(true).deleteCookies("JSESSIONID").permitAll())

				.exceptionHandling(ex -> ex.accessDeniedPage("/?denied=true"));

		http.authenticationProvider(authenticationProvider());

		// Kích hoạt JWT filter cho REST API (/api/**)
		http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}