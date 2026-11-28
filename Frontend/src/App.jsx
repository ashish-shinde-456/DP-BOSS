import HomePage from './pages/HomePage.jsx'
import AdminPage from './pages/AdminPage.jsx'

function App() {
  const path = window.location.pathname.replace(/\/+$/, '')
  return path === '/admin' ? <AdminPage /> : <HomePage />
}

export default App