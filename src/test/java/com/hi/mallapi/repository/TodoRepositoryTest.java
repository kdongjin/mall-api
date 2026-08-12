package com.hi.mallapi.repository;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.hi.mallapi.domain.Todo;

import lombok.extern.log4j.Log4j2;

@Log4j2
//@SpringBootTest
public class TodoRepositoryTest {

	@Autowired
	private TodoRepository todoRepository;

	// @Test
	public void testInsert() {
		log.info("+++++++++testInsert start++++++++++++++++");
		for (int i = 1; i < 101; i++) {
			Todo todo = Todo.builder().title("Title..." + i).writer("user00").build();
			todoRepository.save(todo);
		}
		log.info("+++++++++testInsert stop++++++++++++++++");
	}

	// @Test
	public void testSelect() {
		// 존재하는 번호로 확인
		log.info("+++++++++testSelect start++++++++++++++++");
		Long tno = 33L;
		java.util.Optional<Todo> result = todoRepository.findById(tno);
		Todo todo = result.orElseThrow();
		log.info(todo);
		log.info("+++++++++testSelect start++++++++++++++++");
	}

	// @Test
	public void testUpdate() {
		// 수정하고자 하는 레코드를 가져온다.
		log.info("+++++++++testUpdate start++++++++++++++++");
		Long tno = 33L;
		java.util.Optional<Todo> result = todoRepository.findById(tno);
		Todo todo = result.orElseThrow();

		// 수정하고자하는 레코드에 수정데이타를 chage를 진행
		todo.changeTitle("Modified 33...");
		todo.changeComplete(true);
		todo.changeDueDate(LocalDate.of(2026, 8, 12));
		todoRepository.save(todo);
		log.info("+++++++++testUpdate stop++++++++++++++++");
	}

	// @Test
	public void testDelete() {
		log.info("+++++++++testDelete start++++++++++++++++");
		Long tno = 1L;
		todoRepository.deleteById(tno);
		log.info("+++++++++testDelete stop++++++++++++++++");
	}

	//@Test
	public void testPaging() {
		// 0번째 페이지요청(페이지 인덱스는 0부터 시작), 한 페이지에 10개의 데이터,
		// 정렬기준은 tno 필드를 기준으로 내림차순
		Pageable pageable = PageRequest.of(1, 5, Sort.by("tno").descending());
		// Page<Todo>타입 반환되며, 전체 정보(총 개수, 현재 페이지 등)가 포함
		Page<Todo> result = todoRepository.findAll(pageable);
		// 전체 데이터 개수(전체 Todo 엔티티 수)를 로그로 출력
		log.info(result.getTotalElements());
		// 현재 페이지(0페이지)에 포함된 Todo 목록을 가져온다.
		result.getContent().stream().forEach(todo -> log.info(todo));
	}
}





