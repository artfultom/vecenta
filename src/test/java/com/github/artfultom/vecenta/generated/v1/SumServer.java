package com.github.artfultom.vecenta.generated.v1;

import com.github.artfultom.vecenta.matcher.Entity;

public interface SumServer {

    @Entity("math")
    Integer sum(java.lang.Integer a, java.lang.Integer b);
}