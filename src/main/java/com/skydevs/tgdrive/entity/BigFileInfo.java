package com.skydevs.tgdrive.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BigFileInfo {
    private String fileName;
    private long fileSize;
    private List<String> fileIds;
    private boolean isRecordFile;
}
