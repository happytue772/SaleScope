package com.salescope.batch.parser;

	public enum TransactionType {
	    NORMAL,          // 정상 판매
	    CANCELLED,     // 취소 거래
	    ADJUSTMENT,    // 조정 거래
	    INVALID,       // 유효하지 않은 거래
	    SKIP
	}

