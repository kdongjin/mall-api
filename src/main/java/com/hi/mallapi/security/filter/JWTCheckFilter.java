package com.hi.mallapi.security.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.google.gson.Gson;
import com.hi.mallapi.dto.MemberDTO;
import com.hi.mallapi.util.JWTUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class JWTCheckFilter extends OncePerRequestFilter {

	// 무조건 필터링 -> 토큰 필터링 -> 각Controller 보내진다.
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		log.info("******** doFilterInternal  *********");

		// 리액트에서 api서버 모든 페이지를 요청할때마다.(토큰을 꼭 포함시켜줘야한다.)
		// 헤더 토큰을 집어넣는다.(Bearer 액세스토큰)
		String authHeaderStr = request.getHeader("Authorization");
		try {
			// Bearer accestoken ............... 토큰이 정상적이면 그대로 요구사항진행
			String accessToken = authHeaderStr.substring(7);
			Map<String, Object> claims = JWTUtil.validateToken(accessToken);
			log.info("JWT claims: " + claims);
			// -----------시큐리티 정책 등록----------
			String email = (String) claims.get("email");
			String pw = (String) claims.get("pw");
			String nickname = (String) claims.get("nickname");
			Boolean social = (Boolean) claims.get("social");
			List<String> roleNames = (List<String>) claims.get("roleNames");
			
			MemberDTO memberDTO = new MemberDTO(email, pw, nickname, social.booleanValue(), roleNames);
			
			// 스프링 시큐리티에서 인증 정보를 담는 객체
			UsernamePasswordAuthenticationToken authenticationToken = 
					new UsernamePasswordAuthenticationToken(memberDTO,
					pw, memberDTO.getAuthorities());
			// 이 객체를 SecurityContextHolder에 넣으면,
			// 해당 요청은 인증된 사용자로 처리됨
			SecurityContextHolder.getContext().setAuthentication(authenticationToken);

			// -----------------------------------
			filterChain.doFilter(request, response);
		} catch (Exception e) {
			log.error("JWT Check Error .................................... ");
			log.error(e.getMessage());
			Gson gson = new Gson();
			String msg = gson.toJson(Map.of("error", "ERROR_ACCESS_TOKEN"));
			response.setContentType("application/json");
			PrintWriter printWriter = response.getWriter();
			printWriter.println(msg);
			printWriter.close();
		}

	}

	// 필터링하지말고, 무조건 통과해야될 리스트
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		// Preflight(지금 보내는 요청이 유효한지를 확인하기 위해 OPTIONS 예비 요청을 보내는 것
		if (request.getMethod().equals("OPTIONS")) {
			return true;
		}

		// 요청페이지 /api/todo/insert
		String path = request.getRequestURI();

		// api/member/login api/member/loout api/member/register경로의 호출은 체크하지 않음
		if (path.startsWith("/api/member/")) {
			return true;
		}
		// 이미지 조회 경로는 체크하지 않하고 싶을 때
		if (path.startsWith("/api/products/view/")) {
			return true;
		}
		return false;
	}

}
