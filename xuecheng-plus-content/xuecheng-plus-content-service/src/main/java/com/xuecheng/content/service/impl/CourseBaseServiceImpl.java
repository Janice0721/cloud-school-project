package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 课程基本信息 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class CourseBaseServiceImpl extends ServiceImpl<CourseBaseMapper, CourseBase> implements CourseBaseService {
    @Autowired
    private CourseBaseMapper courseBaseMapper;
    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParams) {
        //构建分页插件
        Page<CourseBase> page =new Page<>(pageParams.getPageNo(),pageParams.getPageSize());

        //构建筛选条件
        LambdaQueryWrapper<CourseBase> wrapper =new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.checkValNotNull(queryCourseParams.getCourseName()),CourseBase::getName,queryCourseParams.getCourseName());
        wrapper.like(StringUtils.checkValNotNull(queryCourseParams.getAuditStatus()),CourseBase::getAuditStatus,queryCourseParams.getAuditStatus());
        wrapper.like(StringUtils.checkValNotNull(queryCourseParams.getPublishStatus()),CourseBase::getStatus,queryCourseParams.getPublishStatus());

        //查询结果
        Page<CourseBase> courseBasePage = courseBaseMapper.selectPage(page, wrapper);
        //构建结果集
        PageResult<CourseBase> result =new PageResult<>(courseBasePage.getRecords(),
                courseBasePage.getTotal(),
                pageParams.getPageNo(),
                pageParams.getPageSize());

        return  result;
    }
}
