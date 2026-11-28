import { useEffect, useState } from 'react'
import GoldenAnkAdminForm from '../components/admin/GoldenAnkAdminForm.jsx'
import AdminManagementPanel from '../components/admin/AdminManagementPanel.jsx'
import AdminPasswordInput from '../components/admin/AdminPasswordInput.jsx'
import AdminPasswordForm from '../components/admin/AdminPasswordForm.jsx'
import RajdhaniResultForm from '../components/admin/RajdhaniResultForm.jsx'
import RajshreeMarketTimingsForm from '../components/admin/RajshreeMarketTimingsForm.jsx'
import {
  endAdminSession,
  fetchAdminProfile,
  fetchMarketTimings,
  fetchAdminRajshreeResults,
  fetchRememberedAdminProfile,
  startAdminSession,
} from '../services/homepageApi.js'

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

function formatMarketTime(time) {
  const [hoursValue, minutes] = time.split(':')
  const hours = Number(hoursValue)
  return `${hours % 12 || 12}:${minutes} ${hours < 12 ? 'AM' : 'PM'}`
}

function AdminPage() {
  const [loginFields, setLoginFields] = useState({ phoneNumber: '', password: '' })
  const [rememberMe, setRememberMe] = useState(false)
  const [credentials, setCredentials] = useState(null)
  const [profile, setProfile] = useState(null)
  const [resultDate, setResultDate] = useState(getTodayInIndia)
  const [results, setResults] = useState([])
  const [marketTimings, setMarketTimings] = useState({
    dayOpenTime: '15:15',
    dayCloseTime: '17:15',
    nightOpenTime: '21:35',
    nightCloseTime: '23:45',
  })
  const [marketTimingsError, setMarketTimingsError] = useState('')
  const [isAuthorized, setIsAuthorized] = useState(false)
  const [isLoading, setIsLoading] = useState(true)
  const [loadError, setLoadError] = useState('')
  const [refreshKey, setRefreshKey] = useState(0)

  useEffect(() => {
    if (!credentials || !resultDate) return undefined

    let isActive = true
    let profileLoaded = false

    fetchAdminProfile(credentials)
      .then((adminProfile) => {
        if (!isActive) return null
        profileLoaded = true
        setProfile(adminProfile)
        setIsAuthorized(true)
        if (adminProfile.mustChangePassword) {
          setResults([])
          return null
        }
        return fetchAdminRajshreeResults(resultDate, credentials)
      })
      .then((data) => {
        if (isActive && data) setResults(data)
      })
      .catch((error) => {
        if (isActive) {
          setResults([])
          if (!profileLoaded) {
            setProfile(null)
            setIsAuthorized(false)
          }
          setLoadError(error instanceof Error ? error.message : 'Unable to load admin results.')
        }
      })
      .finally(() => {
        if (isActive) setIsLoading(false)
      })

    return () => {
      isActive = false
    }
  }, [credentials, resultDate, refreshKey])

  useEffect(() => {
    if (!credentials) return undefined

    let isActive = true
    fetchMarketTimings()
      .then((timings) => {
        if (isActive && timings) {
          setMarketTimings(timings)
          setMarketTimingsError('')
        }
      })
      .catch((error) => {
        if (isActive) {
          setMarketTimingsError(error instanceof Error
            ? error.message
            : 'Unable to load current market timings.')
        }
      })

    return () => {
      isActive = false
    }
  }, [credentials])

  useEffect(() => {
    let isActive = true

    fetchRememberedAdminProfile()
      .then((adminProfile) => {
        if (isActive) {
          setRememberMe(true)
          setCredentials({
            remembered: true,
            phoneNumber: adminProfile.phoneNumber,
          })
        }
      })
      .catch((error) => {
        if (isActive) {
          if (error.status !== 401) {
            setLoadError(error instanceof Error
              ? error.message
              : 'Unable to check for a remembered login.')
          }
          setIsLoading(false)
        }
      })

    return () => {
      isActive = false
    }
  }, [])

  const handleLoginFieldChange = (event) => {
    const { name, value } = event.target
    setLoginFields((current) => ({ ...current, [name]: value }))
    setLoadError('')
  }

  const handleLogin = async (event) => {
    event.preventDefault()
    setIsLoading(true)
    setIsAuthorized(false)
    setProfile(null)
    setResults([])
    setLoadError('')
    const phoneNumber = loginFields.phoneNumber.trim()
    try {
      await startAdminSession(phoneNumber, loginFields.password, rememberMe)
      setCredentials(rememberMe
        ? { remembered: true, phoneNumber }
        : { phoneNumber, password: loginFields.password })
      setLoginFields({ phoneNumber, password: '' })
    } catch (error) {
      setLoadError(error instanceof Error ? error.message : 'Unable to sign in.')
      setIsLoading(false)
    }
  }

  const lockAdmin = async () => {
    setCredentials(null)
    setLoginFields({ phoneNumber: '', password: '' })
    setRememberMe(false)
    setProfile(null)
    setResults([])
    setIsAuthorized(false)
    setIsLoading(false)
    setLoadError('')
    try {
      await endAdminSession()
    } catch (error) {
      setLoadError(error instanceof Error
        ? error.message
        : 'Unable to revoke this remembered login.')
    }
  }

  const refreshResults = () => {
    setIsLoading(true)
    setLoadError('')
    setRefreshKey((current) => current + 1)
  }

  const handlePasswordChanged = async (newPassword, updatedProfile) => {
    const currentCredentials = credentials
    let sessionWarning = ''
    if (currentCredentials?.remembered) {
      try {
        await startAdminSession(currentCredentials.phoneNumber, newPassword, true)
        setCredentials({
          remembered: true,
          phoneNumber: currentCredentials.phoneNumber,
        })
      } catch (error) {
        setCredentials({
          phoneNumber: currentCredentials.phoneNumber,
          password: newPassword,
        })
        setRememberMe(false)
        sessionWarning = error instanceof Error
          ? `Password changed, but this device could not be remembered: ${error.message}`
          : 'Password changed, but this device could not be remembered.'
      }
    } else {
      setCredentials((current) => current && ({ ...current, password: newPassword }))
    }
    setProfile(updatedProfile)
    setIsAuthorized(true)
    setLoadError(sessionWarning)
    setIsLoading(true)
  }

  const dayResult = results.find(
    (result) => result.marketName === 'RAJSHREE DAY' && result.resultDate === resultDate,
  )
  const nightResult = results.find(
    (result) => result.marketName === 'RAJSHREE NIGHT' && result.resultDate === resultDate,
  )

  return (
    <main className="admin-page-shell">
      <header className="admin-page-header">
        <div>
          <h1>Rajshree Results Admin</h1>
          <p>Save the open result first, then save the closing result when it is available.</p>
        </div>
        {isAuthorized && (
          <div className="admin-session">
            <span>{profile.displayName} · {profile.role.toLowerCase()}</span>
            <button type="button" onClick={lockAdmin}>Log out</button>
          </div>
        )}
      </header>

      {!isAuthorized && (
        <form className="admin-access-form" onSubmit={handleLogin}>
          <label htmlFor="admin-phone">Mobile number</label>
          <input
            id="admin-phone"
            name="phoneNumber"
            type="tel"
            inputMode="numeric"
            autoComplete="username"
            pattern="[0-9]{10}"
            maxLength={10}
            value={loginFields.phoneNumber}
            onChange={handleLoginFieldChange}
            required
          />
          <label htmlFor="admin-password">Password</label>
          <AdminPasswordInput
            id="admin-password"
            name="password"
            autoComplete="current-password"
            value={loginFields.password}
            onChange={handleLoginFieldChange}
            required
          />
          <label className="admin-remember-me" htmlFor="admin-remember-me">
            <input
              id="admin-remember-me"
              type="checkbox"
              checked={rememberMe}
              onChange={(event) => setRememberMe(event.target.checked)}
            />
            Remember me on this device for 30 days
          </label>
          <button type="submit" disabled={!loginFields.phoneNumber || !loginFields.password || isLoading}>
            {isLoading ? 'Signing in...' : 'Sign in'}
          </button>
        </form>
      )}

      {isAuthorized && profile.mustChangePassword && (
        <AdminPasswordForm
          credentials={credentials}
          expiresAt={profile.passwordExpiresAt}
          required
          onChanged={handlePasswordChanged}
        />
      )}

      {isAuthorized && !profile.mustChangePassword && (
        <>
          <AdminPasswordForm
            credentials={credentials}
            expiresAt={profile.passwordExpiresAt}
            onChanged={handlePasswordChanged}
          />
          <GoldenAnkAdminForm credentials={credentials} />
          {profile.role === 'OWNER' && (
            <RajshreeMarketTimingsForm
              credentials={credentials}
              onTimingsChanged={setMarketTimings}
            />
          )}
          {marketTimingsError && (
            <p className="admin-error" role="alert">
              Could not load active market timings: {marketTimingsError}
            </p>
          )}
          <div className="admin-date-control">
            <label htmlFor="result-date">Result date</label>
            <input
              id="result-date"
              type="date"
              value={resultDate}
              onChange={(event) => {
                setResults([])
                setIsLoading(Boolean(event.target.value))
                setLoadError('')
                setResultDate(event.target.value)
              }}
              required
            />
            <p>Times use India Standard Time (Asia/Kolkata).</p>
          </div>

          <section className="admin-result-grid" aria-label="Rajshree result forms">
            <RajdhaniResultForm
              key={`day-${resultDate}-${Boolean(dayResult)}`}
              marketName="Rajshree Day"
              marketSlug="rajshree-day"
              openTime={formatMarketTime(marketTimings.dayOpenTime)}
              closeTime={formatMarketTime(marketTimings.dayCloseTime)}
              resultDate={resultDate}
              credentials={credentials}
              existingResult={dayResult}
              onSaved={refreshResults}
            />
            <RajdhaniResultForm
              key={`night-${resultDate}-${Boolean(nightResult)}`}
              marketName="Rajshree Night"
              marketSlug="rajshree-night"
              openTime={formatMarketTime(marketTimings.nightOpenTime)}
              closeTime={formatMarketTime(marketTimings.nightCloseTime)}
              resultDate={resultDate}
              credentials={credentials}
              existingResult={nightResult}
              onSaved={refreshResults}
            />
          </section>

          {profile.role === 'OWNER' && (
            <AdminManagementPanel credentials={credentials} refreshKey={refreshKey} />
          )}
        </>
      )}

      {isLoading && (
        <p role="status">
          {credentials ? 'Loading saved results...' : 'Checking remembered login...'}
        </p>
      )}
      {loadError && <p className="admin-error" role="alert">{loadError}</p>}
    </main>
  )
}

export default AdminPage
