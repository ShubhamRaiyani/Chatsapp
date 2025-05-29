package com.shubham.chatsapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SummaryDTO {
    private Long id;
    private Long chatID;
    private String summaryText;
    private String generatedBy; // "AI" or "Manual"
    private LocalDateTime generatedAt;

}
