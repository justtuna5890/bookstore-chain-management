package com.bookstorechain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperationalAlertDTO {
    private String id;
    private String title;
    private String message;
    private String level; // DANGER, WARNING, INFO
    private String actionUrl;
    private String actionLabel;
    private String category; // BUYBACK, TRANSFER, INVENTORY
}
