package com.hi.mallapi.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hi.mallapi.util.CustomJWTException;
import com.hi.mallapi.util.JWTUtil;

import lombok.extern.log4j.Log4j2;

@RestController
@Log4j2
public class APIRefreshController {
	
	//리액트: 토큰이 만료가 되었다고 통보 => /api/member/refresth (헤더: accessToken, body: refreshToken)
	//문제없는 토큰으로 리턴받기를 원한다. (accessToken , refreshToken)
	@RequestMapping("/api/member/refresh")
	public Map<String, Object> refresh(@RequestHeader("Authorization") String authHeader, String refreshToken) {
		
		if (refreshToken == null) {
			throw new CustomJWTException("NULL_REFRASH");
		}
		
		if (authHeader == null || authHeader.length() < 7) {
			throw new CustomJWTException("INVALID_STRING");
		}

		String accessToken = authHeader.substring(7);
		
		// 1)Access 토큰이 만료되지 않았다면 => 기존 accessToken, 기존 refreshToken 재전송
		if (checkExpiredToken(accessToken) == false) {
			return Map.of("accessToken", accessToken, "refreshToken", refreshToken);
		}
		
		// 2)Access 토큰은 무조건 만료가 됨. , refreshToken 체크한다. 
		// Refresh토큰 검증
		Map<String, Object> claims = JWTUtil.validateToken(refreshToken);
		log.info("refresh ... claims: " + claims);
		
		// 무조건10분 access 토큰생성
		String newAccessToken = JWTUtil.generateToken(claims, 10);
		
		// refresh 토큰이 1시간이 안남았으면 1일 refresh token 생성 (새로 accessTokern, 새로 refreshToken)
		// refresh 토큰이 1시간 이상이면 그대로( 새로운 accessToken , 기존 refreshToken)
		String newRefreshToken = checkTime((Integer) claims.get("exp")) == true ? 
				JWTUtil.generateToken(claims, 60 * 24) : refreshToken;

		return Map.of("accessToken", newAccessToken, "refreshToken", newRefreshToken);
	}

	// 시간이 1시간 미만으로 남았다면
	private boolean checkTime(Integer exp) {
		// JWT exp를 날짜로 변환
		java.util.Date expDate = new java.util.Date((long) exp * (1000));
		// 현재 시간과의 차이 계산 - 밀리세컨즈
		long gap = expDate.getTime() - System.currentTimeMillis();
		// 분단위 계산
		long leftMin = gap / (1000 * 60);
		// 1시간도 안남았는지..
		return leftMin < 60;
	}

	private boolean checkExpiredToken(String token) {
		try {
			JWTUtil.validateToken(token);
		} catch (CustomJWTException ex) {
			// 만료가 된것이냐?
			if (ex.getMessage().equals("Expired")) {
				return true;
			}
		}
		return false;
	}

}
