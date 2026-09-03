package com.hi.mallapi.service;

import java.util.stream.Collectors;

import com.hi.mallapi.domain.Member;
import com.hi.mallapi.dto.MemberDTO;
import com.hi.mallapi.dto.MemberModifyDTO;

public interface MemberService {
	MemberDTO getKakaoMember(String accessToken);
	
	void modifyMember(MemberModifyDTO memberModifyDTO); 

	default MemberDTO entityToDTO(Member member) {
		MemberDTO dto = new MemberDTO(member.getEmail(), member.getPw(), member.getNickname(), member.isSocial(),
				member.getMemberRoleList().stream().map(memberRole -> memberRole.name()).collect(Collectors.toList()));
		return dto;
	}
}
