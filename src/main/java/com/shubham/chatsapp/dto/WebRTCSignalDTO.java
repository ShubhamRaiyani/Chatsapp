package com.shubham.chatsapp.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class WebRTCSignalDTO {
    // call-request | call-accepted | call-rejected | call-ended | offer | answer | ice-candidate
    private String type;

    private String fromEmail;   // always set server-side
    private String toEmail;
    private UUID chatId;
    private String callType;    // AUDIO | VIDEO

    // SDP fields (offer / answer)
    private String sdp;
    private String sdpType;     // "offer" or "answer"

    // ICE candidate fields
    private String candidate;
    private String sdpMid;
    private Integer sdpMLineIndex;
}
