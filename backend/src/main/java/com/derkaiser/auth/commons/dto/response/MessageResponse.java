package com.derkaiser.auth.commons.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponse {

    private String message;

    public static MessageResponse of(String message) {
        return MessageResponse.builder()
                .message(message)
                .build();
    }


}
