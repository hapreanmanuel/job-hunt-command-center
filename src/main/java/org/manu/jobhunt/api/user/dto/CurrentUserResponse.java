package org.manu.jobhunt.api.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class CurrentUserResponse {

    private UUID id;
}
