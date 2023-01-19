package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.execption.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CourseBaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
    @Autowired
    private CourseMarketMapper courseMarketMapper;
    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParams) {
        //构建分页插件
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());

        //构建筛选条件
        LambdaQueryWrapper<CourseBase> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.checkValNotNull(queryCourseParams.getCourseName()), CourseBase::getName, queryCourseParams.getCourseName());
        wrapper.like(StringUtils.checkValNotNull(queryCourseParams.getAuditStatus()), CourseBase::getAuditStatus, queryCourseParams.getAuditStatus());
        wrapper.like(StringUtils.checkValNotNull(queryCourseParams.getPublishStatus()), CourseBase::getStatus, queryCourseParams.getPublishStatus());

        //查询结果
        Page<CourseBase> courseBasePage = courseBaseMapper.selectPage(page, wrapper);
        //构建结果集
        PageResult<CourseBase> result = new PageResult<>(courseBasePage.getRecords(),
                courseBasePage.getTotal(),
                pageParams.getPageNo(),
                pageParams.getPageSize());

        return result;
    }

    /**
     * 需要查询两张表，一个课程信息基本表，一个营销表
     * 封装成CourseBaseInfoDto返回
     *
     * @param course_id
     * @return
     */
    @Override
    public CourseBaseInfoDto getCourseBaseInfoDtoById(Integer course_id) {
        //因为在进行插入操作后，封装了查询查询方法，可直接使用
        return getCourseBaseInfo(course_id);
    }


    @Transactional
    @Override
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto editCourseDto) {
        //业务规则校验，本机构只允许修改本机构的课程
        //课程id
        Long courseId = editCourseDto.getId();
        CourseBase courseBase_u = courseBaseMapper.selectById(courseId);
        if (courseBase_u == null) {
            XueChengPlusException.cast("课程信息不存在");
        }
        if (!companyId.equals(courseBase_u.getCompanyId())) {
            XueChengPlusException.cast("本机构只允许修改本机构的课程");
        }

        //首先将EditCourseDto拆分成基本信息和营销信息，然后根据课程id修改即可
        CourseBase courseBase = new CourseBase();
        BeanUtils.copyProperties(editCourseDto, courseBase);
        //设置修改时间
        courseBase.setChangeDate(LocalDateTime.now());
        //更新基本信息表到数据库
        int i = courseBaseMapper.updateById(courseBase);
        //封装营销表
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(editCourseDto, courseMarket);
        //设置营销表id
        courseMarket.setId(courseId);
        //判断现在是否收费
        //收费规则
        String charge = editCourseDto.getCharge();
        //收费课程必须写价格且价格大于0
        if (charge.equals("201001")) {
            Float price = editCourseDto.getPrice();
            if (price == null || price.floatValue() <= 0) {
                throw new XueChengPlusException("课程设置了收费价格不能为空且必须大于0");
            }
        }
        //更新到营销表
        int j = courseMarketMapper.updateById(courseMarket);

        if(i!=1 || j!=1){
            throw  new XueChengPlusException("更新失败");
        }

        return null;
    }


    //涉及两张表的插入操作，需要用到事务
    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto) {
        //合法性校验，保证非空字段的完整性
        /**
         * 使用到了自定义异常，当抛出异常后将会以统一格式返回给前端，方式不太优雅
         * 实际中，可以与前端商量统一的返回方式，Result
         */
        if (StringUtils.checkValNull(dto.getName())) {
            throw new XueChengPlusException("课程名称为空");
        }
        if (StringUtils.checkValNull(dto.getMt())) {
            throw new XueChengPlusException("课程分类为空");
        }
        if (StringUtils.checkValNull(dto.getSt())) {
            throw new XueChengPlusException("课程分类为空");
        }
        if (StringUtils.checkValNull(dto.getGrade())) {
            throw new XueChengPlusException("课程等级为空");
        }
        if (StringUtils.checkValNull(dto.getTeachmode())) {
            throw new XueChengPlusException("教育模式为空");
        }
        if (StringUtils.checkValNull(dto.getUsers())) {
            throw new XueChengPlusException("适应人群为空");
        }
        if (StringUtils.checkValNull(dto.getCharge())) {
            throw new XueChengPlusException("收费规则为空");
        }

        //创建新增课程对象
        CourseBase courseBaseNew = new CourseBase();
        //将填写的课程信息赋值给新增对象
        BeanUtils.copyProperties(dto, courseBaseNew);
        //设置审核状态,数据字典 202002-未审核
        courseBaseNew.setAuditStatus("202002");
        //设置发布状态  203001-未发布
        courseBaseNew.setStatus("203001");
        //机构id
        courseBaseNew.setCompanyId(companyId);
        //添加时间
        courseBaseNew.setCreateDate(LocalDateTime.now());

        //插入课程基本信息表
        int insert = courseBaseMapper.insert(courseBaseNew);
        Long courseId = courseBaseNew.getId();

        //创建课程营销信息对象
        CourseMarket courseMarketNew = new CourseMarket();
        BeanUtils.copyProperties(dto, courseMarketNew);
        courseMarketNew.setId(courseId);

        //收费规则
        String charge = dto.getCharge();
        //收费课程必须写价格且价格大于0
        if (charge.equals("201001")) {
            Float price = dto.getPrice();
            if (price == null || price.floatValue() <= 0) {
                throw new XueChengPlusException("课程设置了收费价格不能为空且必须大于0");
            }
            courseMarketNew.setPrice(dto.getPrice().floatValue());
        }

        //插入课程营销信息
        int insert1 = courseMarketMapper.insert(courseMarketNew);
        if (insert <= 0 || insert1 <= 0) {
            throw new XueChengPlusException("新增课程基本信息失败");
        }
        //添加成功

        //返回添加的课程信息
        return getCourseBaseInfo(courseId);
    }

    //根据课程id查询课程基本信息，包括基本信息和营销信息
    public CourseBaseInfoDto getCourseBaseInfo(long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        if (courseBase == null) {
            return null;
        }
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        if (courseMarket != null) {
            BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        }

        //查询分类名称
        CourseCategory courseCategoryBySt = courseCategoryMapper.selectById(courseBase.getSt());
        courseBaseInfoDto.setStName(courseCategoryBySt.getName());
        CourseCategory courseCategoryByMt = courseCategoryMapper.selectById(courseBase.getMt());
        courseBaseInfoDto.setMtName(courseCategoryByMt.getName());
        return courseBaseInfoDto;
    }
}
