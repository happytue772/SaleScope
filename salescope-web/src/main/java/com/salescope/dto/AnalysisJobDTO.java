package com.salescope.dto;

import java.sql.Timestamp;

public class AnalysisJobDTO {

    private long jobId;
    private long datasetId;
    private String status;
    private String analysisVersion;
    private Timestamp requestedAt;
    private Timestamp finishedAt;
    private String resultImportedYn;

    public AnalysisJobDTO() {
    }

    public long getJobId() {
        return jobId;
    }

    public void setJobId(long jobId) {
        this.jobId = jobId;
    }

    public long getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(long datasetId) {
        this.datasetId = datasetId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAnalysisVersion() {
        return analysisVersion;
    }

    public void setAnalysisVersion(String analysisVersion) {
        this.analysisVersion = analysisVersion;
    }

    public Timestamp getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(Timestamp requestedAt) {
        this.requestedAt = requestedAt;
    }

    public Timestamp getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Timestamp finishedAt) {
        this.finishedAt = finishedAt;
    }

    public String getResultImportedYn() {
        return resultImportedYn;
    }

    public void setResultImportedYn(String resultImportedYn) {
        this.resultImportedYn = resultImportedYn;
    }
}