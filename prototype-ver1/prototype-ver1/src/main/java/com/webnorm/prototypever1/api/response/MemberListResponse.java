package com.webnorm.prototypever1.api.response;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.webnorm.prototypever1.security.oauth.SocialType;
import lombok.*;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class MemberListResponse {
    private String id;
    private String email;
    private String name;
    private SocialType socialType;

    @Builder
    public MemberListResponse(String id, String email, String name, SocialType socialType) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.socialType = socialType;
    }
}
