package com.sh.cursortest.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageModel implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String content;
    private String timestamp;
    private String type;
} 