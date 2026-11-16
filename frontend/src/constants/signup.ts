import type { FamilyRelation } from '../types/auth';

const FAMILY_RELATIONS = new Map<string, FamilyRelation>([
  ['엄마', 'mother'],
  ['아빠', 'father'],
  ['할머니', 'grandmother'],
  ['할아버지', 'grandfather'],
  ['이모', 'aunt'],
  ['고모', 'aunt'],
  ['삼촌', 'uncle'],
]);

export function getFamilyRelation(relationLabel: string): FamilyRelation {
  return FAMILY_RELATIONS.get(relationLabel.trim()) ?? 'other';
}
