package com.salescope.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.salescope.dto.DatasetDTO;
import com.salescope.util.DBConnectionUtil;

public class DatasetDAO {

    private static final String SELECT_DATASETS_SQL =
        "SELECT DATASET_ID, DISPLAY_NAME, SOURCE_TYPE, SOURCE_NAME " +
        "FROM DATASET " +
        "WHERE IS_DELETED = 'N' " +
        "ORDER BY CREATED_AT DESC, DATASET_ID DESC";

    public List<DatasetDTO> findAll() throws SQLException {

        List<DatasetDTO> datasets = new ArrayList<>();

        try (
            Connection connection = DBConnectionUtil.getConnection();
            PreparedStatement statement =
                connection.prepareStatement(SELECT_DATASETS_SQL);
            ResultSet resultSet = statement.executeQuery()
        ) {

            while (resultSet.next()) {

                DatasetDTO dataset = new DatasetDTO();

                dataset.setDatasetId(
                    resultSet.getLong("DATASET_ID")
                );

                dataset.setDisplayName(
                    resultSet.getString("DISPLAY_NAME")
                );

                dataset.setSourceType(
                    resultSet.getString("SOURCE_TYPE")
                );

                dataset.setSourceName(
                    resultSet.getString("SOURCE_NAME")
                );

                datasets.add(dataset);
            }
        }

        return datasets;
    }
}