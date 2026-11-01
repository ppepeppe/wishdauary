package com.wishdiary.application.reaction;

public interface ToggleHeartReactionUseCase {

    ToggleHeartReactionResponse toggle(Long familyUserId, ToggleHeartReactionRequest request);
}
