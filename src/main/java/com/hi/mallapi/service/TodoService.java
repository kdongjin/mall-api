package com.hi.mallapi.service;

import com.hi.mallapi.dto.PageRequestDTO;
import com.hi.mallapi.dto.PageResponseDTO;
import com.hi.mallapi.dto.TodoDTO;

public interface TodoService {
	public Long insert(TodoDTO todoDTO);
	
	public TodoDTO select(Long tno);
	
	public void update(TodoDTO todoDTO); 

	public void delete(Long tno); 
	
	public PageResponseDTO<TodoDTO> list(PageRequestDTO pageRequestDTO);
}
