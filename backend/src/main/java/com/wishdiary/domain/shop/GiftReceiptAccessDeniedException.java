package com.wishdiary.domain.shop;

public class GiftReceiptAccessDeniedException extends RuntimeException {
    public GiftReceiptAccessDeniedException() { super("내가 요청한 선물만 수령 완료할 수 있어요."); }
}
