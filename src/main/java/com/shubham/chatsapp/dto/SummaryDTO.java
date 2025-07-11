package com.shubham.chatsapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SummaryDTO {
    private UUID id;
    private UUID chatID;
    private String summaryText;
    private LocalDateTime generatedAt;

}
