package com.fan.aivideonotes.controller.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TaskResponse {

    private String message;
    private String taskId;
}
