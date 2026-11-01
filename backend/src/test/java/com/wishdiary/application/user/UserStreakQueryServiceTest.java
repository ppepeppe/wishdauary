package com.wishdiary.application.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.wishdiary.domain.user.UserStreakStats;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserStreakQueryServiceTest {

    @Mock
    private UpdateUserStreakPort updateUserStreakPort;

    private UserStreakQueryService userStreakQueryService;

    @BeforeEach
    void setUp() {
        userStreakQueryService = new UserStreakQueryService(
                updateUserStreakPort,
                new UserStreakResponseMapper()
        );
    }

    @Test
    void getStreakReturnsZeroWhenStatsDoNotExist() {
        when(updateUserStreakPort.loadUserStreak(1L)).thenReturn(Optional.empty());

        UserStreakResponse response = userStreakQueryService.getStreak(1L);

        assertEquals(0, response.currentStreak());
        assertEquals(0, response.maxStreak());
    }

    @Test
    void getStreakReturnsExistingStats() {
        when(updateUserStreakPort.loadUserStreak(1L)).thenReturn(Optional.of(
                new UserStreakStats(1L, 3, 5, LocalDate.of(2026, 4, 11))
        ));

        UserStreakResponse response = userStreakQueryService.getStreak(1L);

        assertEquals(3, response.currentStreak());
        assertEquals(5, response.maxStreak());
    }
}
