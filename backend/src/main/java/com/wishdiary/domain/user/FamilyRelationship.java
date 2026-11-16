package com.wishdiary.domain.user;

import com.wishdiary.domain.group.FamilyRelation;

public record FamilyRelationship(FamilyRelation type, String label) {
    public FamilyRelationship {
        if (type == null || label == null || label.isBlank() || label.strip().length() > 100) {
            throw new InvalidFamilyRelationshipException("아이와의 관계를 1~100자로 입력해 주세요.");
        }
        label = label.strip();
    }

    public static FamilyRelationship forUser(AppRole role, String label) {
        if (role != AppRole.FAMILY) {
            throw new InvalidFamilyRelationshipException("가족 계정에서만 아이와의 관계를 수정할 수 있어요.");
        }
        String normalized = label == null ? "" : label.strip();
        FamilyRelation type = switch (normalized) {
            case "엄마" -> FamilyRelation.MOTHER;
            case "아빠" -> FamilyRelation.FATHER;
            case "할머니" -> FamilyRelation.GRANDMOTHER;
            case "할아버지" -> FamilyRelation.GRANDFATHER;
            case "이모", "고모" -> FamilyRelation.AUNT;
            case "삼촌" -> FamilyRelation.UNCLE;
            default -> FamilyRelation.OTHER_FAMILY;
        };
        return new FamilyRelationship(type, normalized);
    }
}
