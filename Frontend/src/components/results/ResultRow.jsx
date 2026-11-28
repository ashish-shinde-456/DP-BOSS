function ResultRow({ time, result }) {
  return (
    <li className="result-row">
      <span>{time}</span>
      <strong>{result}</strong>
    </li>
  )
}

export default ResultRow