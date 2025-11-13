package com.perifaflow.bemestar.messaging;

import com.perifaflow.bemestar.domain.RitmoEvent;

public interface RitmoPublisher {
    void publish(RitmoEvent event);
}
