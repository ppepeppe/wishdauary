export interface EnglishContent {
  contentId: number;
  sentence: string;
  meaning: string;
  version: number;
  createdAt: string;
  updatedAt: string;
}
export interface EnglishContentOverview {
  contents: EnglishContent[];
  today: string;
  todayContentId: number | null;
}
export interface EnglishContentPayload { sentence: string; meaning: string }
