package com.webnorm.prototypever1.api.request;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Data;

@Data
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class AddCategoryRequest {
    private String name;
}
