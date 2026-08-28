package com.hi.mallapi.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.hi.mallapi.security.filter.JWTCheckFilter;
import com.hi.mallapi.security.handler.APILoginFailHandler;
import com.hi.mallapi.security.handler.APILoginSuccessHandler;
import com.hi.mallapi.security.handler.CustomAccessDeniedHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Configuration
@Log4j2
@RequiredArgsConstructor
@EnableMethodSecurity
public class CustomSecurityConfig {
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		log.info(" scrurityConfig ");
		// 1.프론트엔드에서 오는 교차 출처 요청(CORS)을 이 설정에 따라 허용
		// 메소드 5개허용, Headers 3개방식허용
		http.cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer
				.configurationSource(corsConfigurationSource()));
		// 2.세션을 생성하지 않음(stateless). JWT 같은 토큰 기반 인증 시스템에서 사용된다.
		// 로그인 상태를 서버 세션으로 저장하지 않고, 매 요청마다 인증 정보를 전달해야 한다.
		http.sessionManagement(sessionConfig -> sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		// 3.CSRF(Cross-Site Request Forgery) 보호 기능을 비활성화
		// REST API 서버에서는 일반적으로 CSRF 보호가 필요 없기 때문에 비활성화.
		http.csrf(config -> config.disable());

		//9./api/member/login => CustomUserDetailsService.loadUserByUsername
		http.formLogin((config)->{
			config.loginPage("/api/member/login");
			//11. 로그인 성공 시 실행될 핸들러 객체를 지정 
			config.successHandler(new APILoginSuccessHandler()); 
			//12. 로그인 실패 시 실행될 핸들러 객체 지정
			config.failureHandler(new APILoginFailHandler());
		});
		
		//13. JWT 체크 추가 
		http.addFilterBefore(new JWTCheckFilter(), UsernamePasswordAuthenticationFilter.class); 
		
		//14. 권한이 허가 되지 않았을 때 예외처리 메시지 처리 
		http.exceptionHandling(config  ->  { 
		config.accessDeniedHandler(new CustomAccessDeniedHandler()); 
		}); 
		return http.build();

	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOriginPatterns(Arrays.asList("*"));
		configuration.setAllowedMethods(Arrays.asList("HEAD", "GET", "POST", "PUT", "DELETE"));
		configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache- Control", "Content-Type"));
		// 자격 증명(쿠키, 인증 헤더 등)을 CORS 요청과 함께 보낼 수 있도록 허용
		configuration.setAllowCredentials(true);
		// URL 패턴에 따라 CORS 설정을 매핑할 수 있는 객체
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
	//4.PasswordEncoder 설정 
	@Bean 
	public PasswordEncoder passwordEncoder() { 
	   return new BCryptPasswordEncoder(); 
	}
}
