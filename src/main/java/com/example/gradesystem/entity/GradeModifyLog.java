package com.example.gradesystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("grade_modify_log")
public class GradeModifyLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long gradeId;
    private BigDecimal oldScore;
    private BigDecimal newScore;
    private String reason;
    private Long applicantId;
    private Long approverId;
    private String status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getGradeId() { return gradeId; }
    public void setGradeId(Long gradeId) { this.gradeId = gradeId; }
    public BigDecimal getOldScore() { return oldScore; }
    public void setOldScore(BigDecimal oldScore) { this.oldScore = oldScore; }
    public BigDecimal getNewScore() { return newScore; }
    public void setNewScore(BigDecimal newScore) { this.newScore = newScore; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Long getApplicantId() { return applicantId; }
    public void setApplicantId(Long applicantId) { this.applicantId = applicantId; }
    public Long getApproverId() { return approverId; }
    public void setApproverId(Long approverId) { this.approverId = approverId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
