package com.taobao.behavior.aggregation;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class ItemMetricsAccumulator implements Serializable {
    private static final long serialVersionUID = 1L;

    long categoryId;
    long pvCount;
    long cartCount;
    long favCount;
    long buyCount;
    final Set<Long> userIds = new HashSet<>();
}
