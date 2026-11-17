package com.wishdiary.application.clover;

public record AwardCloverResponse(Long ledgerId, Long childUserId, int amount, String note, int balance) {}
