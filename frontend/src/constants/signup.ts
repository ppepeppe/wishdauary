import type { FamilyRelation } from '../types/auth';

export const FAMILY_RELATION_OPTIONS: Array<{
  value: FamilyRelation;
  label: string;
  defaultRelationLabel: string;
}> = [
  { value: 'mother', label: '엄마', defaultRelationLabel: '엄마' },
  { value: 'father', label: '아빠', defaultRelationLabel: '아빠' },
  { value: 'grandmother', label: '할머니', defaultRelationLabel: '할머니' },
  { value: 'grandfather', label: '할아버지', defaultRelationLabel: '할아버지' },
  { value: 'aunt', label: '이모', defaultRelationLabel: '이모' },
  { value: 'uncle', label: '삼촌', defaultRelationLabel: '삼촌' },
  { value: 'other', label: '기타 가족', defaultRelationLabel: '가족' },
];

export function getDefaultRelationLabel(relation: FamilyRelation) {
  return FAMILY_RELATION_OPTIONS.find((option) => option.value === relation)?.defaultRelationLabel ?? '가족';
}
