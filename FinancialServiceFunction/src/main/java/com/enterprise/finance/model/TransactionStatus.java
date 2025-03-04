package com.enterprise.finance.model;

/**
 * Represents the possible statuses of a financial transaction.
 */
public enum TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED,
    CANCELLED,
    REJECTED,
    REVERSED
} 