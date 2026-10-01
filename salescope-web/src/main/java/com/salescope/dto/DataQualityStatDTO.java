package com.salescope.dto;

public class DataQualityStatDTO {

    private final long jobId;
    private final String qualityType;
    private final long recordCount;
    private final String sampleMessage;

    public DataQualityStatDTO(
            long jobId,
            String qualityType,
            long recordCount,
            String sampleMessage) {

        this.jobId = jobId;
        this.qualityType = qualityType;
        this.recordCount = recordCount;
        this.sampleMessage = sampleMessage;
    }

    public long getJobId() {
        return jobId;
    }

    public String getQualityType() {
        return qualityType;
    }

    public long getRecordCount() {
        return recordCount;
    }

    public String getSampleMessage() {
        return sampleMessage;
    }
}