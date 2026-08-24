package com.hi.mallapi.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hi.mallapi.domain.Product;
import com.hi.mallapi.domain.ProductImage;
import com.hi.mallapi.dto.PageRequestDTO;
import com.hi.mallapi.dto.PageResponseDTO;
import com.hi.mallapi.dto.ProductDTO;
import com.hi.mallapi.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@Transactional
@RequiredArgsConstructor
public class ProductSeviceImpl implements ProductService {

	private final ProductRepository productRepository;

	@Override
	public PageResponseDTO<ProductDTO> selectList(PageRequestDTO pageRequestDTO) {
		Pageable pageable = PageRequest.of(pageRequestDTO.getPage() - 1, pageRequestDTO.getSize(),
				Sort.by("pno").descending());

		// 1페이지 size: 10 => 10개 객체를 리턴
		// [Product(1), ProductImage(fileName=b1.jpg, ord=0)]
		// [Product(2), ProductImage(fileName=b2.jpg, ord=0)]
		// [Product(10), ProductImage(fileName=b10.jpg, ord=0)]
		Page<Object[]> result = productRepository.selectList(pageable);

		// arr =>[Product(1), ProductImage(fileName=b10.jpg, ord=0)]
		List<ProductDTO> dtoList = result.get().map(arr -> {
			Product product = (Product) arr[0];
			ProductImage productImage = (ProductImage) arr[1];
			ProductDTO productDTO = ProductDTO.builder().pno(product.getPno()).pname(product.getPname())
					.pdesc(product.getPdesc()).price(product.getPrice()).build();
			String imageStr = productImage.getFileName();
			productDTO.setUploadFileNames(List.of(imageStr));
			return productDTO;
		}).collect(Collectors.toList());
		long totalCount = result.getTotalElements();
		return PageResponseDTO.<ProductDTO>withAll().dtoList(dtoList).totalCount(totalCount)
				.pageRequestDTO(pageRequestDTO).build();
	}

	@Override
	public Long insert(ProductDTO productDTO) {
		Product product = dtoToEntity(productDTO);
		Product result = productRepository.save(product);
		return result.getPno();
	}

	// ProductDTO => Product 엔티티 변경
	private Product dtoToEntity(ProductDTO productDTO) {
		Product product = Product.builder().pno(productDTO.getPno()).pname(productDTO.getPname())
				.pdesc(productDTO.getPdesc()).price(productDTO.getPrice()).build();
		// 업로드 처리가 끝난 파일들의 이름 리스트
		List<String> uploadFileNames = productDTO.getUploadFileNames();
		if (uploadFileNames == null) {
			return product;
		}
		uploadFileNames.stream().forEach(uploadName -> {
			product.addImageString(uploadName);
		});

		return product;
	}

	@Override
	public ProductDTO select(Long pno) {
		// Eager 로딩방식으로 처리
		java.util.Optional<Product> result = productRepository.selectOne(pno);
		Product product = result.orElseThrow();
		ProductDTO productDTO = entityToDTO(product);
		return productDTO;
	}

	// Product 엔티티를 ProductDTO 변환
	private ProductDTO entityToDTO(Product product) {
		ProductDTO productDTO = ProductDTO.builder().pno(product.getPno()).pname(product.getPname())
				.pdesc(product.getPdesc()).price(product.getPrice()).build();
		List<ProductImage> imageList = product.getImageList();
		if (imageList == null || imageList.size() == 0) {
			return productDTO;
		}
		List<String> fileNameList = imageList.stream().map(productImage -> productImage.getFileName()).toList();

		productDTO.setUploadFileNames(fileNameList);
		return productDTO;
	}

	@Override
	public void update(ProductDTO productDTO) {
		// 1. read
		//1, 4 => 4
		Optional<Product> result = productRepository.findById(productDTO.getPno());
		Product product = result.orElseThrow();
		// change pname, pdesc, price
		product.changeName(productDTO.getPname());
		product.changeDesc(productDTO.getPdesc());
		product.changePrice(productDTO.getPrice());

		// upload File -- clear first
		product.clearList();
		//1, 4 => 4
		List<String> uploadFileNames = productDTO.getUploadFileNames();
		if (uploadFileNames != null && uploadFileNames.size() > 0) {
			uploadFileNames.stream().forEach(uploadName -> {
				product.addImageString(uploadName);
			});
		}
		//pno 가 없으면 insert 가 이루어진다. pno 있으면 업데이트 진행
		productRepository.save(product);
	}

	@Override 
	public void delete(Long pno){ 
		productRepository.updateToDelete(pno, true); 
	} 
}
