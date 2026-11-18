const STORAGE_PREFIX = 'wishdiary.selected-child.';

export function readSelectedChild(userId: number): string | null {
  try { return localStorage.getItem(`${STORAGE_PREFIX}${userId}`); }
  catch { return null; }
}

export function saveSelectedChild(userId: number, childId: number) {
  try { localStorage.setItem(`${STORAGE_PREFIX}${userId}`, String(childId)); }
  catch { /* URL selection still works if browser storage is unavailable. */ }
}
