package com.salescope.service;

import java.sql.SQLException;
import java.util.List;

import com.salescope.dao.ProductSalesDAO;
import com.salescope.dto.ProductSalesSearchCondition;
import com.salescope.dto.ProductSalesStatDTO;
import java.util.Map;

public class ProductSalesService {

    private final ProductSalesDAO productSalesDAO;

    public ProductSalesService() {

        this.productSalesDAO =
            new ProductSalesDAO();
    }

    /**
     * 새로운 검색조건 DTO를 사용하는 조회 메서드
     */
    public List<ProductSalesStatDTO>
            getProductSales(
                ProductSalesSearchCondition condition)
            throws SQLException {

        return productSalesDAO
            .findByCondition(condition);
    }

    /**
     * 기존 호출 코드를 보존하기 위한 호환 메서드
     */
    public List<ProductSalesStatDTO>
            getProductSales(
                long jobId,
                String sortBy,
                int topN,
                String keyword)
            throws SQLException {

        return productSalesDAO.findTopN(
            jobId,
            sortBy,
            topN,
            keyword
        );
    }
    public long getProductCount(
            long jobId)
            throws SQLException {

        return productSalesDAO
            .countByJobId(jobId);
    }
    public List<ProductSalesStatDTO> getDistinctProducts(long jobId) throws SQLException {
        return productSalesDAO.findDistinctProducts(jobId);
    }
    
    public long countProductSalesByCondition(ProductSalesSearchCondition condition) throws SQLException {
        return productSalesDAO.countByCondition(condition);
    }
    
    public Map<String, Number> getSalesDataRange(long jobId) throws SQLException {
        return productSalesDAO.getSalesDataRange(jobId);
    }
}