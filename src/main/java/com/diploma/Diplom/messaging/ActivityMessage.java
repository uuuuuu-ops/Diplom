package com.diploma.Diplom.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityMessage implements Serializable {
    private String userId;
    private String type;
    private String referenceId;
    private String message;
}