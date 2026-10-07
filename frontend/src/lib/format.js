const inr = new Intl.NumberFormat('en-IN', {
  style: 'currency',
  currency: 'INR',
  maximumFractionDigits: 0,
});

export function formatPrice(value) {
  return inr.format(Number(value || 0));
}

export function discountPercent(price, mrp) {
  const p = Number(price);
  const m = Number(mrp);
  return m > p ? Math.round((1 - p / m) * 100) : 0;
}

export function formatCount(count) {
  if (count >= 1000) {
    return `${(count / 1000).toFixed(1).replace(/\.0$/, '')}k`;
  }
  return String(count || 0);
}

export function formatDate(value) {
  return new Date(value).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' });
}

export function formatDateTime(value) {
  return new Date(value).toLocaleString('en-IN', {
    day: 'numeric',
    month: 'short',
    hour: 'numeric',
    minute: '2-digit',
  });
}

/** Requests an image at the width it is displayed at (Pexels supports resizing via the w parameter). */
export function sizedImage(url, width) {
  if (!url) return url;
  try {
    const parsed = new URL(url);
    if (parsed.hostname.endsWith('pexels.com')) {
      parsed.searchParams.set('w', String(width));
      return parsed.toString();
    }
  } catch {
    // not a valid absolute URL, use as is
  }
  return url;
}

export function firstName(name) {
  return (name || '').trim().split(/\s+/)[0] || 'there';
}
