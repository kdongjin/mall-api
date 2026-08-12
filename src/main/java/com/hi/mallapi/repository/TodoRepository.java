package com.hi.mallapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.hi.mallapi.domain.Todo;

public interface TodoRepository extends JpaRepository<Todo, Long> {

}
