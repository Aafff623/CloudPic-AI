package com.yupi.yupicturebackend.model.dto.space.analyze;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用空间分析请求
 * 抽出这三个字段, 让各种具体分析请求类继承它，避免每个接口重复定义这几个字段
 */
@Data
public class SpaceAnalyzeRequest implements Serializable {

    /**
     * 指定的私有空间 ID
     *      空间主人 / 有权限者
     */
    private Long spaceId;

    /**
     * 是否分析公共图库
     *      管理员
     */
    private boolean queryPublic;

    /**
     * 是否分析全部空间
     *      管理员
     */
    private boolean queryAll;

    private static final long serialVersionUID = 1L;
}
