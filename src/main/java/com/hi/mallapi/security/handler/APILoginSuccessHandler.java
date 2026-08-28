package com.hi.mallapi.security.handler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.google.gson.Gson;
import com.hi.mallapi.dto.MemberDTO;
import com.hi.mallapi.util.JWTUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class APILoginSuccessHandler implements AuthenticationSuccessHandler {

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		//시큐리티 정책에 바른 로그인 등록완료(username, password, authentication)
		MemberDTO memberDTO = (MemberDTO)authentication.getPrincipal();  
		
		//memberDTO 사용자정보를 키 : value값 저장되어있음. 
		Map<String, Object> claims = memberDTO.getClaims();
		//accessToken, refreshToken 생성
		String accessToken = JWTUtil.generateToken(claims, 10);
		String refreshToken = JWTUtil.generateToken(claims, 60 * 24);
		//추가로 accessToken, refreshToken 첨부해서 보낸다. 
		claims.put("accessToken", accessToken); // 나중에 구현 
		claims.put("refreshToken", refreshToken); // 나중에 구현 
		 
		Gson gson = new Gson(); 
		String jsonStr = gson.toJson(claims);  
		 
		response.setContentType("application/json; charset=UTF-8"); 
		PrintWriter printWriter = response.getWriter(); 
		printWriter.println(jsonStr); 
		//리액트 전송함.
		printWriter.close();

	}

}
