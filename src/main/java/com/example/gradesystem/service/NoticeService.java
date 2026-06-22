package com.example.gradesystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.gradesystem.common.BusinessException;
import com.example.gradesystem.common.ErrorCode;
import com.example.gradesystem.dto.NoticeRequest;
import com.example.gradesystem.entity.Notice;
import com.example.gradesystem.mapper.NoticeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class NoticeService {

    @Autowired
    private NoticeMapper noticeMapper;

    public IPage<Notice> listNotices(int pageNum, int pageSize, String status, String targetRole) {
        Page<Notice> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Notice::getStatus, status.toUpperCase());
        }
        if (targetRole != null && !targetRole.isEmpty()) {
            wrapper.eq(Notice::getTargetRole, targetRole.toUpperCase());
        }
        wrapper.orderByDesc(Notice::getCreateTime);
        return noticeMapper.selectPage(page, wrapper);
    }

    public Notice getNoticeById(Long id) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "通知公告不存在");
        }
        return notice;
    }

    @Transactional
    public Notice createNotice(NoticeRequest request) {
        Notice notice = new Notice();
        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());
        notice.setPublisherId(request.getPublisherId());
        notice.setTargetRole(request.getTargetRole() != null ? request.getTargetRole().toUpperCase() : "ALL");
        notice.setStatus(request.getStatus() != null ? request.getStatus().toUpperCase() : "DRAFT");
        notice.setCreateTime(LocalDateTime.now());
        notice.setUpdateTime(LocalDateTime.now());

        if ("PUBLISHED".equals(notice.getStatus())) {
            notice.setPublishTime(LocalDateTime.now());
        }

        noticeMapper.insert(notice);
        return notice;
    }

    @Transactional
    public Notice updateNotice(Long id, NoticeRequest request) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "通知公告不存在");
        }

        if (request.getTitle() != null) {
            notice.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            notice.setContent(request.getContent());
        }
        if (request.getTargetRole() != null) {
            notice.setTargetRole(request.getTargetRole().toUpperCase());
        }
        notice.setUpdateTime(LocalDateTime.now());
        noticeMapper.updateById(notice);
        return notice;
    }

    @Transactional
    public void deleteNotice(Long id) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "通知公告不存在");
        }
        noticeMapper.deleteById(id);
    }

    @Transactional
    public Notice publishNotice(Long id) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "通知公告不存在");
        }
        notice.setStatus("PUBLISHED");
        notice.setPublishTime(LocalDateTime.now());
        notice.setUpdateTime(LocalDateTime.now());
        noticeMapper.updateById(notice);
        return notice;
    }

    @Transactional
    public Notice retractNotice(Long id) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "通知公告不存在");
        }
        notice.setStatus("DRAFT");
        notice.setUpdateTime(LocalDateTime.now());
        noticeMapper.updateById(notice);
        return notice;
    }
}
