package com.hi.mallapi.repository;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import com.hi.mallapi.domain.Product;
import com.hi.mallapi.dto.ProductDTO;
import com.hi.mallapi.service.ProductService;

import lombok.extern.log4j.Log4j2;

@SpringBootTest
@Log4j2
public class ProductRepositoryTest {
	@Autowired
	ProductRepository productRepository;
	@Autowired
	ProductService productService;

	// @Test
	public void testInsert() {
		for (int i = 0; i < 10; i++) {
			// project 테이블에 상품명, 가격, 설명, 이미지 2
			Product product = Product.builder().pname("상품" + i).price(100 * i).pdesc("상품설명" + i).delFlag(false).build();
			// 해당되는 상품명에 2개의 이미지를 추가
			product.addImageString(UUID.randomUUID().toString() + "_" + "IMAGE1.jpg");
			product.addImageString(UUID.randomUUID().toString() + "_" + "IMAGE2.jpg");
			log.info("===================");
			productRepository.save(product);
		}

	}

	// Lazy Loading 방식(첫번쨰 Product 테이블은 무조건 가져온다.
	// 두번째 테이블(product_image 테이블은 참조할때만 가져온다)
	// @Transactional
	// @Test
	public void testRead() {
		Long pno = 1L;
		Optional<Product> result = productRepository.findById(pno);
		Product product = result.orElseThrow();
		log.info(product);
		// log.info(product.getImageList());
	}

	// 상품정보 select(Eager 방식)
	// @Test
	public void testRead2() {
		Long pno = 1L;
		Optional<Product> result = productRepository.selectOne(pno);
		Product product = result.orElseThrow();
		log.info(product);
		log.info(product.getImageList());
	}

	@Commit
	@Transactional
	// @Test
	public void testDelete() {
		Long pno = 2L;
		productRepository.updateToDelete(pno, true);
	}

	// @Test
	public void testUpdate() {
		Long pno = 10L;
		Product product = productRepository.selectOne(pno).get();
		product.changeName("10번 상품 kdj");
		product.changeDesc("10번 상품 설명 kdj");
		product.changePrice(15000);
		// 첨부파일 수정
		product.clearList();

		product.addImageString(UUID.randomUUID().toString() + "-" + "NEWIMAGE1.jpg");
		product.addImageString(UUID.randomUUID().toString() + "-" + "NEWIMAGE2.jpg");
		product.addImageString(UUID.randomUUID().toString() + "-" + "NEWIMAGE3.jpg");

		productRepository.save(product);
	}

	// @Test
	public void testList() {
		// org.springframework.data.domain 패키지
		Pageable pageable = PageRequest.of(0, 10, Sort.by("pno").descending());
		Page<Object[]> result = productRepository.selectList(pageable);
		// java.util
		result.getContent().forEach(arr -> log.info(Arrays.toString(arr)));
	}

	// java.util.List.of("","","") => ArrayList<String>
	// @Test
	public void testInsert2() {
		ProductDTO productDTO = ProductDTO.builder().pname("새로운 상품").pdesc("신규 추가 상품입니다.").price(1000).build();
		// uuid가 있어야함
		productDTO.setUploadFileNames(
				java.util.List.of(UUID.randomUUID() + "_" + "Test1.jpg", UUID.randomUUID() + "_" + "Test2.jpg"));
		productService.insert(productDTO);
	}

	@Test
	public void testSelect() {
		// 실제 존재하는 번호로 테스트(DB에서 확인)
		Long pno = 9L;
		ProductDTO productDTO = productService.select(pno);
		log.info(productDTO);
		log.info(productDTO.getUploadFileNames());
	}

}













