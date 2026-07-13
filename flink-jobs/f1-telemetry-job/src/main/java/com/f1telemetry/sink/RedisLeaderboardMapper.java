package com.f1telemetry.sink;

import com.f1telemetry.model.DriverSpeed;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommand;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommandDescription;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisMapper;

/**
 * Maps DriverSpeed to Redis ZADD command for sorted set leaderboard.
 * 
 * Redis structure:
 *   Key: "f1:leaderboard:speed"
 *   Type: Sorted Set (ZSET)
 *   Score: max speed (km/h)
 *   Member: driver number
 * 
 * Query leaderboard:
 *   redis-cli> ZREVRANGE f1:leaderboard:speed 0 9 WITHSCORES
 *   (returns top 10 drivers sorted by speed descending)
 */
public class RedisLeaderboardMapper implements RedisMapper<DriverSpeed> {

    private static final String LEADERBOARD_KEY = "f1:leaderboard:speed";

    @Override
    public RedisCommandDescription getCommandDescription() {
        // ZADD: sorted set command
        return new RedisCommandDescription(RedisCommand.ZADD, LEADERBOARD_KEY);
    }

    @Override
    public String getKeyFromData(DriverSpeed data) {
        // Key is constant (single leaderboard for all drivers)
        return LEADERBOARD_KEY;
    }

    @Override
    public String getValueFromData(DriverSpeed data) {
        // Member = driver number (stored as string)
        return String.valueOf(data.driverNumber);
    }

    /**
     * Score for sorted set = max speed.
     * Redis will automatically maintain descending order by score.
     */
    public double getScoreFromData(DriverSpeed data) {
        return data.maxSpeed;
    }
}
