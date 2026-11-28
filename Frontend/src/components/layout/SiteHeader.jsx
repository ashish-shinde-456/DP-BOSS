import brandImage from '../../assets/reference-brand.png'
import welcomeImage from '../../assets/reference-laxmi.jpg'

function SiteHeader() {
  return (
    <header className="site-header">
      <a className="brand" href="#top" aria-label="DPBOSS home">
        <img src={brandImage} alt="DPBOSS" />
      </a>
      <div className="welcome-panel">
        <img src={welcomeImage} alt="Decorative welcome image" />
        <p>!! Welcome to dpboss international !! Satta Matka Fast Result</p>
      </div>
      <span className="demo-badge">REFERENCE SNAPSHOT - NOT LIVE</span>
    </header>
  )
}

export default SiteHeader