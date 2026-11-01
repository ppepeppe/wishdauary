package com.wishdiary.application.diary;

import com.wishdiary.application.user.LoadedUser;
import com.wishdiary.domain.user.ChildOnlyDiaryCreationException;
import org.springframework.stereotype.Component;

@Component
public class CreateDiaryValidator {

    public void validateAuthorCanCreate(LoadedUser author) {
        if (!author.isChild()) {
            throw new ChildOnlyDiaryCreationException();
        }
    }
}
