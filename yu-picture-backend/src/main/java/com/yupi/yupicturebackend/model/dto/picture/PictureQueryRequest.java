package com.yupi.yupicturebackend.model.dto.picture;

import com.yupi.yupicturebackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 图片查询请求对象
 *      支持名称、简介、分类、标签、格式、宽高、比例、大小等多维检索
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PictureQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签搜索字段，
     *      数据库中 tags 通常以 JSON 字符串存储，查询时需要逐个标签匹配
     */
    private List<String> tags;

    /**
     * 文件体积
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private Integer picWidth;

    /**
     * 图片高度
     */
    private Integer picHeight;

    /**
     * 图片比例
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String picFormat;

    /**
     * 搜索词（同时搜名称、简介等）
     */
    private String searchText;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 审核状态：0-待审核; 1-通过; 2-拒绝
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;

    /**
     * 审核人 ID
     */
    private Long reviewerId;

    /**
     * 审核时间
     */
    private Date reviewTime;

    /**
     * 图片所属空间 id 查询指定空间下的图片
     *      null 表示公共图库图片，非 null 表示私有空间图片
     */
    private Long spaceId;

    /**
     * 是否强制查询公共图库图片，即 spaceId is null
     */
    private boolean nullSpaceId;

    /*
     * 开始编辑时间
     */
    private Date startEditTime;

    /*
     * 结束编辑时间
     */
    private Date endEditTime;

    private static final long serialVersionUID = 1L;
}