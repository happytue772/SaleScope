package com.salescope.batch.parser;

public class ParserSmokeMain {

    public static void main(String[] args) {
    	   System.out.println("=== Parser 테스트 시작 ===");

        /*
         * 1. 헤더
         * 예상 결과: SKIP
         */
        String header =
            "InvoiceNo,StockCode,Description,Quantity,"
            + "InvoiceDate,UnitPrice,CustomerID,Country";

        /*
         * 2. 정상 판매
         * Description 안에 쉼표를 넣어
         * Apache Commons CSV가 정상 처리하는지도 확인한다.
         *
         * 예상 결과: NORMAL
         */
        String normal =
            "536365,85123A,\"WHITE, HANGING HEART\",6,"
            + "12/1/2010 8:26,2.55,17850,United Kingdom";

        /*
         * 3. 취소 거래
         * InvoiceNo가 C로 시작하고 수량이 음수다.
         *
         * 예상 결과: CANCELLED
         */
        String cancelled =
            "C536379,D,Discount,-1,"
            + "12/1/2010 9:41,27.50,14527,United Kingdom";

        /*
         * 4. 조정 거래
         * 일반 InvoiceNo이지만 수량이 음수다.
         * CustomerID가 비어 있어도 거래 분류는 가능하다.
         *
         * 예상 결과: ADJUSTMENT
         */
        String adjustment =
            "536380,POST,POSTAGE,-1,"
            + "2010-12-01 09:00:00,18.00,,United Kingdom";

        /*
         * 5. 컬럼 부족
         * 예상 결과: INVALID
         */
        String invalidColumns = "536365,85123A";

        /*
         * 6. 숫자 변환 오류
         * Quantity에 숫자가 아닌 ABC가 들어 있다.
         *
         * 예상 결과: INVALID
         */
        String invalidNumber =
            "536365,85123A,TEST PRODUCT,ABC,"
            + "2010-12-01 09:00:00,2.55,17850,United Kingdom";

        check("헤더", header, TransactionType.SKIP);
        check("정상 거래", normal, TransactionType.NORMAL);
        check("취소 거래", cancelled, TransactionType.CANCELLED);
        check("조정 거래", adjustment, TransactionType.ADJUSTMENT);
        check("컬럼 부족", invalidColumns, TransactionType.INVALID);
        check("숫자 오류", invalidNumber, TransactionType.INVALID);

        System.out.println();
        System.out.println("모든 Parser 테스트 통과");
    }

    private static void check(
            String testName,
            String csvLine,
            TransactionType expectedType) {

        RetailTransaction tx =
            RetailTransactionParser.parse(csvLine);

        System.out.println("--------------------------------");
        System.out.println("테스트 이름: " + testName);
        System.out.println("예상 유형: " + expectedType);
        System.out.println("실제 유형: " + tx.getType());
        System.out.println("InvoiceNo: " + tx.getInvoiceNo());
        System.out.println("StockCode: " + tx.getStockCode());
        System.out.println("SalesMonth: " + tx.getSalesMonth());
        System.out.println(
            "AbsoluteAmount: "
            + tx.calculateAbsoluteAmount().toPlainString()
        );
        System.out.println(
            "ErrorMessage: "
            + tx.getErrorMessage()
        );

        if (tx.getType() != expectedType) {
            throw new IllegalStateException(
                testName
                + " 실패: 예상="
                + expectedType
                + ", 실제="
                + tx.getType()
            );
        }

        System.out.println("결과: 통과");
    }
}