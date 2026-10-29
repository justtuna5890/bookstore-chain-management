package com.bookstorechain.config;

import com.bookstorechain.security.CustomAuthenticationSuccessHandler;
import com.bookstorechain.security.CustomUserDetailsService;
import com.bookstorechain.security.JwtAuthenticationFilter;
import com.bookstorechain.security.RestAuthenticationEntryPoint;

import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
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
	private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;

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

	// =========================================================================
	// CHAIN 1: REST API (/api/**) - STATELESS, JWT AUTH, TRA VE JSON 401
	// =========================================================================
	@Bean
	@Order(1)
	public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
		http.securityMatcher("/api/**")
				.csrf(csrf -> csrf.disable()) // REST API stateless khong dung session nen khong can CSRF
				.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/auth/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/books/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/books/**").hasAnyRole("ADMIN", "MANAGER")
						.anyRequest().authenticated()
				)
				.exceptionHandling(ex -> ex.authenticationEntryPoint(restAuthenticationEntryPoint))
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	// =========================================================================
	// CHAIN 2: WEB MVC & JSP - SESSION-BASED, FORM LOGIN, CSRF BAT
	// =========================================================================
	@Bean
	@Order(2)
	public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(auth -> auth
						// Cho phép các request FORWARD/INCLUDE/ERROR của Spring MVC (INCLUDE cần cho jsp:include khi chưa đăng nhập)
						.dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.INCLUDE, DispatcherType.ERROR).permitAll()

						// Static resources
						.requestMatchers("/css/**", "/js/**", "/images/**", "/static/**", "/bootstrap/**",
								"/webjars/**")
						.permitAll()

						// Các trang public
						.requestMatchers("/", "/home", "/login", "/register", "/error", "/buyback-request", "/books/**", "/ws/**").permitAll()

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

		return http.build();
	}
}
