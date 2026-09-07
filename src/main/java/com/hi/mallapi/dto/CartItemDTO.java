package com.hi.mallapi.dto;

import lombok.Data;

@Data
public class CartItemDTO {
	//장바구니 사용자정보
	private String email;
	//물품정보
	private Long pno;
	//수량
	private int qty; 
	//장바구니 아이템번호
	private Long cino; 
}
