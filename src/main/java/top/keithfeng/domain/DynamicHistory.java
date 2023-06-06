package top.keithfeng.domain;

import java.io.Serializable;

import cn.hutool.core.date.DateUtil;
import lombok.Data;
import love.forte.simbot.message.Message;

/**
 * 
 * @TableName dynamic_history
 */
@Data
public class DynamicHistory implements Serializable {
    /**
     * 
     */
    private Long dynamicId;

    /**
     * 
     */
    private String type;

    /**
     * 
     */
    private String message;

    /**
     * 
     */
    private String createTime;

    private static final long serialVersionUID = 1L;

    public DynamicHistory() {
    }

    public DynamicHistory(Long dynamicId, String type, Message message) {
        this.dynamicId = dynamicId;
        this.type = type;
        this.message = message.toString();
        this.createTime = DateUtil.now();
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        DynamicHistory other = (DynamicHistory) that;
        return (this.getDynamicId() == null ? other.getDynamicId() == null : this.getDynamicId().equals(other.getDynamicId()))
            && (this.getType() == null ? other.getType() == null : this.getType().equals(other.getType()))
            && (this.getMessage() == null ? other.getMessage() == null : this.getMessage().equals(other.getMessage()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getDynamicId() == null) ? 0 : getDynamicId().hashCode());
        result = prime * result + ((getType() == null) ? 0 : getType().hashCode());
        result = prime * result + ((getMessage() == null) ? 0 : getMessage().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", dynamicId=").append(dynamicId);
        sb.append(", type=").append(type);
        sb.append(", message=").append(message);
        sb.append(", createTime=").append(createTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}