package com.common.utils.id;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class Id {
    private static final long EPOCH = LocalDateTime.of(2018, 5, 29, 13, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    private static final long WORKER_ID_BITS = 5L;
    private static final long DATA_ID_BITS = 5L;

    private static final long SEQUENCE_BITS = 12L;
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATA_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATA_ID_BITS;

    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    private static long WORKER_ID;

    private static long DATA_ID;

    private static long SEQUENCE = 0L;

    private static long LAST_TIMESTAMP = -1L;

    public static synchronized long next() {
        long currentMillis = System.currentTimeMillis();
        if (currentMillis < LAST_TIMESTAMP)
            throw new RuntimeException(
                    String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", LAST_TIMESTAMP - currentMillis)
            );

        if (LAST_TIMESTAMP == currentMillis) {
            if (0L == (SEQUENCE = (SEQUENCE + 1) & SEQUENCE_MASK)) {
                currentMillis = waitUntilNextTime(LAST_TIMESTAMP);
            }
        } else {
            SEQUENCE = 0L;
        }
        LAST_TIMESTAMP = currentMillis;
        return ((currentMillis - EPOCH) << TIMESTAMP_LEFT_SHIFT) | (DATA_ID << DATA_ID_SHIFT)  | (WORKER_ID << WORKER_ID_SHIFT) | SEQUENCE;
    }

    private static long waitUntilNextTime(final long lastTimestamp) {
        long time = System.currentTimeMillis();
        while (time <= lastTimestamp) {
            time = System.currentTimeMillis();
        }
        return time;
    }
}
