import { useState } from 'react'
import AdminPasswordInput from './AdminPasswordInput.jsx'
import { updateAdminPassword } from '../../services/homepageApi.js'
import {
  ADMIN_PASSWORD_PATTERN,
  ADMIN_PASSWORD_REQUIREMENTS,
  isValidAdminPassword,
} from '../../services/adminPasswordPolicy.js'

function formatExpiry(value) {
  return new Intl.DateTimeFormat('en-IN', {
    dateStyle: 'medium',
    timeZone: 'Asia/Kolkata',
  }).format(new Date(value))
}

function AdminPasswordForm({ credentials, expiresAt, required = false, onChanged }) {
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [isSaving, setIsSaving] = useState(false)

  const handleSubmit = async (event) => {
    event.preventDefault()
    setError('')
    setMessage('')

    if (!isValidAdminPassword(newPassword)) {
      setError(ADMIN_PASSWORD_REQUIREMENTS)
      return
    }
    if (newPassword !== confirmPassword) {
      setError('The passwords do not match.')
      return
    }

    setIsSaving(true)
    try {
      const updatedProfile = await updateAdminPassword(newPassword, credentials)
      setNewPassword('')
      setConfirmPassword('')
      setMessage(`Password updated. Change it again by ${formatExpiry(updatedProfile.passwordExpiresAt)}.`)
      onChanged(newPassword, updatedProfile)
    } catch (saveError) {
      setError(saveError instanceof Error ? saveError.message : 'Unable to update your password.')
    } finally {
      setIsSaving(false)
    }
  }

  return (
    <section className="admin-management-card admin-password-card">
      <h2>{required ? 'Password change required' : 'Change your password'}</h2>
      <p>
        {required
          ? 'Your password has expired. Change it to continue to the results admin.'
          : `Your password must be changed every three months. Next change due: ${formatExpiry(expiresAt)}.`}
      </p>
      <form className="admin-password-form" onSubmit={handleSubmit}>
        <label>
          New password
          <AdminPasswordInput
            autoComplete="new-password"
            minLength={12}
            maxLength={72}
            pattern={ADMIN_PASSWORD_PATTERN}
            title={ADMIN_PASSWORD_REQUIREMENTS}
            value={newPassword}
            onChange={(event) => {
              setNewPassword(event.target.value)
              setError('')
            }}
            required
          />
        </label>
        <label>
          Confirm new password
          <AdminPasswordInput
            autoComplete="new-password"
            minLength={12}
            maxLength={72}
            value={confirmPassword}
            onChange={(event) => {
              setConfirmPassword(event.target.value)
              setError('')
            }}
            required
          />
        </label>
        <button type="submit" disabled={isSaving}>
          {isSaving ? 'Updating...' : 'Update password'}
        </button>
      </form>
      <p className="admin-password-hint">{ADMIN_PASSWORD_REQUIREMENTS}</p>
      {message && <p className="admin-success" role="status">{message}</p>}
      {error && <p className="admin-error" role="alert">{error}</p>}
    </section>
  )
}

export default AdminPasswordForm
