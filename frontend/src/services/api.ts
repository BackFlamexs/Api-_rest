export function getBaseUrl() {
  return localStorage.getItem('clash_api_url') || 'http://localhost:8080';
}

export async function apiFetch(endpoint: string, options: RequestInit = {}) {
  const token = localStorage.getItem('clash_api_key');
  const baseUrl = getBaseUrl();

  const headers = new Headers(options.headers);
  if (token) {
    headers.set('X-API-Key', token);
  }
  if (!headers.has('Content-Type') && options.body && typeof options.body === 'string') {
    headers.set('Content-Type', 'application/json');
  }

  const response = await fetch(`${baseUrl}${endpoint}`, {
    ...options,
    headers,
  });

  if (response.status === 401) {
    localStorage.removeItem('clash_api_key');
    window.location.href = '/';
  }

  return response;
}
