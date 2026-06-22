package com.example.gradesystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.gradesystem.common.Result;
import com.example.gradesystem.dto.NoticeRequest;
import com.example.gradesystem.entity.Notice;
import com.example.gradesystem.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/notices")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    @GetMapping
    public Result<IPage<Notice>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String targetRole) {
        return Result.success(noticeService.listNotices(pageNum, pageSize, status, targetRole));
    }

    @GetMapping("/{id}")
    public Result<Notice> getById(@PathVariable Long id) {
        return Result.success(noticeService.getNoticeById(id));
    }

    @PostMapping
    public Result<Notice> create(@RequestBody NoticeRequest request) {
        return Result.success(noticeService.createNotice(request));
    }

    @PutMapping("/{id}")
    public Result<Notice> update(@PathVariable Long id, @RequestBody NoticeRequest request) {
        return Result.success(noticeService.updateNotice(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return Result.success(null);
    }

    @PutMapping("/{id}/publish")
    public Result<Notice> publish(@PathVariable Long id) {
        return Result.success(noticeService.publishNotice(id));
    }

    @PutMapping("/{id}/retract")
    public Result<Notice> retract(@PathVariable Long id) {
        return Result.success(noticeService.retractNotice(id));
    }
}
