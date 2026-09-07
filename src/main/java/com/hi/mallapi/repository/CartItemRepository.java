package com.hi.mallapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.hi.mallapi.domain.CartItem;
import com.hi.mallapi.dto.CartItemListDTO;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
	//조인조건: CartItem = Cart, CartItem = Product, ProductImageList = product (4개의 테이블에서 나온 결과값 모든 필드)
	//조건: Cart.owner.email =:email and productImageList.ord = 0 
	//정렬: CartItem pk 내림차순으로 정렬
	//추출: ci.cino, ci.qty, p.pno, p.pname, p.price, pi.filename
	@Query("select "+" new com.hi.mallapi.dto.CartItemListDTO(ci.cino,ci.qty, p.pno, p.pname, p.price,pi.fileName) " 
			+" from " + " CartItem ci inner join Cart mc on ci.cart = mc "+ " left join Product p on ci.product = p " 
			+ " left join p.imageList pi " + " where "+ " mc.owner.email=:email and pi.ord=0 " + " order by ci desc") 
	public List<CartItemListDTO> getItemsOfCartDTOByEmail(@Param("email") String email);

	//조인조건: CartItem = Cart (2개의 테이블에서 나온 결과값 모든 필드)
	//조건: Cart.owner.email =:email and CartItem.product.pno = :pno  
	//정렬: CartItem pk 내림차순으로 정렬
	//추출: CartItem 있는 모든것을 추출(cno, qty, pno, owner)
	@Query("select "+" ci "+" from "+" CartItem ci inner join Cart c on ci.cart = c "+" "
			+ " where "+" c.owner.email=:email and ci.product.pno=:pno") 
	public CartItem getItemOfPno(@Param("email") String email, @Param("pno") Long pno);

	//조인조건: CartItem = Cart (2개의 테이블에서 나온 결과값 모든 필드)
	//조건:  CartItem.cino =  :cion  
	//추출:  cart.cno
	@Query("select "+" c.cno "+" from " 
	+ " Cart c inner join CartItem ci on ci.cart = c "
	+" where "+" ci.cino=:cino ")
	public Long getCartFromItem(@Param("cino") Long cino);

	//조인조건: CartItem = Cart, CartItem = Product, ProductImageList = product (4개의 테이블에서 나온 결과값 모든 필드)
	//조건: Cart.cno =:cno and productImageList.ord = 0 
	//정렬: Cart.con pk 내림차순으로 정렬
	//추출: ci.cino, ci.qty, p.pno, p.pname, p.price, pi.filename
	@Query("select new com.hi.mallapi.dto.CartItemListDTO "
			+ " (ci.cino, ci.qty, p.pno, p.pname, p.price , pi.fileName) " 
			+ " from " + " CartItem ci inner join  Cart mc	on ci.cart=mc " 
			+ " left join Product p on ci.product=p " + " left join	p.imageList pi " 
			+ " where " + " mc.cno=:cno and pi.ord=0 " + " order by ci desc") 
	public List<CartItemListDTO> getItemsOfCartDTOByCart(@Param("cno") Long cno);
}






