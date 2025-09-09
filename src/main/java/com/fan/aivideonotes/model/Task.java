package com.fan.aivideonotes.model;


import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Task {
    @Id
    private String id; // 我们将使用前端生成的 UUID 作为主键

    private String status; // 例如: PENDING, PROCESSING, COMPLETED, FAILED

    @Lob
    @Column(columnDefinition = "TEXT")
    private String statusMessage; // 用于存放更详细的状态信息或错误信息

    // 关键：建立与 Note 实体的“一对一”关联
    // fetch = FetchType.LAZY 表示在查询 Task 时，不立即加载 Note，提升性能
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "note_id") // 在 task 表里创建一个外键列 note_id
    private Note resultNote;
}
