package com.shubham.chatsapp.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CursorMessagePageDTO {
    private List<MessageDTO> messages;  // same DTO you already use
    private boolean hasMore;
    private UUID nextCursor;            // UUID of the oldest message in this batch

    public CursorMessagePageDTO() {}

    public CursorMessagePageDTO(List<MessageDTO> messages, boolean hasMore, UUID nextCursor) {
        this.messages = messages;
        this.hasMore = hasMore;
        this.nextCursor = nextCursor;
    }
}
