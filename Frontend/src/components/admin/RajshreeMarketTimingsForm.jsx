import { useEffect, useRef, useState } from 'react'
import {
  fetchMarketTimings,
  saveAdminMarketTimings,
} from '../../services/homepageApi.js'

const DEFAULT_TIMINGS = {
  dayOpenTime: '15:15',
  dayCloseTime: '17:15',
  nightOpenTime: '21:35',
  nightCloseTime: '23:45',
}

function RajshreeMarketTimingsForm({ credentials, onTimingsChanged }) {
  const [timings, setTimings] = useState(DEFAULT_TIMINGS)
  const [isLoading, setIsLoading] = useState(true)
  const [isSaving, setIsSaving] = useState(false)
  const [error, setError] = useState('')
  const [loadError, setLoadError] = useState('')
  const [message, setMessage] = useState('')
  const [isOpen, setIsOpen] = useState(false)
  const [reloadKey, setReloadKey] = useState(0)
  const dialogRef = useRef(null)

  useEffect(() => {
    let isActive = true
    fetchMarketTimings()
      .then((savedTimings) => {
        if (isActive && savedTimings) {
          setTimings(savedTimings)
          onTimingsChanged(savedTimings)
        }
      })
      .catch((loadError) => {
        if (isActive) {
          setLoadError(loadError instanceof Error
            ? loadError.message
            : 'Unable to load current market timings.')
        }
      })
      .finally(() => {
        if (isActive) setIsLoading(false)
      })

    return () => {
      isActive = false
    }
  }, [onTimingsChanged, reloadKey])

  useEffect(() => {
    const dialog = dialogRef.current
    if (!dialog) return

    if (isOpen && !dialog.open) {
      dialog.showModal()
    } else if (!isOpen && dialog.open) {
      dialog.close()
    }
  }, [isOpen])

  const updateTime = (event) => {
    const { name, value } = event.target
    setTimings((current) => ({ ...current, [name]: value }))
    setError('')
    setMessage('')
  }

  const handleSave = async (event) => {
    event.preventDefault()
    setIsSaving(true)
    setError('')
    setMessage('')

    try {
      const savedTimings = await saveAdminMarketTimings(timings, credentials)
      setTimings(savedTimings)
      onTimingsChanged(savedTimings)
      setMessage('Rajshree market timings updated.')
      setIsOpen(false)
    } catch (saveError) {
      setError(saveError instanceof Error
        ? saveError.message
        : 'Unable to save market timings.')
    } finally {
      setIsSaving(false)
    }
  }

  return (
    <section className="admin-management-card admin-market-timings">
      <h2>Rajshree market timings</h2>
      <p>Changes apply to future scheduled results and update pending result schedules.</p>
      <button
        className="admin-add-editor-button"
        type="button"
        disabled={isLoading || Boolean(loadError)}
        onClick={() => {
          setError('')
          setIsOpen(true)
        }}
      >
        {isLoading ? 'Loading timings...' : 'Change market timings'}
      </button>
      {loadError && (
        <>
          <p className="admin-error" role="alert">
            Could not load current timings: {loadError}
          </p>
          <button
            className="admin-add-editor-button"
            type="button"
            onClick={() => {
              setIsLoading(true)
              setReloadKey((current) => current + 1)
            }}
          >
            Retry loading timings
          </button>
        </>
      )}
      {message && <p className="admin-success" role="status">{message}</p>}
      {error && !isOpen && <p className="admin-error" role="alert">{error}</p>}

      <dialog
        aria-labelledby="rajshree-timing-dialog-title"
        className="admin-editor-dialog admin-timing-dialog"
        onClose={() => setIsOpen(false)}
        ref={dialogRef}
      >
        <div className="admin-editor-dialog-content">
          <header className="admin-editor-dialog-header">
            <h3 id="rajshree-timing-dialog-title">Change market timings</h3>
            <button
              className="admin-editor-dialog-close"
              type="button"
              onClick={() => setIsOpen(false)}
            >
              Close
            </button>
          </header>
          <form className="admin-timing-form" onSubmit={handleSave}>
            <fieldset>
              <legend>Rajshree Day</legend>
              <label>
                Open result time
                <input
                  name="dayOpenTime"
                  type="time"
                  value={timings.dayOpenTime}
                  onChange={updateTime}
                  required
                />
              </label>
              <label>
                Close result time
                <input
                  name="dayCloseTime"
                  type="time"
                  value={timings.dayCloseTime}
                  onChange={updateTime}
                  required
                />
              </label>
            </fieldset>
            <fieldset>
              <legend>Rajshree Night</legend>
              <label>
                Open result time
                <input
                  name="nightOpenTime"
                  type="time"
                  value={timings.nightOpenTime}
                  onChange={updateTime}
                  required
                />
              </label>
              <label>
                Close result time
                <input
                  name="nightCloseTime"
                  type="time"
                  value={timings.nightCloseTime}
                  onChange={updateTime}
                  required
                />
              </label>
            </fieldset>
            <p>Times use India Standard Time. If a close time is earlier than its open time, it is treated as the following day.</p>
            {error && <p className="admin-error" role="alert">{error}</p>}
            <div className="admin-timing-actions">
              <button type="submit" disabled={isSaving}>
                {isSaving ? 'Saving...' : 'Save timings'}
              </button>
              <button type="button" onClick={() => setIsOpen(false)}>
                Cancel
              </button>
            </div>
          </form>
        </div>
      </dialog>
    </section>
  )
}

export default RajshreeMarketTimingsForm
