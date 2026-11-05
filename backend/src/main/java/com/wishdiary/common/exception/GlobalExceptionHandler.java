package com.wishdiary.common.exception;

import com.wishdiary.domain.sharing.InvalidSharingGroupException;
import com.wishdiary.domain.sharing.SharingGroupAccessDeniedException;
import com.wishdiary.common.response.CommonResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(com.wishdiary.application.shop.InvalidShopPhotoException.class)
    public ResponseEntity<CommonResponse<Void>> handleInvalidPhoto(RuntimeException exception) {
        return ResponseEntity.badRequest().body(CommonResponse.fail("INVALID_SHOP_PHOTO", exception.getMessage()));
    }

    @ExceptionHandler(com.wishdiary.domain.group.FamilyConnectionAccessDeniedException.class)
    public ResponseEntity<CommonResponse<Void>> handleFamilyConnectionAccessDenied(RuntimeException exception) {
        return ResponseEntity.status(403).body(CommonResponse.fail("FAMILY_CONNECTION_ACCESS_DENIED", exception.getMessage()));
    }

    @ExceptionHandler(com.wishdiary.domain.group.InvalidFamilyConnectionRequestException.class)
    public ResponseEntity<CommonResponse<Void>> handleInvalidFamilyConnection(RuntimeException exception) {
        return ResponseEntity.badRequest().body(CommonResponse.fail("INVALID_FAMILY_CONNECTION_REQUEST", exception.getMessage()));
    }

    @ExceptionHandler(com.wishdiary.domain.group.FamilyConnectionRequestConflictException.class)
    public ResponseEntity<CommonResponse<Void>> handleFamilyConnectionConflict(RuntimeException exception) {
        return ResponseEntity.status(409).body(CommonResponse.fail("FAMILY_CONNECTION_REQUEST_CONFLICT", exception.getMessage()));
    }

    @ExceptionHandler(InvalidSharingGroupException.class)
    public ResponseEntity<CommonResponse<Void>> handleInvalidSharingGroup(RuntimeException exception) {
        return ResponseEntity.badRequest().body(CommonResponse.fail("INVALID_SHARING_GROUP", exception.getMessage()));
    }

    @ExceptionHandler(SharingGroupAccessDeniedException.class)
    public ResponseEntity<CommonResponse<Void>> handleSharingGroupAccessDenied(RuntimeException exception) {
        return ResponseEntity.status(403).body(CommonResponse.fail("SHARING_GROUP_ACCESS_DENIED", exception.getMessage()));
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<CommonResponse<Void>> handleCustomException(CustomException exception) {
        CommonResponse<Void> response = CommonResponse.fail(
                exception.getErrorCode(),
                exception.getMessage()
        );
        return ResponseEntity.status(exception.getStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<Void>> handleValidationException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(this::getValidationMessage)
                .orElse("입력한 내용을 다시 확인해 주세요.");

        CommonResponse<Void> response = CommonResponse.fail("INVALID_REQUEST", message);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class})
    public ResponseEntity<CommonResponse<Void>> handleInvalidRequest(Exception exception) {
        return ResponseEntity.badRequest().body(
                CommonResponse.fail("INVALID_REQUEST", "입력한 내용을 다시 확인해 주세요.")
        );
    }

    private String getValidationMessage(FieldError error) {
        String field = switch (error.getField()) {
            case "email" -> "이메일";
            case "password" -> "비밀번호";
            case "nickname" -> "닉네임";
            case "inviteCode" -> "초대 코드";
            case "familyRelation", "relationLabel" -> "가족 관계";
            case "content" -> "내용";
            case "visibilityScope" -> "공개 범위";
            case "writtenDate" -> "기록 날짜";
            case "title" -> "선물 이름";
            case "description" -> "선물 설명";
            case "imageData" -> "선물 사진";
            case "productUrl" -> "쇼핑몰 링크";
            case "cloverPrice" -> "클로버 가격";
            case "purchasePriceWon" -> "실제 구매가";
            case "productId" -> "선물";
            case "receiverFamilyUserId" -> "받을 가족";
            case "message" -> "부탁 메시지";
            case "diaryId" -> "일기";
            case "memberIds" -> "그룹 구성원";
            case "name" -> "그룹 이름";
            case "action" -> "처리 방식";
            default -> "입력값";
        };
        String detail = switch (error.getCode() == null ? "" : error.getCode()) {
            case "NotBlank", "NotNull" -> "입력해 주세요.";
            case "Email" -> "올바른 주소를 입력해 주세요.";
            case "Size" -> "길이를 확인해 주세요.";
            case "Min", "Positive" -> "값을 확인해 주세요.";
            default -> "다시 확인해 주세요.";
        };
        return field + ": " + detail;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<Void>> handleUnexpectedException(Exception exception) {
        CommonResponse<Void> response = CommonResponse.fail(
                "INTERNAL_SERVER_ERROR",
                "서버 오류가 발생했어요. 잠시 후 다시 시도해 주세요."
        );
        return ResponseEntity.internalServerError().body(response);
    }
}

