package com.xuecheng.content.api;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(value = "课程信息编辑接口",tags = "课程信息编辑接口")
public class CourseBaseInfoController {

    @Autowired
    private CourseBaseService courseBaseService;

    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody QueryCourseParamsDto queryCourseParams){
        PageResult<CourseBase>  pageResult= courseBaseService.queryCourseBaseList(pageParams,queryCourseParams);
        return pageResult;
    }
}
