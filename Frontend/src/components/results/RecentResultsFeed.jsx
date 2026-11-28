import ResultRow from './ResultRow.jsx'
import { recentResults } from '../../data/results.js'

function RecentResultsFeed({ results = recentResults }) {
  return (
    <div className="recent-results">
      <h3>Recent Results</h3>
      <ul className="result-list">
        {results.map((result, index) => (
          <ResultRow key={`${result.time}-${index}`} {...result} />
        ))}
      </ul>
    </div>
  )
}

export default RecentResultsFeed