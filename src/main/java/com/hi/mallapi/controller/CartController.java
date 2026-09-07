package com.hi.mallapi.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.hi.mallapi.dto.CartItemDTO;
import com.hi.mallapi.dto.CartItemListDTO;
import com.hi.mallapi.service.CartService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@RequiredArgsConstructor
@Log4j2
public class CartController {

	private final CartService cartService;

	// 사용자정보:email, 상품번호:pno, 상품수량 qty , 장바구니아이템 번호: cino;
	// 사용자의 이메일과 파라미터로 전달된 CartItemDTO의 이메일 주소가 같아야만 호출이 가능하도록 설정
	// authenttication.name (시큐리티가지고 사용자 정보)
	@PreAuthorize("#itemDTO.email == authentication.name")
	@PostMapping("/api/cart/change")
	public List<CartItemListDTO> changeCart(@RequestBody CartItemDTO itemDTO) {
		log.info(itemDTO);
		// 상품수량을 0을 포함한 음수
		if (itemDTO.getQty() <= 0) {
			return cartService.remove(itemDTO.getCino());
		}
		// 해당되는 사용자의 장바구니아이템이 있으면 수량만 수정하고, 없으면 장바구니 아이템 생성
		// 해당되는 사용자의 모든장바구니아이템 리스트를 리턴(방금수정, 방금생성 포함)
		return cartService.addOrModify(itemDTO);
	}

	// 사용자정보를 주면: List<ci.cino,ci.qty, p.pno, p.pname, p.price, pi.fileName>
	@PreAuthorize("hasAnyRole('ROLE_USER')")
	@GetMapping("/api/cart/items")
	public List<CartItemListDTO> getCartItems(Principal principal) {
		String email = principal.getName();
		log.info(" ---------------------- ");
		log.info("email: " + email);
		return cartService.getCartItems(email);
	}

	@PreAuthorize("hasAnyRole('ROLE_USER')")
	@DeleteMapping("/api/cart/{cino}")
	public List<CartItemListDTO> removeFromCart(@PathVariable("cino") Long cino) {
		log.info("cart item no: " + cino);
		return cartService.remove(cino);
	}

}
