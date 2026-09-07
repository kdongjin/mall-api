package com.hi.mallapi;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.test.annotation.Commit;

import com.hi.mallapi.domain.Cart;
import com.hi.mallapi.domain.CartItem;
import com.hi.mallapi.domain.Member;
import com.hi.mallapi.domain.Product;
import com.hi.mallapi.dto.CartItemListDTO;
import com.hi.mallapi.repository.CartItemRepository;
import com.hi.mallapi.repository.CartRepository;
import com.hi.mallapi.repository.MemberRepository;
import com.hi.mallapi.repository.ProductRepository;

import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;

@SpringBootTest
@Log4j2
public class CartRepositoryTests {
	@Autowired
	private CartRepository cartRepository;
	@Autowired
	private MemberRepository memberRepository;
	@Autowired
	private ProductRepository productRepository;
	@Autowired
	private CartItemRepository cartItemRepository;

	@Transactional
	@Commit
	// @Test
	public void testInsertByProduct() {
		log.info("test1 ");
		// 사용자가 전송하는 정보(실제있는 사용자 정보입력할 것)
		String email = "user1@jjjj.com";
		Long pno = 5L; // 실제있는 상품번호를 입력할 것
		int qty = 2;
		/*
		 * @Query("select"+" ci "+" from "
		 * +" CartItem ci inner join Cart c on ci.cart = c "+" " +
		 * "where "+"c.owner.email=:email and ci.product.pno=:pno") public CartItem
		 * getItemOfPno(@Param("email") String email, @Param("pno") Long pno);
		 */
		// 만일 기존에 사용자의 장바구니 아이템이 있었다면
		CartItem cartItem = cartItemRepository.getItemOfPno(email, pno);
		if (cartItem != null) {
			cartItem.changeQty(qty);
			cartItemRepository.save(cartItem);
			return;
		}

		// Cart (사용자정보를 갖고 있는 Cart)
		// 장바구니 아이템이 없었다면 장바구니부터 확인
		// 사용자가 장바구니를 만든적이 있는지 확인
		/*
		 * @Query("select cart from Cart cart where cart.owner.email = :email") public
		 * Optional<Cart> getCartOfMember(@Param("email") String email);
		 */
		Optional<Cart> result = cartRepository.getCartOfMember(email);
		Cart cart = null;

		// 사용자의 장바구니가 존재하지 않으면 장바구니 생성
		if (result.isEmpty()) {
			// Member member = Member.builder().email(email).build();
			Optional<Member> member = memberRepository.findById(email);
			Member _member = member.orElseThrow();
			Cart tempCart = Cart.builder().owner(_member).build();
			// Member => member pk => fk
			cart = cartRepository.save(tempCart);
		} else {
			cart = result.orElseThrow();
		}

		log.info(cart);
		// 장바구니 아이템가 없으면 실제적인 상품번호 생성
		if (cartItem == null) {
			// Product product = Product.builder().pno(pno).build();
			// 상품번호 5번
			Optional<Product> product = productRepository.findById(pno);
			Product _product = product.orElseThrow();

			cartItem = CartItem.builder().product(_product).cart(cart).qty(qty).build();
		}
		cartItemRepository.save(cartItem);
	}

	@Transactional
	// @Test
	@Commit
	public void tesstUpdateByCino() {
		Long cino = 1L; // 위에서 생성된 장바구니 아이템 cino를 적는다.
		int qty = 4;
		Optional<CartItem> result = cartItemRepository.findById(cino);
		CartItem cartItem = result.orElseThrow();

		cartItem.changeQty(qty);
		cartItemRepository.save(cartItem);
	}

	//@Test
	public void testListOfMember() {
		String email = "user1@jjjj.com";
		/*
		 * @Query("select "
		 * +" new com.hi.mallapi.dto.CartItemListDTO(ci.cino,ci.qty, p.pno, p.pname, p.price,pi.fileName) "
		 * +" from " + " CartItem ci inner join Cart mc on ci.cart = mc "+
		 * " left join Product p on ci.product = p " + " left join p.imageList pi " +
		 * " where "+ " mc.owner.email=:email and pi.ord=0 " + " order by ci desc")
		 * public List<CartItemListDTO> getItemsOfCartDTOByEmail(@Param("email") String
		 * email);
		 */
		List<CartItemListDTO> cartItemList = cartItemRepository.getItemsOfCartDTOByEmail(email);
		for (CartItemListDTO dto : cartItemList) {
			log.info(dto);
		}
	}

	@Test
	@Transactional
	@Commit
	public void testDeleteThenList() {
		//장바구니 아이템 번호 
		Long cino = 1L;
		// 장바구니 번호
/*
  * @Query("select "+" c.cno "+" from " 
	+ " Cart c inner join CartItem ci on ci.cart = c "
	+" where "+" ci.cino=:cino ")
	public Long getCartFromItem(@Param("cino") Long cino);
 */
 		Long cno = cartItemRepository.getCartFromItem(cino);
		// 삭제하지말고, 리스트를 본다음 그 다음에 삭제를하고 리스트를 볼것
		cartItemRepository.deleteById(cino);
		// 목록
/*
 * 	@Query("select new com.hi.mallapi.dto.CartItemListDTO "
			+ " (ci.cino, ci.qty, p.pno, p.pname, p.price , pi.fileName) " 
			+ " from " + " CartItem ci inner join  Cart mc	on ci.cart=mc " 
			+ " left join Product p on ci.product=p " + " left join	p.imageList pi " 
			+ " where " + " mc.cno=:cno and pi.ord=0 " + " order by ci desc") 
	public List<CartItemListDTO> getItemsOfCartDTOByCart(@Param("cno") Long cno);		
 */
		List<CartItemListDTO> cartItemList = cartItemRepository.getItemsOfCartDTOByCart(cno);
		for (CartItemListDTO dto : cartItemList) {
			log.info(dto);
		}
	}

}