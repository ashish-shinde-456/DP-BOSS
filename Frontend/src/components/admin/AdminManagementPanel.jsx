import { useEffect, useRef, useState } from 'react'
import {
  createAdminUser,
  deactivateAdminUser,
  fetchAdminUsers,
  fetchRecentResultChanges,
} from '../../services/homepageApi.js'
import AdminPasswordInput from './AdminPasswordInput.jsx'
import {
  ADMIN_PASSWORD_PATTERN,
  ADMIN_PASSWORD_REQUIREMENTS,
  isValidAdminPassword,
} from '../../services/adminPasswordPolicy.js'

function formatChangedAt(value) {
  return new Intl.DateTimeFormat('en-IN', {
    dateStyle: 'medium',
    timeStyle: 'short',
    timeZone: 'Asia/Kolkata',
  }).format(new Date(value))
}

function AdminManagementPanel({ credentials, refreshKey }) {
  const [users, setUsers] = useState([])
  const [auditEntries, setAuditEntries] = useState([])
  const [memberForm, setMemberForm] = useState({
    displayName: '',
    phoneNumber: '',
    password: '',
    confirmPassword: '',
  })
  const [isLoadingUsers, setIsLoadingUsers] = useState(true)
  const [isLoadingAudit, setIsLoadingAudit] = useState(true)
  const [isCreating, setIsCreating] = useState(false)
  const [deactivatingPhone, setDeactivatingPhone] = useState('')
  const [memberError, setMemberError] = useState('')
  const [memberMessage, setMemberMessage] = useState('')
  const [accessError, setAccessError] = useState('')
  const [usersError, setUsersError] = useState('')
  const [auditError, setAuditError] = useState('')
  const [isEditorDialogOpen, setIsEditorDialogOpen] = useState(false)
  const editorDialogRef = useRef(null)

  useEffect(() => {
    const dialog = editorDialogRef.current
    if (!dialog) return

    if (isEditorDialogOpen && !dialog.open) {
      dialog.showModal()
    } else if (!isEditorDialogOpen && dialog.open) {
      dialog.close()
    }
  }, [isEditorDialogOpen])

  useEffect(() => {
    let isActive = true

    fetchAdminUsers(credentials)
      .then((data) => {
        if (isActive) {
          setUsers(data)
          setUsersError('')
        }
      })
      .catch((error) => {
        if (isActive) setUsersError(error.message || 'Unable to load members.')
      })
      .finally(() => {
        if (isActive) setIsLoadingUsers(false)
      })

    fetchRecentResultChanges(credentials)
      .then((data) => {
        if (isActive) {
          setAuditEntries(data)
          setAuditError('')
        }
      })
      .catch((error) => {
        if (isActive) setAuditError(error.message || 'Unable to load result history.')
      })
      .finally(() => {
        if (isActive) setIsLoadingAudit(false)
      })

    return () => {
      isActive = false
    }
  }, [credentials, refreshKey])

  const updateMemberField = (event) => {
    const { name, value } = event.target
    setMemberForm((current) => ({ ...current, [name]: value }))
    setMemberError('')
    setMemberMessage('')
  }

  const handleCreateMember = async (event) => {
    event.preventDefault()
    if (!isValidAdminPassword(memberForm.password)) {
      setMemberError(ADMIN_PASSWORD_REQUIREMENTS)
      return
    }
    if (memberForm.password !== memberForm.confirmPassword) {
      setMemberError('The passwords do not match.')
      return
    }

    setIsCreating(true)
    setMemberError('')
    setMemberMessage('')

    try {
      const newUser = await createAdminUser({
        displayName: memberForm.displayName,
        phoneNumber: memberForm.phoneNumber,
        password: memberForm.password,
      }, credentials)
      setUsers((current) => [...current, newUser]
        .sort((left, right) => left.displayName.localeCompare(right.displayName)))
      setMemberForm({ displayName: '', phoneNumber: '', password: '', confirmPassword: '' })
      setMemberMessage(`${newUser.displayName} can now enter results.`)
      setIsEditorDialogOpen(false)
    } catch (error) {
      setMemberError(error.message || 'Unable to add this member.')
    } finally {
      setIsCreating(false)
    }
  }

  const handleDeactivate = async (phoneNumber) => {
    setDeactivatingPhone(phoneNumber)
    setAccessError('')
    setMemberMessage('')

    try {
      const updatedUser = await deactivateAdminUser(phoneNumber, credentials)
      setUsers((current) => current.map((user) => (
        user.phoneNumber === updatedUser.phoneNumber ? updatedUser : user
      )))
      setMemberMessage(`${updatedUser.displayName}'s access has been removed.`)
    } catch (error) {
      setAccessError(error.message || 'Unable to remove this member.')
    } finally {
      setDeactivatingPhone('')
    }
  }

  return (
    <section className="admin-management" aria-label="Owner administration">
      <section className="admin-management-card">
        <h2>Manage result editors</h2>
        <p>Editors can enter results, but only owners can manage member access.</p>

        <button
          className="admin-add-editor-button"
          type="button"
          onClick={() => {
            setMemberError('')
            setMemberMessage('')
            setIsEditorDialogOpen(true)
          }}
        >
          Add result editor
        </button>
        <dialog
          aria-labelledby="admin-editor-dialog-title"
          className="admin-editor-dialog"
          onClose={() => setIsEditorDialogOpen(false)}
          ref={editorDialogRef}
        >
          <div className="admin-editor-dialog-content">
            <header className="admin-editor-dialog-header">
              <h3 id="admin-editor-dialog-title">Add result editor</h3>
              <button
                className="admin-editor-dialog-close"
                type="button"
                onClick={() => setIsEditorDialogOpen(false)}
              >
                Close
              </button>
            </header>
            <form className="admin-member-form" onSubmit={handleCreateMember}>
              <label>
                New member name
                <input
                  name="displayName"
                  type="text"
                  autoComplete="name"
                  maxLength={80}
                  value={memberForm.displayName}
                  onChange={updateMemberField}
                  required
                />
              </label>
              <label>
                Mobile number
                <input
                  name="phoneNumber"
                  type="tel"
                  inputMode="numeric"
                  autoComplete="tel"
                  pattern="[0-9]{10}"
                  maxLength={10}
                  value={memberForm.phoneNumber}
                  onChange={updateMemberField}
                  required
                />
              </label>
              <label>
                Password for this member
                <AdminPasswordInput
                  name="password"
                  autoComplete="new-password"
                  minLength={12}
                  maxLength={72}
                  pattern={ADMIN_PASSWORD_PATTERN}
                  title={ADMIN_PASSWORD_REQUIREMENTS}
                  value={memberForm.password}
                  onChange={updateMemberField}
                  required
                />
              </label>
              <label>
                Confirm password
                <AdminPasswordInput
                  name="confirmPassword"
                  autoComplete="new-password"
                  minLength={12}
                  maxLength={72}
                  value={memberForm.confirmPassword}
                  onChange={updateMemberField}
                  required
                />
              </label>
              <button type="submit" disabled={isCreating}>
                {isCreating ? 'Adding...' : 'Add editor'}
              </button>
            </form>
            <p className="admin-password-hint">{ADMIN_PASSWORD_REQUIREMENTS}</p>
            {memberError && <p className="admin-error" role="alert">{memberError}</p>}
          </div>
        </dialog>

        {memberMessage && <p className="admin-success" role="status">{memberMessage}</p>}
        {accessError && <p className="admin-error" role="alert">{accessError}</p>}
        {usersError && <p className="admin-error" role="alert">{usersError}</p>}

        {isLoadingUsers ? (
          <p role="status">Loading members...</p>
        ) : (
          <ul className="admin-member-list">
            {users.map((user) => (
              <li key={user.phoneNumber}>
                <span>
                  <strong>{user.displayName}</strong>
                  {' · '}
                  {user.phoneNumber}
                  {' · '}
                  {user.role.toLowerCase()}
                  {!user.active && ' · inactive'}
                </span>
                {user.role === 'EDITOR' && user.active && (
                  <button
                    type="button"
                    disabled={Boolean(deactivatingPhone)}
                    onClick={() => handleDeactivate(user.phoneNumber)}
                  >
                    {deactivatingPhone === user.phoneNumber ? 'Removing...' : 'Remove'}
                  </button>
                )}
              </li>
            ))}
          </ul>
        )}
      </section>

      <section className="admin-management-card">
        <h2>Result changes · last 5 days</h2>
        <p>Every result entry and update is recorded with the account that made it.</p>
        {isLoadingAudit && <p role="status">Loading result history...</p>}
        {auditError && <p className="admin-error" role="alert">{auditError}</p>}
        {!isLoadingAudit && !auditError && auditEntries.length === 0 && (
          <p>No result changes in the last five days.</p>
        )}
        {auditEntries.length > 0 && (
          <div className="admin-audit-table-wrap">
            <table className="admin-audit-table">
              <thead>
                <tr>
                  <th scope="col">Changed</th>
                  <th scope="col">Changed by</th>
                  <th scope="col">Market / date</th>
                  <th scope="col">Change</th>
                </tr>
              </thead>
              <tbody>
                {auditEntries.map((entry) => (
                  <tr key={entry.id}>
                    <td>{formatChangedAt(entry.changedAt)}</td>
                    <td>{entry.actorName} ({entry.actorPhone})</td>
                    <td>{entry.marketName} · {entry.resultDate}</td>
                    <td>
                      {entry.action === 'CREATE' ? 'Added' : `${entry.previousResult} → `}
                      {entry.newResult}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </section>
  )
}

export default AdminManagementPanel
