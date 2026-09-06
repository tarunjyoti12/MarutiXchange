package com.marutixchange.ar.controller;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ArEventRequest {
    private String sessionId;
    private String eventType; // COLOR_CHANGE, RESIZE, REPOSITION, SCREENSHOT
    private String eventData;
}
