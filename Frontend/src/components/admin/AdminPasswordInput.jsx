import { useState } from 'react'

function AdminPasswordInput(props) {
  const [isVisible, setIsVisible] = useState(false)

  return (
    <div className="admin-password-input">
      <input {...props} type={isVisible ? 'text' : 'password'} />
      <button
        className="admin-password-toggle"
        type="button"
        aria-label={isVisible ? 'Hide password' : 'Show password'}
        aria-pressed={isVisible}
        onClick={() => setIsVisible((current) => !current)}
      >
        <svg
          aria-hidden="true"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
        >
          <path d="M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7-10-7-10-7z" />
          <circle cx="12" cy="12" r="3" />
          {isVisible && <path d="M3 3l18 18" />}
        </svg>
      </button>
    </div>
  )
}

export default AdminPasswordInput
