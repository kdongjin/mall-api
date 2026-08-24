package com.hi.mallapi.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hi.mallapi.dto.PageRequestDTO;
import com.hi.mallapi.dto.PageResponseDTO;
import com.hi.mallapi.dto.ProductDTO;
import com.hi.mallapi.service.ProductService;
import com.hi.mallapi.util.CustomFileUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@RequiredArgsConstructor
@Log4j2
public class ProductController {

	// 생성자의존성 주입
	private final ProductService productService;
	private final CustomFileUtil fileUtil;

	@PostMapping("/api/products")
	public Map<String, Long> insert(ProductDTO productDTO) {
		log.info("rgister: " + productDTO);
		List<MultipartFile> files = productDTO.getFiles();

		// 업로드된파일을 => UUID_파일명 => 복사 (c:\\upload\\uuid_파일명.jpg) => List<UUID_파일명>
		List<String> uploadFileNames = fileUtil.saveFiles(files);
		productDTO.setUploadFileNames(uploadFileNames);

		log.info(uploadFileNames);

		// 서비스 호출
		Long pno = productService.insert(productDTO);
		return Map.of("result", pno);
	}

	@GetMapping("/api/products/view/{fileName}")
	public ResponseEntity<Resource> viewFileGET(@PathVariable String fileName) {
		return fileUtil.getFile(fileName);
	}

	@GetMapping("/api/products/delete/{fileName}")
	public Map<String, String> deleteFileGET(@PathVariable String fileName) {
		List<String> imageNameList = new ArrayList<String>();
		imageNameList.add(fileName);
		fileUtil.deleteFiles(imageNameList);
		return Map.of("RESULT", "SUCCESS");
	}

	@GetMapping("/api/products/list")
	public PageResponseDTO<ProductDTO> list(PageRequestDTO pageRequestDTO) {
		log.info("list............." + pageRequestDTO);
		return productService.selectList(pageRequestDTO);
	}

	@GetMapping("/api/products/{pno}")
	public ProductDTO read(@PathVariable(name = "pno") Long pno) {
		return productService.select(pno);
	}

	@PutMapping("/api/products/{pno}")
	public Map<String, String> update(@PathVariable(name = "pno") Long pno, ProductDTO productDTO) {
		productDTO.setPno(pno);
		// 데이타베이스에 있는 파일들 리스트 oldFileNames(기존의 있는 파일명들)
		ProductDTO oldProductDTO = productService.select(pno);
		// 기존파일들 (데이터베이스에 존재하는 파일들-수정 과정에서 삭제되었을 수 있음 1, 4)
		List<String> oldFileNames = oldProductDTO.getUploadFileNames();
		List<String> currentUploadFileNames = null;

		if (productDTO.getFiles().size() > 0) {
			// 새로 업로드 해야 하는 파일들 : currentUploadFileNames(업로드된 파일명들)
			List<MultipartFile> files = productDTO.getFiles();
			// 새로 업로드되어서 만들어진 파일 이름들 0
			currentUploadFileNames = fileUtil.saveFiles(files);
		}

		// 화면에서 변화 없이 계속 유지된 파일들: uploadedFileNames(기존의 있는 파일명들) (4)
		List<String> uploadedFileNames = productDTO.getUploadFileNames();

		// 유지되는 파일들 + 새로 업로드된 파일 이름들이 저장해야 하는 파일 목록이 됨
		if (currentUploadFileNames != null && currentUploadFileNames.size() > 0) {
			// uploadedFileNames.addAll(currentUploadFileNames);
			productDTO.getUploadFileNames().addAll(currentUploadFileNames);
		}
		// 수정 작업
		productService.update(productDTO);

		if (oldFileNames != null && oldFileNames.size() > 0) {
			// 지워야 하는 파일 목록 찾기
			// 예전 파일들 중에서 지워져야 하는 파일이름들
			List<String> removeFiles = oldFileNames.stream()
					.filter(fileName -> uploadedFileNames.indexOf(fileName) == -1).collect(Collectors.toList());
			// 실제 파일 삭제
			fileUtil.deleteFiles(removeFiles);
		}

		return Map.of("RESULT", "SUCCESS");
	}

	@DeleteMapping("/api/products/{pno}")
	public Map<String, String> delete(@PathVariable("pno") Long pno) {
		// 삭제해야 할 파일들 알아내기
		List<String> oldFileNames = productService.select(pno).getUploadFileNames();
		productService.delete(pno);
		fileUtil.deleteFiles(oldFileNames);
		return Map.of("RESULT", "SUCCESS");
	}
}
