export const ADMIN_PASSWORD_PATTERN =
  '(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s]).{12,72}'

export const ADMIN_PASSWORD_REQUIREMENTS =
  'Use 12 to 72 characters (maximum 72 UTF-8 bytes) with an uppercase letter, a lowercase letter, a number, and a symbol.'

export function isValidAdminPassword(password) {
  const passwordBytes = new TextEncoder().encode(password).length
  const passwordPattern = new RegExp(`^(?:${ADMIN_PASSWORD_PATTERN})$`)

  return password.length >= 12
    && password.length <= 72
    && passwordBytes <= 72
    && passwordPattern.test(password)
}
