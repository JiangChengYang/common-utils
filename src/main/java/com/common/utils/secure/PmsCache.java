package com.common.utils.secure;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PmsCache {
    private long expireTime;
    private Set<String> pms;

    @JsonIgnore
    public boolean isExpired() {
        return Instant.now().getEpochSecond() > expireTime;
    }
}
