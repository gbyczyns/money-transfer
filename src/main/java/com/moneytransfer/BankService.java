package com.moneytransfer;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class BankService {

	private final Map<String, BigDecimal> accounts = new ConcurrentHashMap<>();

	public void createAccount(String id, BigDecimal funds) {
		if (accounts.putIfAbsent(id, funds) != null) {
			throw new IllegalArgumentException("There's already an account with id=" + id);
		}
	}

	public void transfer(String sourceAccount, String targetAccount, BigDecimal amount) {
		String lock1, lock2;
		if (sourceAccount.compareTo(targetAccount) > 0) {
			lock1 = sourceAccount.intern();
			lock2 = targetAccount.intern();
		} else {
			lock1 = targetAccount.intern();
			lock2 = sourceAccount.intern();
		}
		synchronized (lock1) {
			synchronized (lock2) {
				addMoney(sourceAccount, amount.negate());
				addMoney(targetAccount, amount);
			}
		}
	}

	private void addMoney(String accountId, BigDecimal amount) {
		BigDecimal newTargetAccountBalance = accounts.get(accountId).add(amount);
		accounts.put(accountId, newTargetAccountBalance);
	}

	public final BigDecimal getTotalFunds() {
		return accounts.values()
				.stream()
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}
}