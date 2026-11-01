export function normalizeProductUrl(value: string) {
  return value.trim();
}

export function isSupportedProductUrl(value: string) {
  const normalizedValue = normalizeProductUrl(value);

  if (!normalizedValue) {
    return true;
  }

  try {
    const url = new URL(normalizedValue);
    return (url.protocol === 'http:' || url.protocol === 'https:') && Boolean(url.hostname);
  } catch {
    return false;
  }
}

export function getProductUrlHost(value?: string | null) {
  if (!value) {
    return null;
  }

  try {
    return new URL(value).hostname.replace(/^www\./, '');
  } catch {
    return null;
  }
}
