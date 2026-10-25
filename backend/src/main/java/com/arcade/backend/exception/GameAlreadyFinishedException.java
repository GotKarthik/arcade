package com.arcade.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class GameAlreadyFinishedException extends RuntimeException {
    public GameAlreadyFinishedException(String sessionId) {
        super("Game session is already finished: " + sessionId);
    }
}
