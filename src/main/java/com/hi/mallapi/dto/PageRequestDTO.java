package com.hi.mallapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
//상속관계 전체에 걸쳐 builder 연결
@SuperBuilder
public class PageRequestDTO {
	@Builder.Default 
	private int page= 1; 
	@Builder.Default 
	private int size = 10; 
}
