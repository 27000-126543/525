package com.ecommerce.platform.common.id;

import cn.hutool.core.net.NetUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SnowflakeIdGenerator {

    private final long twepoch = 1700000000000L;

    private final long workerIdBits = 5L;
    private final long datacenterIdBits = 5L;
    private final long maxWorkerId = ~(-1L << workerIdBits);
    private final long maxDatacenterId = ~(-1L << datacenterIdBits);
    private final long sequenceBits = 12L;

    private final long workerIdShift = sequenceBits;
    private final long datacenterIdShift = sequenceBits + workerIdBits;
    private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;
    private final long sequenceMask = ~(-1L << sequenceBits);

    private final long workerId;
    private final long datacenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public SnowflakeIdGenerator() {
        this.datacenterId = getDatacenterId();
        this.workerId = getMaxWorkerId(datacenterId);
        log.info("SnowflakeIdGenerator initialized. datacenterId: {}, workerId: {}", datacenterId, workerId);
    }

    public SnowflakeIdGenerator(long workerId, long datacenterId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException("workerId can't be greater than " + maxWorkerId + " or less than 0");
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId can't be greater than " + maxDatacenterId + " or less than 0");
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    public synchronized long nextId() {
        long timestamp = timeGen();

        if (timestamp < lastTimestamp) {
            long offset = lastTimestamp - timestamp;
            if (offset <= 5) {
                try {
                    wait(offset << 1);
                    timestamp = timeGen();
                    if (timestamp < lastTimestamp) {
                        throw new RuntimeException("Clock moved backwards. Refusing to generate id for " + offset + " milliseconds");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for clock", e);
                }
            } else {
                throw new RuntimeException("Clock moved backwards. Refusing to generate id for " + offset + " milliseconds");
            }
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - twepoch) << timestampLeftShift)
                | (datacenterId << datacenterIdShift)
                | (workerId << workerIdShift)
                | sequence;
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }

    private long getDatacenterId() {
        try {
            String ip = NetUtil.getLocalhostStr();
            if (ip != null) {
                byte[] bytes = ip.getBytes();
                long sum = 0;
                for (byte b : bytes) {
                    sum += b;
                }
                return sum % (maxDatacenterId + 1);
            }
        } catch (Exception e) {
            log.warn("Failed to get datacenterId from IP, using 0", e);
        }
        return 0L;
    }

    private long getMaxWorkerId(long datacenterId) {
        try {
            String hostname = java.net.InetAddress.getLocalHost().getHostName();
            int hashCode = hostname.hashCode();
            return (hashCode & 0x7fffffff) % (maxWorkerId + 1);
        } catch (Exception e) {
            log.warn("Failed to get workerId from hostname, using 0", e);
        }
        return 0L;
    }

    public String nextIdStr() {
        return String.valueOf(nextId());
    }
}
