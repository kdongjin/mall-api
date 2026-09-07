package com.hi.mallapi.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.hi.mallapi.domain.Cart;
import com.hi.mallapi.domain.CartItem;
import com.hi.mallapi.domain.Member;
import com.hi.mallapi.domain.Product;
import com.hi.mallapi.dto.CartItemDTO;
import com.hi.mallapi.dto.CartItemListDTO;
import com.hi.mallapi.repository.CartItemRepository;
import com.hi.mallapi.repository.CartRepository;
import com.hi.mallapi.repository.MemberRepository;
import com.hi.mallapi.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Service
@Log4j2
public class CartServiceImpl implements CartService {
	private final CartRepository cartRepository;
	private final CartItemRepository cartItemRepository;
	private final MemberRepository memberRepository;
	private final ProductRepository productRepository;

	//해당되는 사용자의 장바구니아이템이 있으면 수량만 수정하고, 없으면 장바구니 아이템 생성
	//해당되느 사용자의 모든장바구니아이템 리스트를 리턴(방금수정, 방금생성 포함)
	@Override
	public List<CartItemListDTO> addOrModify(CartItemDTO cartItemDTO) {
		//사용자정보:이메일, 선택한상품번호, 상품수량, 장바구니아이템번호(없으면->iNSERT , 있으면->UPDATE)
		String email = cartItemDTO.getEmail();
		Long pno = cartItemDTO.getPno();
		int qty = cartItemDTO.getQty();
		Long cino = cartItemDTO.getCino();
		
		log.info("======================");
		log.info(cartItemDTO.getCino() == null);
		//장바구니아이템 존재하니 => UPDATE(수량)
		if (cino != null) { 
			Optional<CartItem> cartItemResult = cartItemRepository.findById(cino);
			CartItem cartItem = cartItemResult.orElseThrow();
			
			cartItem.changeQty(qty);
			cartItemRepository.save(cartItem);
			//사용자가 선택을 통해서 수정한 장바구니 아이템을 포함하고, 사용자가 구입한 모든 장바구니 아이템을 리턴
			return getCartItems(email);
		}
		//사용자의 장바구니 가져온다. 
		//사용자 장바구니 없다. => 장바구니 생성 리턴
		//사용자 장바구니 있다. => 현재 장바구니 리턴 
		Cart cart = getCart(email);
		
		CartItem cartItem = null;
		// 이미 동일한 상품이 담긴적이 있을 수 있으므로
		cartItem = cartItemRepository.getItemOfPno(email, pno);

		if (cartItem == null) {
			// Product product = Product.builder().pno(pno).build();
			Optional<Product> result = productRepository.findById(pno);
			Product product = result.orElseThrow();
			
			cartItem = CartItem.builder().product(product).cart(cart).qty(qty).build();
		} else {
			cartItem.changeQty(qty);
		}

		// 상품 아이템 저장
		cartItemRepository.save(cartItem);
		return getCartItems(email);
	}

	// 사용자의 장바구니가 없었다면 새로운 장바구니를 생성하고 반환
	// 사용자가 장바구니 있다면 기존의 장바구니 리턴
	private Cart getCart(String email) {
		Cart cart = null;
		Optional<Cart> result = cartRepository.getCartOfMember(email);
		//사용자의 장바구니 조회결과 없음=> 장바구니 생성
		if (result.isEmpty()) {
			log.info("Cart of the member is not exist!!");
			// Member member = Member.builder().email(email).build();
			Optional<Member> _result = memberRepository.findById(email);
			Member member = _result.orElseThrow();
			
			Cart tempCart = Cart.builder().owner(member).build();
			cart = cartRepository.save(tempCart);
		} else {
			cart = result.get();
		}
		return cart;
	}

	//사용자정보를 주면: List<ci.cino,ci.qty, p.pno, p.pname, p.price, pi.fileName>
	@Override
	public List<CartItemListDTO> getCartItems(String email) {
		return cartItemRepository.getItemsOfCartDTOByEmail(email);
	}

	//장바구니 아이템번호 : 
	@Override
	public List<CartItemListDTO> remove(Long cino) {
		//장바구니 아이템번호를 사용자의 장바구니 번호를 가져온다. 
		Long cno = cartItemRepository.getCartFromItem(cino);
		log.info("cart no: " + cno);
		//사용자의 해당되는 장바구니 아이템을 삭제
		cartItemRepository.deleteById(cino);
		
		//List<ci.cino, ci.qty, p.pno, p.pname, p.price , pi.fileName>
		return cartItemRepository.getItemsOfCartDTOByCart(cno);
	}

}
