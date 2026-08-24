package com.hi.mallapi.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.coobird.thumbnailator.Thumbnails;

@Component
@Log4j2
@RequiredArgsConstructor
public class CustomFileUtil {
	@Value("${upload.path}")
	private String uploadPath;

	// 생성자 발생한 후에 나를 불러서 실행해줘.
	// 저장할 폴더가 없으면 생성
	@PostConstruct
	public void init() {
		// 현재 폴더가 없으면 생성
		File tempFolder = new File(uploadPath);
		if (tempFolder.exists() == false) {
			tempFolder.mkdir();
		}

		// 절대경로 C:/upload
		this.uploadPath = tempFolder.getAbsolutePath();
	}

	public List<String> saveFiles(List<MultipartFile> files) throws RuntimeException {
		// 업로드될 파이명들을 저장 리스트
		List<String> uploadNames = new ArrayList<>();

		// List<MultipartFile> 내용 유무 체크
		if (files == null || files.size() == 0) {
			return null;
		}

		// 1)업로드된 파일 => 2)UUID_파일명변화 => 업로드된 임시저장파일 복사 붙여넣기(UUID_파일명) =>
		// UUID_파일명을 List<String>

		// 1)업로드된 파일을 분리
		for (MultipartFile multipartFile : files) {
			// 2)UUID_파일명변화 UUID_kdj.jpg => savePath = "c:\\upload\\UUID_kdj.jpg"
			if(multipartFile.getOriginalFilename().length() <= 0) {
				continue;
			}
			String savedName = UUID.randomUUID().toString() + "_" + multipartFile.getOriginalFilename();
			Path savePath = Paths.get(uploadPath, savedName);

			// 3)로드된 원본파일.inputStream() => savePath 복사처리
			// 4)List<String> uploadNames add(UUID_kdj.jpg ....)
			try {
				Files.copy(multipartFile.getInputStream(), savePath);
				// 5)받는파일이 이미지인지 체크
				String contentType = multipartFile.getContentType();
				log.info("contentType.startsWith =" + contentType);
				if (contentType != null && contentType.startsWith("image")) {
					// 썸네일 이지지이름을 생성한다.
					Path thumbnailPath = Paths.get(uploadPath, "s_" + savedName);
					// 원본이미지를 =>해상도(400,400) 바꿔서 => c:\\upload\\s_UUID_cat.jpg
					Thumbnails.of(savePath.toFile()).size(400, 400).toFile(thumbnailPath.toFile());
					log.info(thumbnailPath.toString());
				}
				uploadNames.add(savedName);
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage());
			}
		} // end for
		return uploadNames;
	}

	public ResponseEntity<Resource> getFile(String fileName) {
		// c:\\upload\\{fileName}
		Resource resource = new FileSystemResource(uploadPath + File.separator + fileName);
		// 해당되는 이미지가 없으면 c:\\upload\\default.jpg
		if (!resource.exists()) {
			resource = new FileSystemResource(uploadPath + File.separator + "default.jpg");
		}
		// 브라우저로 전송하기 HttpHeader 포함시켜서 보낸다.
		HttpHeaders headers = new HttpHeaders();
		try {
			// Files.probeContentType()은 파일 경로를 분석하여 MIME 타입을 자동 감지 jpg → image/jpeg, png →
			// image/png pdf → application/pdf 이 정보를 HTTP 응답 헤더에 Content-Type으로 추가한다
			// 브라우저 보내는 파일의 타입을 생성해서 포함시킨다.
			headers.add("Content-Type", Files.probeContentType(resource.getFile().toPath()));
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
		// 성공하면 status : ok(): 200, header 포함시키고, body에 보여줄 이미지를 포함시킨다.
		return ResponseEntity.ok().headers(headers).body(resource);
	}

	public void deleteFiles(List<String> fileNames) {
		if (fileNames == null || fileNames.size() == 0) {
			return;
		}
		
		fileNames.forEach((fileName) -> {
			// 썸네일이 있는지 확인하고 삭제
			String thumbnailFileName = "s_" + fileName;
			//c:\\upload\\s_uuid_~~~.jpg
			//c:\\upload\\uuid_~~~.jpg
			Path thumbnailPath = Paths.get(uploadPath, thumbnailFileName);
			Path filePath = Paths.get(uploadPath, fileName);
			
			try {
				Files.deleteIfExists(filePath);
				Files.deleteIfExists(thumbnailPath);
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage());
			}
		});
	}
}
