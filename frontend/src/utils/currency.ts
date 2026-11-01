const WON_FORMATTER = new Intl.NumberFormat('ko-KR');

export function formatWon(value?: number | null) {
  if (value == null) {
    return null;
  }

  return `${WON_FORMATTER.format(value)}원`;
}
