const DEFAULT_API_BASE_URL = import.meta.env.DEV
  ? `http://${window.location.hostname}:8080/api`
  : 'http://localhost:8080/api'
const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || DEFAULT_API_BASE_URL).replace(/\/+$/, '')

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    credentials: 'include',
    ...options,
    headers: {
      Accept: 'application/json',
      'X-Requested-With': 'XMLHttpRequest',
      ...options.headers,
    },
  })

  if (response.status === 204) return null

  if (!response.ok) {
    const body = await response.text()
    let message = body

    if (body) {
      try {
        const error = JSON.parse(body)
        message = error.detail || error.message || body
      } catch {
        message = body
      }
    }

    const fallbackMessage = response.status === 401
      ? 'Invalid mobile number or password.'
      : response.status === 403
        ? 'This account does not have permission to perform that action.'
        : `Homepage request failed (${response.status})`
    const error = new Error(message || fallbackMessage)
    error.status = response.status
    throw error
  }

  return response.json()
}

function getAuthorizationHeader(credentials) {
  const credentialsBytes = new TextEncoder().encode(
    `${credentials.phoneNumber}:${credentials.password}`,
  )
  let binaryCredentials = ''
  for (const byte of credentialsBytes) {
    binaryCredentials += String.fromCharCode(byte)
  }
  return `Basic ${btoa(binaryCredentials)}`
}

function getAdminHeaders(credentials) {
  if (credentials?.remembered) return {}
  return { Authorization: getAuthorizationHeader(credentials) }
}

export function fetchMarketResults() {
  return request('/market-results')
}

export function fetchRajshreeResult(marketSlug) {
  return request(`/markets/${marketSlug}`)
}

export function fetchMarketTimings() {
  return request('/market-timings')
}

export function fetchGoldenAnk() {
  return request('/golden-ank')
}

export function fetchAdminProfile(credentials) {
  return request('/admin/me', { headers: getAdminHeaders(credentials) })
}

export function fetchRememberedAdminProfile() {
  return request('/admin/me')
}

export function startAdminSession(phoneNumber, password, rememberMe) {
  return request('/admin/session', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ phoneNumber, password, rememberMe }),
  })
}

export function endAdminSession() {
  return request('/admin/session/logout', { method: 'POST' })
}

export function updateAdminPassword(newPassword, credentials) {
  return request('/admin/password', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...getAdminHeaders(credentials),
    },
    body: JSON.stringify({ newPassword }),
  })
}

export function fetchAdminRajshreeResults(date, credentials) {
  const query = new URLSearchParams({ date })
  return request(`/admin/rajshree-results?${query}`, {
    headers: getAdminHeaders(credentials),
  })
}

export function saveRajshreeResult(marketSlug, result, credentials) {
  return request(`/admin/markets/${marketSlug}/results`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...getAdminHeaders(credentials),
    },
    body: JSON.stringify(result),
  })
}

export function saveAdminMarketTimings(timings, credentials) {
  return request('/admin/market-timings', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...getAdminHeaders(credentials),
    },
    body: JSON.stringify(timings),
  })
}

export function fetchAdminGoldenAnk(date, credentials) {
  const query = new URLSearchParams({ date })
  return request(`/admin/golden-ank?${query}`, {
    headers: getAdminHeaders(credentials),
  })
}

export function saveAdminGoldenAnk(result, credentials) {
  return request('/admin/golden-ank', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...getAdminHeaders(credentials),
    },
    body: JSON.stringify(result),
  })
}

export function fetchAdminUsers(credentials) {
  return request('/admin/users', { headers: getAdminHeaders(credentials) })
}

export function createAdminUser(user, credentials) {
  return request('/admin/users', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...getAdminHeaders(credentials),
    },
    body: JSON.stringify(user),
  })
}

export function deactivateAdminUser(phoneNumber, credentials) {
  return request(`/admin/users/${encodeURIComponent(phoneNumber)}`, {
    method: 'DELETE',
    headers: getAdminHeaders(credentials),
  })
}

export function fetchRecentResultChanges(credentials) {
  return request('/admin/audit', { headers: getAdminHeaders(credentials) })
}
