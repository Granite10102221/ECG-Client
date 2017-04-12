package com.orking.egc;

import java.util.List;

/**
 * Created by zhanglei on 2017/4/11.
 */

public class OriginalData {
    private long mStartTime;
    private long mEndTime;
    private List<byte[]> messages;

    public long getStartTime() {
        return mStartTime;
    }

    public void setStartTime(long startTime) {
        this.mStartTime = startTime;
    }

    public long getEndTime() {
        return mEndTime;
    }

    public void setEndTime(long endTime) {
        this.mEndTime = endTime;
    }

    public List<byte[]> getMessages() {
        return messages;
    }

    public void setMessages(List<byte[]> messages) {
        this.messages = messages;
    }
}
