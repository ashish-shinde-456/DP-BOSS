function SiteFooter() {
  return (
    <footer className="site-footer">
      <a href="#markets">Matka Play</a>
      <a href="#charts">VIP Zone</a>
      <button type="button" onClick={() => window.location.reload()}>Refresh</button>
    </footer>
  )
}

export default SiteFooter