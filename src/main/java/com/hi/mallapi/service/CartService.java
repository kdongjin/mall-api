package com.hi.mallapi.service;

import java.util.List;
import com.hi.mallapi.dto.CartItemDTO;
import com.hi.mallapi.dto.CartItemListDTO;
import jakarta.transaction.Transactional;

@Transactional
public interface CartService {
	//장바구니 아이템 추가 혹은 변경 
	public List<CartItemListDTO> addOrModify(CartItemDTO cartItemDTO); 
	//사용자가 진행한 모든 장바구니 아이템 목록 
	public List<CartItemListDTO> getCartItems(String email); 
	//사용자가 사용한 장바구니 아티템 번호를 가지고 삭제하고, 다은 장바구니 아이템을 리스트 리턴  
	public List<CartItemListDTO> remove(Long cino); 
}
