package com.derkaiser.auth.commons.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Respuesta con mensaje de texto")
public class MessageResponse {

    @Schema(description = "Mensaje de la respuesta", example = "Operación realizada exitosamente", required = true)
    private String message;

    public static MessageResponse of(String message) {
        return MessageResponse.builder()
                .message(message)
                .build();
    }


}
