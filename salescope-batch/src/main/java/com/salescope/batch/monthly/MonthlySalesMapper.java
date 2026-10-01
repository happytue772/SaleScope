package com.salescope.batch.monthly;

import java.time.format.DateTimeFormatter;

import com.salescope.batch.parser.RetailTransaction;
import com.salescope.batch.util.AbstractRetailMapper;

/**
 * 거래 일시를 YYYY-MM 형태의
 * 월별 Key로 변환한다.
 */
public class MonthlySalesMapper
        extends AbstractRetailMapper {

    private static final DateTimeFormatter
            YEAR_MONTH_FORMAT =
            DateTimeFormatter.ofPattern(
                    "yyyy-MM"
            );

    @Override
    protected String mapKey(
            RetailTransaction transaction) {

        if (transaction.getInvoiceDate()
                == null) {

            return "";
        }

        return transaction
                .getInvoiceDate()
                .format(YEAR_MONTH_FORMAT);
    }
}