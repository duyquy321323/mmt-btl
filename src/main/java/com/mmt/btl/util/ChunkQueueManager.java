package com.mmt.btl.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ChunkQueueManager {
    private BlockingQueue<Integer> chunkQueue;

    public ChunkQueueManager(int totalChunks) {
        chunkQueue = new LinkedBlockingQueue<>();
        for (int i = 0; i < totalChunks; i++) {
            chunkQueue.add(i); // Thêm tất cả các chunk vào hàng đợi
        }
    }

    public Integer getNextChunk() {
        return chunkQueue.poll(); // Lấy chunk tiếp theo để tải, trả về null nếu hết
    }

    public Integer checkNextChunk(){
        return chunkQueue.peek();
    }

    public void addChunk(int i) {
         chunkQueue.add(i);
    }
}
