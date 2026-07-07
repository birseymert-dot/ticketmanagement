package com.ticketmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CommentRequest {

    /** Kural: yorum bos olamaz. */
    @NotBlank(message = "Yorum bos olamaz")
    @Size(max = 1000, message = "Yorum en fazla 1000 karakter olabilir")
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
