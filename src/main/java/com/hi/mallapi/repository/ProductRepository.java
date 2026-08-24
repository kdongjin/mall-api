package com.hi.mallapi.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hi.mallapi.domain.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

	// 조인해서 데이터를 가져온다.(product, product_image_list 테이블을 조인해서 즉시로딩을 처리함.)
	@EntityGraph(attributePaths = "imageList")
	@Query("select p from Product p where p.pno = :pno")
	Optional<Product> selectOne(@Param("pno") Long pno);

	// @Modifying 어노테이션은 @Query가 SELECT가 아닌 DML(UPDATE, DELETE 등)일 때 필요함.
	@Modifying
	@Query("update Product p set p.delFlag = :flag where p.pno = :pno")
	void updateToDelete(@Param("pno") Long pno, @Param("flag") boolean flag);

	@Query("select p, pi from Product p left join p.imageList pi where pi.ord = 0 and p.delFlag = false")
	Page<Object[]> selectList(Pageable pageable);
	
	
}
