package com.devsu.transaction;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Se evita levantar todo el contexto en test normales")
class TransactionApplicationTests {

	@Test
	void contextLoads() {
	}

}
