package com.xuecheng.content;

import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseCategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;

@SpringBootTest
class XuechengPlusContentServiceApplicationTests {
    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Test
    void contextLoads() {
        CourseBase courseBase = courseBaseMapper.selectById(1);
        System.out.println(courseBase);
    }
    @Autowired
    CourseCategoryService courseCategoryService;
    @Test
    void testqueryTreeNodes() {
        List<CourseCategoryTreeDto> categoryTreeDtos = courseCategoryService.queryTreeNodes("1");
        System.out.println(categoryTreeDtos);
    }

}
