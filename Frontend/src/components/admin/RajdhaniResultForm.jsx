import { useState } from 'react'
import { saveRajshreeResult } from '../../services/homepageApi.js'

function getInitialFields(existingResult) {
  return {
    openPana: existingResult?.openPana || '',
    openDigit: existingResult?.openDigit || '',
    closePana: existingResult?.closePana || '',
    closeDigit: existingResult?.closeDigit || '',
  }
}

function RajdhaniResultForm({
  marketName,
  marketSlug,
  openTime,
  closeTime,
  resultDate,
  credentials,
  existingResult,
  onSaved,
}) {
  const [fields, setFields] = useState(() => getInitialFields(existingResult))
  const [savingStage, setSavingStage] = useState('')
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  const updateField = (event) => {
    const { name, value } = event.target
    setFields((current) => ({ ...current, [name]: value }))
    setError('')
    setMessage('')
  }

  const handleSubmit = async (stage, event) => {
    event.preventDefault()
    setSavingStage(stage)
    setError('')
    setMessage('')

    const stageFields = stage === 'open'
      ? { openPana: fields.openPana, openDigit: fields.openDigit }
      : { closePana: fields.closePana, closeDigit: fields.closeDigit }

    try {
      const saved = await saveRajshreeResult(
        marketSlug,
        { resultDate, ...stageFields },
        credentials,
      )
      await onSaved()
      if (stage === 'open') {
        setMessage(saved.openPublished
          ? 'Open result saved and published.'
          : `Open result saved. It will publish at ${openTime} IST.`)
      } else {
        setMessage(saved.published
          ? 'Closing result saved and published.'
          : `Closing result saved. It will publish at ${closeTime} IST.`)
      }
    } catch (saveError) {
      setError(saveError instanceof Error ? saveError.message : 'Unable to save this result.')
    } finally {
      setSavingStage('')
    }
  }

  const hasSavedOpenResult = Boolean(existingResult?.openPana && existingResult?.openDigit)

  return (
    <article className="admin-result-card">
      <header>
        <h2>{marketName}</h2>
        <p>Open publishes at {openTime} IST | Close publishes at {closeTime} IST</p>
      </header>

      <section className="admin-result-stage" aria-label={`${marketName} open result`}>
        <h3>Open result</h3>
        <p className="admin-publish-status" role="status">
          {existingResult?.openPublished
            ? 'Published'
            : existingResult?.openPana
              ? `Scheduled for ${openTime} IST`
              : `Not saved · Publishes at ${openTime} IST`}
        </p>
        <form className="admin-result-form" onSubmit={(event) => handleSubmit('open', event)}>
          <label>
            Open pana
            <input
              name="openPana"
              type="text"
              inputMode="numeric"
              autoComplete="off"
              pattern="[0-9]{3}"
              maxLength={3}
              value={fields.openPana}
              onChange={updateField}
              required
            />
          </label>
          <label>
            Open digit
            <input
              name="openDigit"
              type="text"
              inputMode="numeric"
              autoComplete="off"
              pattern="[0-9]"
              maxLength={1}
              value={fields.openDigit}
              onChange={updateField}
              required
            />
          </label>
          <button type="submit" disabled={Boolean(savingStage)}>
            {savingStage === 'open' ? 'Saving...' : 'Save open result'}
          </button>
        </form>
      </section>

      <section className="admin-result-stage" aria-label={`${marketName} closing result`}>
        <h3>Closing result</h3>
        <p className="admin-publish-status" role="status">
          {existingResult?.published
            ? 'Published'
            : existingResult?.closePana
              ? `Scheduled for ${closeTime} IST`
              : `Waiting for the closing result - Publishes at ${closeTime} IST`}
        </p>
        <form className="admin-result-form" onSubmit={(event) => handleSubmit('close', event)}>
          <label>
            Close digit
            <input
              name="closeDigit"
              type="text"
              inputMode="numeric"
              autoComplete="off"
              pattern="[0-9]"
              maxLength={1}
              value={fields.closeDigit}
              onChange={updateField}
              required
            />
          </label>
          <label>
            Close pana
            <input
              name="closePana"
              type="text"
              inputMode="numeric"
              autoComplete="off"
              pattern="[0-9]{3}"
              maxLength={3}
              value={fields.closePana}
              onChange={updateField}
              required
            />
          </label>
          <button type="submit" disabled={Boolean(savingStage) || !hasSavedOpenResult}>
            {savingStage === 'close' ? 'Saving...' : 'Save closing result'}
          </button>
        </form>
      </section>

      {message && <p className="admin-success" role="status">{message}</p>}
      {error && <p className="admin-error" role="alert">{error}</p>}
    </article>
  )
}

export default RajdhaniResultForm
