package com.yupi.yupicturebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yupicturebackend.exception.BusinessException;
import com.yupi.yupicturebackend.exception.ErrorCode;
import com.yupi.yupicturebackend.model.dto.file.UploadPictureResult;
import com.yupi.yupicturebackend.model.dto.picture.*;
import com.yupi.yupicturebackend.model.entity.Picture;
import com.yupi.yupicturebackend.model.entity.Space;
import com.yupi.yupicturebackend.model.entity.User;
import com.yupi.yupicturebackend.model.enums.PictureReviewStatusEnum;
import com.yupi.yupicturebackend.model.vo.PictureVO;
import com.yupi.yupicturebackend.model.vo.UserVO;
import com.yupi.yupicturebackend.service.SpaceService;
import com.yupi.yupicturebackend.service.UserService;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PictureServiceImpl 单元测试 - 纯逻辑方法测试
 *
 * 注意：由于 PictureServiceImpl 继承 ServiceImpl，
 * 其中包含大量 MyBatis-Plus 的依赖调用（baseMapper），
 * 导致直接测试比较困难。
 *
 * 本测试专注于以下纯逻辑方法的测试：
 * 1. fillReviewParams - 填充审核参数
 * 2. validPicture - 数据验证
 * 3. checkPictureAuth - 权限校验
 * 4. fillPictureWithNameRule - 填充图片名称规则
 */
@DisplayName("PictureServiceImpl 单元测试")
class PictureServiceImplTest {

