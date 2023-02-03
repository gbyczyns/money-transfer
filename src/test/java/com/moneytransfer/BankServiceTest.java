package com.moneytransfer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BankServiceTest {

	public static final int NUMBER_OF_ACCOUNTS = 10;
	public static final int NUMBER_OF_TRANSACTIONS = 300;
	public static final BigDecimal INITIAL_FUNDS = new BigDecimal("1000.00");
	public static final int NUMBER_OF_THREADS = 100;

	private final BankService bankService = new BankService();

	@Test
	void totalAmountOfMoneyShouldNotChange() throws InterruptedException {
		IntStream.range(0, NUMBER_OF_ACCOUNTS)
				.mapToObj(Integer::toString)
				.forEach(id -> bankService.createAccount(id, INITIAL_FUNDS));

		Runnable runnable = () -> {
			ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
			String sourceAccount = Integer.toString(threadLocalRandom.nextInt(NUMBER_OF_ACCOUNTS));
			String targetAccount = Integer.toString(threadLocalRandom.nextInt(NUMBER_OF_ACCOUNTS));
			BigDecimal amount = new BigDecimal(threadLocalRandom.nextDouble(100d)).setScale(2, RoundingMode.HALF_EVEN);
			bankService.transfer(sourceAccount, targetAccount, amount);
		};

		ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
		for (int i = 0; i < NUMBER_OF_TRANSACTIONS; i++) {
			executorService.submit(runnable);
		}
		executorService.shutdown();
		if (executorService.awaitTermination(15L, TimeUnit.SECONDS)) {
			Assertions.assertEquals(new BigDecimal(NUMBER_OF_ACCOUNTS).multiply(INITIAL_FUNDS), bankService.getTotalFunds());
		} else {
			Assertions.fail("Timed out.");
		}
	}
}