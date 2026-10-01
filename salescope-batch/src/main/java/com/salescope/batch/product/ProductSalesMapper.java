package com.salescope.batch.product;

import com.salescope.batch.parser.RetailTransaction;
import com.salescope.batch.util.AbstractRetailMapper;

/**
 * 상품코드를 기준으로 거래를 그룹화하는 Mapper.
 *
 * CSV 파싱, 거래 분류, Hadoop Counter 처리는
 * AbstractRetailMapper가 담당한다.
 */
public class ProductSalesMapper
        extends AbstractRetailMapper {

    /**
     * 상품별 집계를 위해 StockCode를
     * Mapper 출력 Key로 사용한다.
     */
    @Override
    protected String mapKey(
            RetailTransaction transaction) {

        return transaction.getStockCode();
    }
}