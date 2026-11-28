import SectionHeading from '../common/SectionHeading.jsx'
import RecentResultsFeed from './RecentResultsFeed.jsx'
import { latestResult, latestTime } from '../../data/results.js'

function LiveJackpotSection() {
  return (
    <section className="content-section" id="results" aria-labelledby="live-results-title">
      <SectionHeading id="live-results-title" title="Kalyan Jackpot Live" />
      <div className="result-layout">
        <div className="latest-result">
          <span className="result-label">Latest result</span>
          <strong className="latest-value">{latestResult}</strong>
          <span className="result-label">{latestTime}</span>
          <div className="jackpot-actions">
            <a href="https://dpbossnet.me/jackpot-history.php" target="_blank" rel="noreferrer">History - Full Day Result</a>
            <a href="https://whatsapp.com/channel/0029VbCH7KEGpLHN8giIeL2F" target="_blank" rel="noreferrer">Join Us - Get ID</a>
          </div>
        </div>
        <RecentResultsFeed />
      </div>
    </section>
  )
}

export default LiveJackpotSection