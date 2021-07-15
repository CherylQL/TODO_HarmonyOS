package com.example.todo;

import ohos.hiviewdfx.HiLog;

import java.lang.reflect.Array;
import java.util.List;

public class RequestParam {
    public int idx;
    public String title;
    public String description;
    private List<Object> TODO;

    public int getIndex() {
        return idx;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<Object> getTODOList() {
        return TODO;
    }
}
