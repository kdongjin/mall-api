package com.hi.mallapi.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "tbl_product")
@Getter
@ToString(exclude = "imageList")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PRODUCT_SEQ_GEN")
	private Long pno;

	private String pname;
	private int price;
	private String pdesc;

	private boolean delFlag;
	
	public void changeDel(boolean delFlag) {
		this.delFlag = delFlag;
	}

	// 테이블 : (tbl_product tbl_product_image_list) 생성
	// tbl_product_image_list 컬러명: (tbl_product_id(fk), filename, ord )생성
	@ElementCollection
	@Builder.Default
	private List<ProductImage> imageList = new ArrayList<>();

	public void changePrice(int price) {
		this.price = price;
	}
	
	public void changeDesc(String desc) {
		this.pdesc = desc;
	}

	public void changeName(String name) {
		this.pname = name;
	}

	public void addImage(ProductImage image) {
		// 이미지 추가시 순서(ord) 자동 설정 (0, 1, 2, ...)
		image.setOrd(this.imageList.size());
		imageList.add(image);
	}

	public void addImageString(String fileName) {
		ProductImage productImage = ProductImage.builder().fileName(fileName).build();
		addImage(productImage);
	}

	public void clearList() {
		this.imageList.clear();
	}
}
