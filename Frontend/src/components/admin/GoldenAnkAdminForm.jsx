import { useEffect, useState } from 'react'
import {
  fetchAdminGoldenAnk,
  saveAdminGoldenAnk,
} from '../../services/homepageApi.js'

function getTodayInIndia() {
  const parts = new Intl.DateTimeFormat('en-CA', {
    timeZone: 'Asia/Kolkata',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).formatToParts(new Date())
  const values = Object.fromEntries(parts.map(({ type, value }) => [type, value]))

  return `${values.year}-${values.month}-${values.day}`
}

function emptyFields() {
  return { ankOne: '', ankTwo: '', ankThree: '', ankFour: '' }
}

function GoldenAnkAdminForm({ credentials }) {
  const [effectiveDate, setEffectiveDate] = useState(getTodayInIndia)
  const [fields, setFields] = useState(emptyFields)
  const [savedResult, setSavedResult] = useState(null)
  const [isLoading, setIsLoading] = useState(true)
  const [isSaving, setIsSaving] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const today = getTodayInIndia()

  useEffect(() => {
    let isActive = true

    fetchAdminGoldenAnk(effectiveDate, credentials)
      .then((result) => {
        if (!isActive) return
        setSavedResult(result)
        setFields(result
          ? {
            ankOne: result.ankOne,
            ankTwo: result.ankTwo,
            ankThree: result.ankThree,
            ankFour: result.ankFour,
          }
          : emptyFields())
      })
      .catch((loadError) => {
        if (isActive) {
          setSavedResult(null)
          setFields(emptyFields())
          setError(loadError instanceof Error
            ? loadError.message
            : 'Unable to load Golden Ank for this date.')
        }
      })
      .finally(() => {
        if (isActive) setIsLoading(false)
      })

    return () => {
      isActive = false
    }
  }, [credentials, effectiveDate])

  const updateField = (event) => {
    const { name, value } = event.target
    setFields((current) => ({ ...current, [name]: value }))
    setError('')
    setMessage('')
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    setIsSaving(true)
    setError('')
    setMessage('')

    try {
      const saved = await saveAdminGoldenAnk(
        { resultDate: effectiveDate, ...fields },
        credentials,
      )
      setSavedResult(saved)
      setMessage(effectiveDate > today
        ? `Golden Ank scheduled for ${effectiveDate}; it becomes active at midnight IST.`
        : 'Golden Ank saved and active.')
    } catch (saveError) {
      setError(saveError instanceof Error
        ? saveError.message
        : 'Unable to save Golden Ank.')
    } finally {
      setIsSaving(false)
    }
  }

  return (
    <section className="admin-management-card golden-ank-admin-card">
      <h2>Manage Golden Ank</h2>
      <p>Enter four digits for a date. A future entry becomes active automatically at midnight IST.</p>

      <label className="golden-ank-date-label" htmlFor="golden-ank-date">
        Effective date
      </label>
      <input
        id="golden-ank-date"
        className="golden-ank-date-input"
        type="date"
        min={today}
        value={effectiveDate}
        onChange={(event) => {
          const nextDate = event.target.value
          if (!nextDate) return
          setEffectiveDate(nextDate)
          setFields(emptyFields())
          setSavedResult(null)
          setIsLoading(true)
          setError('')
          setMessage('')
        }}
        required
      />

      <p className="admin-publish-status" role="status">
        {isLoading
          ? 'Loading saved Golden Ank...'
          : savedResult
            ? `Saved for ${savedResult.resultDate}: ${savedResult.goldenAnk}`
            : 'No entry is saved for this date. The last saved Golden Ank stays active until a newer dated entry takes effect.'}
      </p>

      <form className="golden-ank-admin-form" onSubmit={handleSubmit}>
        {[
          ['ankOne', 'First digit'],
          ['ankTwo', 'Second digit'],
          ['ankThree', 'Third digit'],
          ['ankFour', 'Fourth digit'],
        ].map(([name, label]) => (
          <label key={name}>
            {label}
            <input
              name={name}
              type="text"
              inputMode="numeric"
              autoComplete="off"
              pattern="[0-9]"
              maxLength={1}
              value={fields[name]}
              onChange={updateField}
              required
            />
          </label>
        ))}
        <button type="submit" disabled={isLoading || isSaving || Boolean(error)}>
          {isSaving ? 'Saving...' : 'Save Golden Ank'}
        </button>
      </form>

      {message && <p className="admin-success" role="status">{message}</p>}
      {error && <p className="admin-error" role="alert">{error}</p>}
    </section>
  )
}

export default GoldenAnkAdminForm
