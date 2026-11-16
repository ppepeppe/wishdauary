package com.wishdiary.domain.shop;

public class GiftReceiptNotApprovedException extends RuntimeException {
    public GiftReceiptNotApprovedException() { super("가족이 승인한 선물만 수령 완료할 수 있어요."); }
}
