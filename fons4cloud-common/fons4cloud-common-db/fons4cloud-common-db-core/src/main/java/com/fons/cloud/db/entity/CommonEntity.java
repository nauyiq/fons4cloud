package com.fons.cloud.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fons.cloud.db.mybatisplus.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author hongqy
 * @date 2025/2/14
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class CommonEntity extends BaseEntity {

    /**
     * 自增主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 是否删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Boolean deleted;

    /**
     * 版本号
     */
    @Version
    @TableField(fill = FieldFill.INSERT)
    private Integer version;




}