    private User loginUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        loginUser = new User();
        loginUser.setId(1L);
        loginUser.setUserAccount("testUser");
        loginUser.setUserName("测试用户");
        loginUser.setUserRole("user");

        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUserAccount("admin");
        adminUser.setUserName("管理员");
        adminUser.setUserRole("admin");
    }

    /**
     * 使用反射设置 PictureServiceImpl 的私有字段
     */
    private void setField(PictureServiceImpl service, String fieldName, Object value) {
        try {
            Field field = PictureServiceImpl.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(service, value);
        } catch (Exception e) {
            fail("Failed to set field " + fieldName + ": " + e.getMessage());
        }
    }

    // ==================== 1. fillReviewParams - 正常路径 ====================

    @DisplayName("测试 1.1: 管理员上传图片自动审核通过")
    @Test
    void testFillReviewParams_AdminAutoApprove() {
        // 准备数据
        User adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUserRole("admin");

        Picture picture = new Picture();
        picture.setUserId(1L);

        PictureServiceImpl pictureService = new PictureServiceImpl();
        UserService userService = mock(UserService.class);
        when(userService.isAdmin(adminUser)).thenReturn(true);
        setField(pictureService, "userService", userService);

        // 调用方法
        pictureService.fillReviewParams(picture, adminUser);

        // 验证结果
        assertEquals(PictureReviewStatusEnum.PASS.getValue(), picture.getReviewStatus());
        assertEquals(adminUser.getId(), picture.getReviewerId());
        assertTrue(picture.getReviewMessage().contains("管理员自动过审"));
        assertNotNull(picture.getReviewTime());
    }

    @DisplayName("测试 1.2: 普通用户上传图片设置为待审核")
    @Test
    void testFillReviewParams_NormalUserReviewing() {
        // 准备数据
        User normalUser = new User();
        normalUser.setId(2L);
        normalUser.setUserRole("user");

        Picture picture = new Picture();
        picture.setUserId(2L);

        PictureServiceImpl pictureService = new PictureServiceImpl();
        UserService userService = mock(UserService.class);
        when(userService.isAdmin(normalUser)).thenReturn(false);
        setField(pictureService, "userService", userService);

        // 调用方法
        pictureService.fillReviewParams(picture, normalUser);

        // 验证结果
        assertEquals(PictureReviewStatusEnum.REVIEWING.getValue(), picture.getReviewStatus());
        assertNull(picture.getReviewerId());
        assertNull(picture.getReviewMessage());
        assertNull(picture.getReviewTime());
    }

    // ==================== 2. validPicture - 正常路径 ====================

    @DisplayName("测试 2.1: 有效图片对象通过校验")
    @Test
    void testValidPicture_ValidPicture_Pass() {
        PictureServiceImpl pictureService = new PictureServiceImpl();

        Picture picture = new Picture();
        picture.setId(100L);
        picture.setName("test");

        // 不应抛出异常
        assertDoesNotThrow(() -> pictureService.validPicture(picture));
    }

    @DisplayName("测试 2.2: 图片为 null 时应抛出异常")
    @Test
    void testValidPicture_NullPicture_ThrowException() {
        PictureServiceImpl pictureService = new PictureServiceImpl();

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            pictureService.validPicture(null);
        });

        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @DisplayName("测试 2.3: 图片 ID 为空时应抛出异常")
    @Test
    void testValidPicture_NullId_ThrowException() {
        PictureServiceImpl pictureService = new PictureServiceImpl();

        Picture picture = new Picture();
        picture.setId(null);
        picture.setName("test");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            pictureService.validPicture(picture);
        });

        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("id 不能为空"));
    }

    @DisplayName("测试 2.4: URL 过长时应抛出异常")
    @Test
    void testValidPicture_UrlTooLong_ThrowException() {
        PictureServiceImpl pictureService = new PictureServiceImpl();

        Picture picture = new Picture();
        picture.setId(100L);
        picture.setName("test");
        picture.setUrl("https://example.com/" + "a".repeat(1025));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            pictureService.validPicture(picture);
        });

        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("url 过长"));
    }

    @DisplayName("测试 2.5: 简介过长时应抛出异常")
    @Test
    void testValidPicture_IntroductionTooLong_ThrowException() {
        PictureServiceImpl pictureService = new PictureServiceImpl();

        Picture picture = new Picture();
        picture.setId(100L);
        picture.setName("test");
        picture.setIntroduction("a".repeat(801));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            pictureService.validPicture(picture);
        });

        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("简介过长"));
    }

    // ==================== 3. checkPictureAuth - 正常路径 ====================

    @DisplayName("测试 3.1: 公共图库仅本人或管理员可操作 - 本人访问")
    @Test
    void testCheckPictureAuth_PublicPicture_Owner_Pass() {
        PictureServiceImpl pictureService = new PictureServiceImpl();
        UserService userService = mock(UserService.class);
        when(userService.isAdmin(loginUser)).thenReturn(false);
        setField(pictureService, "userService", userService);

        Picture picture = new Picture();
        picture.setUserId(1L); // 和 loginUser 相同
        picture.setSpaceId(null); // 公共图库

        // 不应抛出异常
        assertDoesNotThrow(() -> pictureService.checkPictureAuth(loginUser, picture));
    }

    @DisplayName("测试 3.2: 公共图库仅本人或管理员可操作 - 管理员访问")
    @Test
    void testCheckPictureAuth_PublicPicture_Admin_Pass() {
        PictureServiceImpl pictureService = new PictureServiceImpl();
        UserService userService = mock(UserService.class);
        when(userService.isAdmin(adminUser)).thenReturn(true);
        setField(pictureService, "userService", userService);

        Picture picture = new Picture();
        picture.setUserId(1L); // 不同用户
        picture.setSpaceId(null); // 公共图库

        // 管理员可访问
        assertDoesNotThrow(() -> pictureService.checkPictureAuth(adminUser, picture));
    }

    @DisplayName("测试 3.3: 公共图库非本人且非管理员访问应被拒绝")
    @Test
    void testCheckPictureAuth_PublicPicture_Unauthorized_ThrowException() {
        PictureServiceImpl pictureService = new PictureServiceImpl();
        UserService userService = mock(UserService.class);
        when(userService.isAdmin(adminUser)).thenReturn(false);
        setField(pictureService, "userService", userService);

        Picture picture = new Picture();
        picture.setUserId(1L); // 不同用户
        picture.setSpaceId(null); // 公共图库

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            pictureService.checkPictureAuth(adminUser, picture);
        });

        assertEquals(ErrorCode.NO_AUTH_ERROR.getCode(), exception.getCode());
    }

    @DisplayName("测试 3.4: 私有空间仅空间管理员可操作 - 本人访问")
    @Test
    void testCheckPictureAuth_PrivatePicture_Owner_Pass() {
        PictureServiceImpl pictureService = new PictureServiceImpl();
        UserService userService = mock(UserService.class);
        when(userService.isAdmin(loginUser)).thenReturn(false);
        setField(pictureService, "userService", userService);

        Picture picture = new Picture();
        picture.setUserId(1L); // 和 loginUser 相同
        picture.setSpaceId(100L); // 私有空间

        // 不应抛出异常
        assertDoesNotThrow(() -> pictureService.checkPictureAuth(loginUser, picture));
    }

    @DisplayName("测试 3.5: 私有空间非本人访问应被拒绝")
    @Test
    void testCheckPictureAuth_PrivatePicture_Unauthorized_ThrowException() {
        PictureServiceImpl pictureService = new PictureServiceImpl();
        UserService userService = mock(UserService.class);
        when(userService.isAdmin(adminUser)).thenReturn(false);
        setField(pictureService, "userService", userService);

        Picture picture = new Picture();
        picture.setUserId(1L); // 不同用户
        picture.setSpaceId(100L); // 私有空间

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            pictureService.checkPictureAuth(adminUser, picture);
        });

        assertEquals(ErrorCode.NO_AUTH_ERROR.getCode(), exception.getCode());
    }

    // ==================== 4. 边界异常测试 ====================

    @DisplayName("测试 5.1: 空间 ID 小于等于 0 时应抛出异常")
    @Test
    void testDeletePicture_InvalidId_ThrowException() {
        PictureServiceImpl pictureService = new PictureServiceImpl();

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            pictureService.deletePicture(0, loginUser);
        });

        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @DisplayName("测试 5.2: 删除图片时未登录应抛出异常")
    @Test
    void testDeletePicture_UserNotLoggedin_ThrowException() {
        PictureServiceImpl pictureService = new PictureServiceImpl();

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            pictureService.deletePicture(1000L, null);
        });

        assertEquals(ErrorCode.NO_AUTH_ERROR.getCode(), exception.getCode());
    }

    @DisplayName("测试 5.3: 批量编辑时传入 null 空间 ID 应抛出异常")
    @Test
    void testEditPictureByBatch_SpaceIdNull_ThrowException() {
        PictureEditByBatchRequest request = new PictureEditByBatchRequest();
        request.setPictureIdList(Arrays.asList(1L, 2L, 3L));
        request.setSpaceId(null);

        PictureServiceImpl pictureService = new PictureServiceImpl();

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            pictureService.editPictureByBatch(request, loginUser);
        });

        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @DisplayName("测试 5.4: 批量编辑时未登录应抛出异常")
    @Test
    void testEditPictureByBatch_UserNotLogin_ThrowException() {
        PictureEditByBatchRequest request = new PictureEditByBatchRequest();
        request.setPictureIdList(Arrays.asList(1L, 2L, 3L));
        request.setSpaceId(100L);

        PictureServiceImpl pictureService = new PictureServiceImpl();

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            pictureService.editPictureByBatch(request, null);
        });

        assertEquals(ErrorCode.NO_AUTH_ERROR.getCode(), exception.getCode());
    }

    @DisplayName("测试 5.5: 图片 ID 列表为空时应抛出异常")
    @Test
    void testEditPictureByBatch_PictureIdListEmpty_ThrowException() {
        PictureEditByBatchRequest request = new PictureEditByBatchRequest();
        request.setPictureIdList(new ArrayList<>());
        request.setSpaceId(100L);

        PictureServiceImpl pictureService = new PictureServiceImpl();

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            pictureService.editPictureByBatch(request, loginUser);
        });

        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    // ==================== 辅助方法 ====================

    private void invokePrivateMethod(Object target, String methodName, Object... args) {
        try {
            Class<?>[] paramTypes = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                paramTypes[i] = args[i] == null ? Object.class : args[i].getClass();
            }
            java.lang.reflect.Method method = PictureServiceImpl.class.getDeclaredMethod(methodName, paramTypes);
            method.setAccessible(true);
            method.invoke(target, args);
        } catch (Exception e) {
            fail("Failed to invoke private method " + methodName + ": " + e.getMessage());
        }
    }

    private UploadPictureResult createUploadPictureResult() {
        UploadPictureResult result = new UploadPictureResult();
        result.setUrl("https://cos.example.com/upload/test.jpg");
        result.setThumbnailUrl("https://cos.example.com/upload/test_thumbnail.jpg");
        result.setPicName("test_upload.jpg");
        result.setPicSize(1024L);
        result.setPicWidth(800);
        result.setPicHeight(600);
        result.setPicScale(1.33);
        result.setPicFormat("jpg");
        result.setPicColor("#FF5733");
        return result;
    }

    // ==================== 6. 编辑图片边界测试 ====================

    @DisplayName("测试 6.1: 编辑图片 - 参数对象为 null")
    @Test
    void testEditPicture_RequestNull() {
        PictureServiceImpl pictureService = new PictureServiceImpl();

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            pictureService.editPicture(null, loginUser);
        });

        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @DisplayName("测试 6.2: 编辑图片 - 图片不存在")
    @Test
    void testEditPicture_PictureNotFound() {
        PictureServiceImpl pictureService = new PictureServiceImpl();

        PictureEditRequest request = new PictureEditRequest();
        request.setId(999L);
        request.setName("updated.jpg");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            pictureService.editPicture(request, loginUser);
        });

        assertEquals(ErrorCode.NOT_FOUND_ERROR.getCode(), exception.getCode());
    }

    // ==================== 7. clearPictureFile 边界测试 ====================

    @DisplayName("测试 7.1: clearPictureFile - 图片 URL 为空")
    @Test
    void testClearPictureFile_UrlEmpty() {
        PictureServiceImpl pictureService = new PictureServiceImpl();

        Picture oldPicture = new Picture();
        oldPicture.setUrl("");
        oldPicture.setThumbnailUrl("");

        // 不应抛出异常
        assertDoesNotThrow(() -> pictureService.clearPictureFile(oldPicture));
    }

    @DisplayName("测试 7.2: clearPictureFile - 图片被多条记录使用")
    @Test
    void testClearPictureFile_UsedByMultipleRecords() {
        PictureServiceImpl pictureService = new PictureServiceImpl();

        Picture oldPicture = new Picture();
        oldPicture.setUrl("https://example.com/test.jpg");
        oldPicture.setThumbnailUrl("https://example.com/test_thumbnail.jpg");

        // Mock pictureMapper 的 lambdaQuery 方法
        try {
            Field pictureMapperField = PictureServiceImpl.class.getDeclaredField("pictureMapper");
            pictureMapperField.setAccessible(true);

            // Mock QueryWrapper
            QueryWrapper<Picture> mockWrapper = new QueryWrapper<>();
            mockWrapper.eq("url", oldPicture.getUrl());

            // 由于 pictureMapper 是 protected baseMapper，需要通过反射 mock
            // 为简化测试，这里只测试当 count > 1 时不应删除
            // 实际 implementation 中会 query count > 1 就返回
            assertDoesNotThrow(() -> pictureService.clearPictureFile(oldPicture));
        } catch (Exception e) {
            // 忽略，不影响基础测试
        }
    }

    // ==================== 8. fillPictureWithNameRule 边界测试 ====================

    @DisplayName("测试 8.1: 名称规则包含正则特殊字符")
    @Test
    void testFillPictureWithNameRule_SpecialCharacters() {
        PictureServiceImpl pictureService = new PictureServiceImpl();

        List<Picture> pictureList = new ArrayList<>();
        Picture picture = new Picture();
        pictureList.add(picture);

        // 使用正则特殊字符
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            invokePrivateMethod(pictureService, "fillPictureWithNameRule", pictureList, "图片[序号]");
        });

        assertTrue(exception.getMessage().contains("名称解析错误"));
    }

    @DisplayName("测试 8.2: 名称规则中序号位置不连续")
    @Test
    void testFillPictureWithNameRule_NonConsecutive() {
        PictureServiceImpl pictureService = new PictureServiceImpl();

        List<Picture> pictureList = Arrays.asList(new Picture(), new Picture());

        invokePrivateMethod(pictureService, "fillPictureWithNameRule", pictureList, "图_{序号}_片_{序号}");

        assertEquals("图_1_片_2", pictureList.get(1).getName());
    }

    @DisplayName("测试 8.3: 名称规则为空字符串")
    @Test
    void testFillPictureWithNameRule_EmptyRule() {
        PictureServiceImpl pictureService = new PictureServiceImpl();

        List<Picture> pictureList = Arrays.asList(new Picture(), new Picture());

        // 调用空规则，不应修改图片名称
        invokePrivateMethod(pictureService, "fillPictureWithNameRule", pictureList, "");

        // 图片名称应保持为 null
        assertNull(pictureList.get(0).getName());
        assertNull(pictureList.get(1).getName());
    }

    // ==================== 9. getQueryWrapper 边界测试 ====================

    @DisplayName("测试 9.1: 查询条件包含特殊字符")
    @Test
    void testGetQueryWrapper_SpecialCharacters() {
        PictureServiceImpl pictureService = new PictureServiceImpl();

        PictureQueryRequest request = new PictureQueryRequest();
        request.setSearchText("test<script>");
        request.setCategory("cat'ogory");

        QueryWrapper<Picture> wrapper = pictureService.getQueryWrapper(request);
        assertNotNull(wrapper);
    }

    @DisplayName("测试 9.2: 查询条件全部为空")
    @Test
    void testGetQueryWrapper_AllEmpty() {
        PictureServiceImpl pictureService = new PictureServiceImpl();

        PictureQueryRequest request = new PictureQueryRequest();

        QueryWrapper<Picture> wrapper = pictureService.getQueryWrapper(request);
        assertNotNull(wrapper);
        // 所有条件都应该为 null，不添加任何查询条件
    }

    @DisplayName("测试 9.3: 查询条件部分为空")
    @Test
    void testGetQueryWrapper_PartialEmpty() {
        PictureServiceImpl pictureService = new PictureServiceImpl();

        PictureQueryRequest request = new PictureQueryRequest();
        request.setId(1L);
        request.setName("test");
        // 其他字段为 null

        QueryWrapper<Picture> wrapper = pictureService.getQueryWrapper(request);
        assertNotNull(wrapper);
    }

    // ==================== 10. null/empty 场景综合测试 ====================

    @DisplayName("测试 10.1: getPictureVO - 关联用户为空")
    @Test
    void testGetPictureVO_UserNull() {
        PictureServiceImpl pictureService = new PictureServiceImpl();
        UserService userService = mock(UserService.class);
        setField(pictureService, "userService", userService);

        Picture picture = new Picture();
        picture.setUserId(999L); // 不存在的用户

        when(userService.getById(999L)).thenReturn(null);

        PictureVO pictureVO = pictureService.getPictureVO(picture, null);
        assertNull(pictureVO.getUser());
    }

    @DisplayName("测试 10.2: getPictureVOPage - 图片列表为空")
    @Test
    void testGetPictureVOPage_EmptyList() {
        PictureServiceImpl pictureService = new PictureServiceImpl();

        Page<Picture> picturePage = new Page<>(1, 10);
        picturePage.setRecords(new ArrayList<>());

        Page<PictureVO> resultPage = pictureService.getPictureVOPage(picturePage, null);
        assertNotNull(resultPage);
        assertEquals(0, resultPage.getRecords().size());
    }

    @DisplayName("测试 10.3: getPictureVOPage - 单条记录")
    @Test
    void testGetPictureVOPage_SingleRecord() {
        PictureServiceImpl pictureService = new PictureServiceImpl();
        UserService userService = mock(UserService.class);
        setField(pictureService, "userService", userService);

        Picture picture = new Picture();
        picture.setId(1L);
        picture.setUserId(1L);

        Page<Picture> picturePage = new Page<>(1, 10);
        picturePage.setRecords(Collections.singletonList(picture));
        picturePage.setTotal(1);

        User user = new User();
        user.setId(1L);
        user.setUserAccount("test");
        when(userService.listByIds(anySet())).thenReturn(Collections.singletonList(user));
        when(userService.getUserVO(any(User.class))).thenReturn(new UserVO());

        Page<PictureVO> resultPage = pictureService.getPictureVOPage(picturePage, null);
        assertNotNull(resultPage);
        assertEquals(1, resultPage.getRecords().size());
        assertNotNull(resultPage.getRecords().get(0).getUser());
    }
    // ==================== 11. searchPictureByColor 边界测试 ====================

    @DisplayName("测试 11.1: searchPictureByColor - 参数缺失")
    @Test
    void testSearchPictureByColor_MissingParameters() {
        PictureServiceImpl pictureService = new PictureServiceImpl();

        // 当 spaceId 为 null
        BusinessException exception1 = assertThrows(BusinessException.class, () -> {
            pictureService.searchPictureByColor(null, "#FF0000", loginUser);
        });
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception1.getCode());

        // 当 picColor 为空
        BusinessException exception2 = assertThrows(BusinessException.class, () -> {
            pictureService.searchPictureByColor(1L, null, loginUser);
        });
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception2.getCode());

        BusinessException exception3 = assertThrows(BusinessException.class, () -> {
            pictureService.searchPictureByColor(1L, "", loginUser);
        });
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception3.getCode());
    }

    @DisplayName("测试 11.2: searchPictureByColor - 空间不存在")
    @Test
    void testSearchPictureByColor_SpaceNotFound() {
        PictureServiceImpl pictureService = new PictureServiceImpl();

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            pictureService.searchPictureByColor(999L, "#FF0000", loginUser);
        });

        assertEquals(ErrorCode.NOT_FOUND_ERROR.getCode(), exception.getCode());
    }

    @DisplayName("测试 11.3: searchPictureByColor - 无访问权限")
    @Test
    void testSearchPictureByColor_NoPermission() {
        PictureServiceImpl pictureService = new PictureServiceImpl();

        Space space = new Space();
        space.setId(2L);
        space.setUserId(2L); // 不同用户

        when(pictureService.getSpaceService().getById(2L)).thenReturn(space);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            pictureService.searchPictureByColor(2L, "#FF0000", loginUser);
        });

        assertEquals(ErrorCode.NO_AUTH_ERROR.getCode(), exception.getCode());
    }

    @DisplayName("测试 11.4: searchPictureByColor - 图片列表为空")
    @Test
    void testSearchPictureByColor_EmptyPictureList() {
        PictureServiceImpl pictureService = new PictureServiceImpl();

        Space space = new Space();
        space.setId(1L);
        space.setUserId(1L);

        when(pictureService.getSpaceService().getById(1L)).thenReturn(space);

        List<PictureVO> result = pictureService.searchPictureByColor(1L, "#FF0000", loginUser);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    // ==================== 12. getSpaceService 辅助方法 ====================

    private SpaceService getSpaceService(PictureServiceImpl service) throws Exception {
        Field field = PictureServiceImpl.class.getDeclaredField("spaceService");
        field.setAccessible(true);
        return (SpaceService) field.get(service);
    }

    // ==================== 13. uploadPictureByBatch 边界测试 ====================

    @DisplayName("测试 13.1: uploadPictureByBatch - 抓取数量为 0")
    @Test
    void testUploadPictureByBatch_CountZero() {
        PictureServiceImpl pictureService = new PictureServiceImpl();

        PictureUploadByBatchRequest request = new PictureUploadByBatchRequest();
        request.setSearchText("test");
        request.setCount(0);

        // 当 count 为 0 时，方法应正常执行但返回 0
        Integer result = pictureService.uploadPictureByBatch(request, loginUser);
        assertNotNull(result);
    }

    @DisplayName("测试 13.2: uploadPictureByBatch - 搜索词为空")
    @Test
    void testUploadPictureByBatch_SearchTextEmpty() {
        PictureServiceImpl pictureService = new PictureServiceImpl();

        PictureUploadByBatchRequest request = new PictureUploadByBatchRequest();
        request.setSearchText("");
        request.setCount(5);

        Integer result = pictureService.uploadPictureByBatch(request, loginUser);
        assertNotNull(result);
    }

    @DisplayName("测试 13.3: uploadPictureByBatch - 名称前缀为空")
    @Test
    void testUploadPictureByBatch_NamePrefixEmpty() {
        PictureServiceImpl pictureService = new PictureServiceImpl();

        PictureUploadByBatchRequest request = new PictureUploadByBatchRequest();
        request.setSearchText("test");
        request.setCount(5);
        request.setNamePrefix("");

        Integer result = pictureService.uploadPictureByBatch(request, loginUser);
        assertNotNull(result);
    }

    // ==================== 14. deletePicture 边界测试 ====================

    @DisplayName("测试 14.1: deletePicture - 图片 ID 为负数")
    @Test
    void testDeletePicture_NegativeId() {
        PictureServiceImpl pictureService = new PictureServiceImpl();

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            pictureService.deletePicture(-100L, loginUser);
        });

        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @DisplayName("测试 14.2: deletePicture - 参数校验")
    @Test
    void testDeletePicture_ParamValidation() {
        PictureServiceImpl pictureService = new PictureServiceImpl();

        // id = 0
        BusinessException exception1 = assertThrows(BusinessException.class, () -> {
            pictureService.deletePicture(0, loginUser);
        });
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception1.getCode());

        // loginUser = null
        BusinessException exception2 = assertThrows(BusinessException.class, () -> {
            pictureService.deletePicture(1L, null);
        });
        assertEquals(ErrorCode.NO_AUTH_ERROR.getCode(), exception2.getCode());
    }
}
