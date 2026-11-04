package org.manu.jobhunt.api.user;

import lombok.RequiredArgsConstructor;
import org.manu.jobhunt.api.user.dto.CurrentUserResponse;
import org.manu.jobhunt.api.user.dto.UserDataResponse;
import org.manu.jobhunt.application.user.UserApplication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserApplication applicationFacade;

    @GetMapping
    public CurrentUserResponse currentUser() {
        return new CurrentUserResponse(applicationFacade.currentUserId());
    }

    @GetMapping("/export")
    public UserDataResponse export() {
        return UserDataResponse.toDto(applicationFacade.export());
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete() {
        applicationFacade.deleteUser();
    }
}
