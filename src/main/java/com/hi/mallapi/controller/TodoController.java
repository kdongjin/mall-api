package com.hi.mallapi.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.hi.mallapi.dto.PageRequestDTO;
import com.hi.mallapi.dto.PageResponseDTO;
import com.hi.mallapi.dto.TodoDTO;
import com.hi.mallapi.service.TodoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@RequiredArgsConstructor
@Log4j2
public class TodoController {

	private final TodoService service;

	@GetMapping("/api/todo/{tno}")
	public TodoDTO select(@PathVariable(name = "tno") Long tno) {
		return service.select(tno);
	}

	@GetMapping("/api/todo/list")
	public PageResponseDTO<TodoDTO> list(PageRequestDTO pageRequestDTO) {
		log.info(pageRequestDTO);
		return service.list(pageRequestDTO);
	}

	@PostMapping("/api/todo/")
	public Map<String, Long> insert(@RequestBody TodoDTO todoDTO) {
		log.info("TodoDTO: " + todoDTO);
		Long tno = service.insert(todoDTO);
		return Map.of("TNO", tno);
	}

	@PutMapping("/api/todo/{tno}")
	public Map<String, String> update(@PathVariable(name = "tno") Long tno, @RequestBody TodoDTO todoDTO) {
		todoDTO.setTno(tno);
		log.info("Modify: " + todoDTO);
		service.update(todoDTO);
		return Map.of("RESULT", "SUCCESS");
	}

	@DeleteMapping("/api/todo/{tno}")
	public Map<String, String> delete(@PathVariable(name = "tno") Long tno) {
		log.info("Remove: " + tno);
		service.delete(tno);
		return Map.of("RESULT", "SUCCESS");
	}

}
